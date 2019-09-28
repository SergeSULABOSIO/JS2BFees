/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import ICONES.Icones;
import SOURCES.CallBackFacture.EcouteurActualisationFacture;
import SOURCES.CallBackFacture.EcouteurFacture;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Objets.FileManager;
import SOURCES.UI.PanelFacture;
import SOURCES.Utilitaires_Facture.DataFacture;
import SOURCES.Utilitaires_Facture.DonneesFacture;
import SOURCES.Utilitaires_Facture.ParametresFacture;
import SOURCES.Utilitaires_Facture.SortiesFacture;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Callbacks.EcouteurStandard;
import Source.Interface.InterfaceExercice;
import Source.Interface.InterfaceMonnaie;
import Source.Interface.InterfaceUtilisateur;
import Source.Objet.Ayantdroit;
import Source.Objet.Classe;
import Source.Objet.CouleurBasique;
import Source.Objet.Eleve;
import Source.Objet.Entreprise;
import Source.Objet.Exercice;
import Source.Objet.Frais;
import Source.Objet.Monnaie;
import Source.Objet.Paiement;
import Source.Objet.Periode;
import Source.Objet.UtilObjet;
import Source.Objet.Utilisateur;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

/**
 *
 * @author HP Pavilion
 */
public class GestionPaiements {

    public static String NOM = "PAIEMENT";
    public PanelFacture panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public Eleve eleve;
    //public ParametresFacture parametresFacture;
    //public DonneesFacture donneesFacture;
    public DataFacture dataFacture;
    public boolean deleteCurrentTab = true;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;

    private Exercice exercice = null;
    private FileManager fm;
    private Vector<Classe> classes = new Vector<>();
    private Vector<Frais> frais = new Vector<>();
    private Vector<Ayantdroit> ayantDroits = new Vector<>();
    private Vector<Monnaie> monnaies = new Vector<>();
    private Vector<Periode> periodes = new Vector<>();
    private Vector<Paiement> paiements = new Vector<>();
    private CouleurBasique couleurBasique;
    public String selectedAnnee;
    public boolean canBeSaved = false;
    public Icones icones = null;

    public GestionPaiements(Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Eleve eleve) {
        this.couleurBasique = couleurBasique;
        this.icones = icones;
        this.fm = fm;
        this.eleve = eleve;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
    }

    private DataFacture getData() {
        ParametresFacture parametresFacture = new ParametresFacture(utilisateur, entreprise, exercice, monnaies.firstElement(), monnaies, classes, periodes);
        DonneesFacture donneesFacture = new DonneesFacture(eleve, frais, paiements, ayantDroits);
        return new DataFacture(donneesFacture, parametresFacture);
    }

    public void gl_setDonneesFromFileManager(String selectedAnnee, boolean deleteCurrentTab) {
        this.deleteCurrentTab = deleteCurrentTab;
        this.selectedAnnee = selectedAnnee;
        if (fm != null) {
            boolean mustLoadData = true;
            if (deleteCurrentTab == true) {
                int nbOnglets = tabOnglet.getComponentCount();
                for (int i = 0; i < nbOnglets; i++) {
                    //JPanel onglet = (JPanel) tabOnglet.getComponentAt(i);
                    String titreOnglet = tabOnglet.getTitleAt(i);
                    //System.out.println("Onglet - " + titreOnglet);
                    if (titreOnglet.equals(NOM + " - " + eleve.getNom() + " " + eleve.getPrenom())) {
                        //System.out.println("Une page d'adhésion était déjà ouverte, je viens de la fermer");
                        tabOnglet.remove(i);
                        mustLoadData = true;
                    }
                }
            }

            if (mustLoadData == true) {
                fm.fm_ouvrirTout(0, Exercice.class, UtilObjet.DOSSIER_ANNEE, 1, 1000, new EcouteurOuverture() {
                    @Override
                    public boolean isCriteresRespectes(Object object) {
                        Exercice annee = (Exercice) object;
                        if (annee.getNom().equals(selectedAnnee)) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void onElementLoaded(String message, Object data) {
                        exercice = (Exercice) data;
                    }

                    @Override
                    public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                        loadMonnaies();
                    }

                    @Override
                    public void onError(String string) {
                        progress.setVisible(false);
                        progress.setIndeterminate(false);
                    }

                    @Override
                    public void onProcessing(String string) {
                        progress.setVisible(true);
                        progress.setIndeterminate(true);
                    }
                });
            }
        }
    }

