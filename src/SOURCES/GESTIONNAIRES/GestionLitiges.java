/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import ICONES.Icones;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Objets.FileManager;
import SOURCES.UI.PanelLitige;
import SOURCES.Utilitaires.CalculateurLitiges;
import SOURCES.Utilitaires.DataLitiges;
import SOURCES.Utilitaires.ParametresLitige;
import Source.Callbacks.ConstructeurCriteres;
import Source.Callbacks.EcouteurCrossCanal;
import Source.Callbacks.EcouteurNavigateurPages;
import Source.Interface.InterfaceLitige;
import Source.Objet.Ayantdroit;
import Source.Objet.Classe;
import Source.Objet.CouleurBasique;
import Source.Objet.Echeance;
import Source.Objet.Eleve;
import Source.Objet.Entreprise;
import Source.Objet.Annee;
import Source.Objet.Frais;
import Source.Objet.Litige;
import Source.Objet.Monnaie;
import Source.Objet.Paiement;
import Source.Objet.Periode;
import Source.Objet.UtilObjet;
import Source.Objet.Utilisateur;
import Source.UI.NavigateurPages;
import Sources.CHAMP_LOCAL;
import Sources.PROPRIETE;
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

    public Annee exercice = null;
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

    public Ayantdroit ayantdroit;

    public CouleurBasique couleurBasique;
    public String selectedAnnee;
    public Eleve eleveConcerned = null;
    public boolean deleteCurrentTab = true;
    public JFrame fenetre;
    public Icones icones;

    public int idFrais = -1;
    public int idPeriode = -1;
    public int idSolvabilite = -1;
    public Litige litige = null;

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

    public GestionLitiges(JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Eleve eleveConcerned) {
        this.couleurBasique = couleurBasique;
        this.fenetre = fenetre;
        this.icones = icones;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = eleveConcerned;
    }

    private DataLitiges getData() {
        ParametresLitige parametreLitige = new ParametresLitige(utilisateur, entreprise, exercice, monnaies.firstElement(), monnaies, classes, frais, periodes);
        return new DataLitiges(parametreLitige);
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
                    //System.out.println("Onglet - " + titreOnglet);
                    String Snom = NOM;
                    if (eleveConcerned != null) {
                        Snom = NOM + " - " + eleveConcerned.getNom() + " " + eleveConcerned.getPrenom();
                    }
                    if (titreOnglet.equals(Snom)) {
                        //System.out.println("Une page d'adhésion était déjà ouverte, je viens de la fermer");
                        tabOnglet.remove(i);
                        mustLoadData = true;
                    }
                }
            }

            if (mustLoadData == true) {
                fm.fm_ouvrirTout(0, Annee.class, UtilObjet.DOSSIER_ANNEE, 1, 100, new EcouteurOuverture() {
                    @Override
                    public boolean isCriteresRespectes(Object object) {
                        Annee annee = (Annee) object;
                        return (annee.getNom().equals(selectedAnnee));
                    }

                    @Override
                    public void onElementLoaded(String message, Object data) {
                        Annee annee = (Annee) data;
                        exercice = annee;
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

    public boolean checkCritere(String motCle, Object data, JS2BPanelPropriete prop) {
        Eleve eleve = (Eleve) data;
        if (eleve.getIdExercice() != exercice.getId()) {
            return false;
        }
        
        if(eleveConcerned != null){
            if(eleveConcerned.getId() != eleve.getId()){
                return false;
            }
        }

        boolean repClasse = true;
        boolean repMotCle = panel.verifierNomEleve(motCle, eleve);

        if (prop != null) {
            PROPRIETE propClasse = prop.getPropriete("Classe");
            repClasse = panel.verifierClasse(propClasse.getValeurSelectionne() + "", eleve);

            PROPRIETE propFrais = prop.getPropriete("Frais");
            if ((propFrais.getValeurSelectionne() + "").trim().length() != 0) {
                Frais frs = panel.getFrais(propFrais.getValeurSelectionne() + "");
                if (frs != null) {
                    idFrais = frs.getId();
                }
            }

            PROPRIETE propPeriode = prop.getPropriete("Période");
            if ((propPeriode.getValeurSelectionne() + "").trim().length() != 0) {
                Periode prd = panel.getPeriode(propPeriode.getValeurSelectionne() + "");
                if (prd != null) {
                    idPeriode = prd.getId();
                }
            }

            PROPRIETE propSolvabilite = prop.getPropriete("Solvabilité");
            String valSolva = (propSolvabilite.getValeurSelectionne() + "");
            if (valSolva.trim().length() != 0) {
                if (valSolva.equals("SOLVABLES")) {
                    idSolvabilite = 0;
                } else if (valSolva.equals("INSOLVABLES")) {
                    idSolvabilite = 1;
                }
            }
        }

        if (repMotCle == true && repClasse == true) {
            loadAyantDroit(eleve);
            loadPaiements(eleve, idFrais);
            Vector<Echeance> listeEcheances = CalculateurLitiges.getEcheances(idSolvabilite, idFrais, idPeriode, eleve, ayantdroit, paiements, panel.getParametresLitige());
            if (listeEcheances != null) {
                if (!listeEcheances.isEmpty()) {
                    //litige = new Litige(1, eleve.getId(), eleve.getIdClasse(), listeEcheances, InterfaceLitige.BETA_EXISTANT);
                    return true;
                }
            }
        }
        return false;
    }

    private void loadEleves(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        //eleves.removeAllElements();
        fm.fm_ouvrirTout(0, Eleve.class, UtilObjet.DOSSIER_ELEVE, pageActuelle, taillePage, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return checkCritere(motCle, object, criteresAvances);
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Eleve eleveLoaded = (Eleve) data;
                createLitige(eleveLoaded);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
                navigateurPages.setInfos(resultatTotal, panel.getTailleResultatLitiges());
                navigateurPages.patienter(false, "Prêt.");
            }

            @Override
            public void onError(String string) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
                navigateurPages.setInfos(0, 0);
                navigateurPages.patienter(false, "Prêt.");
            }

            @Override
            public void onProcessing(String string) {
                progress.setVisible(true);
                progress.setIndeterminate(true);
            }
        });
    }

    public void createLitige(Eleve eleveLoaded) {
        if (!eleves.contains(eleveLoaded)) {
            loadAyantDroit(eleveLoaded);
            loadPaiements(eleveLoaded, idFrais);
            Vector<Echeance> listeEcheances = CalculateurLitiges.getEcheances(idSolvabilite, idFrais, idPeriode, eleveLoaded, ayantdroit, paiements, panel.getParametresLitige());
            if (listeEcheances != null) {
                if (!listeEcheances.isEmpty()) {
                    litige = new Litige(1, eleveLoaded.getId(), eleveLoaded.getIdClasse(), listeEcheances, InterfaceLitige.BETA_EXISTANT);
                }
            }
            panel.setDonneesLitiges(litige, eleveLoaded, ayantdroit);
            eleves.add(eleveLoaded);
        }

    }

    private void loadAyantDroit(Eleve eleve) {
        ayantdroit = null;
        fm.fm_ouvrirTout(0, Ayantdroit.class, UtilObjet.DOSSIER_AYANT_DROIT, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Ayantdroit aya = (Ayantdroit) object;
                if (aya != null) {
                    if (aya.getSignatureEleve() == eleve.getSignature() && exercice.getId() == aya.getIdExercice()) {
                        ayantdroit = aya;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                //ayantdroit = (Ayantdroit) data;
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {

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
                if (classe.getIdExercice() == exercice.getId()) {
                    if (eleveConcerned != null) {
                        return (classe.getId() == eleveConcerned.getIdClasse());
                    } else {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Classe classe = (Classe) data;
                classes.add(classe);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
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
        fm.fm_ouvrirTout(0, Monnaie.class, UtilObjet.DOSSIER_MONNAIE, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Monnaie monnaie = (Monnaie) object;
                return (monnaie.getIdExercice() == exercice.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Monnaie monnaie = (Monnaie) data;
                monnaies.add(monnaie);
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

    private void loadPeriodes() {
        periodes.removeAllElements();
        fm.fm_ouvrirTout(0, Periode.class, UtilObjet.DOSSIER_PERIODE, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Periode classe = (Periode) object;
                return (classe.getIdExercice() == exercice.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Periode classe = (Periode) data;
                periodes.add(classe);
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

    private void loadPaiements(Eleve eleve, int idFrais) {
        paiements.removeAllElements();
        fm.fm_ouvrirTout(0, Paiement.class, UtilObjet.DOSSIER_PAIEMENT, 1, 10000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Paiement paiement = (Paiement) object;
                if (paiement.getIdEleve() == eleve.getId() && paiement.getIdExercice() == exercice.getId()) {
                    if (paiement.getIdFrais() == idFrais && idFrais != -1) {
                        paiements.add(paiement);
                        return true;
                    } else if (idFrais == -1) {
                        paiements.add(paiement);
                        return true;
                    }

                }

                return false;
            }

            @Override
            public void onElementLoaded(String message, Object data) {

            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {

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
        fm.fm_ouvrirTout(0, Frais.class, UtilObjet.DOSSIER_FRAIS, 1, 100, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Frais oFrais = (Frais) object;
                return (oFrais.getIdExercice() == exercice.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Frais oFrais = (Frais) data;
                frais.add(oFrais);
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

    private void chercherEleves(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        loadEleves(motCle, pageActuelle, taillePage, criteresAvances, navigateurPages);
    }

    private void initUI(String nomTab) {
        panel = new PanelLitige(couleurBasique, tabOnglet, getData(), progress, new EcouteurCrossCanal() {
            @Override
            public void onOuvrirPaiements(Eleve eleve) {
                new Thread() {
                    public void run() {
                        new GestionPaiements(icones, couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve)
                                .gl_setDonneesFromFileManager(selectedAnnee, true);
                    }
                }.start();
            }

            @Override
            public void onOuvrirInscription(Eleve eleve) {
                new Thread() {
                    public void run() {
                        new GestionAdhesion(fenetre, icones, couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve)
                                .gi_setDonneesFromFileManager(selectedAnnee, true);
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
                        idFrais = -1;
                        idPeriode = -1;
                        idSolvabilite = -1;
                        litige = null;
                        
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
                JS2BPanelPropriete panProp = new JS2BPanelPropriete(icones.getFiltrer_01(), "Critères avancés", true);
                panProp.viderListe();

                //Critres classes
                Vector listeClasses = new Vector();
                listeClasses.add("TOUTES");
                for (Classe cl : panel.getParametresLitige().getListeClasse()) {
                    listeClasses.add(cl.getNom());
                }
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getClasse_01(), "Classe", "cls", listeClasses, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                //Critres Frais
                Vector listeFrais = new Vector();
                listeFrais.add("TOUS");
                for (Frais cl : panel.getParametresLitige().getListeFraises()) {
                    listeFrais.add(cl.getNom());
                }
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getTaxes_01(), "Frais", "cls", listeFrais, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                //Critres Période
                Vector listePeriodes = new Vector();
                listePeriodes.add("TOUTES");
                for (Periode per : panel.getParametresLitige().getListePeriodes(-1)) {
                    listePeriodes.add(per.getNom());
                }
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "Période", "cls", listePeriodes, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                Vector listeSolvabilite = new Vector();
                listeSolvabilite.add("TOUS");
                listeSolvabilite.add("SOLVABLES");
                listeSolvabilite.add("INSOLVABLES");
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getRecette_01(), "Solvabilité", "cls", listeSolvabilite, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                return panProp;
            }
        }
        );

        naviNavigateurPages.setInfos(0, eleves.size());
        //Chargement du gestionnaire sur l'onglet
        if (deleteCurrentTab == true) {
            tabOnglet.addTab(nomTab, panel);
            tabOnglet.setSelectedComponent(panel);
        }
        progress.setVisible(false);
        progress.setIndeterminate(false);
        
        naviNavigateurPages.criteresActuels_activer();
    }

}








































