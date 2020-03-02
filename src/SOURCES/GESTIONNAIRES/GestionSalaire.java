/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import ICONES.Icones;
import SOURCES.CALLBACK.EcouteurGestionPaie;
import SOURCES.CallBack_Paie.EcouteurPaie;
import SOURCES.Callback.CritereSuppression;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Objets.FileManager;
import SOURCES.UI_Paie.PanelPaie;
import SOURCES.Utilitaires_Paie.DataPaie;
import SOURCES.Utilitaires_Paie.ParametreFichesDePaie;
import SOURCES.Utilitaires_Paie.SortiesFichesDePaies;
import SOURCES.Utilitaires_Paie.UtilPaie;
import SOURCES.Utilitaires_Tresorerie.UtilTresorerie;
import Source.Callbacks.ConstructeurCriteres;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Callbacks.EcouteurFreemium;
import Source.Callbacks.EcouteurNavigateurPages;
import Source.Callbacks.EcouteurStandard;
import Source.Interface.InterfaceMonnaie;
import Source.Objet.Agent;
import Source.Objet.CouleurBasique;
import Source.Objet.Entreprise;
import Source.Objet.Annee;
import Source.Objet.Fiche_paie;
import Source.Objet.Monnaie;
import Source.Objet.UtilObjet;
import Source.Objet.Utilisateur;
import Source.UI.NavigateurPages;
import Sources.CHAMP_LOCAL;
import Sources.PROPRIETE;
import Sources.UI.JS2BPanelPropriete;
import java.util.Date;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import Source.Interface.InterfaceAnnee;

/**
 *
 * @author HP Pavilion
 */
public class GestionSalaire {

    public static String NOM = "SALAIRE";
    public PanelPaie panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;

    public FileManager fm;

    public Annee exercice = null;
    public Vector<Monnaie> monnaies = new Vector<>();
    public Vector<Agent> agents = new Vector<>();
    //public Vector<Fiche> fiches = new Vector<>();
    public CouleurBasique couleurBasique;
    public String selectedAnnee;
    public Agent agentConcerned = null;
    public boolean deleteCurrentTab = true;
    public JFrame fenetre;
    public Icones icones;
    public boolean canBeSaved = false;
    private EcouteurFreemium ef = null;
    private EcouteurGestionPaie ep = null;

    public GestionSalaire(EcouteurGestionPaie ep, EcouteurFreemium ef, JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.ef = ef;
        this.ep = ep;
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.fenetre = fenetre;
        this.icones = icones;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.agentConcerned = null;
    }

    public GestionSalaire(EcouteurGestionPaie ep, EcouteurFreemium ef, JFrame fenetre, Icones icones, CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Agent agentConcerned) {
        this.ef = ef;
        this.ep = ep;
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.fenetre = fenetre;
        this.icones = icones;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.agentConcerned = agentConcerned;
    }

    private DataPaie getData() {
        ParametreFichesDePaie parametreFichesDePaie = new ParametreFichesDePaie(utilisateur, entreprise, exercice, agents, monnaies);
        return new DataPaie(parametreFichesDePaie);
    }

