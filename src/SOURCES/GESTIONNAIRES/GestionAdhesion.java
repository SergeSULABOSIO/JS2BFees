/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import ICONES.Icones;
import SOURCES.CALLBACK.EcouteurGestionInscription;
import SOURCES.Callback.EcouteurContains;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback_Insc.EcouteurInscription;
import SOURCES.Objets.FileManager;
import SOURCES.UI_Insc.PanelInscription;
import SOURCES.Utilitaires_Insc.DataInscription;
import SOURCES.Utilitaires_Insc.ParametreInscription;
import SOURCES.Utilitaires_Insc.SortiesInscription;
import SOURCES.Utilitaires_Insc.UtilInscription;
import Source.Callbacks.ConstructeurCriteres;
import Source.Callbacks.EcouteurCrossCanal;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Callbacks.EcouteurFreemium;
import Source.Callbacks.EcouteurNavigateurPages;
import Source.Callbacks.EcouteurStandard;
import Source.Interface.InterfaceEleve;
import Source.Interface.InterfaceMonnaie;
import Source.Objet.Ayantdroit;
import Source.Objet.Classe;
import Source.Objet.CouleurBasique;
import Source.Objet.Eleve;
import Source.Objet.Entreprise;
import Source.Objet.Annee;
import Source.Objet.Frais;
import Source.Objet.Monnaie;
import Source.Objet.UtilObjet;
import Source.Objet.Utilisateur;
import Source.UI.NavigateurPages;
import Sources.CHAMP_LOCAL;
import Sources.PROPRIETE;
import Sources.UI.JS2BPanelPropriete;
import static java.lang.Thread.sleep;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import Source.Interface.InterfaceAnnee;
import Source.Objet.Paiement;

/**
 *
 * @author HP Pavilion
 */
public class GestionAdhesion {

    public static String NOM = "ADHESION";
    public PanelInscription panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;

    private Annee exercice = null;
    private FileManager fm;

    //Paramètres
    private Vector<Classe> classes = new Vector<>();
    private Vector<Frais> frais = new Vector<>();
    private Vector<Monnaie> monnaies = new Vector<>();
    //Donnees
    private Vector<Eleve> eleves = new Vector<>();
    private Vector<Ayantdroit> ayantDroit = new Vector<>();

    private CouleurBasique couleurBasique;
    public String selectedAnnee = "";
    public Eleve eleveConcerned = null;
    boolean deleteCurrentTab = true;
    public JFrame fenetre = null;
    public Icones icones = null;
    public boolean canBeSaved;
    public EcouteurFreemium ef = null;
    private EcouteurGestionInscription ei;

    public GestionAdhesion(EcouteurGestionInscription ei, EcouteurFreemium ef, JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.ef = ef;
        this.ei = ei;
        this.fenetre = fenetre;
        this.icones = icones;
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = null;
    }

    public GestionAdhesion(EcouteurGestionInscription ei, EcouteurFreemium ef, JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Eleve eleveConcerned) {
        this.ef = ef;
        this.ei = ei;
        this.fenetre = fenetre;
        this.icones = icones;
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = eleveConcerned;
    }

    private DataInscription getData() {
        ParametreInscription parametreInscription = new ParametreInscription(monnaies, classes, frais, entreprise, exercice, utilisateur);
        return new DataInscription(parametreInscription);
    }

