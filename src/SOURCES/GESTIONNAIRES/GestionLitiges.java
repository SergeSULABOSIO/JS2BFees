/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.GESTIONNAIRES;

import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Objets.FileManager;
import SOURCES.UI.PanelLitige;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires.DonneesLitige;
import SOURCES.Utilitaires.ParametresLitige;
import Source.Callbacks.EcouteurCrossCanal;
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
import java.util.Vector;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

/**
 *
 * @author HP Pavilion
 */
public class GestionLitiges {

    public static String NOM = "LITIGE";
    public PanelLitige panel = null;
    public Entreprise entreprise;
    public Utilisateur utilisateur;
    public ParametresLitige parametreLitige;
    public DonneesLitige donneesLitige;
    public JTabbedPane tabOnglet;
    public JProgressBar progress;

    public Exercice exercice = null;
    public FileManager fm;
    public Vector<Classe> classes = new Vector<>();
    public Vector<Frais> frais = new Vector<>();
    public Vector<Eleve> eleves = new Vector<>();
    public Vector<Ayantdroit> ayantDroits = new Vector<>();
    public Vector<Monnaie> monnaies = new Vector<>();
    public Vector<Periode> periodes = new Vector<>();
    public Vector<Paiement> paiements = new Vector<>();
    public CouleurBasique couleurBasique;
    public String selectedAnnee;
    public Eleve eleveConcerned = null;
    
    public GestionLitiges(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = null;
    }
    
    public GestionLitiges(CouleurBasique couleurBasique, FileManager fm, JTabbedPane tabOnglet, JProgressBar progress, Entreprise entreprise, Utilisateur utilisateur, Eleve eleveConcerned) {
        this.couleurBasique = couleurBasique;
        this.fm = fm;
        this.progress = progress;
        this.tabOnglet = tabOnglet;
        this.utilisateur = utilisateur;
        this.entreprise = entreprise;
        this.eleveConcerned = eleveConcerned;
    }

    private void initParamsEtDonnees() {
        this.parametreLitige = new ParametresLitige(utilisateur.getId(), utilisateur.getNom() + " " + utilisateur.getPrenom(), entreprise, exercice, monnaies.firstElement(), monnaies, classes, frais, periodes);
        this.donneesLitige = new DonneesLitige(eleves, ayantDroits, paiements);
    }
    
    public void gl_setDonneesFromFileManager(String selectedAnnee) {
        this.selectedAnnee = selectedAnnee;
        if (fm != null) {
            boolean mustLoadData = true;
            int nbOnglets = tabOnglet.getComponentCount();
            for (int i = 0; i < nbOnglets; i++) {
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
        ayantDroits.removeAllElements();
        fm.fm_ouvrirTout(0, Ayantdroit.class, UtilFees.DOSSIER_AYANT_DROIT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Ayantdroit ayantdroit = (Ayantdroit) o;
                    if (ayantdroit.getIdExercice() == exercice.getId()) {
                        if(eleveConcerned != null){
                            if(ayantdroit.getSignatureEleve() == eleveConcerned.getSignature()){
                                ayantDroits.add(ayantdroit);
                            }
                        }else{
                            ayantDroits.add(ayantdroit);
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
                        if(eleveConcerned != null){
                            if(classe.getId() == eleveConcerned.getIdClasse()){
                                classes.add(classe);
                            }
                        }else{
                            classes.add(classe);
                        }
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
                loadPeriodes();
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

    private void loadPeriodes() {
        periodes.removeAllElements();
        fm.fm_ouvrirTout(0, Periode.class, UtilFees.DOSSIER_PERIODE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Periode classe = (Periode) o;
                    if (classe.getIdExercice() == exercice.getId()) {
                        periodes.add(classe);
                        System.out.println(" * " + classe.toString());
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

    private void loadPaiements() {
        paiements.removeAllElements();
        fm.fm_ouvrirTout(0, Paiement.class, UtilFees.DOSSIER_PAIEMENT, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                System.out.println(message);
                for (Object o : data) {
                    Paiement paiement = (Paiement) o;
                    if (paiement.getIdExercice() == exercice.getId()) {
                        if(eleveConcerned != null){
                            if(paiement.getIdEleve() == eleveConcerned.getId()){
                                paiements.add(paiement);
                            }
                        }else{
                            paiements.add(paiement);
                        }
                        System.out.println(" * " + paiement.toString());
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

    private void initUI(String nomTab) {
        initParamsEtDonnees();

        panel = new PanelLitige(couleurBasique, tabOnglet, donneesLitige, parametreLitige, progress, new EcouteurCrossCanal() {
            @Override
            public void onOuvrirPaiements(Eleve eleve) {
                new GestionPaiements(couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve).gl_setDonneesFromFileManager(selectedAnnee);
            }

            @Override
            public void onOuvrirInscription(Eleve eleve) {
                new GestionAdhesion(couleurBasique, fm, tabOnglet, progress, entreprise, utilisateur, eleve).gi_setDonneesFromFileManager(selectedAnnee);
            }

            @Override
            public void onOuvrirLitiges(Eleve eleve) {
                
            }
            
            
        });
        
        //Chargement du gestionnaire sur l'onglet
        tabOnglet.addTab(nomTab, panel);
        tabOnglet.setSelectedComponent(panel);
        progress.setVisible(false);
        progress.setIndeterminate(false);
    }

}
