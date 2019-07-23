/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import SOURCES.CALLBACK.EcouteurGestionExercice;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback.EcouteurStandard;
import SOURCES.Callback_Exercice.EcouteurExerice;
import SOURCES.Objets.FileManager;
import SOURCES.UI_Exercice.PanelExercice;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires_Exercice.DonneesExercice;
import SOURCES.Utilitaires_Exercice.ParametreExercice;
import SOURCES.Utilitaires_Exercice.SortiesExercice;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Interface.InterfaceExercice;
import Source.Interface.InterfaceFrais;
import Source.Interface.InterfaceMonnaie;
import Source.Objet.Agent;
import Source.Objet.Charge;
import Source.Objet.Classe;
import Source.Objet.CouleurBasique;
import Source.Objet.Cours;
import Source.Objet.Entreprise;
import Source.Objet.Exercice;
import Source.Objet.Frais;
import Source.Objet.LiaisonFraisClasse;
import Source.Objet.LiaisonFraisPeriode;
import Source.Objet.Monnaie;
import Source.Objet.Periode;
import Source.Objet.Revenu;
import Source.Objet.Utilisateur;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

/**
 *
 * @author user
 */
public class GestionExercice {

    public PanelExercice panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public Monnaie monnaie_output;
    public ParametreExercice parametreExercice;
    public DonneesExercice donneesExercice;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;
    //private SortiesExercice sortiesExercice = null;

    private Exercice newIannee = null;
    private FileManager fm;
    private Vector<Agent> agents = new Vector<>();
    private Vector<Charge> charges = new Vector<>();
    private Vector<Classe> classes = new Vector<>();
    private Vector<Cours> cours = new Vector<>();
    private Vector<Frais> fraises = new Vector<>();
    private Vector<Monnaie> monnaies = new Vector<>();
    private Vector<Periode> periodes = new Vector<>();
    private Vector<Revenu> revenus = new Vector<>();
    private EcouteurGestionExercice ecouteurExercice;
    private CouleurBasique couleurBasique;