    private void loadAyantDroit() {
        ayantDroits.removeAllElements();
        fm.fm_ouvrirTout(0, Ayantdroit.class, UtilObjet.DOSSIER_AYANT_DROIT, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Ayantdroit ayantdroit = (Ayantdroit) object;
                if (ayantdroit.getIdExercice() == exercice.getId() && eleve.getSignature() == ayantdroit.getSignatureEleve()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Ayantdroit ayantdroit = (Ayantdroit) data;
                ayantDroits.add(ayantdroit);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                if (eleve != null) {
                    initUI(NOM + " - " + eleve.getNom() + " " + eleve.getPrenom());
                }
            }

            @Override
            public void onError(String string) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
            }

            @Override
            public void onProcessing(String string) {
                progress.setVisible(true);
                progress.setIndeterminate(true);
            }
        });
    }

    private void loadClasses() {
        classes.removeAllElements();
        fm.fm_ouvrirTout(0, Classe.class, UtilObjet.DOSSIER_CLASSE, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Classe classe = (Classe) object;
                if (classe.getIdExercice() == exercice.getId() && eleve.getIdClasse() == classe.getId()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Classe classe = (Classe) data;
                classes.add(classe);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                loadAyantDroit();
            }

            @Override
            public void onError(String string) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
            }

            @Override
            public void onProcessing(String string) {
                progress.setVisible(true);
                progress.setIndeterminate(true);
            }
        });
    }

    private void loadMonnaies() {
        monnaies.removeAllElements();
        fm.fm_ouvrirTout(0, Monnaie.class, UtilObjet.DOSSIER_MONNAIE, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Monnaie monnaie = (Monnaie) object;
                if (monnaie.getIdExercice() == exercice.getId()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Monnaie monnaie = (Monnaie) data;
                monnaies.add(monnaie);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                loadPeriodes();
            }

            @Override
            public void onError(String string) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
            }

            @Override
            public void onProcessing(String string) {
                progress.setVisible(true);
                progress.setIndeterminate(true);
            }
        });
    }

    private void loadPeriodes() {
        periodes.removeAllElements();
        fm.fm_ouvrirTout(0, Periode.class, UtilObjet.DOSSIER_PERIODE, 1, 1000, new EcouteurOuverture() {

            @Override
            public boolean isCriteresRespectes(Object object) {
                Periode periode = (Periode) object;
                if (periode.getIdExercice() == exercice.getId()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Periode periode = (Periode) data;
                periodes.add(periode);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                loadPaiements();
            }

            @Override
            public void onError(String string) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
            }

            @Override
            public void onProcessing(String string) {
                progress.setVisible(true);
                progress.setIndeterminate(true);
            }
        });
    }

    private void loadPaiements() {
        paiements.removeAllElements();
        fm.fm_ouvrirTout(0, Paiement.class, UtilObjet.DOSSIER_PAIEMENT, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Paiement paiement = (Paiement) object;
                if (paiement.getIdExercice() == exercice.getId() && paiement.getIdEleve() == eleve.getId()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Paiement paiement = (Paiement) data;
                paiements.add(paiement);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                loadFrais();
            }

            @Override
            public void onError(String string) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
            }

            @Override
            public void onProcessing(String string) {
                progress.setVisible(true);
                progress.setIndeterminate(true);
            }
        });
    }

    private void loadFrais() {
        frais.removeAllElements();
        fm.fm_ouvrirTout(0, Frais.class, UtilObjet.DOSSIER_FRAIS, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Frais oFrais = (Frais) object;
                if (oFrais.getIdExercice() == exercice.getId()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Frais oFrais = (Frais) data;
                frais.add(oFrais);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                loadClasses();
            }

            @Override
            public void onError(String string) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
            }

            @Override
            public void onProcessing(String string) {
                progress.setVisible(true);
                progress.setIndeterminate(true);
            }
        });
    }

    private void savePaiements(SortiesFacture se, EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<Paiement> listeNewEleves = se.getPaiements();
        Vector<Paiement> listeNewElevesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Paiement ia : listeNewEleves) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                canBeSaved = true;
                
                //Pour id étrangère incomplètes
                if (ia.getIdFrais() == -1) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, "Désolé,\nVeuillez préciser le frais que " + eleve.getNom()+" est en train de payer!", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }
                if (ia.getIdPeriode() == -1) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, "Désolé,\nVeuillez préciser la période pour laquelle " + eleve.getNom()+" est en train de payer!", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }
                if (ia.getMontant() == 0) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, "Désolé,\nLe montant ne peut pas être égale à Zéro (0)!", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }

                if (canBeSaved == true) {
                    ia.setIdExercice(annee.getId());
                    ia.setIdEleve(eleve.getId());
                    ia.setBeta(InterfaceExercice.BETA_EXISTANT);
                    listeNewElevesTempo.add(ia);
                }
            }
        }
        if (!listeNewElevesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewElevesTempo, UtilObjet.DOSSIER_PAIEMENT, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    //System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Paiements enregistrés !");
                }

                @Override
                public void onError(String message) {
                    //System.err.println(message);
                    ee.onError("Erreur !");
                }

                @Override
                public void onProcessing(String message) {
                    //System.out.println(message);
                    ee.onUploading("Enregistrement...");
                }
            });
        }
    }

    private void initUI(String nomTab) {
        panel = new PanelFacture(couleurBasique, progress, tabOnglet, getData(), new EcouteurFacture() {
            @Override
            public void onEnregistre(SortiesFacture sortiesFacture) {
                savePaiements(sortiesFacture, sortiesFacture.getEcouteurEnregistrement(), utilisateur, exercice);
            }

            @Override
            public void onDetruitPaiement(int idPaiement, long signature) {
                if (idPaiement != -1 && fm != null) {
                    boolean rep = fm.fm_supprimer(UtilObjet.DOSSIER_PAIEMENT, idPaiement, signature);
                }
            }

            @Override
            public void onDetruitTousLesPaiements(int idEleve, int idExercice) {
                //System.out.println("DESTRUCTION DES PAIEMENTS DE L'ELEVE " + idEleve + ", POUR l'ANNEE SCOLAIRE " + idExercice);
            }
        }, new EcouteurActualisationFacture() {
            @Override
            public DataFacture onRechargeDonneesEtParametres() {
                gl_setDonneesFromFileManager(selectedAnnee, false);
                return getData();
            }
        });

        //Chargement du gestionnaire sur l'onglet
        if (deleteCurrentTab == true) {
            tabOnglet.addTab(nomTab, panel);
            tabOnglet.setSelectedComponent(panel);
        }
        progress.setVisible(false);
        progress.setIndeterminate(false);
    }

}


























