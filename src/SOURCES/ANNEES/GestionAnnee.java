/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.ANNEES;

import SOURCES.Callback.EcouteurAnneeScolaire;
import SOURCES.Callback.EcouteurEnregistrement;
import SOURCES.Callback.EcouteurStandard;
import SOURCES.Interfaces.InterfaceAgent;
import SOURCES.Interfaces.InterfaceCharge;
import SOURCES.Interfaces.InterfaceClasse;
import SOURCES.Interfaces.InterfaceCours;
import SOURCES.Interfaces.InterfaceExercice;
import SOURCES.Interfaces.InterfaceFrais;
import SOURCES.Interfaces.InterfaceMonnaie;
import SOURCES.Interfaces.InterfacePeriode;
import SOURCES.Interfaces.InterfaceRevenu;
import SOURCES.Interfaces.InterfaceUtilisateur;
import SOURCES.OBJETS.Exercice;
import SOURCES.OBJETS.Monnaie;
import SOURCES.Objets.Entreprise;
import SOURCES.Objets.FileManager;
import SOURCES.Objets.Utilisateur;
import SOURCES.UI.Panel;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires.DonneesExercice;
import SOURCES.Utilitaires.LiaisonClasseFrais;
import SOURCES.Utilitaires.LiaisonPeriodeFrais;
import SOURCES.Utilitaires.ParametreExercice;
import SOURCES.Utilitaires.SortiesExercice;
import java.util.Vector;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

/**
 *
 * @author user
 */
public class GestionAnnee {

    public Panel panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public Monnaie monnaie_output;
    public ParametreExercice parametreExercice;
    public DonneesExercice donneesExercice;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;
    private SortiesExercice sortiesExercice = null;
    private InterfaceExercice newIannee = null;
    private FileManager fm;

