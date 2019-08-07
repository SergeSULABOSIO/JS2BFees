/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SOURCES.GESTIONNAIRES;

import SOURCES.CallBack_Tresorerie.EcouteurActualisationTresorerie;
import SOURCES.CallBack_Tresorerie.EcouteurTresorerie;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Objets.FileManager;
import SOURCES.UI_Tresorerie.PanelTresorerie;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires_Tresorerie.DataTresorerie;
import SOURCES.Utilitaires_Tresorerie.DonneesTresorerie;
import SOURCES.Utilitaires_Tresorerie.ParametreTresorerie;
import SOURCES.Utilitaires_Tresorerie.SortiesTresorerie;
import Source.Callbacks.EcouteurEnregistrement;
import Source.Callbacks.EcouteurStandard;
import Source.Interface.InterfaceDecaissement;
import Source.Interface.InterfaceEncaissement;
import Source.Interface.InterfacePaiement;
import Source.Objet.Agent;
import Source.Objet.Charge;
import Source.Objet.CouleurBasique;
import Source.Objet.Decaissement;
import Source.Objet.Encaissement;
import Source.Objet.Entreprise;
import Source.Objet.Exercice;
import Source.Objet.Fiche;
import Source.Objet.Frais;
import Source.Objet.Monnaie;
import Source.Objet.Paiement;
import Source.Objet.Revenu;
import Source.Objet.Utilisateur;
import java.util.Vector;
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
    
    public Exercice exercice = null;
    public FileManager fm;
    
    public Vector<Monnaie> monnaies = new Vector<>();
    public Vector<Revenu> revenus = new Vector<>();
    public Vector<Charge> charges = new Vector<>();
    public Vector<Encaissement> encaissements = new Vector<>();
    public Vector<Decaissement> decaissements = new Vector<>();
    
    public Vector<Frais> frais = new Vector<>();
    public Vector<Fiche> fiches = new Vector<>();
    public Vector<Agent> agents = new Vector<>();
    public Vector<Paiement> paiements = new Vector<>();
    public CouleurBasique couleurBasique;
    public String selectedAnnee;
    public boolean deleteCurrentTab = true;
    
    public GestionTresorerie(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
    }
    
    private DataTresorerie getData() {
        ParametreTresorerie parametreTresorerie = new ParametreTresorerie(entreprise, exercice, utilisateur, monnaies, revenus, charges);
        DonneesTresorerie donneesTresorerie = new DonneesTresorerie(encaissements, decaissements);
        System.out.println("getData!");
        return new DataTresorerie(donneesTresorerie, parametreTresorerie);
    }

    
    public void gt_setDonneesFromFileManager(String selectedAnnee, boolean deleteCurrentTab) {
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
        fm.fm_ouvrirTout(0, Revenu.class, UtilFees.DOSSIER_REVENU, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Revenu revenu = (Revenu) o;
                    if (revenu.getIdExercice() == exercice.getId()) {
                        revenus.add(revenu);
                        System.out.println(" * " + revenu.toString());
                    }
                }
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
    
    private void loadPaiements() {
        paiements.removeAllElements();
        fm.fm_ouvrirTout(0, Paiement.class, UtilFees.DOSSIER_PAIEMENT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Paiement paiement = (Paiement) o;
                    if (paiement.getIdExercice() == exercice.getId()) {
                        paiements.add(paiement);
                        System.out.println(" * " + paiement.toString());
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
        frais.removeAllElements();
        fm.fm_ouvrirTout(0, Frais.class, UtilFees.DOSSIER_FRAIS, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Frais ff = (Frais) o;
                    if (ff.getIdExercice() == exercice.getId()) {
                        frais.add(ff);
                        System.out.println(" * " + ff.toString());
                    }
                }
                loadFichesDePaie();
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
    
    private void loadFichesDePaie() {
        fiches.removeAllElements();
        fm.fm_ouvrirTout(0, Fiche.class, UtilFees.DOSSIER_FICHE_DE_PAIE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Fiche ff = (Fiche) o;
                    if (ff.getIdExercice() == exercice.getId()) {
                        fiches.add(ff);
                        System.out.println(" * " + ff.toString());
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
                    Agent ff = (Agent) o;
                    if (ff.getIdExercice() == exercice.getId()) {
                        agents.add(ff);
                        System.out.println(" * " + ff.toString());
                    }
                }
                initUI(NOM);
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
                    if (charge.getIdExercice() == exercice.getId()) {
                        charges.add(charge);
                        System.out.println(" * " + charge.toString());
                    }
                }
                loadEncaissements();
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
    
    private void loadEncaissements() {
        encaissements.removeAllElements();
        fm.fm_ouvrirTout(0, Encaissement.class, UtilFees.DOSSIER_ENCAISSEMENT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Encaissement encaissement = (Encaissement) o;
                    if (encaissement.getIdExercice() == exercice.getId()) {
                        encaissements.add(encaissement);
                        System.out.println(" * " + encaissement.toString());
                    }
                }
                loadDecaissements();
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
    
    private void loadDecaissements() {
        decaissements.removeAllElements();
        fm.fm_ouvrirTout(0, Decaissement.class, UtilFees.DOSSIER_DECAISSEMENT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Decaissement decaissement = (Decaissement) o;
                    if (decaissement.getIdExercice() == exercice.getId()) {
                        decaissements.add(decaissement);
                        System.out.println(" * " + decaissement.toString());
                    }
                }
                loadPaiements();
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
    
    private void saveEncaissements(SortiesTresorerie se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Encaissement> listeNewEncaissements = se.getListeEncaissements();
        Vector<Encaissement> listeNewEncaissementsTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Encaissement ia : listeNewEncaissements) {
            if(ia.getId() == -100){
                ia.setBeta(InterfaceEncaissement.BETA_EXISTANT);
                System.out.println("PAS D'ENREGISTREMENT POUR CECI !!!!!");
            }else if (ia.getBeta() == InterfaceEncaissement.BETA_MODIFIE || ia.getBeta() == InterfaceEncaissement.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdUtilisateur(user.getId());
                ia.setBeta(InterfaceEncaissement.BETA_EXISTANT);
                listeNewEncaissementsTempo.add(ia);
            }
        }
        if (!listeNewEncaissementsTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewEncaissementsTempo, UtilFees.DOSSIER_ENCAISSEMENT, new EcouteurStandard() {
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
    
    private void saveDecaissements(SortiesTresorerie se, EcouteurEnregistrement ee, Utilisateur user, Exercice annee) {
        Vector<Decaissement> listeNewDecaissements = se.getListeDecaissements();
        Vector<Decaissement> listeNewDecaissementsTempo = new Vector<>();
        //On précise qui est en train d'enregistrer cette donnée
        for (Decaissement ia : listeNewDecaissements) {
            if (ia.getBeta() == InterfaceDecaissement.BETA_MODIFIE || ia.getBeta() == InterfaceDecaissement.BETA_NOUVEAU) {
                ia.setIdExercice(annee.getId());
                ia.setIdUtilisateur(user.getId());
                ia.setBeta(InterfaceEncaissement.BETA_EXISTANT);
                listeNewDecaissementsTempo.add(ia);
            }
        }
        if (!listeNewDecaissementsTempo.isEmpty()) {
            fm.fm_enregistrer(0, listeNewDecaissementsTempo, UtilFees.DOSSIER_DECAISSEMENT, new EcouteurStandard() {
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
    
    private Frais getFrais(int idFrais){
        for(Frais ff: frais){
            if(ff.getId() == idFrais){
                return ff;
            }
        }
        return null;
    }
    
    private Monnaie getMonnaie(int idMonnaie){
        for(Monnaie mm: monnaies){
            if(mm.getId() == idMonnaie){
                return mm;
            }
        }
        return null;
    }
    
    private Agent getAgent(int idAgent){
        for(Agent mm: agents){
            if(mm.getId() == idAgent){
                return mm;
            }
        }
        return null;
    }
    
    
    
    private void convertirPaiementFrais(){
        paiements.stream().map((p) -> {
            int destination = InterfaceEncaissement.DESTINATION_BANQUE;
            if(p.getMode() == InterfacePaiement.MODE_CAISSE){
                destination = InterfaceEncaissement.DESTINATION_CAISSE;
            }
            Frais fra = getFrais(p.getIdFrais());
            int idMonnaie = -1;
            String nomFrais = "";
            String codeMonnaie = "";
            if(fra != null){
                idMonnaie = fra.getIdMonnaie();
                nomFrais = fra.getNom();
            }
            Monnaie mon = getMonnaie(idMonnaie);
            if(mon != null){
                codeMonnaie = mon.getCode();
            }
            String motif = nomFrais + " - " + p.getNomEleve();
            int idRevenu = -1;
            String nomRevenu = "";
            for(Revenu rr: revenus){
                if(rr.getNom().toLowerCase().contains("frais")){
                    nomRevenu = rr.getNom();
                    idRevenu = rr.getId();
                }
            }
            Encaissement newEncaissement = new Encaissement(-100, destination, p.getReference(), p.getDate(), p.getMontant(), idMonnaie, codeMonnaie, p.getNomDepositaire(), motif, idRevenu, nomRevenu, p.getIdExercice(), utilisateur.getId(), InterfaceEncaissement.BETA_EXISTANT);
            return newEncaissement;
        }).forEachOrdered((newEncaissement) -> {
            encaissements.add(newEncaissement);
        });
    }
    
    private void convertirPaiementSalaire(){
        fiches.stream().map((fichePaie) -> {
            int source = InterfaceEncaissement.DESTINATION_CAISSE;
            int idMonnaie = fichePaie.getIdMonnaie();
            String codeMonnaie = "";
            Monnaie mon = getMonnaie(idMonnaie);
            if(mon != null){
                codeMonnaie = mon.getCode();
            }
            String motif = "";
            String beneficiaire = "";
            Agent agentEncours = getAgent(fichePaie.getIdAgent());
            if(agentEncours != null){
                beneficiaire = agentEncours.getNom() + " " + agentEncours.getPostnom() + " " + agentEncours.getPrenom();
                motif = "Salaire " + fichePaie.getMois();
            }
            
            
            double avoire = 0;
            avoire += fichePaie.getAutresGains();
            avoire += fichePaie.getLogement();
            avoire += fichePaie.getSalaireBase();
            avoire += fichePaie.getTransport();
            
            double retenu = 0;
            retenu += fichePaie.getRetenu_ABSENCE();
            retenu += fichePaie.getRetenu_AVANCE_SALAIRE();
            retenu += fichePaie.getRetenu_CAFETARIAT();
            retenu += fichePaie.getRetenu_INSS();
            retenu += fichePaie.getRetenu_IPR();
            retenu += fichePaie.getRetenu_ORDINATEUR();
            retenu += fichePaie.getRetenu_SYNDICAT();
            
            double montant = avoire - retenu;
            
            int idCharge = -1;
            String nomCharge = "";
            for(Charge rr: charges){
                if(rr.getNom().toLowerCase().contains("salaire")){
                    nomCharge = rr.getNom();
                    idCharge = rr.getId();
                }
            }
            Decaissement decaissement = new Decaissement(-100, source, fichePaie.getId()+"", fichePaie.getDateEnregistrement(), montant, idMonnaie, codeMonnaie, beneficiaire, motif, idCharge, nomCharge, fichePaie.getIdExercice(), fichePaie.getIdUtilisateur(), InterfaceDecaissement.BETA_EXISTANT);
            return decaissement;
        }).forEachOrdered((newDecaissement) -> {
            decaissements.add(newDecaissement);
        });
    }

    private void initUI(String nomTab) {
        convertirPaiementFrais();
        convertirPaiementSalaire();
        
        panel = new PanelTresorerie(couleurBasique, progress, tabOnglet, getData(), new EcouteurTresorerie() {
            @Override
            public void onEnregistre(SortiesTresorerie st) {
                if (st != null) {
                    System.out.println("DANGER !!!!!! TRESORERIE: Enregistrement...");
                    action_save(st);
                }
            }

            @Override
            public void onDetruitElements(int idElement, int index) {
                System.out.println("DESTRUCTION DE: " + idElement + ", index: " + index);
                System.out.println("DANGER !!!!!! EXERCICE: Destruction de " + idElement + ", indice " + index);
                if (idElement != -1 && idElement != -100) {
                    switch (index) {
                        case 0://ENCAISSEMENT
                            fm.fm_supprimer(UtilFees.DOSSIER_ENCAISSEMENT, idElement);
                            break;
                        case 1://DECAISSEMENT
                            fm.fm_supprimer(UtilFees.DOSSIER_DECAISSEMENT, idElement);
                            break;
                        default:
                    }
                }
            }
        });
        
        panel.setEcouteurActualisationTresorerie(new EcouteurActualisationTresorerie() {
            @Override
            public DataTresorerie onRechargeDonneesEtParametres() {
                gt_setDonneesFromFileManager(selectedAnnee, false);
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





















































































































































































































































































































