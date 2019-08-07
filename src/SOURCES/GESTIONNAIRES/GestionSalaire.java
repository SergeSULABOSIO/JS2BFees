/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import SOURCES.CallBack_Paie.EcouteurActualisationPaie;
import SOURCES.CallBack_Paie.EcouteurPaie;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback.EcouteurStandard;
import SOURCES.Objets.FileManager;
import SOURCES.UI_Paie.PanelPaie;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires_Paie.DataPaie;
import SOURCES.Utilitaires_Paie.DonneesFicheDePaie;
import SOURCES.Utilitaires_Paie.ParametreFichesDePaie;
import SOURCES.Utilitaires_Paie.SortiesFichesDePaies;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Interface.InterfaceExercice;
import Source.Interface.InterfaceMonnaie;
import Source.Objet.Agent;
import Source.Objet.CouleurBasique;
import Source.Objet.Entreprise;
import Source.Objet.Exercice;
import Source.Objet.Fiche;
import Source.Objet.Monnaie;
import Source.Objet.Utilisateur;
import java.util.Vector;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

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

    public Exercice exercice = null;
    public FileManager fm;
    public Vector<Monnaie> monnaies = new Vector<>();
    public Vector<Agent> agents = new Vector<>();
    public Vector<Fiche> fiches = new Vector<>();
    public CouleurBasique couleurBasique;
    public String selectedAnnee;
    public Agent agentConcerned = null;
    public boolean deleteCurrentTab = true;

    public GestionSalaire(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.agentConcerned = null;
    }

    public GestionSalaire(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Agent agentConcerned) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.agentConcerned = agentConcerned;
    }

    private DataPaie getData() {
        ParametreFichesDePaie parametreFichesDePaie = new ParametreFichesDePaie(utilisateur, entreprise, exercice, agents, monnaies);
        DonneesFicheDePaie donneesFicheDePaie = new DonneesFicheDePaie(fiches);
        System.out.println("getData!");
        return new DataPaie(parametreFichesDePaie, donneesFicheDePaie);
    }

    public void gp_setDonneesFromFileManager(String selectedAnnee, boolean deleteCurrentTab) {
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
                    if (agentConcerned != null) {
                        Snom = NOM + " - " + agentConcerned.getNom() + " " + agentConcerned.getPrenom();
                    }
                    if (titreOnglet.equals(Snom)) {
                        System.out.println("Une page d'adhésion était déjà ouverte, je viens de la fermer");
                        tabOnglet.remove(i);
                        mustLoadData = true;
                    }
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
        fm.fm_ouvrirTout(0, Agent.class, UtilFees.DOSSIER_AGENT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Agent agent = (Agent) o;
                    if (agent.getIdExercice() == exercice.getId()) {
                        agents.add(agent);
                        System.out.println(" * " + agent.toString());
                    }
                }
                loadFiches();
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

    private void loadFiches() {
        fiches.removeAllElements();
        fm.fm_ouvrirTout(0, Fiche.class, UtilFees.DOSSIER_FICHE_DE_PAIE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Fiche ficheDePaie = (Fiche) o;
                    if (ficheDePaie.getIdExercice() == exercice.getId()) {
                        if (agentConcerned != null) {
                            if (ficheDePaie.getIdAgent() == agentConcerned.getId()) {
                                fiches.add(ficheDePaie);
                            }
                        } else {
                            fiches.add(ficheDePaie);
                        }
                        System.out.println(" * " + ficheDePaie.toString());
                    }
                }

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

    private void saveFiches(SortiesFichesDePaies se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Fiche> listeNewFiches = se.getListeFichesDePaie();
        Vector<Fiche> listeNewFichesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Fiche ia : listeNewFiches) {
            if (ia.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ia.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdUtilisateur(user.getId());
                ia.setIdEntreprise(user.getIdEntreprise());
                ia.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewFichesTempo.add(ia);
            }
        }
        if (!listeNewFichesTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewFichesTempo, UtilFees.DOSSIER_FICHE_DE_PAIE, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    progress.setVisible(false);
                    progress.setIndeterminate(false);
                    System.out.println(message);
                    ee.onDone("Eleves enregistrées !");
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

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            };
            th.start();
        }
    }

    private void initUI(String nomTab) {
        panel = new PanelPaie(couleurBasique, progress, tabOnglet, getData(), new EcouteurPaie() {
            @Override
            public void onEnregistre(SortiesFichesDePaies si) {
                if (si != null) {
                    System.out.println("DANGER !!!!!! ADHESION: Enregistrement...");
                    action_save(si);
                }
            }

            @Override
            public void onDetruitTout(int idExercice) {
                System.out.println("Destruction des fiches de paie pour l'exercice " + idExercice);
                for (Fiche Ofiche : fiches) {
                    if (Ofiche.getIdExercice() == idExercice) {
                        fm.fm_supprimer(UtilFees.DOSSIER_FICHE_DE_PAIE, Ofiche.getId());
                    }
                }
            }

            @Override
            public void onDetruitElement(int idElement) {
                System.out.println("Suppression de la fiche " + idElement);
                if (idElement != -1) {
                    fm.fm_supprimer(UtilFees.DOSSIER_FICHE_DE_PAIE, idElement);
                }
            }
        });

        panel.setEcouteurActualisationPaie(new EcouteurActualisationPaie() {
            @Override
            public DataPaie onRechargeDonneesEtParametres() {
                gp_setDonneesFromFileManager(selectedAnnee, false);
                return getData();
            }
        });
        //Chargement du gestionnaire sur l'onglet
        if (deleteCurrentTab == true) {
            tabOnglet.addTab(nomTab, panel);
            tabOnglet.setSelectedComponent(panel);
        }
        progress.setVisible(false);
        progress.setIndeterminate(false);
    }

}





