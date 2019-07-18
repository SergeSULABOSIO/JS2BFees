/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;


import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback.EcouteurStandard;
import SOURCES.Objets.FileManager;
import SOURCES.UI.PanelLitige;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires.DonneesLitige;
import SOURCES.Utilitaires.ParametresLitige;
import SOURCES.Utilitaires_Insc.SortiesInscription;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Interface.InterfaceExercice;
import Source.Interface.InterfaceMonnaie;
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
import static java.lang.Thread.sleep;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

/**
 *
 * @author HP Pavilion
 */
public class GestionLitiges {

    public PanelLitige panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public ParametresLitige parametreLitige;
    public DonneesLitige donneesLitige;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;

    private Exercice exercice = null;
    private FileManager fm;
    private Vector<Classe> classes = new Vector<>();
    private Vector<Frais> frais = new Vector<>();
    private Vector<Eleve> eleves = new Vector<>();
    private Vector<Ayantdroit> ayantDroits = new Vector<>();
    private Vector<Monnaie> monnaies = new Vector<>();
    private Vector<Periode> periodes = new Vector<>();
    private Vector<Paiement> paiements = new Vector<>();
    private CouleurBasique couleurBasique;

    public GestionLitiges(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
    }

    private void initParamsEtDonnees() {
        this.parametreLitige = new ParametresLitige(utilisateur.getId(), utilisateur.getNom() + " " + utilisateur.getPrenom(), entreprise, exercice, monnaies.firstElement(), monnaies, classes, frais, periodes);
        this.donneesLitige = new DonneesLitige(eleves, ayantDroits, paiements);
    }

    public void gi_setDonneesFromFileManager(String selectedAnnee) {
        if (fm != null) {
            boolean mustLoadData = true;
            int nbOnglets = tabOnglet.getComponentCount();
            for (int i = 0; i < nbOnglets; i++) {
                JPanel onglet = (JPanel) tabOnglet.getComponentAt(i);
                String titreOnglet = tabOnglet.getTitleAt(i);
                System.out.println("Onglet - " + titreOnglet);
                if (titreOnglet.equals(selectedAnnee + " - Adhésion")) {
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
                    Eleve classe = (Eleve) o;
                    if (classe.getIdExercice() == exercice.getId()) {
                        eleves.add(classe);
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
                progress.setVisible(false);
                progress.setIndeterminate(false);
                initUI(exercice.getNom() + " - Adhésion");
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
        
        panel = new PanelLitige(couleurBasique, tabOnglet, donneesLitige, parametreLitige);
        //Chargement du gestionnaire sur l'onglet
        tabOnglet.addTab(nomTab, panel);
        tabOnglet.setSelectedComponent(panel);
        
        
        /*
        
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
        });

        //Chargement du gestionnaire sur l'onglet
        tabOnglet.addTab(nomTab, panel);
        tabOnglet.setSelectedComponent(panel);
        
        */
        
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

    private void saveEleves(SortiesInscription se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Eleve> listeNewEleves = se.getListeEleves();
        Vector<Eleve> listeNewElevesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Eleve ia : listeNewEleves) {
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

    private void saveAyantDroits(SortiesInscription se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
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

}