    public GestionExercice(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Monnaie monnaie_output, EcouteurGestionExercice ecouteurExercice) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.ecouteurExercice = ecouteurExercice;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.monnaie_output = monnaie_output;
        initParams();
    }

    private void initParams() {
        this.parametreExercice = new ParametreExercice(entreprise, utilisateur.getNom() + " " + utilisateur.getPrenom(), utilisateur.getId(), monnaie_output);
    }

    public void ga_setDonnees(Exercice anneeExistant, Vector<Agent> agents, Vector<Charge> charges, Vector<Classe> classes, Vector<Cours> cours, Vector<Frais> frais, Vector<Monnaie> monnaies, Vector<Revenu> revenus, Vector<Periode> periodes) {
        if (anneeExistant != null) {
            newIannee = anneeExistant;
        }
        this.donneesExercice = new DonneesExercice(anneeExistant, agents, charges, classes, cours, frais, monnaies, revenus, periodes);
    }

    public void ga_setDonneesFromFileManager(String selectedAnnee) {
        if (fm != null) {
            boolean mustLoadData = true;
            int nbOnglets = tabOnglet.getComponentCount();
            for (int i = 0; i < nbOnglets; i++) {
                JPanel onglet = (JPanel) tabOnglet.getComponentAt(i);
                String titreOnglet = tabOnglet.getTitleAt(i);
                System.out.println("Onglet - " + titreOnglet);
                if (titreOnglet.equals(selectedAnnee)) {
                    System.out.println(" * Lannée " + titreOnglet + " est déjà ouverte, inutile de charger les données. On va juste activer l'onglet");
                    tabOnglet.setSelectedIndex(i);
                    mustLoadData = false;
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
                                newIannee = annee;
                                break;
                            }

                        }
                        loadAgents();
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

    private void loadAgents() {
        agents.removeAllElements();
        fm.fm_ouvrirTout(0, Agent.class, UtilFees.DOSSIER_AGENT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                if (newIannee != null) {
                    System.out.println(message);
                    for (Object o : data) {
                        Agent agent = (Agent) o;
                        if (agent.getIdExercice() == newIannee.getId()) {
                            agents.add(agent);
                            System.out.println(" * " + agent.toString());
                        }
                    }
                    loadCharges();
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

    private void loadCharges() {
        charges.removeAllElements();
        fm.fm_ouvrirTout(0, Charge.class, UtilFees.DOSSIER_CHARGE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Charge charge = (Charge) o;
                    if (charge.getIdExercice() == newIannee.getId()) {
                        charges.add(charge);
                        System.out.println(" * " + charge.toString());
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
                    if (classe.getIdExercice() == newIannee.getId()) {
                        classes.add(classe);
                        System.out.println(" * " + classe.toString());
                    }
                }
                loadCours();
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

    private void loadCours() {
        cours.removeAllElements();
        fm.fm_ouvrirTout(0, Cours.class, UtilFees.DOSSIER_COURS, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Cours Ocours = (Cours) o;
                    if (Ocours.getIdExercice() == newIannee.getId()) {
                        cours.add(Ocours);
                        System.out.println(" * " + Ocours.toString());
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
        fraises.removeAllElements();
        fm.fm_ouvrirTout(0, Frais.class, UtilFees.DOSSIER_FRAIS, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Frais frais = (Frais) o;
                    if (frais.getIdExercice() == newIannee.getId()) {
                        fraises.add(frais);
                        System.out.println(" * " + frais.getNom());
                        System.out.println("Liaison classe:");
                        for (LiaisonFraisClasse lc : frais.getLiaisonsClasses()) {
                            System.out.println(" ** " + lc.toString());
                        }
                        System.out.println("Liaison période:");
                        for (LiaisonFraisPeriode lp : frais.getLiaisonsPeriodes()) {
                            System.out.println(" ** " + lp.toString());
                        }
                    }
                }
                loadMonnaie();
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

    private void loadMonnaie() {
        monnaies.removeAllElements();
        fm.fm_ouvrirTout(0, Monnaie.class, UtilFees.DOSSIER_MONNAIE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Monnaie monnaie = (Monnaie) o;
                    if (monnaie.getIdExercice() == newIannee.getId()) {
                        monnaies.add(monnaie);
                        System.out.println(" * " + monnaie.toString());
                    }
                }
                loadPeriode();
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

    private void loadPeriode() {
        periodes.removeAllElements();
        fm.fm_ouvrirTout(0, Periode.class, UtilFees.DOSSIER_PERIODE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Periode periode = (Periode) o;
                    if (periode.getIdExercice() == newIannee.getId()) {
                        periodes.add(periode);
                        System.out.println(" * " + periode.toString());
                    }
                }
                loadRevenu();
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

    private void loadRevenu() {
        revenus.removeAllElements();
        fm.fm_ouvrirTout(0, Revenu.class, UtilFees.DOSSIER_REVENU, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Revenu revenu = (Revenu) o;
                    if (revenu.getIdExercice() == newIannee.getId()) {
                        revenus.add(revenu);
                        System.out.println(" * " + revenu.toString());
                    }
                }
                donneesExercice = new DonneesExercice(newIannee, agents, charges, classes, cours, fraises, monnaies, revenus, periodes);
                ga_initUI(newIannee.getNom());
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

    public void ga_initUI(String nomTab) {
        panel = new PanelExercice(couleurBasique, tabOnglet, parametreExercice, donneesExercice, new EcouteurExerice() {
            @Override
            public void onEnregistre(SortiesExercice se) {
                if (se != null) {
                    System.out.println("DANGER !!!!!! EXERCICE: Enregistrement...");
                    action_save(se);
                }
            }

            @Override
            public void onDetruitExercice(int idExercice) {
                System.out.println("DANGER !!!!!! EXERCICE: Destruction de l'Exercice " + idExercice);
                if (idExercice != -1) {
                    Exercice anneeSupp = (Exercice) fm.fm_ouvrir(Exercice.class, UtilFees.DOSSIER_ANNEE, idExercice);
                    if (anneeSupp != null) {
                        fm.fm_supprimer(UtilFees.DOSSIER_ANNEE, idExercice);
                        detruireChields();
                        if (ecouteurExercice != null) {
                            ecouteurExercice.onExerciceDeleteded(anneeSupp.getNom());
                        }
                    }

                }
            }

            @Override
            public void onDetruitElements(int idElement, int index) {
                System.out.println("DANGER !!!!!! EXERCICE: Destruction de " + idElement + ", indice " + index);
                if (idElement != -1) {
                    switch (index) {
                        case 0://PERIODE
                            fm.fm_supprimer(UtilFees.DOSSIER_PERIODE, idElement);
                            break;
                        case 1://MONNAIE
                            fm.fm_supprimer(UtilFees.DOSSIER_MONNAIE, idElement);
                            break;
                        case 2://CLASSE
                            fm.fm_supprimer(UtilFees.DOSSIER_CLASSE, idElement);
                            break;
                        case 3://FRAIS
                            fm.fm_supprimer(UtilFees.DOSSIER_FRAIS, idElement);
                            break;
                        case 4://CHARGE
                            fm.fm_supprimer(UtilFees.DOSSIER_CHARGE, idElement);
                            break;
                        case 5://REVENU
                            fm.fm_supprimer(UtilFees.DOSSIER_REVENU, idElement);
                            break;
                        case 6://AGENT
                            fm.fm_supprimer(UtilFees.DOSSIER_AGENT, idElement);
                            break;
                        case 7://COURS
                            fm.fm_supprimer(UtilFees.DOSSIER_COURS, idElement);
                            break;
                        default:
                    }
                }
            }

        });
        //Chargement du gestionnaire sur l'onglet
        tabOnglet.addTab(nomTab, panel);
        tabOnglet.setSelectedComponent(panel);
        progress.setVisible(false);
        progress.setIndeterminate(false);
    }

    private void detruireChields() {
        //Destruction des AGENTS
        donneesExercice.getAgents().forEach((iAgent) -> {
            fm.fm_supprimer(UtilFees.DOSSIER_AGENT, iAgent.getId());
        });
        //Destruction des AGENTS
        donneesExercice.getCharges().forEach((iCharge) -> {
            fm.fm_supprimer(UtilFees.DOSSIER_CHARGE, iCharge.getId());
        });
        //Destruction des CLASSES
        donneesExercice.getClasses().forEach((iClasses) -> {
            fm.fm_supprimer(UtilFees.DOSSIER_CLASSE, iClasses.getId());
        });
        //Destruction des COURS
        donneesExercice.getCours().forEach((iCours) -> {
            fm.fm_supprimer(UtilFees.DOSSIER_COURS, iCours.getId());
        });
        //Destruction des FRAIS
        donneesExercice.getFrais().forEach((iFrais) -> {
            fm.fm_supprimer(UtilFees.DOSSIER_FRAIS, iFrais.getId());
        });
        //Destruction des MONNAIES
        donneesExercice.getMonnaies().forEach((iMonnaie) -> {
            fm.fm_supprimer(UtilFees.DOSSIER_MONNAIE, iMonnaie.getId());
        });
        //Destruction des PERIODES
        donneesExercice.getPeriodes().forEach((iPeriode) -> {
            fm.fm_supprimer(UtilFees.DOSSIER_PERIODE, iPeriode.getId());
        });
        //Destruction des REVENUS
        donneesExercice.getRevenus().forEach((iRevenu) -> {
            fm.fm_supprimer(UtilFees.DOSSIER_REVENU, iRevenu.getId());
        });
    }

    private void action_save(SortiesExercice se) {
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
                        newIannee = se.getExercice();
                        System.out.println(" =>EXERCICE EXISTANT: " + newIannee.toString());
                        if (newIannee.getBeta() == InterfaceExercice.BETA_MODIFIE || newIannee.getBeta() == InterfaceExercice.BETA_NOUVEAU) {
                            saveExercice(se, ee, newIannee, user);
                        } else {
                            //On enregistre les éventuelles éléments dépendant de l'année scolaire
                            saveChildes(se, ee, user, newIannee);
                        }

                        se.getEcouteurEnregistrement().onDone("Enregistré!");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            };
            th.start();
        }
    }

    private void saveChildes(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice newIannee) {
        saveMonnaies(se, ee, user, newIannee);
    }

    private void saveExercice(SortiesExercice se, EcouteurEnregistrement ee, Exercice newIa, Utilisateur user) {
        System.out.println(" * EXERCICE: " + newIannee.toString());
        //On précise qui est en train d'enregistrer cette donnée
        newIa.setIdUtilisateur(user.getId());
        newIa.setIdEntreprise(user.getIdEntreprise());
        newIa.setBeta(InterfaceExercice.BETA_EXISTANT);
        newIannee = newIa;
        fm.fm_enregistrer(newIannee, UtilFees.DOSSIER_ANNEE, new EcouteurStandard() {
            @Override
            public void onDone(String message) {
                int index = tabOnglet.getSelectedIndex();
                if (index != -1) {
                    tabOnglet.setTitleAt(index, newIannee.getNom());
                    ecouteurExercice.onExerciceAdded(newIannee.getNom());
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone(newIannee.getNom() + " enregistrée (" + newIannee.getId() + ").");
                    donneesExercice.setExercice(newIannee);

                    //On enregistre les éventuelles éléments dépendant de l'année scolaire
                    saveChildes(se, ee, user, newIannee);
                } else {
                    progress.setVisible(false);
                    progress.setIndeterminate(false);
                }
            }

            @Override
            public void onError(String message) {
                System.err.println(message);
                ee.onError("Erreur !");
                progress.setVisible(false);
                progress.setIndeterminate(false);
            }

            @Override
            public void onProcessing(String message) {
                System.out.println(message);
                ee.onUploading("Enregistrement...");
            }
        });
    }

    private void saveMonnaies(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Monnaie> listeNewMonnaie = se.getListeMonnaies();
        Vector<Monnaie> listeNewMonnaieTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Monnaie im : listeNewMonnaie) {
            if (im.getBeta() == InterfaceMonnaie.BETA_MODIFIE || im.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                im.setIdExercice(annee.getId());
                im.setIdUtilisateur(user.getId());
                im.setIdEntreprise(user.getIdEntreprise());
                im.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewMonnaieTempo.add(im);
            }
        }
        if (!listeNewMonnaieTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewMonnaieTempo, UtilFees.DOSSIER_MONNAIE, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Monnaies enregistrées !");
                    //donneesExercice.setMonnaies(listeNewMonnaie);
                    saveClasses(se, ee, user, newIannee);
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
            saveClasses(se, ee, user, newIannee);
        }
    }

    private void saveClasses(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Classe> listeNewClasses = se.getListeClasse();
        Vector<Classe> listeNewClassesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Classe ic : listeNewClasses) {
            if (ic.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ic.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ic.setIdExercice(annee.getId());
                ic.setIdUtilisateur(user.getId());
                ic.setIdEntreprise(user.getIdEntreprise());
                ic.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewClassesTempo.add(ic);
            }
        }
        if (!listeNewClassesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewClassesTempo, UtilFees.DOSSIER_CLASSE, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Classes enregistrées !");
                    //donneesExercice.setClasses(listeNewClasses);
                    savePeriodes(se, ee, user, newIannee);
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
            savePeriodes(se, ee, user, newIannee);
        }
    }

    private void savePeriodes(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Periode> listeNewPeriodes = se.getListePeriodes();
        Vector<Periode> listeNewPeriodesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Periode ip : listeNewPeriodes) {
            if (ip.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ip.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ip.setIdExercice(annee.getId());
                ip.setIdUtilisateur(user.getId());
                ip.setIdEntreprise(user.getIdEntreprise());
                ip.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewPeriodesTempo.add(ip);
            }
        }
        if (!listeNewPeriodesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewPeriodesTempo, UtilFees.DOSSIER_PERIODE, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Périodes enregistrées !");
                    //donneesExercice.setPeriodes(listeNewPeriodes);
                    saveAgents(se, ee, user, newIannee);
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
            saveAgents(se, ee, user, newIannee);
        }
    }

    private void saveAgents(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Agent> listeNewAgents = se.getListeAgents();
        Vector<Agent> listeNewAgentsTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Agent ia : listeNewAgents) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdUtilisateur(user.getId());
                ia.setIdEntreprise(user.getIdEntreprise());
                ia.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewAgentsTempo.add(ia);
            }
        }
        if (!listeNewAgentsTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewAgentsTempo, UtilFees.DOSSIER_AGENT, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Agents enregistrées !");
                    //donneesExercice.setAgents(listeNewAgentsTempo);
                    saveCours(se, ee, user, newIannee);
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
            saveCours(se, ee, user, newIannee);
        }
    }

    private int getIdClasse(SortiesExercice se, long signature) {
        if (se != null) {
            for (Classe ic : se.getListeClasse()) {
                if (ic.getSignature() == signature) {
                    return ic.getId();
                }
            }
        }
        return -1;
    }

    private int getIdPeriode(SortiesExercice se, long signature) {
        if (se != null) {
            for (Periode ic : se.getListePeriodes()) {
                if (ic.getSignature() == signature) {
                    return ic.getId();
                }
            }
        }
        return -1;
    }

    private String getNomClasse(SortiesExercice se, long signature) {
        if (se != null) {
            for (Classe ic : se.getListeClasse()) {
                if (ic.getSignature() == signature) {
                    return ic.getNom();
                }
            }
        }
        return "";
    }

    private String getNomPeriode(SortiesExercice se, long signature) {
        if (se != null) {
            for (Periode ic : se.getListePeriodes()) {
                if (ic.getSignature() == signature) {
                    return ic.getNom();
                }
            }
        }
        return "";
    }

    private int getIdAgent(SortiesExercice se, long signature) {
        if (se != null) {
            for (Agent ia : se.getListeAgents()) {
                if (ia.getSignature() == signature) {
                    return ia.getId();
                }
            }
        }
        return -1;
    }

    private int getIdMonnaie(SortiesExercice se, long signature) {
        if (se != null) {
            for (InterfaceMonnaie ia : se.getListeMonnaies()) {
                if (ia.getSignature() == signature) {
                    return ia.getId();
                }
            }
        }
        return -1;
    }

    private void saveCours(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Cours> listeNewCours = se.getListeCours();
        Vector<Cours> listeNewCoursTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Cours ic : listeNewCours) {
            if (ic.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ic.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ic.setIdClasse(getIdClasse(se, ic.getSignatureClasse()));
                ic.setIdEnseignant(getIdAgent(se, ic.getSignatureEnseignant()));
                ic.setIdExercice(annee.getId());
                ic.setIdUtilisateur(user.getId());
                ic.setIdEntreprise(user.getIdEntreprise());
                ic.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewCoursTempo.add(ic);
            }
        }
        if (!listeNewCoursTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewCoursTempo, UtilFees.DOSSIER_COURS, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Cours enregistrés !");
                    //donneesExercice.setCours(listeNewCoursTempo);
                    saveRevenus(se, ee, user, newIannee);
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
            saveRevenus(se, ee, user, newIannee);
        }
    }

    private void saveRevenus(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Revenu> listeNewRevenus = se.getListeRevenus();
        Vector<Revenu> listeNewRevenusTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Revenu ir : listeNewRevenus) {
            if (ir.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ir.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ir.setIdMonnaie(getIdMonnaie(se, ir.getSignatureMonnaie()));
                ir.setIdExercice(annee.getId());
                ir.setIdUtilisateur(user.getId());
                ir.setIdEntreprise(user.getIdEntreprise());
                ir.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewRevenusTempo.add(ir);
            }
        }
        if (!listeNewRevenusTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewRevenusTempo, UtilFees.DOSSIER_REVENU, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Revenus enregistrés !");
                    //donneesExercice.setRevenus(listeNewRevenusTempo);
                    saveCharges(se, ee, user, newIannee);
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
            saveCharges(se, ee, user, newIannee);
        }
    }

    private void saveCharges(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Charge> listeNewCharges = se.getListeCharges();
        Vector<Charge> listeNewChargesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Charge ic : listeNewCharges) {
            if (ic.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ic.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ic.setIdMonnaie(getIdMonnaie(se, ic.getSignatureMonnaie()));
                ic.setIdExercice(annee.getId());
                ic.setIdUtilisateur(user.getId());
                ic.setIdEntreprise(user.getIdEntreprise());
                ic.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewChargesTempo.add(ic);
            }
        }
        if (!listeNewChargesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewChargesTempo, UtilFees.DOSSIER_CHARGE, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Charges enregistrées !");
                    //donneesExercice.setCharges(listeNewChargesTempo);
                    saveFrais(se, ee, user, newIannee);
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
            saveFrais(se, ee, user, newIannee);
        }
    }

    private void saveFrais(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Frais> listeNewFrais = se.getListeFrais();
        Vector<Frais> listeNewFraisTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Frais frais : listeNewFrais) {
            if (frais.getBeta() == InterfaceFrais.BETA_MODIFIE || frais.getBeta() == InterfaceFrais.BETA_NOUVEAU) {
                frais.setIdMonnaie(getIdMonnaie(se, frais.getSignatureMonnaie()));
                frais.setIdExercice(annee.getId());
                frais.setIdUtilisateur(user.getId());
                frais.setIdEntreprise(user.getIdEntreprise());

                System.out.println("FRAIS: " + frais.toString());
                System.out.println(" * LIAISON CLASSE:");
                /* */
                for (LiaisonFraisClasse lcf : frais.getLiaisonsClasses()) {
                    lcf.setIdClasse(getIdClasse(se, lcf.getSignatureClasse()));
                    lcf.setNomClasse(getNomClasse(se, lcf.getSignatureClasse()));
                    System.out.println(" * * " + lcf.toString());
                }
                System.out.println("-----------");
                System.out.println(" * LIAISON PERIODE:");
                for (LiaisonFraisPeriode lcp : frais.getLiaisonsPeriodes()) {
                    lcp.setIdPeriode(getIdPeriode(se, lcp.getSignaturePeriode()));
                    lcp.setNomPeriode(getNomPeriode(se, lcp.getSignaturePeriode()));
                    System.out.println(" * * " + lcp.toString());
                }

                System.out.println("-----");
                frais.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewFraisTempo.add(frais);
            }
        }
        if (!listeNewFraisTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewFraisTempo, UtilFees.DOSSIER_FRAIS, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Enregistrés avec succès!");
                    //donneesExercice.setFrais(listeNewFraisTempo);
                    progress.setVisible(false);
                    progress.setIndeterminate(false);
                }

                @Override
                public void onError(String message) {
                    System.err.println(message);
                    ee.onError("Erreur !");

                    progress.setVisible(false);
                    progress.setIndeterminate(false);
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
