/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import ICONES.Icones;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.EcouteurLitiges.EcouteurActualisationLitiges;
import SOURCES.Objets.FileManager;
import SOURCES.UI.PanelLitige;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires.DataLitiges;
import SOURCES.Utilitaires.DonneesLitige;
import SOURCES.Utilitaires.ParametresLitige;
import Source.Callbacks.ConstructeurCriteres;
import Source.Callbacks.EcouteurCrossCanal;
import Source.Callbacks.EcouteurNavigateurPages;
import Source.Objet.Ayantdroit;
import Source.Objet.Classe;
import Source.Objet.CouleurBasique;
import Source.Objet.Eleve;
import Source.Objet.Entreprise;
import Source.Objet.Exercice;
import Source.Objet.Frais;
import Source.Objet.LiaisonFraisClasse;
import Source.Objet.LiaisonFraisPeriode;
import Source.Objet.Litige;
import Source.Objet.Monnaie;
import Source.Objet.Paiement;
import Source.Objet.Periode;
import Source.Objet.Utilisateur;
import Source.UI.NavigateurPages;
import Sources.UI.JS2BPanelPropriete;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

/**
 *
 * @author HP Pavilion
 */
public class GestionLitiges {

    public static String NOM = "LITIGE";
    public PanelLitige panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;

    public Exercice exercice = null;
    public FileManager fm;
    //Parametres
    public Vector<Classe> classes = new Vector<>();
    public Vector<Frais> frais = new Vector<>();
    public Vector<Monnaie> monnaies = new Vector<>();
    public Vector<Periode> periodes = new Vector<>();
    //Donnees  àcharger aux moments opportuns
    public Vector<Paiement> paiements = new Vector<>();
    public Vector<Eleve> eleves = new Vector<>();
    public Vector<Ayantdroit> ayantDroits = new Vector<>();
    public Vector<Litige> litiges = new Vector<>();

    public CouleurBasique couleurBasique;
    public String selectedAnnee;
    public Eleve eleveConcerned = null;
    public boolean deleteCurrentTab = true;
    public JFrame fenetre;
    public Icones icones;

    public GestionLitiges(JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.couleurBasique = couleurBasique;
        this.fenetre = fenetre;
        this.icones = icones;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = null;
    }

