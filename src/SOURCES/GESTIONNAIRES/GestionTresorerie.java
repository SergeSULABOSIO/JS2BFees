/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import ICONES.Icones;
import SOURCES.CALLBACK.EcouteurGestionTresorerie;
import SOURCES.CallBack_Tresorerie.EcouteurTresorerie;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback.EcouteurParametreDecaissement;
import SOURCES.Callback.EcouteurParametreEncaissement;
import SOURCES.Objets.FileManager;
import SOURCES.UI_Tresorerie.PanelTresorerie;
import SOURCES.Utilitaires_Tresorerie.DataTresorerie;
import SOURCES.Utilitaires_Tresorerie.ParametreTresorerie;
import SOURCES.Utilitaires_Tresorerie.SortiesTresorerie;
import SOURCES.Utilitaires_Tresorerie.UtilTresorerie;
import Source.Callbacks.ConstructeurCriteres;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Callbacks.EcouteurFreemium;
import Source.Callbacks.EcouteurNavigateurPages;
import Source.Callbacks.EcouteurStandard;
import Source.Interface.InterfaceDecaissement;
import Source.Interface.InterfaceEncaissement;
import Source.Objet.Agent;
import Source.Objet.Charge;
import Source.Objet.CouleurBasique;
import Source.Objet.Decaissement;
import Source.Objet.Encaissement;
import Source.Objet.Entreprise;
import Source.Objet.Annee;
import Source.Objet.Frais;
import Source.Objet.Monnaie;
import Source.Objet.Revenu;
import Source.Objet.UtilObjet;
import Source.Objet.Utilisateur;
import Source.UI.NavigateurPages;
import Sources.CHAMP_LOCAL;
import Sources.PROPRIETE;
import Sources.UI.JS2BPanelPropriete;
import java.util.Date;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

/**
 *
 * @author HP Pavilion
 */
public class GestionTresorerie {

    public static String NOM = "TRESORERIE";
    public PanelTresorerie panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;

    public Annee exercice = null;
    public FileManager fm;

    //les paramètres
    public Vector<Monnaie> monnaies = new Vector<>();
    public Vector<Revenu> revenus = new Vector<>();
    public Vector<Charge> charges = new Vector<>();
    public Vector<Frais> frais = new Vector<>();
    public Vector<Agent> agents = new Vector<>();

    public Revenu revenueConcerned = null;
    public Charge chargeConcerned = null;

    public CouleurBasique couleurBasique;
    public String selectedAnnee;
    public boolean deleteCurrentTab = true;
    public JFrame fenetre;
    public Icones icones;
    public boolean canBeSaved = false;
    public EcouteurFreemium ef = null;
    public EcouteurGestionTresorerie et = null;

    public GestionTresorerie(EcouteurGestionTresorerie et, EcouteurFreemium ef, JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.ef = ef;
        this.et = et;
        this.couleurBasique = couleurBasique;
        this.fenetre = fenetre;
        this.icones = icones;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.revenueConcerned = null;
        this.chargeConcerned = null;
    }

    public GestionTresorerie(EcouteurGestionTresorerie et, EcouteurFreemium ef, JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Revenu revenueConcerned) {
        this.ef = ef;
        this.et = et;
        this.couleurBasique = couleurBasique;
        this.fenetre = fenetre;
        this.icones = icones;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.revenueConcerned = revenueConcerned;
        this.chargeConcerned = null;
    }

    public GestionTresorerie(EcouteurGestionTresorerie et, EcouteurFreemium ef, JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Charge chargeConcerned) {
        this.ef = ef;
        this.et = et;
        this.couleurBasique = couleurBasique;
        this.fenetre = fenetre;
        this.icones = icones;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.revenueConcerned = null;
        this.chargeConcerned = chargeConcerned;
    }

    private DataTresorerie getData() {
        ParametreTresorerie parametreTresorerie = new ParametreTresorerie(entreprise, exercice, utilisateur, monnaies, revenus, charges);
        return new DataTresorerie(parametreTresorerie);
    }

