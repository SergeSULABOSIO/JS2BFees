/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.UI;

import BEAN_BARRE_OUTILS.BarreOutils;
import BEAN_BARRE_OUTILS.Bouton;
import BEAN_BARRE_OUTILS.BoutonListener;
import ICONES.Icones;
import SOURCES.GESTIONNAIRES.GestionAnnee;
import SOURCES.CALLBACK.EcouteurExercice;
import SOURCES.Callback.EcouteurLongin;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback.EcouteurStandard;
import SOURCES.Interfaces.InterfaceAgent;
import SOURCES.Interfaces.InterfaceCharge;
import SOURCES.Interfaces.InterfaceClasse;
import SOURCES.Interfaces.InterfaceCours;
import SOURCES.Interfaces.InterfaceFrais;
import SOURCES.Interfaces.InterfaceMonnaie;
import SOURCES.Interfaces.InterfaceRevenu;
import SOURCES.Interfaces.InterfaceUtilisateur;
import SOURCES.OBJETS.Exercice;
import SOURCES.Objets.Entreprise;
import SOURCES.Objets.FileManager;
import SOURCES.Objets.Paiement;
import SOURCES.Objets.Session;
import SOURCES.Objets.Utilisateur;
import SOURCES.UTILITAIRES.UtilFees;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 *
 * @author user
 */
public class Principal extends javax.swing.JFrame {

    public BarreOutils bOutils = null;
    public Bouton btAnnee, btInscription, btFacture, btAyantDroit, btTresorerie, btPaie, btLitige, btLicence;
    private Icones icones = null;
    private FileManager fm = new FileManager("http://www.visiterlardc.com/s2b/processeurS2B.php");
    private JFrame moi = null;
    private Session session;
    private EcouteurExercice ecouteurExercice = null;

    //Les GEstionnaires
    public GestionAnnee gestionAnnee = null;

    /**
     * Creates new form Principal
     */
    public Principal() {
        initComponents();
        panOutils.setVisible(false);
        moi = this;
        fm.fm_setEcouteurFenetre(moi);  // On écoute désormais les mouvements de la fenetre
        lf_initEcouteurExercice();
        lf_initIcones();
        lf_construirePageLogin();
        loadUserSession();
    }