    public GestionLitiges(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Eleve eleveConcerned) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = eleveConcerned;
    }

    private DataLitiges getData() {
        ParametresLitige parametreLitige = new ParametresLitige(utilisateur.getId(), utilisateur.getNom() + " " + utilisateur.getPrenom(), entreprise, exercice, monnaies.firstElement(), monnaies, classes, frais, periodes);
        DonneesLitige donneesLitige = new DonneesLitige(eleves, ayantDroits, paiements);
        System.out.println("getData!");
        return new DataLitiges(donneesLitige, parametreLitige);
    }

    public void gl_setDonneesFromFileManager(String selectedAnnee, boolean deleteCurrentTab) {
        this.deleteCurrentTab = deleteCurrentTab;
        this.selectedAnnee = selectedAnnee;
        if (fm != null) {
            boolean mustLoadData = true;
            if (deleteCurrentTab == true) {
                int nbOnglets = tabOnglet.getComponentCount();
                for (int i = 0; i < nbOnglets; i++) {
                    String titreOnglet = tabOnglet.getTitleAt(i);
                    System.out.println("Onglet - " + titreOnglet);
                    String Snom = NOM;
                    if (eleveConcerned != null) {
                        Snom = NOM + " - " + eleveConcerned.getNom() + " " + eleveConcerned.getPrenom();
                    }
                    if (titreOnglet.equals(Snom)) {
                        System.out.println("Une page d'adhésion était déjà ouverte, je viens de la fermer");
                        tabOnglet.remove(i);
                        mustLoadData = true;
                    }
                }
            }

            if (mustLoadData == true) {
                fm.fm_ouvrirTout(0, Exercice.class, UtilFees.DOSSIER_ANNEE, 1, 100, new EcouteurOuverture() {
                    @Override
                    public boolean isCriteresRespectes(Object object) {
                        return true;
                    }

                    @Override
                    public void onElementLoaded(String message, Object data) {
                        Exercice annee = (Exercice) data;
                        if (annee.getNom().equals(selectedAnnee)) {
                            exercice = annee;
                        }
                    }

                    @Override
                    public void onDone(String message, int resultatTotal) {
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
    
    public boolean checkCritere(String motCle, Object data, JS2BPanelPropriete prop){
        Eleve eleve = (Eleve) data;
        boolean repClasse = true;
        boolean repMotCle = panel.verifierNomEleve(motCle, eleve);
        int idFrais = -1;
        int idPeriode = -1;
        int idSolvabilite = -1;
        Litige litige = null;
        Ayantdroit aya = null;
        
        /*
        
        panel.setDonneesEleves(eleveLoaded);
                    loadAyantDroit(eleveLoaded.getSignature());
        
        */
        
        
        
        
        
    }

    private void loadEleves(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        eleves.removeAllElements();
        fm.fm_ouvrirTout(0, Eleve.class, UtilFees.DOSSIER_ELEVE, pageActuelle, taillePage, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return checkCritere(motCle, object, criteresAvances);
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Eleve eleveLoaded = (Eleve) data;
                if (eleveLoaded != null) {
                    if (eleveLoaded.getIdExercice() == exercice.getId()) {
                        if (eleveConcerned != null) {
                            if (eleveLoaded.getId() == eleveConcerned.getId()) {
                                eleves.add(eleveLoaded);
                            }
                        } else {
                            eleves.add(eleveLoaded);
                        }
                    }
                }
            }

            @Override
            public void onDone(String message, int resultatTotal) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
                navigateurPages.setInfos(resultatTotal, litiges.size());
                navigateurPages.patienter(false, "Prêt.");
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

    private void loadAyantDroit() {
        ayantDroits.removeAllElements();
        fm.fm_ouvrirTout(0, Ayantdroit.class, UtilFees.DOSSIER_AYANT_DROIT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Ayantdroit ayantdroit = (Ayantdroit) o;
                    if (ayantdroit.getIdExercice() == exercice.getId()) {
                        if (eleveConcerned != null) {
                            if (ayantdroit.getSignatureEleve() == eleveConcerned.getSignature()) {
                                ayantDroits.add(ayantdroit);
                            }
                        } else {
                            ayantDroits.add(ayantdroit);
                        }
                        System.out.println(" * " + ayantdroit.toString());
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

    private void loadClasses() {
        classes.removeAllElements();
        fm.fm_ouvrirTout(0, Classe.class, UtilFees.DOSSIER_CLASSE, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return true;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Classe classe = (Classe) data;
                if (classe.getIdExercice() == exercice.getId()) {
                    if (eleveConcerned != null) {
                        if (classe.getId() == eleveConcerned.getIdClasse()) {
                            classes.add(classe);
                        }
                    } else {
                        classes.add(classe);
                    }
                }
            }

            @Override
            public void onDone(String message, int resultatTotal) {
                if (eleveConcerned == null) {
                    initUI(NOM);
                } else {
                    initUI(NOM + " - " + eleveConcerned.getNom() + " " + eleveConcerned.getPrenom());
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

    private void loadMonnaies() {
        monnaies.removeAllElements();
        fm.fm_ouvrirTout(0, Monnaie.class, UtilFees.DOSSIER_MONNAIE, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return true;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Monnaie monnaie = (Monnaie) data;
                if (monnaie != null && exercice != null) {
                    if (monnaie.getIdExercice() == exercice.getId()) {
                        monnaies.add(monnaie);
                    }
                }
            }

            @Override
            public void onDone(String message, int resultatTotal) {
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

    private void loadPeriodes() {
        periodes.removeAllElements();
        fm.fm_ouvrirTout(0, Periode.class, UtilFees.DOSSIER_PERIODE, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return true;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Periode classe = (Periode) data;
                if (classe.getIdExercice() == exercice.getId()) {
                    periodes.add(classe);
                }
            }

            @Override
            public void onDone(String message, int resultatTotal) {
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

    private void loadPaiements() {
        paiements.removeAllElements();
        fm.fm_ouvrirTout(0, Paiement.class, UtilFees.DOSSIER_PAIEMENT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Paiement paiement = (Paiement) o;
                    if (paiement.getIdExercice() == exercice.getId()) {
                        if (eleveConcerned != null) {
                            if (paiement.getIdEleve() == eleveConcerned.getId()) {
                                paiements.add(paiement);
                            }
                        } else {
                            paiements.add(paiement);
                        }
                        System.out.println(" * " + paiement.toString());
                    }
                }
                loadEleves();
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
        fm.fm_ouvrirTout(0, Frais.class, UtilFees.DOSSIER_FRAIS, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return true;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Frais oFrais = (Frais) data;
                if (oFrais != null && exercice != null) {
                    if (oFrais.getIdExercice() == exercice.getId()) {
                        if (!frais.contains(oFrais)) {
                            frais.add(oFrais);
                        }
                    }
                }
            }

            @Override
            public void onDone(String message, int resultatTotal) {
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

    private void chercherEleves(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        loadEleves();
    }

    private void initUI(String nomTab) {
        panel = new PanelLitige(couleurBasique, tabOnglet, getData(), progress, new EcouteurCrossCanal() {
            @Override
            public void onOuvrirPaiements(Eleve eleve) {
                new Thread() {
                    public void run() {
                        new GestionPaiements(couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve).gl_setDonneesFromFileManager(selectedAnnee, true);
                    }
                }.start();
            }

            @Override
            public void onOuvrirInscription(Eleve eleve) {
                new Thread() {
                    public void run() {
                        new GestionAdhesion(couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve).gi_setDonneesFromFileManager(selectedAnnee, true);
                    }
                }.start();
            }

            @Override
            public void onOuvrirLitiges(Eleve eleve) {

            }

        });

        NavigateurPages naviNavigateurPages = panel.getNavigateurPage();

        naviNavigateurPages.initialiser(fenetre, new EcouteurNavigateurPages() {
            @Override
            public void onRecharge(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances) {
                new Thread() {
                    public void run() {
                        naviNavigateurPages.setInfos(0, litiges.size());
                        naviNavigateurPages.patienter(true, "Chargement...");
                        panel.reiniliserLitige();
                        chercherEleves(motCle, pageActuelle, taillePage, criteresAvances, naviNavigateurPages);
                    }
                }.start();
            }
        }, new ConstructeurCriteres() {
            @Override
            public JS2BPanelPropriete onInitialise() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

        naviNavigateurPages.setInfos(0, eleves.size());
        //Chargement du gestionnaire sur l'onglet
        if (deleteCurrentTab == true) {
            tabOnglet.addTab(nomTab, panel);
            tabOnglet.setSelectedComponent(panel);
        }
        progress.setVisible(false);
        progress.setIndeterminate(false);
    }

}