    public void gt_setDonneesFromFileManager(String selectedAnnee, boolean deleteCurrentTab) {
        this.deleteCurrentTab = deleteCurrentTab;
        this.selectedAnnee = selectedAnnee;
        if (fm != null) {
            boolean mustLoadData = true;
            if (deleteCurrentTab == true) {
                int nbOnglets = tabOnglet.getComponentCount();
                if (nbOnglets != 0) {
                    for (int i = 0; i < nbOnglets; i++) {
                        if (tabOnglet.getComponentCount() > i) {
                            if (i < nbOnglets) {
                                String titreOnglet = tabOnglet.getTitleAt(i);
                                String Snom = NOM;
                                if (titreOnglet.equals(Snom)) {
                                    tabOnglet.remove(i);
                                    mustLoadData = true;
                                }
                            }
                        }

                    }
                }
            }

            if (mustLoadData == true) {
                fm.fm_ouvrirTout(0, Annee.class, UtilObjet.DOSSIER_ANNEE, 1, 1000, new EcouteurOuverture() {
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

    private void loadMonnaies() {
        monnaies.removeAllElements();
        fm.fm_ouvrirTout(0, Monnaie.class, UtilObjet.DOSSIER_MONNAIE, 1, 1000, new EcouteurOuverture() {
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
                loadRevenus();
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

    private void loadRevenus() {
        revenus.removeAllElements();
        fm.fm_ouvrirTout(0, Revenu.class, UtilObjet.DOSSIER_REVENU, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Revenu revenu = (Revenu) object;
                return (revenu.getIdExercice() == exercice.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Revenu revenu = (Revenu) data;
                revenus.add(revenu);
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

    private void loadFrais() {
        frais.removeAllElements();
        fm.fm_ouvrirTout(0, Frais.class, UtilObjet.DOSSIER_FRAIS, 1, 1000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Frais frais = (Frais) object;
                return (frais.getIdExercice() == exercice.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Frais frs = (Frais) data;
                frais.add(frs);
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

    private void loadAgents() {
        agents.removeAllElements();
        fm.fm_ouvrirTout(0, Agent.class, UtilObjet.DOSSIER_AGENT, 1, 10000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Agent ff = (Agent) object;
                return (ff.getIdExercice() == exercice.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Agent ff = (Agent) data;
                agents.add(ff);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                if (revenueConcerned == null && chargeConcerned == null) {
                    initUI(NOM);
                } else if (revenueConcerned != null && chargeConcerned == null) {
                    initUI(NOM + " - " + revenueConcerned.getNom());
                } else if (revenueConcerned == null && chargeConcerned != null) {
                    initUI(NOM + " - " + chargeConcerned.getNom());
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
        fm.fm_ouvrirTout(0, Charge.class, UtilObjet.DOSSIER_CHARGE, 1, 10000, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                Charge charge = (Charge) object;
                return (charge.getIdExercice() == exercice.getId());
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Charge charge = (Charge) data;
                charges.add(charge);
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

    private void loadEncaissements(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        EcouteurParametreEncaissement epe = new EcouteurParametreEncaissement() {
            @Override
            public int getIdUtilisateur() {
                return utilisateur.getId();
            }

            @Override
            public Frais getFrais(int idFrais) {
                return getFrais_(idFrais);
            }

            @Override
            public Monnaie getMonnaie(int idMonnaie) {
                return getMonnaie_(idMonnaie);
            }

            @Override
            public Revenu getRevenu() {
                for (Revenu rr : revenus) {
                    if (rr.getNom().toLowerCase().contains("frais")) {
                        return rr;
                    }
                }
                return null;
            }
        };

        fm.fm_ouvrirEncaissements(pageActuelle, taillePage, epe, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return checkCritereEncaissement(motCle, object, criteresAvances);
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Encaissement encaissement = (Encaissement) data;
                panel.setDonneesEncaissement(encaissement);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                if (navigateurPages != null) {
                    navigateurPages.setInfos(resultatTotal, panel.getTailleResultatEncaissements());
                    navigateurPages.patienter(false, "Prêt.");
                }

                panel.setDonneesEncaissement_all(resultatTotalObjets);

                progress.setVisible(false);
                progress.setIndeterminate(false);
            }

            @Override
            public void onError(String string) {
                progress.setVisible(false);
                progress.setIndeterminate(false);
                if (navigateurPages != null) {
                    navigateurPages.setInfos(0, 0);
                    navigateurPages.patienter(false, "Prêt.");
                }
            }

            @Override
            public void onProcessing(String string) {
                progress.setVisible(true);
                progress.setIndeterminate(true);
            }
        });
    }

    public boolean checkCritereDecaissement(String motCle, Object data, JS2BPanelPropriete prop) {
        Decaissement decaissement = (Decaissement) data;

        if (decaissement.getIdExercice() != exercice.getId()) {
            return false;
        }
        boolean repMotCle = panel.search_verifier_motcle_decaiss(decaissement, motCle);
        boolean repMonnaie = false;
        boolean repCharge = false;
        boolean repSource = false;

        if (prop != null) {
            PROPRIETE propDateA = prop.getPropriete("A partir du");
            PROPRIETE propDateB = prop.getPropriete("Jusqu'au");
            boolean repPeriode = panel.search_verifier_periode_decaiss(decaissement, (Date) propDateA.getValeurSelectionne(), (Date) propDateB.getValeurSelectionne());
            if (repPeriode == false) {
                return false;
            }
            PROPRIETE propMonnaie = prop.getPropriete("Monnaie");
            int idMonnaie = panel.getIdMonnaie(propMonnaie.getValeurSelectionne() + "");
            if (idMonnaie == -1) {
                repMonnaie = true;
            } else {
                repMonnaie = (idMonnaie == decaissement.getIdMonnaie());
            }
            if (chargeConcerned == null) {
                PROPRIETE propRevenu = prop.getPropriete("Type de charge");
                int idCharge = panel.getIdCharge(propRevenu.getValeurSelectionne() + "");
                if (idCharge == -1) {
                    repCharge = true;
                } else {
                    repCharge = (idCharge == decaissement.getIdCharge());
                }
            } else {
                repCharge = (chargeConcerned.getId() == decaissement.getIdCharge());
            }

            PROPRIETE propDestination = prop.getPropriete("Source des fonds");
            int idSource = panel.getDestination(propDestination.getValeurSelectionne() + "");
            if (idSource == -1) {
                repSource = true;
            } else {
                repSource = (idSource == decaissement.getSource());
            }
        }
        if (repMotCle == true && repMonnaie == true && repCharge == true && repSource == true) {
            return true;
        }
        return false;
    }

    public boolean checkCritereEncaissement(String motCle, Object data, JS2BPanelPropriete prop) {
        Encaissement encaissement = (Encaissement) data;

        if (encaissement.getIdExercice() != exercice.getId()) {
            return false;
        }

        boolean repMonnaie = false;
        boolean repRevenu = false;
        boolean repDestination = false;

        boolean repMotCle = panel.search_verifier_motcle_encaiss(encaissement, motCle);
        if (prop != null) {
            PROPRIETE propDateA = prop.getPropriete("A partir du");
            PROPRIETE propDateB = prop.getPropriete("Jusqu'au");
            boolean repPeriode = panel.search_verifier_periode_encaiss(encaissement, (Date) propDateA.getValeurSelectionne(), (Date) propDateB.getValeurSelectionne());
            if (repPeriode == false) {
                return false;
            }
            PROPRIETE propMonnaie = prop.getPropriete("Monnaie");
            int idMonnaie = panel.getIdMonnaie(propMonnaie.getValeurSelectionne() + "");
            if (idMonnaie == -1) {
                repMonnaie = true;
            } else {
                repMonnaie = (idMonnaie == encaissement.getIdMonnaie());
            }
            if (revenueConcerned == null) {
                PROPRIETE propRevenu = prop.getPropriete("Type de revenu");
                int idRevenu = panel.getIdRevenu(propRevenu.getValeurSelectionne() + "");
                if (idRevenu == -1) {
                    repRevenu = true;
                } else {
                    repRevenu = (idRevenu == encaissement.getIdRevenu());
                }
            } else {
                repRevenu = (revenueConcerned.getId() == encaissement.getIdRevenu());
            }

            PROPRIETE propDestination = prop.getPropriete("Destination des fonds");
            int idDestination = panel.getDestination(propDestination.getValeurSelectionne() + "");
            if (idDestination == -1) {
                repDestination = true;
            } else {
                repDestination = (idDestination == encaissement.getDestination());
            }
        }

        if (repMotCle == true && repMonnaie == true && repRevenu == true && repDestination == true) {
            return true;
        }
        return false;
    }

    private void loadDecaissements(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        EcouteurParametreDecaissement epd = new EcouteurParametreDecaissement() {
            @Override
            public int getIdUtilisateur() {
                return utilisateur.getId();
            }

            @Override
            public Agent getAgent(int idAgent) {
                return getAgent_(idAgent);
            }

            @Override
            public Monnaie getMonnaie(int idMonnaie) {
                return getMonnaie_(idMonnaie);
            }

            @Override
            public Charge getCharge() {
                for (Charge rr : charges) {
                    if (rr.getNom().toLowerCase().contains("salaire")) {
                        return rr;
                    }
                }
                return null;
            }
        };

        fm.fm_ouvrirDecaissements(pageActuelle, taillePage, epd, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return checkCritereDecaissement(motCle, object, criteresAvances);
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Decaissement decaissement = (Decaissement) data;

                panel.setDonneesDecaissement(decaissement);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                if (navigateurPages != null) {
                    navigateurPages.setInfos(resultatTotal, panel.getTailleResultatDecaissements());
                    navigateurPages.patienter(false, "Prêt.");
                }

                panel.setDonneesDecaissement_all(resultatTotalObjets);

                progress.setVisible(false);
                progress.setIndeterminate(false);
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

    private void action_save(SortiesTresorerie se) {
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
                        saveEncaissements(se, ee, user, exercice);

                        se.getEcouteurEnregistrement().onDone("Enregistré!");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            };
            th.start();
        }
    }

    private void saveEncaissements(SortiesTresorerie se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Encaissement> listeNewEncaissements = se.getListeEncaissements();
        Vector<Encaissement> listeNewEncaissementsTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Encaissement ia : listeNewEncaissements) {
            if (ia.getId() == -100) {
                ia.setBeta(InterfaceEncaissement.BETA_EXISTANT);
                //System.out.println("PAS D'ENREGISTREMENT POUR CECI !!!!!");
            } else if (ia.getBeta() == InterfaceEncaissement.BETA_MODIFIE || ia.getBeta() == InterfaceEncaissement.BETA_NOUVEAU) {
                canBeSaved = true;
                //Pour id étrangère incomplètes
                if (ia.getIdRevenu() == -1) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, "Désolé\nVeuillez définir la nature de cet encaissement.", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }
                if (ia.getMontant() == 0) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, "Désolé\nVeuillez définir un montant différent de Zéro.", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }

                if (canBeSaved == true) {
                    ia.setIdExercice(annee.getId());
                    ia.setIdUtilisateur(user.getId());
                    ia.setBeta(InterfaceEncaissement.BETA_EXISTANT);
                    listeNewEncaissementsTempo.add(ia);
                }
            }
        }
        if (!listeNewEncaissementsTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewEncaissementsTempo, UtilObjet.DOSSIER_ENCAISSEMENT, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Encaissement enregistrés !");
                    //donneesExercice.setAgents(listeNewAgentsTempo);
                    saveDecaissements(se, ee, user, exercice);
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
            saveDecaissements(se, ee, user, exercice);
        }
    }

    private void saveDecaissements(SortiesTresorerie se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Decaissement> listeNewDecaissements = se.getListeDecaissements();
        Vector<Decaissement> listeNewDecaissementsTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Decaissement ia : listeNewDecaissements) {
            if (ia.getBeta() == InterfaceDecaissement.BETA_MODIFIE || ia.getBeta() == InterfaceDecaissement.BETA_NOUVEAU) {
                canBeSaved = true;
                //Pour id étrangère incomplètes
                if (ia.getIdCharge() == -1) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, "Désolé\nVeuillez définir la nature de ce décaissement.", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }
                if (ia.getMontant() == 0) {
                    canBeSaved = false;
                    JOptionPane.showMessageDialog(panel, "Désolé\nVeuillez définir le montant", "Alert!", JOptionPane.ERROR_MESSAGE, icones.getAlert_02());
                    panel.setBtEnregistrerNouveau();
                }

                if (canBeSaved == true) {
                    ia.setIdExercice(annee.getId());
                    ia.setIdUtilisateur(user.getId());
                    ia.setBeta(InterfaceEncaissement.BETA_EXISTANT);
                    listeNewDecaissementsTempo.add(ia);
                }
            }
        }
        if (!listeNewDecaissementsTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewDecaissementsTempo, UtilObjet.DOSSIER_DECAISSEMENT, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    progress.setVisible(false);
                    progress.setIndeterminate(false);
                    ee.onDone("Décaisement enregistrés !");
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

    public Frais getFrais_(int idFrais) {
        for (Frais ff : frais) {
            if (ff.getId() == idFrais) {
                return ff;
            }
        }
        return null;
    }

    public Monnaie getMonnaie_(int idMonnaie) {
        for (Monnaie mm : monnaies) {
            if (mm.getId() == idMonnaie) {
                return mm;
            }
        }
        return null;
    }

    public Agent getAgent_(int idAgent) {
        for (Agent mm : agents) {
            if (mm.getId() == idAgent) {
                return mm;
            }
        }
        return null;
    }

    private void initUI(String nomTab) {
        panel = new PanelTresorerie(ef, couleurBasique, progress, tabOnglet, getData(), new EcouteurTresorerie() {
            @Override
            public void onEnregistre(SortiesTresorerie st) {
                if (st != null) {
                    action_save(st);
                }
            }

            @Override
            public void onDetruitElement(int idElement, int index, long signature) {
                if (idElement != -1 && idElement != -100) {
                    switch (index) {
                        case 0://ENCAISSEMENT
                            fm.fm_supprimer(UtilObjet.DOSSIER_ENCAISSEMENT, idElement, signature);
                            break;
                        case 1://DECAISSEMENT
                            fm.fm_supprimer(UtilObjet.DOSSIER_DECAISSEMENT, idElement, signature);
                            break;
                        default:
                    }
                }
            }

            @Override
            public void onClosed() {
                if (et != null) {
                    et.onClosed();
                }
            }

            @Override
            public boolean onCanDelete(int idElement, int index, long signature) {
                boolean canDelete = true;
                switch (index) {
                    case 0://ENCAISSEMENT
                        canDelete = true;
                        break;
                    case 1://DECAISSEMENT
                        canDelete = true;
                        break;
                    default:
                }
                return canDelete;
            }
        });

        //Navigateur pour Encaissements
        NavigateurPages navigateurPagesEncaissement = panel.getNavigateurPagesEncaissement();
        navigateurPagesEncaissement.initialiser(fenetre, new EcouteurNavigateurPages() {
            @Override
            public void onRecharge(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances) {
                new Thread() {
                    public void run() {
                        navigateurPagesEncaissement.setInfos(0, panel.getTailleResultatEncaissements());
                        navigateurPagesEncaissement.patienter(true, "Chargement...");
                        panel.reiniliserEncaissements();
                        chercherEncaissements(motCle, pageActuelle, taillePage, criteresAvances, navigateurPagesEncaissement);
                    }
                }.start();

            }
        }, new ConstructeurCriteres() {
            @Override
            public JS2BPanelPropriete onInitialise() {
                JS2BPanelPropriete panProp = new JS2BPanelPropriete(icones.getFiltrer_01(), "Critères avancés", true);
                panProp.viderListe();

                Date defaultDateA = (revenueConcerned == null) ? UtilTresorerie.getDate_CeMatin(new Date()) : UtilTresorerie.getDate_AjouterAnnee(new Date(), -10);
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "A partir du", "du", null, defaultDateA, PROPRIETE.TYPE_CHOIX_DATE), 0);
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "Jusqu'au", "Au", null, UtilTresorerie.getDate_ZeroHeure(new Date()), PROPRIETE.TYPE_CHOIX_DATE), 0);

                //Critres Monnaie
                Vector listeMonnaies = new Vector();
                listeMonnaies.add("TOUTES");
                for (Monnaie cl : panel.getParametreTresorerie().getMonnaies()) {
                    listeMonnaies.add(cl.getNom());
                }
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getTaxes_01(), "Monnaie", "Monnaie", listeMonnaies, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                //Critres Revenu
                Vector listeRevenus = new Vector();
                listeRevenus.add("TOUS");
                for (Revenu per : panel.getParametreTresorerie().getRevenus()) {
                    listeRevenus.add(per.getNom());
                }
                String selectedRevenu = (revenueConcerned == null) ? "" : revenueConcerned.getNom();
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "Type de revenu", "Revenu", listeRevenus, selectedRevenu, PROPRIETE.TYPE_CHOIX_LISTE), 0);

                Vector listeDestinations = new Vector();
                listeDestinations.add("TOUTES");
                listeDestinations.add("BANQUE");
                listeDestinations.add("CAISSE");

                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getRecette_01(), "Destination des fonds", "Destination", listeDestinations, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                return panProp;
            }
        });

        //Navigateur pour Décaissements
        NavigateurPages navigateurPagesDecaissements = panel.getNavigateurPagesDecaissement();
        navigateurPagesDecaissements.initialiser(fenetre, new EcouteurNavigateurPages() {
            @Override
            public void onRecharge(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances) {
                new Thread() {
                    public void run() {
                        navigateurPagesDecaissements.setInfos(0, panel.getTailleResultatDecaissements());
                        navigateurPagesDecaissements.patienter(true, "Chargement...");
                        panel.reiniliserDecaissements();
                        chercherDecaissements(motCle, pageActuelle, taillePage, criteresAvances, navigateurPagesDecaissements);
                    }
                }.start();
            }
        }, new ConstructeurCriteres() {
            @Override
            public JS2BPanelPropriete onInitialise() {
                JS2BPanelPropriete panProp = new JS2BPanelPropriete(icones.getFiltrer_01(), "Critères avancés", true);
                panProp.viderListe();

                //Critres date A
                Date defaultDateA = (chargeConcerned == null) ? UtilTresorerie.getDate_CeMatin(new Date()) : UtilTresorerie.getDate_AjouterAnnee(new Date(), -10);
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "A partir du", "du", null, defaultDateA, PROPRIETE.TYPE_CHOIX_DATE), 0);

                //Critres date B
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "Jusqu'au", "Au", null, UtilTresorerie.getDate_ZeroHeure(new Date()), PROPRIETE.TYPE_CHOIX_DATE), 0);

                //Critres Monnaie
                Vector listeMonnaies = new Vector();
                listeMonnaies.add("TOUTES");
                for (Monnaie cl : panel.getParametreTresorerie().getMonnaies()) {
                    listeMonnaies.add(cl.getNom());
                }
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getTaxes_01(), "Monnaie", "Monnaie", listeMonnaies, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                //Critres Revenu
                Vector listeRevenus = new Vector();
                listeRevenus.add("TOUS");
                for (Charge per : panel.getParametreTresorerie().getCharges()) {
                    listeRevenus.add(per.getNom());
                }
                String selectedCharge = (chargeConcerned == null) ? "" : chargeConcerned.getNom();
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "Type de charge", "Charge", listeRevenus, selectedCharge, PROPRIETE.TYPE_CHOIX_LISTE), 0);

                Vector listeDestinations = new Vector();
                listeDestinations.add("TOUTES");
                listeDestinations.add("BANQUE");
                listeDestinations.add("CAISSE");

                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getRecette_01(), "Source des fonds", "Source", listeDestinations, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                return panProp;
            }
        });

        //Chargement du gestionnaire sur l'onglet
        if (deleteCurrentTab == true) {
            tabOnglet.addTab(nomTab, panel);
            tabOnglet.setSelectedComponent(panel);
        }

        progress.setVisible(false);
        progress.setIndeterminate(false);

        if (revenueConcerned != null) {
            panel.tabPrincipal.setSelectedIndex(0);
            panel.activerBoutons(0);

            navigateurPagesEncaissement.setInfos(0, panel.getTailleResultatEncaissements());
            navigateurPagesEncaissement.criteresActuels_activer();
        } else if (chargeConcerned != null) {
            panel.tabPrincipal.setSelectedIndex(1);
            panel.activerBoutons(1);

            navigateurPagesDecaissements.setInfos(0, panel.getTailleResultatDecaissements());
            navigateurPagesDecaissements.criteresActuels_activer();
        } else {
            navigateurPagesEncaissement.setInfos(0, panel.getTailleResultatEncaissements());
            navigateurPagesDecaissements.setInfos(0, panel.getTailleResultatDecaissements());

            //On active les criteres et lance automatiquement la recherche
            navigateurPagesEncaissement.criteresActuels_activer();
            navigateurPagesDecaissements.criteresActuels_activer();
        }

    }

    private void chercherEncaissements(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        loadEncaissements(motCle, pageActuelle, taillePage, criteresAvances, navigateurPages);
    }

    private void chercherDecaissements(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        loadDecaissements(motCle, pageActuelle, taillePage, criteresAvances, navigateurPages);
    }

}