    public void gi_setDonneesFromFileManager(String selectedAnnee, boolean deleteCurrentTab) {
        this.deleteCurrentTab = deleteCurrentTab;
        this.selectedAnnee = selectedAnnee;
        if (fm != null) {
            boolean mustLoadData = true;
            if (deleteCurrentTab == true) {
                int nbOnglets = tabOnglet.getComponentCount();
                for (int i = 0; i < nbOnglets; i++) {
                    if (tabOnglet.getComponentCount() > i) {
                        String titreOnglet = tabOnglet.getTitleAt(i);
                        String Snom = NOM;
                        if (eleveConcerned != null) {
                            Snom = NOM + " - " + eleveConcerned.getNom() + " " + eleveConcerned.getPrenom();
                        }
                        if (titreOnglet.equals(Snom)) {
                            tabOnglet.remove(i);
                            mustLoadData = true;
                        }
                    }
                }
            }

            if (mustLoadData == true) {
                fm.fm_ouvrirTout(0, Annee.class, UtilObjet.DOSSIER_ANNEE, 1, 100, new EcouteurOuverture() {

                    @Override
                    public boolean isCriteresRespectes(Object object) {
                        return true;
                    }

                    @Override
                    public void onElementLoaded(String message, Object data) {
                        Annee annee = (Annee) data;
                        if (annee.getNom().equals(selectedAnnee)) {
                            exercice = annee;
                        }
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

    private void chercherEleves(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        loadEleves(motCle, pageActuelle, taillePage, criteresAvances, navigateurPages);
    }

    private boolean verifierNomEleve(String motCle, Eleve Ieleve) {
        boolean reponse = false;
        if (motCle.trim().length() == 0) {
            reponse = true;
        } else {
            reponse = ((UtilInscription.contientMotsCles(Ieleve.getNom() + " " + Ieleve.getPostnom() + " " + Ieleve.getPrenom(), motCle)));
        }
        return reponse;
    }

    public boolean checkCritere(String motCle, Object data, JS2BPanelPropriete jsbpp) {
        Eleve eleve = (Eleve) data;
        boolean canAdd = false;
        boolean repSexe = false;
        boolean repStat = false;
        boolean repClasse = false;
        boolean repMotCle = verifierNomEleve(motCle, eleve);

        if (eleve.getIdExercice() == exercice.getId()) {
            if (eleveConcerned != null) {
                if (eleve.getId() == eleveConcerned.getId()) {
                    canAdd = true;
                }
            } else {
                canAdd = true;
            }

            if (canAdd == true) {
                if (jsbpp != null) {
                    PROPRIETE propGenre = jsbpp.getPropriete("Genre");
                    if (eleve.getSexe() == InterfaceEleve.SEXE_MASCULIN && (propGenre.getValeurSelectionne() + "").equals("Masculin")) {
                        repSexe = true;
                    } else if (eleve.getSexe() == InterfaceEleve.SEXE_FEMININ && (propGenre.getValeurSelectionne() + "").equals("Féminin")) {
                        repSexe = true;
                    } else if ((propGenre.getValeurSelectionne() + "").trim().length() == 0) {
                        repSexe = true;
                    } else {
                        repSexe = false;
                    }

                    PROPRIETE propStatut = jsbpp.getPropriete("Statut");
                    if (eleve.getStatus() == InterfaceEleve.STATUS_ACTIF && (propStatut.getValeurSelectionne() + "").equals("ACTIF")) {
                        repStat = true;
                    } else if (eleve.getStatus() == InterfaceEleve.STATUS_INACTIF && (propStatut.getValeurSelectionne() + "").equals("INACTIF")) {
                        repStat = true;
                    } else if ((propStatut.getValeurSelectionne() + "").trim().length() == 0) {
                        repStat = true;
                    } else {
                        repStat = false;
                    }

                    PROPRIETE propClasse = jsbpp.getPropriete("Classe");
                    if ((propClasse.getValeurSelectionne() + "").trim().length() == 0) {
                        repClasse = true;
                    } else {
                        Classe clss = panel.getClasse(propClasse.getValeurSelectionne() + "");
                        if (clss != null) {
                            if (clss.getId() == eleve.getIdClasse()) {
                                repClasse = true;
                            }
                        }
                    }
                } else {
                    repSexe = true;
                    repStat = true;
                    repClasse = true;
                }
            }
        }
        if (repMotCle == true && repSexe == true && repStat == true && repClasse == true) {//
            return true;
        } else {
            return false;
        }
    }

    private void loadEleves(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete jsbpp, NavigateurPages nav) {
        /**/
        //eleves.removeAllElements();
        fm.fm_ouvrirTout(0, Eleve.class, UtilObjet.DOSSIER_ELEVE, pageActuelle, taillePage, new EcouteurOuverture() {

            @Override
            public boolean isCriteresRespectes(Object object) {
                //System.out.println("Mot clé: " + motCle);
                return checkCritere(motCle, object, jsbpp);
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Eleve eleveLoaded = (Eleve) data;
                if (eleveLoaded != null) {
                    //eleves.add(eleveLoaded);
                    panel.setDonneesEleves(eleveLoaded);
                    loadAyantDroit(eleveLoaded.getSignature());
                }
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                progress.setVisible(false);
                progress.setIndeterminate(false);

                nav.setInfos(resultatTotal, panel.getTailleResultatEleves());
                nav.patienter(false, "Prêt.");

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

    private void loadAyantDroit(long signatureEleve) {
        /* */
        fm.fm_ouvrirTout(0, Ayantdroit.class, UtilObjet.DOSSIER_AYANT_DROIT, 1, 100, new EcouteurOuverture() {

            @Override
            public boolean isCriteresRespectes(Object object) {
                return true;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Ayantdroit ayantdroit = (Ayantdroit) data;
                if (ayantdroit.getIdExercice() == exercice.getId() && signatureEleve == ayantdroit.getSignatureEleve()) {
                    if (eleveConcerned != null) {
                        if (ayantdroit.getSignatureEleve() == eleveConcerned.getSignature()) {
                            if (!ayantDroit.contains(ayantdroit)) {
                                ayantDroit.add(ayantdroit);
                                panel.setDonneesAyantDroit(ayantdroit);
                            }
                        }
                    } else {
                        if (!ayantDroit.contains(ayantdroit)) {
                            ayantDroit.add(ayantdroit);
                            panel.setDonneesAyantDroit(ayantdroit);
                        }
                    }
                }
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {

            }

            @Override
            public void onError(String string) {
                //progress.setVisible(false);
                //progress.setIndeterminate(false);
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
                return true;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Classe classe = (Classe) data;
                if (classe.getIdExercice() == exercice.getId()) {
                    if (!classes.contains(classe)) {
                        classes.add(classe);
                    }
                }
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
        fm.fm_ouvrirTout(0, Frais.class, UtilObjet.DOSSIER_FRAIS, 1, 100, new EcouteurOuverture() {
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

    private void action_save(SortiesInscription se) {
        if (se != null) {
            Thread th = new Thread() {
                @Override
                public void run() {
                    try {
                        EcouteurEnregistrement ee = se.getEcouteurEnregistrement();
                        Utilisateur user = fm.fm_getSession().getUtilisateur();
                        ee.onUploading("Chargement...");

                        progress.setVisible(true);
                        progress.setIndeterminate(true);

                        sleep(50);

                        //DEBUT D'ENREGISTREMENT
                        saveEleves(se, ee, user, exercice);

                        se.getEcouteurEnregistrement().onDone("Enregistré!");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            };
            th.start();
        }
    }

    private void saveEleves(SortiesInscription se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Eleve> listeNewEleves = se.getListeEleves();
        Vector<Eleve> listeNewElevesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Eleve ia : listeNewEleves) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                canBeSaved = true;
                String infosEleve = ia.getNom() + " " + ia.getPostnom() + " " + ia.getPrenom();

                //Pour id étrangère incomplètes
                if (ia.getIdClasse() == -1) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, infosEleve + " n'est pas enregistré car vous n'avez pas défini sa classe!\nVeuillez préciser sa classe puis enregistrer de nouveau.", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }

                //Controle pour éviter de doublon
                fm.fm_contains(Eleve.class, UtilObjet.DOSSIER_ELEVE, new EcouteurContains() {
                    @Override
                    public boolean isOk(Object objectToCheck) {
                        Eleve eChecking = (Eleve) objectToCheck;
                        String elA = ia.getNom().trim() + "" + ia.getPostnom().trim() + "" + ia.getPrenom().trim() + "" + ia.getIdClasse();
                        String elB = eChecking.getNom().trim() + "" + eChecking.getPostnom().trim() + "" + eChecking.getPrenom().trim() + "" + eChecking.getIdClasse();
                        return (elA.trim().toLowerCase()).equals((elB.trim().toLowerCase())) && ia.getBeta() == InterfaceEleve.BETA_NOUVEAU;
                    }

                    @Override
                    public void onSuccess(String message, boolean reponse, Vector TabObjsFound) {
                        if (reponse == true) {
                            canBeSaved = false;
                            for (Object oo : TabObjsFound) {
                                Eleve eFound = (Eleve) oo;
                                String infoEF = eFound.getNom() + " " + eFound.getPostnom() + " " + eFound.getPrenom();
                                JOptionPane.showMessageDialog(panel, infoEF + " est déjà enregistré(e)!", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                                panel.setBtEnregistrerNouveau();
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {
                        System.err.println(message);
                    }

                    @Override
                    public void onProcessing(String message) {

                    }
                });

                if (canBeSaved == true) {
                    ia.setIdExercice(annee.getId());
                    ia.setIdUtilisateur(user.getId());
                    ia.setIdEntreprise(user.getIdEntreprise());
                    ia.setBeta(InterfaceAnnee.BETA_EXISTANT);
                    listeNewElevesTempo.add(ia);
                }
            }
        }
        if (!listeNewElevesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewElevesTempo, UtilObjet.DOSSIER_ELEVE, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Eleves enregistrées !");
                    //donneesExercice.setAgents(listeNewAgentsTempo);
                    saveAyantDroits(se, ee, user, exercice);
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
        } else {
            saveAyantDroits(se, ee, user, exercice);
        }
    }

    private void saveAyantDroits(SortiesInscription se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Ayantdroit> listeNewAy = se.getListeAyantDroit();
        Vector<Ayantdroit> listeNewAYTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        //System.out.println("AYANT DROIT **** ");
        for (Ayantdroit ia : listeNewAy) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                canBeSaved = true;

                //Pour id étrangère incomplètes
                if (ia.getSignatureEleve() == -1) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, "Désolé, veuillez préciser l'élève à considérer comme Ayant droit!", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }

                if (canBeSaved == true) {
                    ia.setIdExercice(annee.getId());
                    ia.setIdUtilisateur(user.getId());
                    ia.setIdEntreprise(user.getIdEntreprise());
                    ia.setBeta(InterfaceAnnee.BETA_EXISTANT);
                    listeNewAYTempo.add(ia);
                }
            }
        }
        if (!listeNewAYTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewAYTempo, UtilObjet.DOSSIER_AYANT_DROIT, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    progress.setVisible(false);
                    progress.setIndeterminate(false);

                    //System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Ayant-droits enregistrés !");
                }

                @Override
                public void onError(String message) {
                    progress.setVisible(false);
                    progress.setIndeterminate(false);
                    //System.err.println(message);
                    ee.onError("Erreur !");
                }

                @Override
                public void onProcessing(String message) {
                    //System.out.println(message);
                    ee.onUploading("Enregistrement...");
                }
            });
        } else {
            progress.setVisible(false);
            progress.setIndeterminate(false);
        }
    }

    private void initUI(String nomTab) {
        panel = new PanelInscription(ef, couleurBasique, tabOnglet, getData(), progress, new EcouteurInscription() {
            @Override
            public void onEnregistre(SortiesInscription si) {
                if (si != null) {
                    action_save(si);
                }
            }

            @Override
            public void onDetruitExercice(int idExercice) {

            }

            @Override
            public void onDetruitElements(int idElement, int index, long signature) {
                if (idElement != -1) {
                    switch (index) {
                        case 0://ELEVE
                            //On va d'abord charger tous les paiement effectué par cet élève que l'on veut supprimer
                            //L'objectif ici est de supprimer également tous les paiements que celui-ci a déjà effectués
                            fm.fm_ouvrirTout(0, Paiement.class, UtilObjet.DOSSIER_PAIEMENT, 1, 1000000, new EcouteurOuverture() {
                                @Override
                                public boolean isCriteresRespectes(Object object) {
                                    Paiement p = (Paiement) object;
                                    return p.getIdEleve() == idElement;
                                }

                                @Override
                                public void onElementLoaded(String message, Object data) {
                                    Paiement pas = (Paiement) data;
                                    fm.fm_supprimer(UtilObjet.DOSSIER_PAIEMENT, pas.getId(), pas.getSignature());
                                }

                                @Override
                                public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                                    //C'est quand on a finit de supprimer les paiements de cet élève que l'on va lui supprimer lui-même
                                    fm.fm_supprimer(UtilObjet.DOSSIER_ELEVE, idElement, signature);
                                    progress.setVisible(false);
                                    progress.setIndeterminate(false);
                                    progress.setString("Prêt.");
                                    
                                }

                                @Override
                                public void onError(String message) {
                                    progress.setVisible(false);
                                    progress.setIndeterminate(false);
                                    progress.setString("Erreur !");
                                }

                                @Override
                                public void onProcessing(String message) {
                                    progress.setVisible(true);
                                    progress.setIndeterminate(true);
                                    progress.setString("Suppression en cours");
                                }
                            });

                            break;
                        case 1://AYANT-DROIT
                            //On va d'abord charger tous les paiement effectué par cet ayant-droit que l'on veut supprimer
                            //L'objectif ici est de supprimer également tous les paiements que celui-ci a déjà effectués
                            fm.fm_ouvrirTout(0, Paiement.class, UtilObjet.DOSSIER_PAIEMENT, 1, 1000000, new EcouteurOuverture() {
                                @Override
                                public boolean isCriteresRespectes(Object object) {
                                    Paiement p = (Paiement) object;
                                    return p.getIdEleve() == idElement;
                                }

                                @Override
                                public void onElementLoaded(String message, Object data) {
                                    Paiement pas = (Paiement) data;
                                    fm.fm_supprimer(UtilObjet.DOSSIER_PAIEMENT, pas.getId(), pas.getSignature());
                                }

                                @Override
                                public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                                    //C'est quand on a finit de supprimer les paiements de cet élève que l'on va lui supprimer lui-même
                                    fm.fm_supprimer(UtilObjet.DOSSIER_AYANT_DROIT, idElement, signature);
                                    progress.setVisible(false);
                                    progress.setIndeterminate(false);
                                    progress.setString("Prêt.");
                                    
                                }

                                @Override
                                public void onError(String message) {
                                    progress.setVisible(false);
                                    progress.setIndeterminate(false);
                                    progress.setString("Erreur !");
                                }

                                @Override
                                public void onProcessing(String message) {
                                    progress.setVisible(true);
                                    progress.setIndeterminate(true);
                                    progress.setString("Suppression en cours");
                                }
                            });
                            
                            break;
                        default:
                    }
                }
            }

            @Override
            public void onClose() {
                ei.onClosed();
            }
        }, new EcouteurCrossCanal() {
            @Override
            public void onOuvrirPaiements(Eleve eleve) {
                new Thread() {
                    public void run() {
                        new GestionPaiements(ef, icones, couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve).gl_setDonneesFromFileManager(selectedAnnee, true);
                    }
                }.start();
            }

            @Override
            public void onOuvrirInscription(Eleve eleve) {
                //on ne fait rien
            }

            @Override
            public void onOuvrirLitiges(Eleve eleve) {
                new Thread() {
                    public void run() {
                        System.out.println("Ouverture des litiges de " + eleve.getNom());
                        new GestionLitiges(null, ef, fenetre, icones, couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve).gl_setDonneesFromFileManager(selectedAnnee, true);
                    }
                }.start();
            }

        });

        NavigateurPages naviNavigateurPages = panel.getNavigateur();
        naviNavigateurPages.initialiser(fenetre, new EcouteurNavigateurPages() {
            @Override
            public void onRecharge(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances) {
                new Thread() {
                    public void run() {
                        naviNavigateurPages.setInfos(0, eleves.size());
                        naviNavigateurPages.patienter(true, "Chargement...");
                        panel.reiniliserEleves();
                        panel.reiniliserAyantDroit();
                        chercherEleves(motCle, pageActuelle, taillePage, criteresAvances, naviNavigateurPages);
                    }
                }.start();
            }
        }, new ConstructeurCriteres() {
            @Override
            public JS2BPanelPropriete onInitialise() {
                Vector listeClasses = new Vector();
                listeClasses.add("TOUTES LES CLASSES");
                for (Classe cl : panel.getDataInscription().getParametreInscription().getListeClasses()) {
                    listeClasses.add(cl.getNom());
                }

                Vector listeGenre = new Vector();
                listeGenre.add("TOUT GENRE");
                listeGenre.add("Masculin");
                listeGenre.add("Féminin");

                Vector listeStatut = new Vector();
                listeStatut.add("TOUT STATUT");
                listeStatut.add("ACTIF");
                listeStatut.add("INACTIF");

                JS2BPanelPropriete panProp = new JS2BPanelPropriete(icones.getFiltrer_01(), "Critères avancés", true);
                panProp.viderListe();
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getClasse_01(), "Classe", "cls", listeClasses, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getClient_01(), "Genre", "cls", listeGenre, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getAimer_01(), "Statut", "cls", listeStatut, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                return panProp;
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

        naviNavigateurPages.criteresActuels_activer();
    }
}
