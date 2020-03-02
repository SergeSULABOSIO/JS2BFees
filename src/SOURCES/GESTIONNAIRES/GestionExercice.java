/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import ICONES.Icones;
import SOURCES.CALLBACK.EcouteurGestionExercice;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback_Exercice.EcouteurExerice;
import SOURCES.Objets.FileManager;
import SOURCES.UI_Exercice.PanelExercice;
import SOURCES.Utilitaires_Exercice.DonneesExercice;
import SOURCES.Utilitaires_Exercice.ParametreExercice;
import SOURCES.Utilitaires_Exercice.SortiesExercice;
import Source.Callbacks.EcouteurCrossCanalAgent;
import Source.Callbacks.EcouteurCrossCanalCharge;
import Source.Callbacks.EcouteurCrossCanalRevenu;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Callbacks.EcouteurFreemium;
import Source.Callbacks.EcouteurStandard;
import Source.Interface.InterfaceFrais;
import Source.Interface.InterfaceMonnaie;
import Source.Objet.Agent;
import Source.Objet.Charge;
import Source.Objet.Classe;
import Source.Objet.CouleurBasique;
import Source.Objet.Cours;
import Source.Objet.Entreprise;
import Source.Objet.Annee;
import Source.Objet.Frais;
import Source.Objet.LiaisonFraisClasse;
import Source.Objet.LiaisonFraisPeriode;
import Source.Objet.Monnaie;
import Source.Objet.Periode;
import Source.Objet.Revenu;
import Source.Objet.UtilObjet;
import Source.Objet.Utilisateur;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import Source.Interface.InterfaceAnnee;
import Source.Objet.Ayantdroit;
import Source.Objet.Decaissement;
import Source.Objet.Eleve;
import Source.Objet.Encaissement;
import Source.Objet.Fiche_paie;
import Source.Objet.Paiement;
import javax.swing.JOptionPane;

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

    private Annee newIannee = null;
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
    public JFrame fenetre;
    public Icones icones;
    public EcouteurFreemium ef = null;
    public static String NOM = "ANNEE";
    public boolean canDelete = false;

    public GestionExercice(EcouteurFreemium ef, JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Monnaie monnaie_output, EcouteurGestionExercice ecouteurExercice) {
        this.ef = ef;
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.fenetre = fenetre;
        this.icones = icones;
        this.ecouteurExercice = ecouteurExercice;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.monnaie_output = monnaie_output;
        initParams();
    }

    private void initParams() {
        this.parametreExercice = new ParametreExercice(entreprise, utilisateur, monnaie_output);
    }

    public void ga_setDonnees(Annee anneeExistant, Vector<Agent> agents, Vector<Charge> charges, Vector<Classe> classes, Vector<Cours> cours, Vector<Frais> frais, Vector<Monnaie> monnaies, Vector<Revenu> revenus, Vector<Periode> periodes) {
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
                //JPanel onglet = (JPanel) tabOnglet.getComponentAt(i);
                if (tabOnglet.getComponentCount() > i) {
                    String titreOnglet = tabOnglet.getTitleAt(i);
                    System.out.println("Onglet - " + titreOnglet);
                    if (titreOnglet.equals(selectedAnnee)) {
                        System.out.println(" * Lannée " + titreOnglet + " est déjà ouverte, inutile de charger les données. On va juste activer l'onglet");
                        tabOnglet.setSelectedIndex(i);
                        mustLoadData = false;
                    }
                }
            }

            if (mustLoadData == true) {
                fm.fm_ouvrirTout(0, Annee.class, UtilObjet.DOSSIER_ANNEE, 1, 1000, new EcouteurOuverture() {
                    @Override
                    public boolean isCriteresRespectes(Object object) {
                        Annee annee = (Annee) object;
                        if (annee.getNom().equals(selectedAnnee)) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void onElementLoaded(String message, Object data) {
                        Annee annee = (Annee) data;
                        newIannee = annee;
                    }

                    @Override
                    public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
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
        fm.fm_ouvrirTout(0, Agent.class, UtilObjet.DOSSIER_AGENT, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Agent agent = (Agent) object;
                if (agent.getIdExercice() == newIannee.getId()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Agent agent = (Agent) data;
                agents.add(agent);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                loadCharges();
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
        fm.fm_ouvrirTout(0, Charge.class, UtilObjet.DOSSIER_CHARGE, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Charge charge = (Charge) object;
                return charge.getIdExercice() == newIannee.getId();
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Charge charge = (Charge) data;
                charges.add(charge);
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

    private void loadClasses() {
        classes.removeAllElements();
        fm.fm_ouvrirTout(0, Classe.class, UtilObjet.DOSSIER_CLASSE, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Classe classe = (Classe) object;
                return (classe.getIdExercice() == newIannee.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Classe classe = (Classe) data;
                classes.add(classe);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
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
        fm.fm_ouvrirTout(0, Cours.class, UtilObjet.DOSSIER_COURS, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Cours Ocours = (Cours) object;
                return (Ocours.getIdExercice() == newIannee.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Cours Ocours = (Cours) data;
                cours.add(Ocours);
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
        fraises.removeAllElements();
        fm.fm_ouvrirTout(0, Frais.class, UtilObjet.DOSSIER_FRAIS, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Frais frais = (Frais) object;
                return (frais.getIdExercice() == newIannee.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Frais frais = (Frais) data;
                fraises.add(frais);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
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
        fm.fm_ouvrirTout(0, Monnaie.class, UtilObjet.DOSSIER_MONNAIE, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Monnaie monnaie = (Monnaie) object;
                return (monnaie.getIdExercice() == newIannee.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Monnaie monnaie = (Monnaie) data;
                monnaies.add(monnaie);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
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
        fm.fm_ouvrirTout(0, Periode.class, UtilObjet.DOSSIER_PERIODE, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Periode periode = (Periode) object;
                return (periode.getIdExercice() == newIannee.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Periode periode = (Periode) data;
                periodes.add(periode);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
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
        fm.fm_ouvrirTout(0, Revenu.class, UtilObjet.DOSSIER_REVENU, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Revenu revenu = (Revenu) object;
                return (revenu.getIdExercice() == newIannee.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Revenu revenu = (Revenu) data;
                revenus.add(revenu);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
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

    private void detruireChields(int idExercice) {
        //Destruction des AGENTS
        donneesExercice.getAgents().forEach((iAgent) -> {
            fm.fm_supprimer(UtilObjet.DOSSIER_AGENT, iAgent.getId(), iAgent.getSignature());
        });
        //Destruction des CHARGES
        donneesExercice.getCharges().forEach((iCharge) -> {
            fm.fm_supprimer(UtilObjet.DOSSIER_CHARGE, iCharge.getId(), iCharge.getSignature());
        });
        //Destruction des CLASSES
        donneesExercice.getClasses().forEach((iClasses) -> {
            fm.fm_supprimer(UtilObjet.DOSSIER_CLASSE, iClasses.getId(), iClasses.getSignature());
        });
        //Destruction des COURS
        donneesExercice.getCours().forEach((iCours) -> {
            fm.fm_supprimer(UtilObjet.DOSSIER_COURS, iCours.getId(), iCours.getSignature());
        });
        //Destruction des FRAIS
        donneesExercice.getFrais().forEach((iFrais) -> {
            fm.fm_supprimer(UtilObjet.DOSSIER_FRAIS, iFrais.getId(), iFrais.getSignature());
        });
        //Destruction des MONNAIES
        donneesExercice.getMonnaies().forEach((iMonnaie) -> {
            fm.fm_supprimer(UtilObjet.DOSSIER_MONNAIE, iMonnaie.getId(), iMonnaie.getSignature());
        });
        //Destruction des PERIODES
        donneesExercice.getPeriodes().forEach((iPeriode) -> {
            fm.fm_supprimer(UtilObjet.DOSSIER_PERIODE, iPeriode.getId(), iPeriode.getSignature());
        });
        //Destruction des REVENUS
        donneesExercice.getRevenus().forEach((iRevenu) -> {
            fm.fm_supprimer(UtilObjet.DOSSIER_REVENU, iRevenu.getId(), iRevenu.getSignature());
        });
    }

    public void deleteAyantDroits(int idExercice) {
        fm.fm_ouvrirTout(0, Ayantdroit.class, UtilObjet.DOSSIER_AYANT_DROIT, 1, 1000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Ayantdroit aya = (Ayantdroit) object;
                return aya.getIdExercice() == idExercice;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Ayantdroit aya = (Ayantdroit) data;
                boolean rep = fm.fm_supprimer(UtilObjet.DOSSIER_AYANT_DROIT, aya.getId(), aya.getSignature());
                System.out.println("SUPRESSION = " + rep);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                deleteDecaissement(idExercice);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    public void deleteDecaissement(int idExercice) {
        fm.fm_ouvrirTout(0, Decaissement.class, UtilObjet.DOSSIER_DECAISSEMENT, 1, 1000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Decaissement aya = (Decaissement) object;
                return aya.getIdExercice() == idExercice;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Decaissement aya = (Decaissement) data;
                boolean rep = fm.fm_supprimer(UtilObjet.DOSSIER_DECAISSEMENT, aya.getId(), aya.getSignature());
                System.out.println("SUPRESSION = " + rep);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                deleteEleve(idExercice);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    public void deleteEleve(int idExercice) {
        fm.fm_ouvrirTout(0, Eleve.class, UtilObjet.DOSSIER_ELEVE, 1, 1000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Eleve aya = (Eleve) object;
                return aya.getIdExercice() == idExercice;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Eleve aya = (Eleve) data;
                boolean rep = fm.fm_supprimer(UtilObjet.DOSSIER_ELEVE, aya.getId(), aya.getSignature());
                System.out.println("SUPRESSION = " + rep);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                deleteEncaissement(idExercice);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    public void deleteEncaissement(int idExercice) {
        fm.fm_ouvrirTout(0, Encaissement.class, UtilObjet.DOSSIER_ENCAISSEMENT, 1, 1000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Encaissement aya = (Encaissement) object;
                return aya.getIdExercice() == idExercice;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Encaissement aya = (Encaissement) data;
                boolean rep = fm.fm_supprimer(UtilObjet.DOSSIER_ENCAISSEMENT, aya.getId(), aya.getSignature());
                System.out.println("SUPRESSION = " + rep);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                deletePaiement(idExercice);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    public void deletePaiement(int idExercice) {
        fm.fm_ouvrirTout(0, Paiement.class, UtilObjet.DOSSIER_PAIEMENT, 1, 1000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Paiement aya = (Paiement) object;
                return aya.getIdExercice() == idExercice;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Paiement aya = (Paiement) data;
                boolean rep = fm.fm_supprimer(UtilObjet.DOSSIER_PAIEMENT, aya.getId(), aya.getSignature());
                System.out.println("SUPRESSION = " + rep);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                detruireChields(idExercice);
                if (ecouteurExercice != null) {
                    ecouteurExercice.onSynchronise();
                    ecouteurExercice.onExerciceDeleteded(anneeSupp.getNom());
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
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
                        if (newIannee.getBeta() == InterfaceAnnee.BETA_MODIFIE || newIannee.getBeta() == InterfaceAnnee.BETA_NOUVEAU) {
                            saveExercice(se, ee, newIannee, user);
                        } else {
                            //On enregistre les éventuelles éléments dépendant de l'année scolaire
                            saveChildes(se, ee, user, newIannee);
                        }

                        se.getEcouteurEnregistrement().onDone("Enregistré!");

                        if (ecouteurExercice != null) {
                            ecouteurExercice.onSynchronise();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            };
            th.start();
        }
    }

    private void saveChildes(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee newIannee) {
        saveMonnaies(se, ee, user, newIannee);
    }

    private void saveExercice(SortiesExercice se, EcouteurEnregistrement ee, Annee newIa, Utilisateur user) {
        System.out.println(" * EXERCICE: " + newIannee.toString());
        //On précise qui est en train d'enregistrer cette donnée
        newIa.setIdUtilisateur(user.getId());
        newIa.setIdEntreprise(user.getIdEntreprise());
        newIa.setBeta(InterfaceAnnee.BETA_EXISTANT);
        newIannee = newIa;
        fm.fm_enregistrer(newIannee, UtilObjet.DOSSIER_ANNEE, new EcouteurStandard() {
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

    private void saveMonnaies(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Monnaie> listeNewMonnaie = se.getListeMonnaies();
        Vector<Monnaie> listeNewMonnaieTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Monnaie im : listeNewMonnaie) {
            if (im.getBeta() == InterfaceMonnaie.BETA_MODIFIE || im.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                im.setIdExercice(annee.getId());
                im.setIdUtilisateur(user.getId());
                im.setIdEntreprise(user.getIdEntreprise());
                im.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewMonnaieTempo.add(im);
            }
        }
        if (!listeNewMonnaieTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewMonnaieTempo, UtilObjet.DOSSIER_MONNAIE, new EcouteurStandard() {
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

    private void saveClasses(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Classe> listeNewClasses = se.getListeClasse();
        Vector<Classe> listeNewClassesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Classe ic : listeNewClasses) {
            if (ic.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ic.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ic.setIdExercice(annee.getId());
                ic.setIdUtilisateur(user.getId());
                ic.setIdEntreprise(user.getIdEntreprise());
                ic.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewClassesTempo.add(ic);
            }
        }
        if (!listeNewClassesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewClassesTempo, UtilObjet.DOSSIER_CLASSE, new EcouteurStandard() {
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

    private void savePeriodes(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Periode> listeNewPeriodes = se.getListePeriodes();
        Vector<Periode> listeNewPeriodesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Periode ip : listeNewPeriodes) {
            if (ip.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ip.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ip.setIdExercice(annee.getId());
                ip.setIdUtilisateur(user.getId());
                ip.setIdEntreprise(user.getIdEntreprise());
                ip.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewPeriodesTempo.add(ip);
            }
        }
        if (!listeNewPeriodesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewPeriodesTempo, UtilObjet.DOSSIER_PERIODE, new EcouteurStandard() {
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

    private void saveAgents(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Agent> listeNewAgents = se.getListeAgents();
        Vector<Agent> listeNewAgentsTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Agent ia : listeNewAgents) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdUtilisateur(user.getId());
                ia.setIdEntreprise(user.getIdEntreprise());
                ia.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewAgentsTempo.add(ia);
            }
        }
        if (!listeNewAgentsTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewAgentsTempo, UtilObjet.DOSSIER_AGENT, new EcouteurStandard() {
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

    private void saveCours(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
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
                ic.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewCoursTempo.add(ic);
            }
        }
        if (!listeNewCoursTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewCoursTempo, UtilObjet.DOSSIER_COURS, new EcouteurStandard() {
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

    private void saveRevenus(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Revenu> listeNewRevenus = se.getListeRevenus();
        Vector<Revenu> listeNewRevenusTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Revenu ir : listeNewRevenus) {
            if (ir.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ir.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ir.setIdMonnaie(getIdMonnaie(se, ir.getSignatureMonnaie()));
                ir.setIdExercice(annee.getId());
                ir.setIdUtilisateur(user.getId());
                ir.setIdEntreprise(user.getIdEntreprise());
                ir.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewRevenusTempo.add(ir);
            }
        }
        if (!listeNewRevenusTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewRevenusTempo, UtilObjet.DOSSIER_REVENU, new EcouteurStandard() {
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

    private void saveCharges(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Charge> listeNewCharges = se.getListeCharges();
        Vector<Charge> listeNewChargesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Charge ic : listeNewCharges) {
            if (ic.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ic.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ic.setIdMonnaie(getIdMonnaie(se, ic.getSignatureMonnaie()));
                ic.setIdExercice(annee.getId());
                ic.setIdUtilisateur(user.getId());
                ic.setIdEntreprise(user.getIdEntreprise());
                ic.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewChargesTempo.add(ic);
            }
        }
        if (!listeNewChargesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewChargesTempo, UtilObjet.DOSSIER_CHARGE, new EcouteurStandard() {
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

    private void saveFrais(SortiesExercice se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
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
                frais.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewFraisTempo.add(frais);
            }
        }
        if (!listeNewFraisTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewFraisTempo, UtilObjet.DOSSIER_FRAIS, new EcouteurStandard() {
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

    boolean rep = false;
    Annee anneeSupp = null;

    public void ga_initUI(String nomTab) {
        panel = new PanelExercice(ef, couleurBasique, tabOnglet, parametreExercice, donneesExercice, new EcouteurExerice() {
            @Override
            public void onEnregistre(SortiesExercice se) {
                if (se != null) {
                    System.out.println("DANGER !!!!!! EXERCICE: Enregistrement...");
                    action_save(se);
                }
            }

            @Override
            public void onDetruitExercice(int idExercice, long signature) {
                System.out.println("DANGER !!!!!! EXERCICE: Destruction de l'Exercice " + idExercice);
                if (idExercice != -1) {
                    anneeSupp = (Annee) fm.fm_ouvrir(Annee.class, UtilObjet.DOSSIER_ANNEE, idExercice);
                    if (anneeSupp != null) {
                        fm.fm_supprimer(UtilObjet.DOSSIER_ANNEE, idExercice, anneeSupp.getSignature());

                        //Destruction des AYANT DROIT
                        deleteAyantDroits(idExercice);
                    }
                }
            }

            @Override
            public void onDetruitElements(int idElement, int index, long signature) {
                System.out.println("DANGER !!!!!! EXERCICE: Destruction de " + idElement + ", indice " + index);
                if (idElement != -1) {
                    switch (index) {
                        case 0://PERIODE
                            fm.fm_supprimer(UtilObjet.DOSSIER_PERIODE, idElement, signature);
                            break;
                        case 1://MONNAIE
                            fm.fm_supprimer(UtilObjet.DOSSIER_MONNAIE, idElement, signature);
                            break;
                        case 2://CLASSE
                            fm.fm_supprimer(UtilObjet.DOSSIER_CLASSE, idElement, signature);
                            break;
                        case 3://FRAIS
                            fm.fm_supprimer(UtilObjet.DOSSIER_FRAIS, idElement, signature);
                            break;
                        case 4://CHARGE
                            fm.fm_supprimer(UtilObjet.DOSSIER_CHARGE, idElement, signature);
                            break;
                        case 5://REVENU
                            fm.fm_supprimer(UtilObjet.DOSSIER_REVENU, idElement, signature);
                            break;
                        case 6://AGENT
                            fm.fm_supprimer(UtilObjet.DOSSIER_AGENT, idElement, signature);
                            break;
                        case 7://COURS
                            fm.fm_supprimer(UtilObjet.DOSSIER_COURS, idElement, signature);
                            break;
                        default:
                    }
                    //On lance la syncrhonisation avec le serveur
                    if (ecouteurExercice != null) {
                        ecouteurExercice.onSynchronise();
                    }
                }
            }

            @Override
            public void onClose() {
                if (ecouteurExercice != null) {
                    ecouteurExercice.onClosed();
                }
            }

            @Override
            public boolean onCanDelete(int idElement, int index, long signature) {
                System.out.println("Vérification avant suppression de " + idElement + ", indice " + index);
                switch (index) {
                    case 0://PERIODE
                        isThisPeriodeUsedInPaiement(idElement);
                        break;
                    case 1://MONNAIE
                        isThisMonnaieUsedInPaiement(idElement);
                        break;
                    case 2://CLASSE
                        isThisClasseUsedInInscription(idElement);
                        break;
                    case 3://FRAIS
                        isThisFraisUsedInPaiement(idElement);
                        break;
                    case 4://CHARGE
                        isThisChargeUsedInDecaissement(idElement);
                        break;
                    case 5://REVENU
                        isThisRevenuUsedInEncaissement(idElement);
                        break;
                    case 6://AGENT
                        isThisAgentUsedInPaie(idElement);
                        break;
                    case 7://COURS
                        canDelete = true;
                        break;
                }
                return canDelete;
            }

        });
        //Chargement du gestionnaire sur l'onglet

        panel.setEcouteurCrossCanalAgent(new EcouteurCrossCanalAgent() {
            @Override
            public void onOuvrirFicheDePaie(Agent agent) {
                new Thread() {
                    public void run() {
                        //On ouvre les fiches de paie de l'agent séléctioné
                        //System.out.println("");
                        //System.out.println("Agent : " + agent);
                        GestionSalaire gestionSalaire = new GestionSalaire(null, ef, fenetre, icones, couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, agent);
                        gestionSalaire.gp_setDonneesFromFileManager(newIannee.getNom(), true);
                    }
                }.start();
            }
        });

        panel.setEcouteurCrossCanalRevenu(new EcouteurCrossCanalRevenu() {
            @Override
            public void onOuvrirRevenu(Revenu revenu) {
                new Thread() {
                    public void run() {
                        GestionTresorerie gestionTresorerie = new GestionTresorerie(null, ef, fenetre, icones, couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, revenu);
                        gestionTresorerie.gt_setDonneesFromFileManager(newIannee.getNom(), true);
                    }
                }.start();
            }
        });

        panel.setEcouteurCrossCanalCharge(new EcouteurCrossCanalCharge() {
            @Override
            public void onOuvrirCharge(Charge charge) {
                new Thread() {
                    public void run() {
                        GestionTresorerie gestionTresorerie = new GestionTresorerie(null, ef, fenetre, icones, couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, charge);
                        gestionTresorerie.gt_setDonneesFromFileManager(newIannee.getNom(), true);
                    }
                }.start();
            }
        });

        tabOnglet.addTab(NOM + " - " + nomTab, panel);
        tabOnglet.setSelectedComponent(panel);
        progress.setVisible(false);
        progress.setIndeterminate(false);
    }

    private void isThisPeriodeUsedInPaiement(int idElement) {
        canDelete = false;
        //On doit s'assurer que cette période n'est pas encore utilisée dans les frais payés
        fm.fm_ouvrirTout(0, Paiement.class, UtilObjet.DOSSIER_PAIEMENT, 1, 100000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Paiement pp = (Paiement) object;
                return (pp.getIdPeriode() == idElement);
            }

            @Override
            public void onElementLoaded(String message, Object data) {

            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                System.out.println("IsEmpty = " + resultatTotalObjets.isEmpty());
                canDelete = (resultatTotalObjets.isEmpty());
                if (canDelete == false) {
                    JOptionPane.showMessageDialog(panel, "Impossible de supprimer cette infos car elle actuellement indispensable pour d'autres renregistrements.", "Impossible!", JOptionPane.ERROR_MESSAGE, icones.getAlert_03());
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    private void isThisFraisUsedInPaiement(int idElement) {
        canDelete = false;
        //On doit s'assurer que cette période n'est pas encore utilisée dans les frais payés
        fm.fm_ouvrirTout(0, Paiement.class, UtilObjet.DOSSIER_PAIEMENT, 1, 100000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Paiement pp = (Paiement) object;
                return (pp.getIdFrais() == idElement);
            }

            @Override
            public void onElementLoaded(String message, Object data) {

            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                System.out.println("IsEmpty = " + resultatTotalObjets.isEmpty());
                canDelete = (resultatTotalObjets.isEmpty());
                if (canDelete == false) {
                    JOptionPane.showMessageDialog(panel, "Impossible de supprimer cette infos car elle actuellement indispensable pour d'autres renregistrements.", "Impossible!", JOptionPane.ERROR_MESSAGE, icones.getAlert_03());
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    private void isThisChargeUsedInDecaissement(int idElement) {
        canDelete = false;
        //On doit s'assurer que cette période n'est pas encore utilisée dans les frais payés
        fm.fm_ouvrirTout(0, Decaissement.class, UtilObjet.DOSSIER_DECAISSEMENT, 1, 100000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Decaissement pp = (Decaissement) object;
                return (pp.getIdCharge() == idElement);
            }

            @Override
            public void onElementLoaded(String message, Object data) {

            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                System.out.println("IsEmpty = " + resultatTotalObjets.isEmpty());
                canDelete = (resultatTotalObjets.isEmpty());
                if (canDelete == false) {
                    JOptionPane.showMessageDialog(panel, "Impossible de supprimer cette infos car elle actuellement indispensable pour d'autres renregistrements.", "Impossible!", JOptionPane.ERROR_MESSAGE, icones.getAlert_03());
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    private void isThisMonnaieUsedInPaiement(int idElement) {
        canDelete = false;
        //On doit s'assurer que cette période n'est pas encore utilisée dans les frais payés
        fm.fm_ouvrirTout(0, Paiement.class, UtilObjet.DOSSIER_PAIEMENT, 1, 100000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Paiement pp = (Paiement) object;
                boolean rep = false;
                if (pp.getIdFrais() != -1) {
                    Frais ff = (Frais) fm.fm_ouvrir(Frais.class, UtilObjet.DOSSIER_FRAIS, pp.getIdFrais());
                    rep = (ff.getIdMonnaie() == idElement);
                }
                return rep;
            }

            @Override
            public void onElementLoaded(String message, Object data) {

            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                System.out.println("IsEmpty = " + resultatTotalObjets.isEmpty());
                canDelete = (resultatTotalObjets.isEmpty());
                if (canDelete == false) {
                    JOptionPane.showMessageDialog(panel, "Impossible de supprimer cette infos car elle actuellement indispensable pour d'autres renregistrements.", "Impossible!", JOptionPane.ERROR_MESSAGE, icones.getAlert_03());
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    private void isThisRevenuUsedInEncaissement(int idElement) {
        canDelete = false;
        //On doit s'assurer que cette période n'est pas encore utilisée dans les frais payés
        fm.fm_ouvrirTout(0, Encaissement.class, UtilObjet.DOSSIER_ENCAISSEMENT, 1, 100000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Encaissement pp = (Encaissement) object;
                return (pp.getIdRevenu() == idElement);
            }

            @Override
            public void onElementLoaded(String message, Object data) {

            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                System.out.println("IsEmpty = " + resultatTotalObjets.isEmpty());
                canDelete = (resultatTotalObjets.isEmpty());
                if (canDelete == false) {
                    JOptionPane.showMessageDialog(panel, "Impossible de supprimer cette infos car elle actuellement indispensable pour d'autres renregistrements.", "Impossible!", JOptionPane.ERROR_MESSAGE, icones.getAlert_03());
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    private void isThisAgentUsedInPaie(int idElement) {
        canDelete = false;
        //On doit s'assurer que cette période n'est pas encore utilisée dans les frais payés
        fm.fm_ouvrirTout(0, Fiche_paie.class, UtilObjet.DOSSIER_FICHE_DE_PAIE, 1, 100000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Fiche_paie pp = (Fiche_paie) object;
                return (pp.getIdAgent() == idElement);
            }

            @Override
            public void onElementLoaded(String message, Object data) {

            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                System.out.println("IsEmpty = " + resultatTotalObjets.isEmpty());
                canDelete = (resultatTotalObjets.isEmpty());
                if (canDelete == false) {
                    JOptionPane.showMessageDialog(panel, "Impossible de supprimer cette infos car elle actuellement indispensable pour d'autres renregistrements.", "Impossible!", JOptionPane.ERROR_MESSAGE, icones.getAlert_03());
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }

    private void isThisClasseUsedInInscription(int idElement) {
        canDelete = false;
        //On doit s'assurer que cette période n'est pas encore utilisée dans les frais payés
        fm.fm_ouvrirTout(0, Eleve.class, UtilObjet.DOSSIER_ELEVE, 1, 100000000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Eleve pp = (Eleve) object;
                return (pp.getIdClasse() == idElement);
            }

            @Override
            public void onElementLoaded(String message, Object data) {

            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                System.out.println("IsEmpty = " + resultatTotalObjets.isEmpty());
                canDelete = (resultatTotalObjets.isEmpty());
                if (canDelete == false) {
                    JOptionPane.showMessageDialog(panel, "Impossible de supprimer cette infos car elle actuellement indispensable pour d'autres renregistrements.", "Impossible!", JOptionPane.ERROR_MESSAGE, icones.getAlert_03());
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProcessing(String message) {

            }
        });
    }
}
