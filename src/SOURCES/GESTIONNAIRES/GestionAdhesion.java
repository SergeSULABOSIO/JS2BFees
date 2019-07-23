/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback.EcouteurStandard;
import SOURCES.Callback_Insc.EcouteurInscription;
import SOURCES.Objets.FileManager;
import SOURCES.UI_Insc.PanelInscription;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires_Insc.DonneesInscription;
import SOURCES.Utilitaires_Insc.ParametreInscription;
import SOURCES.Utilitaires_Insc.SortiesInscription;
import Source.Callbacks.EcouteurCrossCanal;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Interface.InterfaceEleve;
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
import Source.Objet.Utilisateur;
import static java.lang.Thread.sleep;
import java.util.Vector;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

/**
 *
 * @author HP Pavilion
 */
public class GestionAdhesion {

    public static String NOM = "ADHESION";
    public PanelInscription panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public ParametreInscription parametreInscription;
    public DonneesInscription donneesInscription;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;

    private Exercice exercice = null;
    private FileManager fm;
    private Vector<Classe> classes = new Vector<>();
    private Vector<Frais> frais = new Vector<>();
    private Vector<Eleve> eleves = new Vector<>();
    private Vector<Monnaie> monnaies = new Vector<>();
    private Vector<Ayantdroit> ayantDroit = new Vector<>();
    private CouleurBasique couleurBasique;
    public String selectedAnnee = "";
    public Eleve eleveConcerned = null;

    public GestionAdhesion(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = null;
    }
    
    public GestionAdhesion(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Eleve eleveConcerned) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = eleveConcerned;
    }

    private void initParamsEtDonnees() {
        this.parametreInscription = new ParametreInscription(monnaies, classes, frais, entreprise, exercice, utilisateur.getId(), utilisateur.getNom() + " " + utilisateur.getPrenom());
        this.donneesInscription = new DonneesInscription(eleves, ayantDroit);
    }

    public void gi_setDonneesFromFileManager(String selectedAnnee) {
        if (fm != null) {
            this.selectedAnnee = selectedAnnee;
            boolean mustLoadData = true;
            int nbOnglets = tabOnglet.getComponentCount();
            for (int i = 0; i < nbOnglets; i++) {
                //JPanel onglet = (JPanel) tabOnglet.getComponentAt(i);
                String titreOnglet = tabOnglet.getTitleAt(i);
                System.out.println("Onglet - " + titreOnglet);
                
                String Snom = NOM;
                if(eleveConcerned != null){
                    Snom = NOM + " - " + eleveConcerned.getNom() + " " + eleveConcerned.getPrenom();
                }
                if (titreOnglet.equals(Snom)) {
                    System.out.println("Une page d'adhésion était déjà ouverte, je viens de la fermer");
                    tabOnglet.remove(i);
                    mustLoadData = true;
                }
            }

            if (mustLoadData == true) {
                fm.fm_ouvrirTout(100, Exercice.class, UtilFees.DOSSIER_ANNEE, new EcouteurOuverture() {
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

    private void loadEleves() {
        eleves.removeAllElements();
        fm.fm_ouvrirTout(0, Eleve.class, UtilFees.DOSSIER_ELEVE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Eleve eleve = (Eleve) o;
                    if (eleve.getIdExercice() == exercice.getId()) {
                        if(eleveConcerned != null){
                            if(eleve.getId() == eleveConcerned.getId()){
                                eleves.add(eleve);
                            }
                        }else{
                            eleves.add(eleve);
                        }
                        System.out.println(" * " + eleve.toString());
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

    private void loadAyantDroit() {
        ayantDroit.removeAllElements();
        fm.fm_ouvrirTout(0, Ayantdroit.class, UtilFees.DOSSIER_AYANT_DROIT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Ayantdroit ayantdroit = (Ayantdroit) o;
                    if (ayantdroit.getIdExercice() == exercice.getId()) {
                        if(eleveConcerned != null){
                            if(ayantdroit.getSignatureEleve() == eleveConcerned.getSignature()){
                                ayantDroit.add(ayantdroit);
                            }
                        }else{
                            ayantDroit.add(ayantdroit);
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
                if(eleveConcerned == null){
                    initUI(NOM);
                }else{
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

    private void saveEleves(SortiesInscription se, EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<Eleve> listeNewEleves = se.getListeEleves();
        Vector<InterfaceEleve> listeNewElevesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfaceEleve ia : listeNewEleves) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdUtilisateur(user.getId());
                ia.setIdEntreprise(user.getIdEntreprise());
                ia.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewElevesTempo.add(ia);
            }
        }
        if (!listeNewElevesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewElevesTempo, UtilFees.DOSSIER_ELEVE, new EcouteurStandard() {
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

    private void saveAyantDroits(SortiesInscription se, EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<Ayantdroit> listeNewAy = se.getListeAyantDroit();
        Vector<Ayantdroit> listeNewAYTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        System.out.println("AYANT DROIT **** ");
        for (Ayantdroit ia : listeNewAy) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdUtilisateur(user.getId());
                ia.setIdEntreprise(user.getIdEntreprise());
                ia.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewAYTempo.add(ia);
            }
        }
        if (!listeNewAYTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewAYTempo, UtilFees.DOSSIER_AYANT_DROIT, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    progress.setVisible(false);
                    progress.setIndeterminate(false);

                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Ayant-droits enregistrés !");
                }

                @Override
                public void onError(String message) {
                    progress.setVisible(false);
                    progress.setIndeterminate(false);
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
            progress.setVisible(false);
            progress.setIndeterminate(false);
        }
    }
    
    
    private void initUI(String nomTab) {
        initParamsEtDonnees();
        panel = new PanelInscription(couleurBasique, tabOnglet, donneesInscription, parametreInscription, new EcouteurInscription() {
            @Override
            public void onEnregistre(SortiesInscription si) {
                if (si != null) {
                    System.out.println("DANGER !!!!!! ADHESION: Enregistrement...");
                    action_save(si);
                }
            }

            @Override
            public void onDetruitExercice(int idExercice) {

            }

            @Override
            public void onDetruitElements(int idElement, int index) {
                System.out.println("DANGER !!!!!! ADHESION: Destruction de " + idElement + ", indice " + index);
                if (idElement != -1) {
                    switch (index) {
                        case 0://ELEVE
                            fm.fm_supprimer(UtilFees.DOSSIER_ELEVE, idElement);
                            break;
                        case 1://AYANT-DROIT
                            fm.fm_supprimer(UtilFees.DOSSIER_AYANT_DROIT, idElement);
                            break;
                        default:
                    }
                }
            }
        }, new EcouteurCrossCanal() {
            @Override
            public void onOuvrirPaiements(Eleve eleve) {
                new GestionPaiements(couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve).gl_setDonneesFromFileManager(selectedAnnee);
            }

            @Override
            public void onOuvrirInscription(Eleve eleve) {
                //on ne fait rien
            }

            @Override
            public void onOuvrirLitiges(Eleve eleve) {
                new GestionLitiges(couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve).gl_setDonneesFromFileManager(selectedAnnee);
            }
            
            
        });
        
        //Chargement du gestionnaire sur l'onglet
        tabOnglet.addTab(nomTab, panel);
        tabOnglet.setSelectedComponent(panel);
        progress.setVisible(false);
        progress.setIndeterminate(false);
    }

}