    public GestionAnnee(FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Monnaie monnaie_output) {
        this.fm = fm;
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

    public void ga_setDonnees(Exercice anneeExistant, Vector<InterfaceAgent> agents, Vector<InterfaceCharge> charges, Vector<InterfaceClasse> classes, Vector<InterfaceCours> cours, Vector<InterfaceFrais> frais, Vector<InterfaceMonnaie> monnaies, Vector<InterfaceRevenu> revenus, Vector<InterfacePeriode> periodes) {
        if (anneeExistant != null) {
            newIannee = anneeExistant;
        }
        this.donneesExercice = new DonneesExercice(anneeExistant, agents, charges, classes, cours, frais, monnaies, revenus, periodes);
    }

    public void ga_initUI() {
        panel = new Panel(tabOnglet, parametreExercice, donneesExercice, new EcouteurAnneeScolaire() {
            @Override
            public void onEnregistre(SortiesExercice se) {
                sortiesExercice = se;
                System.out.println("EXERCICE: Enregistrement...");
                action_save();
            }
        });
        //Chargement du gestionnaire sur l'onglet
        tabOnglet.addTab("New Année Scolaire", panel);
        tabOnglet.setSelectedComponent(panel);
    }

    private void action_save() {
        if (sortiesExercice != null) {
            Thread th = new Thread() {
                @Override
                public void run() {
                    try {
                        EcouteurEnregistrement ee = sortiesExercice.getEcouteurEnregistrement();
                        Utilisateur user = fm.fm_getSession().getUtilisateur();
                        ee.onUploading("Chargement...");
                        progress.setVisible(true);
                        progress.setIndeterminate(true);
                        sleep(50);

                        //DEBUT D'ENREGISTREMENT
                        newIannee = sortiesExercice.getExercice();
                        System.out.println(" =>EXERCICE EXISTANT: " + newIannee.toString());
                        if (newIannee.getBeta() == InterfaceExercice.BETA_MODIFIE || newIannee.getBeta() == InterfaceExercice.BETA_NOUVEAU) {
                            saveExercice(ee, newIannee, user);
                        } else {
                            //On enregistre les éventuelles éléments dépendant de l'année scolaire
                            saveChildes(ee, user, newIannee);
                        }

                        sortiesExercice.getEcouteurEnregistrement().onDone("Enregistré!");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            };
            th.start();
        }
    }

    private void saveChildes(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice newIannee) {
        saveMonnaies(ee, user, newIannee);
    }

    private void saveExercice(EcouteurEnregistrement ee, InterfaceExercice newIannee, InterfaceUtilisateur user) {
        System.out.println(" * EXERCICE: " + newIannee.toString());
        //On précise qui est en train d'enregistrer cette donnée
        newIannee.setIdUtilisateur(user.getId());
        newIannee.setIdEntreprise(user.getIdEntreprise());
        newIannee.setBeta(InterfaceExercice.BETA_EXISTANT);

        fm.fm_enregistrer(newIannee, UtilFees.DOSSIER_ANNEE, new EcouteurStandard() {
            @Override
            public void onDone(String message) {
                System.out.println(message);
                //Après enregistrement
                ee.onDone(newIannee.getNom() + " enregistrée (" + newIannee.getId() + ").");
                donneesExercice.setExercice(newIannee);

                //On enregistre les éventuelles éléments dépendant de l'année scolaire
                saveChildes(ee, user, newIannee);
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
    }

    private void saveMonnaies(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<InterfaceMonnaie> listeNewMonnaie = sortiesExercice.getListeMonnaies();
        Vector<InterfaceMonnaie> listeNewMonnaieTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfaceMonnaie im : listeNewMonnaie) {
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
                    donneesExercice.setMonnaies(listeNewMonnaie);
                    saveClasses(ee, user, newIannee);
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
            saveClasses(ee, user, newIannee);
        }
    }

    private void saveClasses(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<InterfaceClasse> listeNewClasses = sortiesExercice.getListeClasse();
        Vector<InterfaceClasse> listeNewClassesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfaceClasse ic : listeNewClasses) {
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
                    donneesExercice.setClasses(listeNewClasses);
                    savePeriodes(ee, user, newIannee);
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
            savePeriodes(ee, user, newIannee);
        }
    }

    private void savePeriodes(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<InterfacePeriode> listeNewPeriodes = sortiesExercice.getListePeriodes();
        Vector<InterfacePeriode> listeNewPeriodesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfacePeriode ip : listeNewPeriodes) {
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
                    donneesExercice.setPeriodes(listeNewPeriodes);
                    saveAgents(ee, user, newIannee);
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
            saveAgents(ee, user, newIannee);
        }
    }

    private void saveAgents(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<InterfaceAgent> listeNewAgents = sortiesExercice.getListeAgents();
        Vector<InterfaceAgent> listeNewAgentsTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfaceAgent ia : listeNewAgents) {
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
                    donneesExercice.setAgents(listeNewAgentsTempo);
                    saveCours(ee, user, newIannee);
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
            saveCours(ee, user, newIannee);
        }
    }

    private int getIdClasse(long signature) {
        if (sortiesExercice != null) {
            for (InterfaceClasse ic : sortiesExercice.getListeClasse()) {
                if (ic.getSignature() == signature) {
                    return ic.getId();
                }
            }
        }
        return -1;
    }
    
    private String getNomClasse(long signature) {
        if (sortiesExercice != null) {
            for (InterfaceClasse ic : sortiesExercice.getListeClasse()) {
                if (ic.getSignature() == signature) {
                    return ic.getNom();
                }
            }
        }
        return "";
    }
    
    private String getNomPeriode(long signature) {
        if (sortiesExercice != null) {
            for (InterfacePeriode ic : sortiesExercice.getListePeriodes()) {
                if (ic.getSignature() == signature) {
                    return ic.getNom();
                }
            }
        }
        return "";
    }

    private int getIdAgent(long signature) {
        if (sortiesExercice != null) {
            for (InterfaceAgent ia : sortiesExercice.getListeAgents()) {
                if (ia.getSignature() == signature) {
                    return ia.getId();
                }
            }
        }
        return -1;
    }
    
    private int getIdMonnaie(long signature) {
        if (sortiesExercice != null) {
            for (InterfaceMonnaie ia : sortiesExercice.getListeMonnaies()) {
                if (ia.getSignature() == signature) {
                    return ia.getId();
                }
            }
        }
        return -1;
    }

    private void saveCours(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<InterfaceCours> listeNewCours = sortiesExercice.getListeCours();
        Vector<InterfaceCours> listeNewCoursTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfaceCours ic : listeNewCours) {
            if (ic.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ic.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ic.setIdClasse(getIdClasse(ic.getSignatureClasse()));
                ic.setIdEnseignant(getIdAgent(ic.getSignatureEnseignant()));
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
                    donneesExercice.setCours(listeNewCoursTempo);
                    saveRevenus(ee, user, newIannee);
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
            saveRevenus(ee, user, newIannee);
        }
    }

    private void saveRevenus(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<InterfaceRevenu> listeNewRevenus = sortiesExercice.getListeRevenus();
        Vector<InterfaceRevenu> listeNewRevenusTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfaceRevenu ir : listeNewRevenus) {
            if (ir.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ir.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ir.setIdMonnaie(getIdMonnaie(ir.getSignatureMonnaie()));
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
                    donneesExercice.setRevenus(listeNewRevenusTempo);
                    saveCharges(ee, user, newIannee);
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
            saveCharges(ee, user, newIannee);
        }
    }

    private void saveCharges(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<InterfaceCharge> listeNewCharges = sortiesExercice.getListeCharges();
        Vector<InterfaceCharge> listeNewChargesTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfaceCharge ic : listeNewCharges) {
            if (ic.getBeta() == InterfaceMonnaie.BETA_MODIFIE || ic.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                ic.setIdMonnaie(getIdMonnaie(ic.getSignatureMonnaie()));
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
                    donneesExercice.setCharges(listeNewChargesTempo);
                    saveFrais(ee, user, newIannee);
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
            saveFrais(ee, user, newIannee);
        }
    }

    private void saveFrais(EcouteurEnregistrement ee, InterfaceUtilisateur user, InterfaceExercice annee) {
        Vector<InterfaceFrais> listeNewFrais = sortiesExercice.getListeFrais();
        Vector<InterfaceFrais> listeNewFraisTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (InterfaceFrais iff : listeNewFrais) {
            if (iff.getBeta() == InterfaceMonnaie.BETA_MODIFIE || iff.getBeta() == InterfaceMonnaie.BETA_NOUVEAU) {
                iff.setIdMonnaie(getIdMonnaie(iff.getSignatureMonnaie()));
                iff.setIdExercice(annee.getId());
                iff.setIdUtilisateur(user.getId());
                iff.setIdEntreprise(user.getIdEntreprise());
                //Il faut actualiser les liaisons aussi
                for(LiaisonClasseFrais lcf: iff.getLiaisonsClasses()){
                    lcf.setIdClasse(getIdClasse(lcf.getSignatureClasse()));
                    lcf.setNomClasse(getNomClasse(lcf.getSignatureClasse()));
                }
                for(LiaisonPeriodeFrais lcp: iff.getLiaisonsPeriodes()){
                    lcp.setIdPeriode(getIdClasse(lcp.getSignaturePeriode()));
                    lcp.setNomPeriode(getNomPeriode(lcp.getSignaturePeriode()));
                }
                iff.setBeta(InterfaceExercice.BETA_EXISTANT);
                listeNewFraisTempo.add(iff);
            }
        }
        if (!listeNewFraisTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewFraisTempo, UtilFees.DOSSIER_FRAIS, new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    System.out.println(message);
                    //Après enregistrement
                    ee.onDone("Enregistrés avec succès!");
                    donneesExercice.setFrais(listeNewFraisTempo);

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



































