/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import SOURCES.CallBackFacture.EcouteurFacture;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback.EcouteurStandard;
import SOURCES.Objets.FileManager;
import SOURCES.UI.PanelFacture;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires_Facture.DonneesFacture;
import SOURCES.Utilitaires_Facture.ParametresFacture;
import SOURCES.Utilitaires_Facture.SortiesFacture;
import Source.Callbacks.EcouteurEnregistrement;
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
import Source.Objet.LiaisonFraisClasse;
import Source.Objet.LiaisonFraisPeriode;
import Source.Objet.Monnaie;
import Source.Objet.Paiement;
import Source.Objet.Periode;
import Source.Objet.Utilisateur;
import java.util.Vector;
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
    public ParametresFacture parametresFacture;
    public DonneesFacture donneesFacture;
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

    public GestionPaiements(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Eleve eleve) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.eleve = eleve;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
    }

    private void initParamsEtDonnees() {
        this.parametresFacture = new ParametresFacture(utilisateur, entreprise, exercice, monnaies.firstElement(), monnaies, classes, periodes);
        this.donneesFacture = new DonneesFacture(eleve, frais, paiements, ayantDroits);
    }

    public void gl_setDonneesFromFileManager(String selectedAnnee) {
        if (fm != null) {
            boolean mustLoadData = true;
            int nbOnglets = tabOnglet.getComponentCount();
            for (int i = 0; i < nbOnglets; i++) {
                //JPanel onglet = (JPanel) tabOnglet.getComponentAt(i);
                String titreOnglet = tabOnglet.getTitleAt(i);
                System.out.println("Onglet - " + titreOnglet);
                if (titreOnglet.equals(NOM + " - " + eleve.getNom() + " " + eleve.getPrenom())) {
                    System.out.println("Une page d'adhésion était déjà ouverte, je viens de la fermer");
                    tabOnglet.remove(i);
                    mustLoadData = true;
                }
            }

            if (mustLoadData == true) {
                fm.fm_ouvrirTout(0, Exercice.class, UtilFees.DOSSIER_ANNEE, new EcouteurOuverture() {
                    @Override
                    public void onDone(String message, Vector data) {
                        System.out.println("CHARGEMENT ANNEE: " + message);
                        for (Object Oannee : data) {
                            Exercice annee = (Exercice) Oannee;
                            if (annee.getNom().equals(selectedAnnee)) {
                                System.out.println(" * " + annee.getNom());
                                exercice = annee;
                                break;
                            }

                        }
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
        fm.fm_ouvrirTout(0, Ayantdroit.class, UtilFees.DOSSIER_AYANT_DROIT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Ayantdroit classe = (Ayantdroit) o;
                    if (classe.getIdExercice() == exercice.getId()) {
                        ayantDroits.add(classe);
                        System.out.println(" * " + classe.toString());
                    }
                }
                initUI(NOM + " - " + eleve.getNom() + " " + eleve.getPrenom());
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
        fm.fm_ouvrirTout(0, Classe.class, UtilFees.DOSSIER_CLASSE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Classe classe = (Classe) o;
                    if (classe.getIdExercice() == exercice.getId()) {
                        classes.add(classe);
                        System.out.println(" * " + classe.toString());
                    }
                }
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
        fm.fm_ouvrirTout(0, Monnaie.class, UtilFees.DOSSIER_MONNAIE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Monnaie classe = (Monnaie) o;
                    if (classe.getIdExercice() == exercice.getId()) {
                        monnaies.add(classe);
                        System.out.println(" * " + classe.toString());
                    }
                }
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
        fm.fm_ouvrirTout(0, Periode.class, UtilFees.DOSSIER_PERIODE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Periode classe = (Periode) o;
                    if (classe.getIdExercice() == exercice.getId()) {
                        periodes.add(classe);
                        System.out.println(" * " + classe.toString());
                    }
                }
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
        fm.fm_ouvrirTout(0, Paiement.class, UtilFees.DOSSIER_PAIEMENT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Paiement paiement = (Paiement) o;
                    if (paiement.getIdExercice() == exercice.getId() && paiement.getIdEleve() == eleve.getId()) {
                        paiements.add(paiement);
                        System.out.println(" * " + paiement.toString());
                    }
                }
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
        fm.fm_ouvrirTout(0, Frais.class, UtilFees.DOSSIER_FRAIS, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Frais oFrais = (Frais) o;
                    if (oFrais.getIdExercice() == exercice.getId()) {
                        frais.add(oFrais);
                        System.out.println(" * " + oFrais.getNom());
                        System.out.println("Liaison classe:");
                        for (LiaisonFraisClasse lc : oFrais.getLiaisonsClasses()) {
                            System.out.println(" ** " + lc.toString());
                        }
                        System.out.println("Liaison période:");
                        for (LiaisonFraisPeriode lp : oFrais.getLiaisonsPeriodes()) {
                            System.out.println(" ** " + lp.toString());
                        }
                    }
                }
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

    private void initUI(String nomTab) {
        initParamsEtDonnees();
        panel = new PanelFacture(couleurBasique, tabOnglet, parametresFacture, donneesFacture, new EcouteurFacture() {
            @Override
            public void onEnregistre(SortiesFacture sortiesFacture) {
                System.out.println("ENREGISTREMENT DES PAIEMENTS DE L'EVELEVE EN COURS.");
                savePaiements(sortiesFacture, sortiesFacture.getEcouteurEnregistrement(), utilisateur, exercice);
            }

            @Override
            public void onDetruitPaiement(int idPaiement) {
                System.out.println("DESTRUCTION DU PAIEMENT " + idPaiement);
                if (idPaiement != -1 && fm != null) {
                    boolean rep = fm.fm_supprimer(UtilFees.DOSSIER_PAIEMENT, idPaiement);
                    System.out.println("SUPPRESSION = " + rep);
                    
                    //On doit actualiser le gestionnaire parent
                    
                }
            }

            @Override
            public void onDetruitTousLesPaiements(int idEleve, int idExercice) {
                System.out.println("DESTRUCTION DES PAIEMENTS DE L'ELEVE " + idEleve + ", POUR l'ANNEE SCOLAIRE " + idExercice);
            }
        });

        //Chargement du gestionnaire sur l'onglet
        tabOnglet.addTab(nomTab, panel);
        tabOnglet.setSelectedComponent(panel);
        progress.setVisible(false);
        progress.setIndeterminate(false);
    }

    private void savePaiements(SortiesFacture se, EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<Paiement> listeNewEleves = se.getPaiements();
        Vector<Paiement> listeNewElevesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Paiement ia : listeNewEleves) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdEleve(eleve.getId());
                ia.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewElevesTempo.add(ia);
            }
        }
        if (!listeNewElevesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewElevesTempo, UtilFees.DOSSIER_PAIEMENT, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Paiements enregistrés !");
                }

                @Override
                public void onError(String message) {
                    System.err.println(message);
                    ee.onError("Erreur !");
                }

                @Override
                public void onProcessing(String message) {
                    System.out.println(message);
                    ee.onUploading("Enregistrement...");
                }
            });
        }
    }

}