    public void gp_setDonneesFromFileManager(String selectedAnnee, boolean deleteCurrentTab) {
        this.deleteCurrentTab = deleteCurrentTab;
        this.selectedAnnee = selectedAnnee;
        if (fm != null) {
            boolean mustLoadData = true;
            if (deleteCurrentTab == true) {
                int nbOnglets = tabOnglet.getComponentCount();
                for (int i = 0; i < nbOnglets; i++) {
                    if (tabOnglet.getComponentCount() > i) {
                        String titreOnglet = tabOnglet.getTitleAt(i);
                        //System.out.println("Onglet - " + titreOnglet);
                        String Snom = NOM;
                        if (agentConcerned != null) {
                            Snom = NOM + " - " + agentConcerned.getNom() + " " + agentConcerned.getPrenom();
                        }
                        if (titreOnglet.equals(Snom)) {
                            //System.out.println("Une page d'adhésion était déjà ouverte, je viens de la fermer");
                            tabOnglet.remove(i);
                            mustLoadData = true;
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
        fm.fm_ouvrirTout(0, Agent.class, UtilObjet.DOSSIER_AGENT, 1, 1000, new EcouteurOuverture() {
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
                if (agentConcerned == null) {
                    initUI(NOM);
                } else {
                    initUI(NOM + " - " + agentConcerned.getNom() + " " + agentConcerned.getPrenom());
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

    public boolean checkCriteresPaie(String motCle, Object data, JS2BPanelPropriete jsbpp) {
        Fiche_paie paie = (Fiche_paie) data;

        if (paie.getIdExercice() != exercice.getId()) {
            return false;
        }

        boolean repCategorie = false;
        boolean repMois = false;
        boolean repPeriode = false;
        boolean repMotCle = false;

        PROPRIETE propDateA = null;
        PROPRIETE propDateB = null;
        PROPRIETE propCategorie = null;
        PROPRIETE propMois = null;

        if (agentConcerned != null) {
            if (agentConcerned.getId() != paie.getIdAgent()) {
                return false;
            }
        }

        repMotCle = panel.search_verifier_motcle(paie, motCle);
        if (repMotCle == false) {
            return false;
        }
        if (jsbpp != null) {
            propDateA = jsbpp.getPropriete("A partir du");
            propDateB = jsbpp.getPropriete("Jusqu'au");
            repPeriode = panel.search_verifier_periode(paie, (Date) propDateA.getValeurSelectionne(), (Date) propDateB.getValeurSelectionne());
            if (repPeriode == false) {
                return false;
            }

            if (agentConcerned == null) {
                propCategorie = jsbpp.getPropriete("Catégorie d'agents");
                repCategorie = panel.search_verifier_categorie(paie, panel.getCategorie(propCategorie.getValeurSelectionne() + ""));
            } else {
                repCategorie = true;
            }

            propMois = jsbpp.getPropriete("Paie du mois de");
            repMois = panel.search_verifier_mois(paie, propMois.getValeurSelectionne() + "");

            //System.out.println("Categorie:" + idCategorie+", Mois:" + repMois+", Periode:" + repPeriode);
        } else {
            repCategorie = true;
            repMois = true;
            repPeriode = true;
        }

        if (repMotCle == true && repCategorie == true && repMois == true && repPeriode == true) {
            //panel.setDonneesFichePaie(paie);
            return true;
        } else {
            //panel.setDonneesFichePaie(null);
            return false;
        }
    }

    private void loadFiches(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        fm.fm_ouvrirTout(0, Fiche_paie.class, UtilObjet.DOSSIER_FICHE_DE_PAIE, pageActuelle, taillePage, new EcouteurOuverture() {
            @Override
            public boolean isCriteresRespectes(Object object) {
                return checkCriteresPaie(motCle, object, criteresAvances);
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                panel.setDonneesFichePaie((Fiche_paie) data);
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                if (navigateurPages != null) {
                    navigateurPages.setInfos(resultatTotal, panel.getTailleResultatFiches());
                    navigateurPages.patienter(false, "Prêt.");
                }
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

    private void saveFiches(SortiesFichesDePaies se, EcouteurEnregistrement ee, Utilisateur user, Annee annee) {
        Vector<Fiche_paie> listeNewFiches = se.getListeFichesDePaie();
        Vector<Fiche_paie> listeNewFichesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Fiche_paie ia : listeNewFiches) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdUtilisateur(user.getId());
                ia.setIdEntreprise(user.getIdEntreprise());
                ia.setBeta(InterfaceAnnee.BETA_EXISTANT);
                listeNewFichesTempo.add(ia);
            }
        }
        if (!listeNewFichesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewFichesTempo, UtilObjet.DOSSIER_FICHE_DE_PAIE, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    progress.setVisible(false);
                    progress.setIndeterminate(false);
                    //System.out.println(message);
                    ee.onDone("Eleves enregistrées !");
                }

                @Override
                public void onError(String message) {
                    System.err.println(message);
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

    private void action_save(SortiesFichesDePaies se) {
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
                        saveFiches(se, ee, user, exercice);

                        se.getEcouteurEnregistrement().onDone("Enregistré!");

                        //On ance la syncrhonisation avec le serveur
                        if (ep != null) {
                            ep.onSynchronise();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            };
            th.start();
        }
    }

    private void chercherFiches(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances, NavigateurPages navigateurPages) {
        loadFiches(motCle, pageActuelle, taillePage, criteresAvances, navigateurPages);
    }

    private void initUI(String nomTab) {
        panel = new PanelPaie(ef, couleurBasique, progress, tabOnglet, getData(), new EcouteurPaie() {
            @Override
            public void onEnregistre(SortiesFichesDePaies si) {
                if (si != null) {
                    //System.out.println("DANGER !!!!!! ADHESION: Enregistrement...");
                    action_save(si);
                }
            }

            @Override
            public void onDetruitTout(int idExercice) {
                System.out.println("Destruction des fiches de paie pour l'exercice " + idExercice);
                fm.fm_supprimerTout(Fiche_paie.class, UtilObjet.DOSSIER_FICHE_DE_PAIE, null, new CritereSuppression() {
                    @Override
                    public boolean canBeDeleted(Object objToDelete) {
                        return ((Fiche_paie) objToDelete).getIdExercice() == exercice.getId();
                    }
                });

                //On ance la syncrhonisation avec le serveur
                if (ep != null) {
                    ep.onSynchronise();
                }
            }

            @Override
            public void onDetruitElement(int idElement, long signature) {
                //System.out.println("Suppression de la fiche " + idElement);
                if (idElement != -1) {
                    boolean rep = fm.fm_supprimer(UtilObjet.DOSSIER_FICHE_DE_PAIE, idElement, signature);
                    if (rep == true) {
                        //On ance la syncrhonisation avec le serveur
                        if (ep != null) {
                            ep.onSynchronise();
                        }
                    }

                }
            }

            @Override
            public void onClosed() {
                if (ep != null) {
                    ep.onClosed();
                }
            }

            @Override
            public boolean onCanDelete(int idElement, long signature) {
                return true;
            }
        });

        NavigateurPages navigateurPages = panel.getNavigateurPagesFichePaie();
        navigateurPages.initialiser(fenetre, new EcouteurNavigateurPages() {
            @Override
            public void onRecharge(String motCle, int pageActuelle, int taillePage, JS2BPanelPropriete criteresAvances) {
                new Thread() {
                    public void run() {
                        navigateurPages.setInfos(0, panel.getTailleResultatFiches());
                        navigateurPages.patienter(true, "Chargement...");
                        panel.reiniliserFichePaie();
                        chercherFiches(motCle, pageActuelle, taillePage, criteresAvances, navigateurPages);
                    }
                }.start();
            }
        }, new ConstructeurCriteres() {
            @Override
            public JS2BPanelPropriete onInitialise() {
                JS2BPanelPropriete panProp = new JS2BPanelPropriete(icones.getFiltrer_01(), "Critères avancés", true);
                panProp.viderListe();

                Date defaultDateA = (agentConcerned == null) ? UtilTresorerie.getDate_CeMatin(new Date()) : UtilTresorerie.getDate_AjouterAnnee(new Date(), -10);
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "A partir du", "du", null, defaultDateA, PROPRIETE.TYPE_CHOIX_DATE), 0);
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "Jusqu'au", "Au", null, UtilPaie.getDate_ZeroHeure(new Date()), PROPRIETE.TYPE_CHOIX_DATE), 0);

                //Critres Monnaie
                Vector listeCategories = new Vector();
                listeCategories.add("TOUTES");
                listeCategories.add("ADMINISTRATION_1");
                listeCategories.add("ADMINISTRATION_2");
                listeCategories.add("MATERNELLE");
                listeCategories.add("PARTIEL");
                listeCategories.add("PRIMAIRE");
                listeCategories.add("PRIME");
                listeCategories.add("SECONDAIRE");
                listeCategories.add("SURVEILLANT");
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getTaxes_01(), "Catégorie d'agents", "Monnaie", listeCategories, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

                //Critres Revenu
                Vector listeMois = new Vector();
                listeMois.add("TOUS");
                for (String Omois : UtilPaie.getListeMois(exercice.getDebut(), exercice.getFin())) {
                    listeMois.add(Omois);
                }
                panProp.AjouterPropriete(new CHAMP_LOCAL(icones.getCalendrier_01(), "Paie du mois de", "mois", listeMois, "", PROPRIETE.TYPE_CHOIX_LISTE), 0);

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

        navigateurPages.setInfos(0, panel.getTailleResultatFiches());
        navigateurPages.criteresActuels_activer();
    }

}