    private void updateListeExercice(String selectedExercice) {
        System.out.println("******************* ACTUALISER LISTE ANNEES ************************");
        fm.fm_ouvrirTout(0, Exercice.class, UtilFees.DOSSIER_ANNEE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                progressEtat.setVisible(false);
                progressEtat.setIndeterminate(false);
                System.out.println(message);
                System.out.println("LISTE D'ANNEES SCOLAIRES ACTUALISEE:");

                int nbRow = comboListeAnneesScolaires.getItemCount();
                for (int i = nbRow - 1; 0 < i; i--) {
                    comboListeAnneesScolaires.removeItemAt(i);
                }

                for (Object o : data) {
                    Exercice oAnnee = (Exercice) o;
                    System.out.println(" * " + oAnnee.toString());
                    if (!listeExerciceContient(oAnnee.getNom())) {
                        comboListeAnneesScolaires.addItem(oAnnee.getNom());
                    }
                }
                comboListeAnneesScolaires.setSelectedItem(selectedExercice);
            }

            @Override
            public void onError(String message) {
                System.err.println(message);
                progressEtat.setVisible(false);
                progressEtat.setIndeterminate(false);
            }

            @Override
            public void onProcessing(String string) {
                progressEtat.setVisible(true);
                progressEtat.setIndeterminate(true);
            }
        });
    }

    private void lf_initEcouteurExercice() {
        ecouteurExercice = new EcouteurExercice() {
            @Override
            public void onExerciceAdded(String nomExercice) {
                System.out.println("EXERCICE ADDED");
                updateListeExercice(nomExercice);
            }

            @Override
            public void onExerciceDeleteded(String nom) {
                if (listeExerciceContient(nom)) {
                    comboListeAnneesScolaires.removeItem(nom);
                }
            }

        };
    }

    private boolean listeExerciceContient(String nomAnnee) {
        for (int i = 0; i < comboListeAnneesScolaires.getItemCount(); i++) {
            if (nomAnnee.equals(comboListeAnneesScolaires.getItemAt(i))) {
                return true;
            }
        }
        return false;
    }

    private void loadUserSession() {
        if (fm != null) {
            fm.fm_loadSession(new EcouteurLongin() {
                @Override
                public void onConnected(String string, Session session) {
                    lf_chargerEspaceAdmin(session);
                }

                @Override
                public void onEchec(String string) {
                    lf_construirePageLogin();
                }

                @Override
                public void onProcessing(String string) {
                    labLoginMessage.setText("Ouverture de la session...");
                    lf_progress(true, "Patientez...", progressLogin);
                    lf_progress(true, "Patientez...", progressEtat);
                }
            });
        } else {
            lf_construirePageLogin();
        }
    }

    private void lf_logout() {
        if (fm != null) {
            fm.fm_logout(new EcouteurStandard() {
                @Override
                public void onDone(String string) {
                    lf_construirePageLogin();
                }

                @Override
                public void onError(String string) {
                    lf_progress(false, "", progressEtat);
                }

                @Override
                public void onProcessing(String string) {
                    lf_progress(true, "Déconnexion...", progressEtat);
                }
            });
        } else {
            System.err.println("Le gestionnaire de fichier est introuvable");
        }
    }

    private void lf_progress(boolean afficher, String message, JProgressBar progressBar) {
        progressBar.setIndeterminate(afficher);
        progressBar.setVisible(afficher);
        progressBar.setStringPainted(afficher);
        progressBar.setString(message);
    }

    private void lf_initIcones() {
        icones = new Icones();
        menuDeconnexion.setIcon(icones.getDéconnecté_01());
        menuQuitter.setIcon(icones.getSortie_01());
    }

    private void lf_construirePageLogin() {
        this.session = null;
        btLoginConnexion.setIcon(icones.getOk_01());
        chLoginEmail.setIcon(icones.getEmail_01());
        chLoginEmail.setText("");
        chLoginEmail.setTextInitial("Email");

        chLoginMotDePasse.setIcon(icones.getMot_de_passe_01());
        chLoginMotDePasse.setText("");
        chLoginMotDePasse.setTextInitial("Mot de passe");

        chLoginIDEcole.setIcon(icones.getEnseignant_01());
        chLoginIDEcole.setText("");
        chLoginIDEcole.setTextInitial("ID Ecole");

        panOutils.setVisible(false);
        barreEtat.setVisible(false);
        barreOutils.setVisible(false);
        progressLogin.setVisible(false);

        moi.setTitle(UtilFees.nomApplication);
        labLoginMessage.setText("Veuillez saisir vos identifiants");

        lf_progress(false, "", progressEtat);
        lf_progress(false, "", progressLogin);

        tabPrincipal.removeAll();
        tabPrincipal.add("Login", panLogin);
    }

    private void lf_contruireBarreEtats(Session session) {
        btEtatEntreprise.setIcon(icones.getClasse_01());
        btEtatUser.setIcon(icones.getUtilisateur_01());
        btEtatLicence.setIcon(icones.getAdresse_01());
        btEtatBackup.setIcon(icones.getServeur_01());
        barreEtat.setVisible(true);

        lf_construireListeAnneesScolaires();
        lf_construireBoutons();

        Utilisateur user = session.getUtilisateur();
        String texteTitre = "";
        if (user != null) {
            String noms = user.getNom() + " " + user.getPostnom() + " " + user.getPrenom();
            btEtatUser.setText(user.getPrenom());
            texteTitre += UtilFees.nomApplication + " - " + noms;
        } else {
            btEtatUser.setVisible(false);
        }

        Entreprise ese = session.getEntreprise();
        if (ese != null) {
            btEtatEntreprise.setText(ese.getNom());
            texteTitre += " - " + ese.getNom();
        } else {
            btEtatEntreprise.setVisible(false);
        }

        Paiement licence = session.getPaiement();
        if (licence != null) {
            String dateExpirationL = UtilFees.convertDatePaiement(licence.getDateExpiration()).toLocaleString();
            btEtatLicence.setText(dateExpirationL);
            texteTitre += " - Echéance: " + dateExpirationL;
        } else {
            btEtatLicence.setVisible(false);
        }
        btEtatBackup.setText("<Pas disponible>");

        moi.setTitle(texteTitre);

        lf_progress(false, "", progressEtat);
    }

    private void appliquerDroit() {
        if (btAnnee != null && btAyantDroit != null && btFacture != null && btInscription != null && btLicence != null && btLitige != null && btPaie != null && btTresorerie != null) {
            if (session != null) {
                Utilisateur user = session.getUtilisateur();
                System.out.println("Droit Année: " + UtilFees.getSDroitAccess(user.getDroitExercice()));
                System.out.println("Droit facture: " + UtilFees.getSDroitAccess(user.getDroitFacture()));
                System.out.println("Droit Inscription: " + UtilFees.getSDroitAccess(user.getDroitInscription()));
                System.out.println("Droit Litige: " + UtilFees.getSDroitAccess(user.getDroitLitige()));
                System.out.println("Droit Paie: " + UtilFees.getSDroitAccess(user.getDroitPaie()));
                System.out.println("Droit Tresorerie: " + UtilFees.getSDroitAccess(user.getDroitTresorerie()));
                System.out.println("Droit User: " + UtilFees.getSDroitAccess(user.getDroitUtilisateur()));

                if (comboListeAnneesScolaires.getSelectedIndex() == 0) {    //tentative de création
                    btAnnee.setText("Démarrer", 12, true);
                    btAnnee.setInfosBulle("Commencer par créer un Exercice (Année scolaire ou Année académique)");
                    btAnnee.setIcone(icones.getDémarrer_03());
                    
                    if (user.getDroitExercice() == InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btAnnee.setVisible(false);
                    } else {
                        btAnnee.setVisible(true);
                    }
                    btInscription.setVisible(false);
                    btFacture.setVisible(false);
                    btLitige.setVisible(false);
                    btPaie.setVisible(false);
                    btTresorerie.setVisible(false);
                } else {  //tentative de modification ou suppression
                    btAnnee.setText("Exercice", 12, true);
                    btAnnee.setInfosBulle("Ouvrir l'Exercice séléctionné");
                    btAnnee.setIcone(icones.getCalendrier_03());
                    
                    if (user.getDroitExercice() == InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btAnnee.setVisible(false);
                    } else {
                        btAnnee.setVisible(true);
                    }
                    if (user.getDroitInscription()== InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btInscription.setVisible(false);
                    }else{
                        btInscription.setVisible(true);
                    }
                    if (user.getDroitFacture()== InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btFacture.setVisible(false);
                    }else{
                        btFacture.setVisible(true);
                    }
                    if (user.getDroitLitige()== InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btLitige.setVisible(false);
                    }else{
                        btLitige.setVisible(true);
                    }
                    if (user.getDroitPaie()== InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btPaie.setVisible(false);
                    }else{
                        btPaie.setVisible(true);
                    }
                    if (user.getDroitTresorerie()== InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btTresorerie.setVisible(false);
                    }else{
                        btTresorerie.setVisible(true);
                    }
                }

                /*
            
            
            
            if (bOutils != null) {
                bOutils.SupprimerBouton(btLicence);
                bOutils.SupprimerBouton(btAnnee);
                bOutils.SupprimerBouton(btAyantDroit);
                bOutils.SupprimerBouton(btFacture);
                bOutils.SupprimerBouton(btInscription);
                bOutils.SupprimerBouton(btInscription);
                bOutils.SupprimerBouton(btLitige);
                bOutils.SupprimerBouton(btPaie);
                bOutils.SupprimerBouton(btTresorerie);
            }

            
            if (comboListeAnneesScolaires.getSelectedIndex() == 0) {    //tentative de création
                if (user.getDroitExercice() == InterfaceUtilisateur.DROIT_CONTROLER) {
                    bOutils.AjouterBouton(btAnnee);
                }
            } else {  //tentative de modification ou suppression
                if (user.getDroitExercice() != InterfaceUtilisateur.DROIT_PAS_ACCES) {
                    bOutils.AjouterBouton(btAnnee);
                }
            }

            if (comboListeAnneesScolaires.getSelectedIndex() > 0) {
                String selectedExerc = comboListeAnneesScolaires.getSelectedItem() + "";
                fm.fm_ouvrirTout(0, Exercice.class, UtilFees.DOSSIER_ANNEE, new EcouteurOuverture() {
                    @Override
                    public void onDone(String string, Vector data) {
                        lf_progress(false, "", progressEtat);
                        for (Object o : data) {
                            Exercice exerc = (Exercice) o;
                            if (exerc.getNom().equals(selectedExerc)) {
                                btAnnee.setIcone(icones.getCalendrier_02());
                                btAnnee.setText("Exercice", 9, false);
                                btAnnee.setInfosBulle("Paramètres de l'exercice " + exerc.getNom());

                                if (user.getDroitFacture() != InterfaceUtilisateur.DROIT_PAS_ACCES) {
                                    bOutils.AjouterBouton(btFacture);
                                }
                                if (user.getDroitInscription() != InterfaceUtilisateur.DROIT_PAS_ACCES) {
                                    bOutils.AjouterBouton(btInscription);
                                }
                                if (user.getDroitPaie() != InterfaceUtilisateur.DROIT_PAS_ACCES) {
                                    bOutils.AjouterBouton(btPaie);
                                }
                                if (user.getDroitLitige() != InterfaceUtilisateur.DROIT_PAS_ACCES) {
                                    bOutils.AjouterBouton(btLitige);
                                }
                                if (user.getDroitTresorerie() != InterfaceUtilisateur.DROIT_PAS_ACCES) {
                                    bOutils.AjouterBouton(btTresorerie);
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onError(String string) {
                        lf_progress(false, "", progressEtat);
                    }

                    @Override
                    public void onProcessing(String string) {
                        lf_progress(true, "", progressEtat);
                    }
                });
            }
            
            
            
            
            
            **/
            }
        }
    }

    public void lf_construireBoutons() {
        bOutils = new BarreOutils(barreOutils);

        btLicence = new Bouton(12, "Licence", "Payer votre abonnement", true, icones.getAdresse_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                UtilFees.lancerPageWebAdmin();
            }
        });
        btLicence.setForeground(Color.ORANGE);
        btLicence.setBackground(Color.BLUE);
        
        btAnnee = new Bouton(12, "Démarrer", "Créer une voulle année scolaire", true, icones.getDémarrer_03(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                if (comboListeAnneesScolaires.getSelectedIndex() == 0) {
                    //Nouvelle année scolaire
                    gestionAnnee = new GestionAnnee(fm, tabPrincipal, progressEtat, session.getEntreprise(), session.getUtilisateur(), null, ecouteurExercice);
                    gestionAnnee.ga_setDonnees(null, new Vector<InterfaceAgent>(), new Vector<InterfaceCharge>(), new Vector<InterfaceClasse>(), new Vector<InterfaceCours>(), new Vector<InterfaceFrais>(), new Vector<InterfaceMonnaie>(), new Vector<InterfaceRevenu>(), new Vector<>());
                    gestionAnnee.ga_initUI("Nouvel Exercice");
                } else {
                    //On ouvre une année scolaire existante
                    System.out.println("Modification et/ou Suppression de l'année scolaire " + comboListeAnneesScolaires.getSelectedItem());
                    gestionAnnee = new GestionAnnee(fm, tabPrincipal, progressEtat, session.getEntreprise(), session.getUtilisateur(), null, ecouteurExercice);
                    gestionAnnee.ga_setDonneesFromFileManager(comboListeAnneesScolaires.getSelectedItem() + "");
                }

            }
        });
        btAnnee.setForeground(Color.ORANGE);
        
        btAyantDroit = new Bouton(12, "Ayant D.", "Les Ayant-droits", true, icones.getUtilisateur_03(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {

            }
        });
        btAyantDroit.setForeground(Color.ORANGE);
        
        btFacture = new Bouton(12, "Facture", "Paiement des frais", true, icones.getFacture_03(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {

            }
        });
        btFacture.setForeground(Color.ORANGE);
        
        btInscription = new Bouton(12, "Inscription", "Inscriptin des étudiants", true, icones.getFacture_03(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {

            }
        });
        btInscription.setForeground(Color.ORANGE);
        
        btPaie = new Bouton(12, "Paie", "Paie des Agents de l'établissement", true, icones.getFacture_03(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {

            }
        });
        btPaie.setForeground(Color.ORANGE);

        btTresorerie = new Bouton(12, "Trésorerie", "La trésorerie (Encaissements & Décaissements)", true, icones.getFacture_03(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {

            }
        });
        btTresorerie.setForeground(Color.ORANGE);
        
        btLitige = new Bouton(12, "Litiges", "Litiges et reglèment des dettes", true, icones.getFournisseur_03(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {

            }
        });
        btLitige.setForeground(Color.ORANGE);
        
        bOutils.AjouterBouton(btLicence);
        bOutils.AjouterSeparateur();
        bOutils.AjouterBouton(btAnnee);
        bOutils.AjouterBouton(btInscription);
        bOutils.AjouterBouton(btFacture);
        bOutils.AjouterBouton(btLitige);
        bOutils.AjouterBouton(btPaie);
        bOutils.AjouterBouton(btTresorerie);
        
        panOutils.setVisible(true);
        barreOutils.setVisible(true);
        
        appliquerDroit();
    }

    private void lf_chargerEspaceAdmin(Session session) {
        this.session = session;
        tabPrincipal.remove(panLogin);
        lf_contruireBarreEtats(session);
        labLoginMessage.setForeground(Color.darkGray);
        labLoginMessage.setText("Prêt.");
        lf_progress(false, "", progressLogin);
        lf_progress(false, "", progressEtat);
        labInfoEtat.setText("Connecté!");
    }

    private void lf_construireListeAnneesScolaires() {
        //on enlèves les autres elements du combo, sauf le premier element
        comboListeAnneesScolaires.removeAllItems();
        comboListeAnneesScolaires.addItem("-- Liste d'Années --");

        System.out.println("Liste d'années: Construction");

        fm.fm_ouvrirTout(0, Exercice.class, UtilFees.DOSSIER_ANNEE, new EcouteurOuverture() {
            @Override
            public void onDone(String message, Vector data) {
                lf_progress(false, "", progressEtat);
                System.out.println("GESTION ANNEE: " + message);
                for (Object oRetrieved : data) {
                    Exercice annee = (Exercice) oRetrieved;
                    comboListeAnneesScolaires.addItem(annee.getNom());
                }
                System.out.println("Liste d'années: Fin");
            }

            @Override
            public void onError(String string) {
                lf_progress(false, "", progressEtat);
            }

            @Override
            public void onProcessing(String string) {
                lf_progress(true, "Pateintez...", progressEtat);
            }
        });
    }

    private void lf_login() {
        String loginEmail = chLoginEmail.getText();
        String loginPasseW = chLoginMotDePasse.getText();
        String loginID = chLoginIDEcole.getText();

        if (loginEmail.length() == 0 || loginPasseW.length() == 0 || loginID.length() == 0) {
            labLoginMessage.setForeground(Color.red);
            labLoginMessage.setText("Un ou plusieurs champs sont vides!");
            lf_progress(false, "", progressLogin);
        } else {
            fm.fm_login(loginID, loginEmail, loginPasseW, new EcouteurLongin() {
                @Override
                public void onConnected(String message, Session session) {
                    if (session != null) {
                        lf_chargerEspaceAdmin(session);
                    } else {
                        labLoginMessage.setForeground(Color.RED);
                        labLoginMessage.setText("Identifiants non reconnus!");
                        lf_progress(false, "", progressLogin);
                    }
                }

                @Override
                public void onEchec(String string) {
                    labLoginMessage.setForeground(Color.RED);
                    labLoginMessage.setText("Connexion impossible");
                    lf_progress(false, "", progressLogin);
                }

                @Override
                public void onProcessing(String message) {
                    labLoginMessage.setForeground(Color.BLACK);
                    labLoginMessage.setText("Veuillez patienter...");
                    lf_progress(true, "Patientez...", progressLogin);
                    System.out.println(message);
                }
            });
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barreEtat = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        comboListeAnneesScolaires = new javax.swing.JComboBox<>();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        btEtatEntreprise = new javax.swing.JButton();
        btEtatUser = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btEtatLicence = new javax.swing.JButton();
        btEtatBackup = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        labInfoEtat = new javax.swing.JLabel();
        progressEtat = new javax.swing.JProgressBar();
        tabPrincipal = new javax.swing.JTabbedPane();
        panLogin = new javax.swing.JPanel();
        panLoginInfos = new javax.swing.JPanel();
        btLoginConnexion = new javax.swing.JButton();
        labLoginNouveauCompte = new javax.swing.JLabel();
        chLoginEmail = new UI.JS2bTextField();
        chLoginMotDePasse = new UI.JS2BPassword();
        chLoginIDEcole = new UI.JS2bTextField();
        panLoginMessage = new javax.swing.JPanel();
        labLoginMessage = new javax.swing.JLabel();
        progressLogin = new javax.swing.JProgressBar();
        panOutils = new javax.swing.JPanel();
        barreOutils = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuDeconnexion = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuQuitter = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        barreEtat.setBackground(java.awt.Color.orange);

        jToolBar1.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);

        comboListeAnneesScolaires.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Liste d'Années --" }));
        comboListeAnneesScolaires.setToolTipText("Liste d'années scolaires actuellement stockées en base de données");
        comboListeAnneesScolaires.setPreferredSize(new java.awt.Dimension(200, 22));
        comboListeAnneesScolaires.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboListeAnneesScolairesItemStateChanged(evt);
            }
        });
        comboListeAnneesScolaires.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboListeAnneesScolairesActionPerformed(evt);
            }
        });
        jToolBar1.add(comboListeAnneesScolaires);
        jToolBar1.add(jSeparator6);

        btEtatEntreprise.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        btEtatEntreprise.setText("Ecole");
        btEtatEntreprise.setToolTipText("Ecole actuellement connectée");
        btEtatEntreprise.setFocusable(false);
        btEtatEntreprise.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btEtatEntreprise.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btEtatEntreprise);

        btEtatUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        btEtatUser.setText("User");
        btEtatUser.setToolTipText("Utilisateur actuel");
        btEtatUser.setFocusable(false);
        btEtatUser.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btEtatUser.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btEtatUser);
        jToolBar1.add(jSeparator2);

        btEtatLicence.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        btEtatLicence.setText("Licence");
        btEtatLicence.setToolTipText("Votre lience actuelle. Cliquer pour ouvrir la page web d'administration pour payer.");
        btEtatLicence.setFocusable(false);
        btEtatLicence.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btEtatLicence.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btEtatLicence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEtatLicenceActionPerformed(evt);
            }
        });
        jToolBar1.add(btEtatLicence);

        btEtatBackup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        btEtatBackup.setText("Backup");
        btEtatBackup.setToolTipText("Sauvegarde des données sur le serveur distant. Cliquer pour relancer le chargement.");
        btEtatBackup.setFocusable(false);
        btEtatBackup.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btEtatBackup.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btEtatBackup);
        jToolBar1.add(jSeparator5);

        labInfoEtat.setText("Info");
        jToolBar1.add(labInfoEtat);

        progressEtat.setIndeterminate(true);
        jToolBar1.add(progressEtat);

        javax.swing.GroupLayout barreEtatLayout = new javax.swing.GroupLayout(barreEtat);
        barreEtat.setLayout(barreEtatLayout);
        barreEtatLayout.setHorizontalGroup(
            barreEtatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, barreEtatLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        barreEtatLayout.setVerticalGroup(
            barreEtatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barreEtatLayout.createSequentialGroup()
                .addGap(0, 5, Short.MAX_VALUE)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );

        panLoginInfos.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btLoginConnexion.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        btLoginConnexion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        btLoginConnexion.setText("CONNEXION");
        btLoginConnexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLoginConnexionActionPerformed(evt);
            }
        });
        btLoginConnexion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                btLoginConnexionKeyReleased(evt);
            }
        });

        labLoginNouveauCompte.setForeground(new java.awt.Color(0, 0, 204));
        labLoginNouveauCompte.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labLoginNouveauCompte.setText("Nouveau compte");
        labLoginNouveauCompte.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labLoginNouveauCompteMouseClicked(evt);
            }
        });

        chLoginEmail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        chLoginEmail.setTextInitial("Email");
        chLoginEmail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                chLoginEmailKeyReleased(evt);
            }
        });

        chLoginMotDePasse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        chLoginMotDePasse.setTextInitial("Mot de passe");
        chLoginMotDePasse.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                chLoginMotDePasseKeyReleased(evt);
            }
        });

        chLoginIDEcole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        chLoginIDEcole.setTextInitial("ID de l'école");
        chLoginIDEcole.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                chLoginIDEcoleKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout panLoginInfosLayout = new javax.swing.GroupLayout(panLoginInfos);
        panLoginInfos.setLayout(panLoginInfosLayout);
        panLoginInfosLayout.setHorizontalGroup(
            panLoginInfosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLoginInfosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panLoginInfosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btLoginConnexion, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(chLoginEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chLoginMotDePasse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chLoginIDEcole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labLoginNouveauCompte, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panLoginInfosLayout.setVerticalGroup(
            panLoginInfosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLoginInfosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chLoginEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(chLoginMotDePasse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(chLoginIDEcole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btLoginConnexion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labLoginNouveauCompte)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        labLoginMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labLoginMessage.setText("Veuillez saisir vos identifiants");

        javax.swing.GroupLayout panLoginMessageLayout = new javax.swing.GroupLayout(panLoginMessage);
        panLoginMessage.setLayout(panLoginMessageLayout);
        panLoginMessageLayout.setHorizontalGroup(
            panLoginMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLoginMessageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panLoginMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labLoginMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressLogin, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                .addContainerGap())
        );
        panLoginMessageLayout.setVerticalGroup(
            panLoginMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLoginMessageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labLoginMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panLoginLayout = new javax.swing.GroupLayout(panLogin);
        panLogin.setLayout(panLoginLayout);
        panLoginLayout.setHorizontalGroup(
            panLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLoginLayout.createSequentialGroup()
                .addContainerGap(229, Short.MAX_VALUE)
                .addGroup(panLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panLoginInfos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panLoginMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(230, Short.MAX_VALUE))
        );
        panLoginLayout.setVerticalGroup(
            panLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLoginLayout.createSequentialGroup()
                .addContainerGap(87, Short.MAX_VALUE)
                .addComponent(panLoginInfos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panLoginMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(76, Short.MAX_VALUE))
        );

        tabPrincipal.addTab("Login", panLogin);

        panOutils.setBackground(new java.awt.Color(0, 0, 153));
        panOutils.setForeground(new java.awt.Color(255, 255, 255));

        barreOutils.setFloatable(false);
        barreOutils.setForeground(new java.awt.Color(153, 153, 153));
        barreOutils.setOrientation(javax.swing.SwingConstants.VERTICAL);
        barreOutils.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture02.png"))); // NOI18N
        jButton1.setToolTipText("Elève");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barreOutils.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture02.png"))); // NOI18N
        jButton2.setToolTipText("Inscription");
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barreOutils.add(jButton2);

        javax.swing.GroupLayout panOutilsLayout = new javax.swing.GroupLayout(panOutils);
        panOutils.setLayout(panOutilsLayout);
        panOutilsLayout.setHorizontalGroup(
            panOutilsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barreOutils, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        panOutilsLayout.setVerticalGroup(
            panOutilsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barreOutils, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jMenu1.setText("Fichier");

        menuDeconnexion.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK));
        menuDeconnexion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        menuDeconnexion.setText("Déconnexion");
        menuDeconnexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuDeconnexionActionPerformed(evt);
            }
        });
        jMenu1.add(menuDeconnexion);
        jMenu1.add(jSeparator1);

        menuQuitter.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        menuQuitter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        menuQuitter.setText("Quitter");
        menuQuitter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuQuitterActionPerformed(evt);
            }
        });
        jMenu1.add(menuQuitter);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barreEtat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panOutils, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(tabPrincipal))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panOutils, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(0, 0, 0)))
                .addComponent(barreEtat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuDeconnexionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuDeconnexionActionPerformed
        // TODO add your handling code here:
        lf_logout();
    }//GEN-LAST:event_menuDeconnexionActionPerformed

    private void menuQuitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuQuitterActionPerformed
        // TODO add your handling code here:
        System.exit(3);
    }//GEN-LAST:event_menuQuitterActionPerformed

    private void btLoginConnexionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLoginConnexionActionPerformed
        // TODO add your handling code here:
        lf_login();
    }//GEN-LAST:event_btLoginConnexionActionPerformed

    private void btLoginConnexionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btLoginConnexionKeyReleased
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            lf_login();
        }
    }//GEN-LAST:event_btLoginConnexionKeyReleased

    private void chLoginEmailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_chLoginEmailKeyReleased
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            lf_login();
        }
    }//GEN-LAST:event_chLoginEmailKeyReleased

    private void chLoginMotDePasseKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_chLoginMotDePasseKeyReleased
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            lf_login();
        }
    }//GEN-LAST:event_chLoginMotDePasseKeyReleased

    private void chLoginIDEcoleKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_chLoginIDEcoleKeyReleased
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            lf_login();
        }
    }//GEN-LAST:event_chLoginIDEcoleKeyReleased

    private void labLoginNouveauCompteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labLoginNouveauCompteMouseClicked
        // TODO add your handling code here:
        UtilFees.lancerPageWebAdmin();
    }//GEN-LAST:event_labLoginNouveauCompteMouseClicked

    private void btEtatLicenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEtatLicenceActionPerformed
        // TODO add your handling code here:
        UtilFees.lancerPageWebAdmin();
    }//GEN-LAST:event_btEtatLicenceActionPerformed

    private void comboListeAnneesScolairesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboListeAnneesScolairesItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            System.out.println("Combo: Selection - " + evt.getItem());
            appliquerDroit();
        }

    }//GEN-LAST:event_comboListeAnneesScolairesItemStateChanged

    private void comboListeAnneesScolairesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboListeAnneesScolairesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboListeAnneesScolairesActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel barreEtat;
    private javax.swing.JToolBar barreOutils;
    private javax.swing.JButton btEtatBackup;
    private javax.swing.JButton btEtatEntreprise;
    private javax.swing.JButton btEtatLicence;
    private javax.swing.JButton btEtatUser;
    private javax.swing.JButton btLoginConnexion;
    private UI.JS2bTextField chLoginEmail;
    private UI.JS2bTextField chLoginIDEcole;
    private UI.JS2BPassword chLoginMotDePasse;
    private javax.swing.JComboBox<String> comboListeAnneesScolaires;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel labInfoEtat;
    private javax.swing.JLabel labLoginMessage;
    private javax.swing.JLabel labLoginNouveauCompte;
    private javax.swing.JMenuItem menuDeconnexion;
    private javax.swing.JMenuItem menuQuitter;
    private javax.swing.JPanel panLogin;
    private javax.swing.JPanel panLoginInfos;
    private javax.swing.JPanel panLoginMessage;
    private javax.swing.JPanel panOutils;
    private javax.swing.JProgressBar progressEtat;
    private javax.swing.JProgressBar progressLogin;
    private javax.swing.JTabbedPane tabPrincipal;
    // End of variables declaration//GEN-END:variables
}
