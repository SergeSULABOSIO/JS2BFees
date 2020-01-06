/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.UI;

import BEAN_BARRE_OUTILS.BarreOutils;
import BEAN_BARRE_OUTILS.Bouton;
import BEAN_BARRE_OUTILS.BoutonListener;
import BEAN_BARRE_OUTILS.CustomBordure;
import ICONES.Icones;
import SOURCES.CALLBACK.EcouteurGestionExercice;
import SOURCES.CALLBACK.EcouteurGestionInscription;
import SOURCES.CALLBACK.EcouteurGestionLitige;
import SOURCES.CALLBACK.EcouteurGestionPaie;
import SOURCES.CALLBACK.EcouteurGestionTresorerie;
import SOURCES.Callback.EcouteurInternet;
import SOURCES.Callback.EcouteurLongin;
import SOURCES.Callback.EcouteurOuverture;
import SOURCES.Callback.EcouteurSuiviEdition;
import SOURCES.Callback.EcouteurSynchronisation;
import SOURCES.GESTIONNAIRES.GestionAdhesion;
import SOURCES.GESTIONNAIRES.GestionExercice;
import SOURCES.GESTIONNAIRES.GestionLitiges;
import SOURCES.GESTIONNAIRES.GestionSalaire;
import SOURCES.GESTIONNAIRES.GestionTresorerie;
import SOURCES.Objets.FileManager;
import SOURCES.Objets.PaiementLicence;
import SOURCES.Objets.Session;
import SOURCES.UTILITAIRES.UtilFees;
import SOURCES.Utilitaires.UtilFileManager;
import Source.Callbacks.EcouteurFreemium;
import Source.Callbacks.EcouteurStandard;
import Source.Interface.InterfaceUtilisateur;
import Source.Objet.Agent;
import Source.Objet.Charge;
import Source.Objet.Classe;
import Source.Objet.CouleurBasique;
import Source.Objet.Cours;
import Source.Objet.Entreprise;
import Source.Objet.Annee;
import Source.Objet.Frais;
import Source.Objet.Monnaie;
import Source.Objet.Revenu;
import Source.Objet.UtilObjet;
import Source.Objet.Utilisateur;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author user
 */
public class Principal extends javax.swing.JFrame {

    public BarreOutils bOutils = null;
    public Bouton btAnnee, btInscription, btTresorerie, btPaie, btLitige, btLicence, btLogo, btUtilisateur, btBackup;
    private Icones icones = null;
    private FileManager fm = null;
    private JFrame moi = null;
    private Session session;
    
    private EcouteurGestionExercice ecouteurExercice = null;
    private EcouteurGestionInscription ecouteurGestionInscription = null;
    private EcouteurGestionLitige ecouteurGestionLitige = null;
    private EcouteurGestionPaie ecouteurGestionPaie = null;
    private EcouteurGestionTresorerie ecouteurGestionTresorerie = null;
    
    
    private CouleurBasique couleurBasique;

    //Les GEstionnaires
    public GestionExercice gestionAnnee = null;
    public GestionAdhesion gestionAdhesion = null;
    public GestionLitiges gestionLitiges = null;
    public GestionTresorerie gestionTresorerie = null;
    public GestionSalaire gestionSalaire = null;
    public Vector<Annee> listeExercTempo = new Vector<>();
    public int idExerciceSelected;
    public String texteTitre = "";
    public EcouteurFreemium ef = null;

    /**
     * Creates new form Principal
     */
    public Principal() {
        initComponents();
        lf_initCouleurs();
        panOutils.setVisible(false);
        moi = this;
        lf_initIcones();
        lf_initBtLogo();
        lf_initFileManaer();
        lf_initEcuteurFreemium();
        
        lf_initEcouteurExercice();
        lf_initEcouteurInscription();
        lf_initEcouteurLitige();
        lf_initEcouteurPaie();
        lf_initEcouteurTreoserie();
        
        lf_construirePageLogin();
        loadUserSession();
        initEffectSelectionTab();
    }

    private void lf_initBtLogo() {
        btLogo = new Bouton(12, "", "Votre logo - Cliquer pour ouvrir votre page web", true, icones.getSablier_03(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                UtilObjet.lancerPageWebAdmin(moi, session.getUtilisateur(), session.getEntreprise(), UtilObjet.ACTION_MODIFIER_LOGO, icones.getAlarme_02());
            }
        });
        btLogo.setForeground(couleurBasique.getCouleur_encadrement_selection());
    }

    private void initEffectSelectionTab() {
        tabPrincipal.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int tabIndex = tabPrincipal.getSelectedIndex();
                if (tabIndex != -1) {
                    
                    String titre = tabPrincipal.getTitleAt(tabIndex);
                    System.out.println("Tab: " + tabIndex + " : " + titre);
                    
                    //Effet selection Tab ANNEE
                    if (titre.startsWith(UtilObjet.DOSSIER_ANNEE)) {
                        if (btAnnee != null) {
                            btAnnee.setIsSelected(true);
                        }
                    } else {
                        if (btAnnee != null) {
                            btAnnee.setIsSelected(false);
                        }
                    }
                    
                    //Effet selection Tab INSCRIPTION
                    if (titre.startsWith(GestionAdhesion.NOM)) {
                        if (btInscription != null) {
                            btInscription.setIsSelected(true);
                        }
                    } else {
                        if (btInscription != null) {
                            btInscription.setIsSelected(false);
                        }
                    }
                    
                    //Effet selection Tab LITIGE
                    if (titre.startsWith(GestionLitiges.NOM)) {
                        if (btLitige != null) {
                            btLitige.setIsSelected(true);
                        }
                    } else {
                        if (btLitige != null) {
                            btLitige.setIsSelected(false);
                        }
                    }
                    
                    //Effet selection Tab SALAIRE
                    if (titre.startsWith(GestionSalaire.NOM)) {
                        if (btPaie != null) {
                            btPaie.setIsSelected(true);
                        }
                    } else {
                        if (btPaie != null) {
                            btPaie.setIsSelected(false);
                        }
                    }
                    
                    //Effet selection Tab TRESORERIE
                    if (titre.startsWith(GestionTresorerie.NOM)) {
                        if (btTresorerie != null) {
                            btTresorerie.setIsSelected(true);
                        }
                    } else {
                        if (btTresorerie != null) {
                            btTresorerie.setIsSelected(false);
                        }
                    }
                }
            }
        });
    }

    private void lf_initFileManaer() {
        fm = new FileManager("http://www.visiterlardc.com/s2b", "processeurS2B.php", btLogo.getBouton());
        fm.fm_setEcouteurFenetre(moi);  // On écoute désormais les mouvements de la fenetre
    }

    private void lf_initCouleurs() {
        couleurBasique = new CouleurBasique();
    }

    private void updateListeExercice(String selectedExercice) {
        //System.out.println("******************* ACTUALISER LISTE ANNEES ************************");
        int nbRow = comboListeAnneesScolaires.getItemCount();
        for (int i = nbRow - 1; 0 < i; i--) {
            comboListeAnneesScolaires.removeItemAt(i);
        }

        fm.fm_ouvrirTout(0, Annee.class, UtilObjet.DOSSIER_ANNEE, 1, 10000, new EcouteurOuverture() {

            @Override
            public boolean isCriteresRespectes(Object object) {
                return true;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                //System.out.println(message);
                Annee oAnnee = (Annee) data;
                //System.out.println(" * " + oAnnee.toString());
                if (!listeExerciceContient(oAnnee.getNom())) {
                    comboListeAnneesScolaires.addItem(oAnnee.getNom());
                }
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                progressEtat.setVisible(false);
                progressEtat.setIndeterminate(false);
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
        ecouteurExercice = new EcouteurGestionExercice() {
            @Override
            public void onExerciceAdded(String nomExercice) {
                //System.out.println("EXERCICE ADDED");
                updateListeExercice(nomExercice);
            }

            @Override
            public void onExerciceDeleteded(String nom) {
                if (listeExerciceContient(nom)) {
                    comboListeAnneesScolaires.removeItem(nom);
                }
            }

            @Override
            public void onClosed() {
                btAnnee.setIsClosed();
            }

        };
    }
    
    private void lf_initEcouteurInscription() {
        ecouteurGestionInscription = new EcouteurGestionInscription() {
            @Override
            public void onClosed() {
                btInscription.setIsClosed();
            }
        };
    }
    
    private void lf_initEcouteurLitige() {
        ecouteurGestionLitige = new EcouteurGestionLitige() {
            @Override
            public void onClosed() {
                btLitige.setIsClosed();
            }
        };
    }
    
    private void lf_initEcouteurPaie() {
        ecouteurGestionPaie = new EcouteurGestionPaie() {
            @Override
            public void onClosed() {
                btPaie.setIsClosed();
            }
        };
    }
    
    private void lf_initEcouteurTreoserie() {
        ecouteurGestionTresorerie = new EcouteurGestionTresorerie() {
            @Override
            public void onClosed() {
                btTresorerie.setIsClosed();
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
            //S'il y a connexion, il faut se reloger au serveur distant.
            //Sinon, on load la session enregistré localement si le User ne s'était pas encore déconnecté
            if (UtilFees.isNewWorkAvailable() == true) {
                lf_login();
            } else {
                fm.fm_loadSession(new EcouteurLongin() {
                    @Override
                    public void onConnected(String string, Session session) {
                        lf_chargerEspaceTravail(session);
                    }

                    @Override
                    public void onEchec(String string) {
                        lf_construirePageLogin();
                    }

                    @Override
                    public void onProcessing(String string) {
                        labLoginMessage.setText("Ouverture de la session...");
                        lf_progress(true, "Patientez...", progressLogin, -1);
                        lf_progress(true, "Patientez...", progressEtat, -1);
                    }
                });
            }
        } else {
            lf_construirePageLogin();
        }
    }

    private void synchroniser() {
        Annee exerciceConnected = null;
        for (Annee ex : listeExercTempo) {
            if ((comboListeAnneesScolaires.getSelectedItem() + "").equals(ex.getNom())) {
                exerciceConnected = ex;
            }
        }

        if ((exerciceConnected != null)) {
            idExerciceSelected = exerciceConnected.getId();
        } else {
            idExerciceSelected = -1;
        }

        UtilFileManager.isNewWorkAvailable("http://www.google.com", new EcouteurInternet() {
            @Override
            public void onInternet(String adresseWebDisponible) {
                fm.fm_synchroniser(session.getUtilisateur(), idExerciceSelected, new EcouteurSynchronisation() {
                    @Override
                    public void onSuccess(String message) {
                        if (idExerciceSelected == -1) {
                            lf_construireListeAnneesScolaires();
                        }
                        lf_progressBackUpToobar(false, "Prêt", backProgress, 0);
                        backBouton.setEnabled(true);
                        btBackup.getBouton().setEnabled(true);
                        menuSynchroniser.setEnabled(true);
                        comboListeAnneesScolaires.setEnabled(true);
                        backLabel.setText("Vos données viennent d'être sauvegardées sur le serveur.");
                    }

                    @Override
                    public void onEchec(String message) {
                        lf_progressBackUpToobar(false, "Erreur !", backProgress, 0);
                        backBouton.setEnabled(true);
                        btBackup.getBouton().setEnabled(true);
                        menuSynchroniser.setEnabled(true);
                        comboListeAnneesScolaires.setEnabled(true);
                        backLabel.setText(message);
                    }

                    @Override
                    public void onProcessing(String message, int pourcentage) {
                        lf_progressBackUpToobar(true, "(" + pourcentage + "%) Patientez...", backProgress, pourcentage);
                        backBouton.setEnabled(false);
                        btBackup.getBouton().setEnabled(false);
                        menuSynchroniser.setEnabled(false);
                        comboListeAnneesScolaires.setEnabled(false);
                        backLabel.setText("Back-up en cours: " + message);
                    }
                });
            }

            @Override
            public void onError() {
                JOptionPane.showMessageDialog(moi, "Veuillez vérifier votre connexion Internet!", "Pas de connexion", JOptionPane.WARNING_MESSAGE, icones.getAlert_02());
                backBouton.setEnabled(true);
                btBackup.getBouton().setEnabled(true);
                menuSynchroniser.setEnabled(true);
                comboListeAnneesScolaires.setEnabled(true);
                lf_progressBackUpToobar(false, "Veuillez vérifier votre connexion Internet, puis réessayer!", backProgress, -1);
                backLabel.setText("Aucune connexion Internet.");
            }

            @Override
            public void onVerification(String message) {
                lf_progressBackUpToobar(true, "Patientez...", backProgress, 75);
                backBouton.setEnabled(false);
                btBackup.getBouton().setEnabled(false);
                menuSynchroniser.setEnabled(false);
                comboListeAnneesScolaires.setEnabled(false);
                backLabel.setText(message);
            }
        });
    }

    private void lf_synchroniser(boolean isDialogBoxNeeded) {
        if (fm != null) {
            if (isDialogBoxNeeded == true) {
                if (fm.fm_isLicenceValide(moi, icones.getAdresse_02()) == true) {
                    synchroniser();
                }
            } else {
                if (fm.fm_isLicenceValide(null, null) == true) {
                    synchroniser();
                }
            }

        }
    }

    private void lf_logout() {
        if (fm != null) {

            fm.fm_logout(new EcouteurStandard() {
                @Override
                public void onDone(String message) {
                    lf_construirePageLogin();
                }

                @Override
                public void onError(String message) {
                    lf_progress(false, "", progressEtat, -1);
                }

                @Override
                public void onProcessing(String message) {
                    lf_progress(true, "Déconnexion...", progressEtat, -1);
                }
            });
        } else {
            System.err.println("Le gestionnaire de fichier est introuvable");
        }
    }

    private void lf_progress(boolean afficher, String message, JProgressBar progressBar, int pourcentage) {
        if (pourcentage == -1) {
            progressBar.setIndeterminate(afficher);
        } else {
            progressBar.setValue(pourcentage);
        }
        progressBar.setVisible(afficher);
        progressBar.setStringPainted(afficher);
        progressBar.setString(message);
    }

    private void lf_progressBackUpToobar(boolean afficher, String message, JProgressBar progressBar, int pourcentage) {
        if (pourcentage == -1) {
            progressBar.setIndeterminate(afficher);
        } else {
            progressBar.setValue(pourcentage);
        }
        progressBar.setVisible(afficher);
        progressBar.setStringPainted(afficher);
        progressBar.setString(message);
    }

    private void lf_initIcones() {
        icones = new Icones();
        menuSynchroniser.setIcon(icones.getServeur_01());
        menuDeconnexion.setIcon(icones.getDéconnecté_01());
        menuQuitter.setIcon(icones.getSortie_01());
        this.setIconImage(icones.getAdresse_03().getImage());
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

        moi.setTitle(UtilObjet.nomApplication);
        labLoginMessage.setText("Veuillez saisir vos identifiants");

        lf_progress(false, "", progressEtat, 0);
        lf_progress(false, "", progressLogin, 0);

        tabPrincipal.removeAll();
        tabPrincipal.add("Login", panLogin);
    }

    private void lf_contruireBarreEtats(Session session) {
        btEtatEntreprise.setIcon(icones.getClasse_01());
        btEtatUser.setIcon(icones.getUtilisateur_01());
        btEtatLicence.setIcon(icones.getAdresse_01());
        backBouton.setIcon(icones.getServeur_01());
        barreEtat.setVisible(true);

        lf_construireListeAnneesScolaires();
        lf_construireBoutons();

        Utilisateur user = session.getUtilisateur();

        if (user != null) {
            String noms = user.getNom() + " " + user.getPostnom() + " " + user.getPrenom();
            btEtatUser.setText(user.getPrenom());
            texteTitre = UtilObjet.nomApplication + " - " + noms;
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

        PaiementLicence licence = session.getPaiement();
        if (licence != null) {
            if (fm.fm_isLicenceValide(null, null) == true) {
                String dateExpirationL = UtilFees.convertDatePaiement(licence.getDateExpiration()).toLocaleString();
                btEtatLicence.setText(dateExpirationL);
                texteTitre += " - Echéance: " + dateExpirationL;
            } else {
                btEtatLicence.setText("Mode Gratuit");
                texteTitre += " - Mode Gratuit";
            }
        } else {
            btEtatLicence.setVisible(false);
        }

        moi.setTitle(texteTitre);

        lf_progress(false, "", progressEtat, 0);
    }

    private void appliquerDroit() {
        if (btAnnee != null && btInscription != null && btLicence != null && btLitige != null && btPaie != null && btTresorerie != null) {
            if (session != null) {
                Utilisateur user = session.getUtilisateur();

                if (comboListeAnneesScolaires.getSelectedIndex() == 0) {    //tentative de création
                    btAnnee.setText("Démarrer", 12, true);
                    btAnnee.setInfosBulle("Commencer par créer un Exercice (Année scolaire ou Année académique)");
                    btAnnee.setIcone(icones.getDémarrer_03());

                    if (user.getDroitExercice() < InterfaceUtilisateur.DROIT_CONTROLER) {
                        btAnnee.setVisible(false);
                    } else {
                        btAnnee.setVisible(true);
                    }
                    btInscription.setVisible(false);
                    btLitige.setVisible(false);
                    btPaie.setVisible(false);
                    btTresorerie.setVisible(false);
                    btUtilisateur.setVisible(false);

                } else {  //tentative de modification ou suppression
                    btAnnee.setText("Exercice", 12, true);
                    btAnnee.setInfosBulle("Ouvrir l'Exercice séléctionné");
                    btAnnee.setIcone(icones.getCalendrier_02());

                    if (user.getDroitExercice() == InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btAnnee.setVisible(false);
                    } else {
                        btAnnee.setVisible(true);
                    }
                    if (user.getDroitInscription() == InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btInscription.setVisible(false);
                    } else {
                        btInscription.setVisible(true);
                    }
                    if (user.getDroitLitige() == InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btLitige.setVisible(false);
                    } else {
                        btLitige.setVisible(true);
                    }
                    if (user.getDroitPaie() == InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btPaie.setVisible(false);
                    } else {
                        btPaie.setVisible(true);
                    }
                    if (user.getDroitTresorerie() == InterfaceUtilisateur.DROIT_PAS_ACCES) {
                        btTresorerie.setVisible(false);
                    } else {
                        btTresorerie.setVisible(true);
                    }
                    if (user.getDroitUtilisateur() == InterfaceUtilisateur.DROIT_CONTROLER) {
                        btUtilisateur.setVisible(true);
                    } else {
                        btUtilisateur.setVisible(false);
                    }

                    //Juste après sélction de l'année scolaire, il faut lancer la synchronisation très vite
                    lf_synchroniser(false);
                }
            }
        }
    }

    private void lf_initEcuteurFreemium() {
        ef = new EcouteurFreemium() {
            @Override
            public boolean onVerifie() {
                if (fm != null) {
                    return fm.fm_isLicenceValide(moi, icones.getAdresse_02());
                } else {
                    return false;
                }
            }

            @Override
            public boolean onVerifieNombre(String nomTable) {
                int nombreMax = 100;    //Nombre maximal de données à ne pas dépasser pour le mode Free.
                int nomActuelData;  //Nombre actuel des données déjà enregistrées dans la base de données.
                if (fm != null) {
                    if (nomTable == null) {
                        nomActuelData = fm.fm_getContenusDossier(UtilObjet.DOSSIER_ELEVE).length;
                        if (nomActuelData < 21) {   //On ne fait que quand on a au maximum 20 étudiants
                            return true;
                        } else {
                            return fm.fm_isLicenceValide(moi, icones.getAdresse_02()) == true;
                        }
                    } else {
                        nomActuelData = fm.fm_getContenusDossier(nomTable).length;
                        if (nomActuelData < nombreMax) {
                            return true;
                        } else {
                            return fm.fm_isLicenceValide(moi, icones.getAdresse_02()) == true;
                        }
                    }
                } else {
                    return false;
                }
            }
        };
    }

    public void lf_construireBoutons() {
        bOutils = new BarreOutils(barreOutils);
        btLicence = new Bouton(12, ":: Licence ::", "Payer votre abonnement", true, icones.getAdresse_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                UtilObjet.lancerPagePaiementEnLigne(session.getUtilisateur(), session.getEntreprise());
            }
        });
        btLicence.setForeground(UtilFees.COULEUR_ORANGE);

        btAnnee = new Bouton(12, "Démarrer", "Créer une voulle année scolaire", true, icones.getDémarrer_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {

                if (comboListeAnneesScolaires.getSelectedIndex() == 0) {
                    new Thread() {
                        public void run() {
                            //Nouvelle année scolaire
                            gestionAnnee = new GestionExercice(ef, moi, icones, couleurBasique, fm, tabPrincipal, progressEtat, session.getEntreprise(), session.getUtilisateur(), null, ecouteurExercice);
                            gestionAnnee.ga_setDonnees(null, new Vector<Agent>(), new Vector<Charge>(), new Vector<Classe>(), new Vector<Cours>(), new Vector<Frais>(), new Vector<Monnaie>(), new Vector<Revenu>(), new Vector<>());
                            gestionAnnee.ga_initUI("Nouvel Exercice");
                        }
                    }.start();
                } else {
                    new Thread() {
                        public void run() {
                            //On ouvre une année scolaire existante
                            //System.out.println("Modification et/ou Suppression de l'année scolaire " + comboListeAnneesScolaires.getSelectedItem());
                            gestionAnnee = new GestionExercice(ef, moi, icones, couleurBasique, fm, tabPrincipal, progressEtat, session.getEntreprise(), session.getUtilisateur(), null, ecouteurExercice);
                            gestionAnnee.ga_setDonneesFromFileManager(comboListeAnneesScolaires.getSelectedItem() + "");
                        }
                    }.start();
                }

                btAnnee.setIsOpenned();
            }
        });
        btAnnee.setForeground(UtilFees.COULEUR_ORANGE);

        btInscription = new Bouton(12, "Adhésion", "Inscrire des étudiants", true, icones.getAjouter_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                if (comboListeAnneesScolaires.getSelectedIndex() != 0) {
                    new Thread() {
                        public void run() {
                            //On ouvre les inscriptions
                            //System.out.println("Ouverture des adhésions");
                            gestionAdhesion = new GestionAdhesion(ecouteurGestionInscription, ef, moi, icones, couleurBasique, fm, tabPrincipal, progressEtat, session.getEntreprise(), session.getUtilisateur());
                            gestionAdhesion.gi_setDonneesFromFileManager(comboListeAnneesScolaires.getSelectedItem() + "", true);
                        }
                    }.start();
                }
                btInscription.setIsOpenned();
            }
        });
        btInscription.setForeground(UtilFees.COULEUR_ORANGE);

        btPaie = new Bouton(12, "Salaire", "Paie des Agents de l'établissement", true, icones.getRecette_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                if (comboListeAnneesScolaires.getSelectedIndex() != 0) {
                    new Thread() {
                        public void run() {
                            //On ouvre les inscriptions
                            System.out.println("Ouverture des fiches de paie");
                            gestionSalaire = new GestionSalaire(ecouteurGestionPaie, ef, moi, icones, couleurBasique, fm, tabPrincipal, progressEtat, session.getEntreprise(), session.getUtilisateur());
                            gestionSalaire.gp_setDonneesFromFileManager(comboListeAnneesScolaires.getSelectedItem() + "", true);
                        }
                    }.start();
                }
                btPaie.setIsOpenned();
            }
        });
        btPaie.setForeground(UtilFees.COULEUR_ORANGE);

        btTresorerie = new Bouton(12, "Trésorerie", "La trésorerie (Encaissements & Décaissements)", true, icones.getTableau_de_bord_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                /*
                 */
                new Thread() {
                    public void run() {
                        //On ouvre les inscriptions
                        //System.out.println("Ouverture de la trésorerie");
                        gestionTresorerie = new GestionTresorerie(ecouteurGestionTresorerie, ef, moi, icones, couleurBasique, fm, tabPrincipal, progressEtat, session.getEntreprise(), session.getUtilisateur());
                        gestionTresorerie.gt_setDonneesFromFileManager(comboListeAnneesScolaires.getSelectedItem() + "", true);
                    }
                }.start();
                btTresorerie.setIsOpenned();
            }
        });
        btTresorerie.setForeground(UtilFees.COULEUR_ORANGE);

        btLitige = new Bouton(12, "Litiges", "Litiges et reglèment des dettes", true, icones.getFournisseur_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                /**/
                if (comboListeAnneesScolaires.getSelectedIndex() != 0) {
                    new Thread() {
                        public void run() {
                            //On ouvre les inscriptions
                            //System.out.println("Ouverture des litiges");
                            gestionLitiges = new GestionLitiges(ecouteurGestionLitige, ef, moi, icones, couleurBasique, fm, tabPrincipal, progressEtat, session.getEntreprise(), session.getUtilisateur());
                            gestionLitiges.gl_setDonneesFromFileManager(comboListeAnneesScolaires.getSelectedItem() + "", true);
                        }
                    }.start();
                }
                btLitige.setIsOpenned();
            }
        });
        btLitige.setForeground(UtilFees.COULEUR_ORANGE);

        btUtilisateur = new Bouton(12, "Utilisateurs", "Gérer les utilisateurs ainsi que leurs droits d'accès.", true, icones.getUtilisateur_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                UtilObjet.lancerPageWebAdmin(moi, session.getUtilisateur(), session.getEntreprise(), UtilObjet.ACTION_LISTER_UTILISATEUR, icones.getAlarme_02());
            }
        });
        btUtilisateur.setForeground(UtilFees.COULEUR_ORANGE);

        btBackup = new Bouton(12, "Back-up", "Sauvegarder vos données en ligne", true, icones.getServeur_02(), new BoutonListener() {
            @Override
            public void OnEcouteLeClick() {
                lf_synchroniser(true);
            }
        });
        btBackup.setForeground(UtilFees.COULEUR_ORANGE);

        bOutils.AjouterBouton(btLogo);
        bOutils.AjouterBouton(btLicence);
        bOutils.AjouterSeparateur();
        bOutils.AjouterBouton(btAnnee);
        bOutils.AjouterBouton(btInscription);
        bOutils.AjouterBouton(btLitige);
        bOutils.AjouterBouton(btTresorerie);
        bOutils.AjouterBouton(btPaie);
        bOutils.AjouterBouton(btUtilisateur);
        bOutils.AjouterBouton(btBackup);

        panOutils.setVisible(true);
        barreOutils.setVisible(true);

        appliquerDroit();
    }

    private void lf_chargerEspaceTravail(Session session) {
        this.session = session;
        tabPrincipal.remove(panLogin);
        lf_contruireBarreEtats(session);
        labLoginMessage.setForeground(Color.darkGray);
        labLoginMessage.setText("Prêt.");
        lf_progress(false, "", progressLogin, 0);
        lf_progress(false, "", progressEtat, 0);
        labInfoEtat.setText("Connecté!");
        backProgress.setVisible(false);

        //on doit directement commencer à écouter le suiveur d'édition
        fm.setEcouteurSuiviEdition(new EcouteurSuiviEdition() {
            @Override
            public void onSuiveurActive(Date dateDernireModification) {
                if (backBouton != null) {
                    String texteAlert = "Dernière modification: " + UtilObjet.getDateFrancais(dateDernireModification) + ". Cliquez sur BACK-UP pour sauvegarder!";
                    backBouton.setText("Back-up(!)");
                    backBouton.setToolTipText(texteAlert);
                    backBouton.setForeground(Color.red);
                    backBouton.setFont(backBouton.getFont().deriveFont(Font.BOLD));
                    backLabel.setText(texteAlert);
                    backLabel.setFont(backBouton.getFont().deriveFont(Font.BOLD));
                }
            }

            @Override
            public void onSuiveurDesactive() {
                if (backBouton != null) {
                    backBouton.setText("Back-up");
                    backBouton.setForeground(Color.black);
                    backBouton.setFont(backBouton.getFont().deriveFont(Font.PLAIN));
                    backLabel.setFont(backBouton.getFont().deriveFont(Font.PLAIN));
                }
            }
        });

        //On lance directement la synchronisation.
        lf_synchroniser(false);
    }

    private void lf_construireListeAnneesScolaires() {
        //on enlèves les autres elements du combo, sauf le premier element
        comboListeAnneesScolaires.removeAllItems();
        comboListeAnneesScolaires.addItem("-- Liste d'Années --");
        System.out.println("CHARGEMENT DES ANNEES SCOLAIRES....");
        fm.fm_ouvrirTout(0, Annee.class, UtilObjet.DOSSIER_ANNEE, 1, 1000, new EcouteurOuverture() {

            @Override
            public boolean isCriteresRespectes(Object object) {
                return true;
            }

            @Override
            public void onElementLoaded(String message, Object data) {
                Annee annee = (Annee) data;
                System.out.println("\t - " + annee.toString());
                if (!listeExercTempo.contains(annee)) {
                    comboListeAnneesScolaires.addItem(annee.getNom());
                    listeExercTempo.add(annee);
                }
            }

            @Override
            public void onDone(String message, int resultatTotal, Vector resultatTotalObjets) {
                lf_progress(false, "", progressEtat, 0);
            }

            @Override
            public void onError(String message) {
                lf_progress(false, "", progressEtat, 0);
            }

            @Override
            public void onProcessing(String message) {
                lf_progress(true, "Pateintez...", progressEtat, -1);
            }
        });
    }

    private void lf_login() {
        String loginEmail = chLoginEmail.getText();
        String loginPasseW = chLoginMotDePasse.getText();
        String loginID = chLoginIDEcole.getText();

        if (loginEmail.length() == 0 || loginPasseW.length() == 0 || loginID.length() == 0) {
            labLoginMessage.setForeground(Color.red);
            labLoginMessage.setText("Veuillez fournir vos identifiants!");
            lf_progress(false, "", progressLogin, 0);
        } else {
            fm.fm_login(loginID, loginEmail, loginPasseW, new EcouteurLongin() {
                @Override
                public void onConnected(String message, Session session) {
                    if (session != null) {
                        lf_chargerEspaceTravail(session);
                    } else {
                        labLoginMessage.setForeground(Color.RED);
                        labLoginMessage.setText(message);
                        lf_progress(false, "", progressLogin, 0);
                    }
                }

                @Override
                public void onEchec(String message) {
                    labLoginMessage.setForeground(Color.RED);
                    labLoginMessage.setText(message);
                    lf_progress(false, "", progressLogin, 0);
                }

                @Override
                public void onProcessing(String message) {
                    labLoginMessage.setForeground(Color.BLACK);
                    labLoginMessage.setText("Veuillez patienter...");
                    lf_progress(true, "Patientez...", progressLogin, -1);
                    //System.out.println(message);
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
        jSeparator5 = new javax.swing.JToolBar.Separator();
        labInfoEtat = new javax.swing.JLabel();
        progressEtat = new javax.swing.JProgressBar();
        backUpToolbar = new javax.swing.JToolBar();
        backBouton = new javax.swing.JButton();
        backProgress = new javax.swing.JProgressBar();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        backLabel = new javax.swing.JLabel();
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
        jButton2 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        menu = new javax.swing.JMenu();
        menuSynchroniser = new javax.swing.JMenuItem();
        menuDeconnexion = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuQuitter = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        barreEtat.setBackground(new java.awt.Color(251, 155, 12));

        jToolBar1.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);

        comboListeAnneesScolaires.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Liste d'Années --" }));
        comboListeAnneesScolaires.setToolTipText("Liste d'années scolaires actuellement stockées en base de données");
        comboListeAnneesScolaires.setMaximumSize(new java.awt.Dimension(500, 32767));
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
        btEtatEntreprise.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEtatEntrepriseActionPerformed(evt);
            }
        });
        jToolBar1.add(btEtatEntreprise);

        btEtatUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        btEtatUser.setText("User");
        btEtatUser.setToolTipText("Utilisateur actuel");
        btEtatUser.setFocusable(false);
        btEtatUser.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btEtatUser.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btEtatUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEtatUserActionPerformed(evt);
            }
        });
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
        jToolBar1.add(jSeparator5);

        labInfoEtat.setText("Info");
        jToolBar1.add(labInfoEtat);

        progressEtat.setIndeterminate(true);
        jToolBar1.add(progressEtat);

        backUpToolbar.setFloatable(false);
        backUpToolbar.setRollover(true);

        backBouton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        backBouton.setText("Back-up");
        backBouton.setToolTipText("Sauvegarde des données sur le serveur distant. Cliquer pour relancer le chargement.");
        backBouton.setFocusable(false);
        backBouton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        backBouton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        backBouton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBoutonActionPerformed(evt);
            }
        });
        backUpToolbar.add(backBouton);

        backProgress.setMaximumSize(new java.awt.Dimension(100, 14));
        backUpToolbar.add(backProgress);
        backUpToolbar.add(jSeparator4);

        backLabel.setText("Pour la sauvegarde de vos données sur le serveur.");
        backUpToolbar.add(backLabel);

        javax.swing.GroupLayout barreEtatLayout = new javax.swing.GroupLayout(barreEtat);
        barreEtat.setLayout(barreEtatLayout);
        barreEtatLayout.setHorizontalGroup(
            barreEtatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barreEtatLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(barreEtatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(backUpToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        barreEtatLayout.setVerticalGroup(
            barreEtatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barreEtatLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(backUpToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tabPrincipal.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabPrincipalStateChanged(evt);
            }
        });

        btLoginConnexion.setBackground(new java.awt.Color(251, 155, 12));
        btLoginConnexion.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        btLoginConnexion.setForeground(new java.awt.Color(26, 46, 77));
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

        labLoginNouveauCompte.setForeground(new java.awt.Color(26, 46, 77));
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
                    .addComponent(labLoginMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addContainerGap(249, Short.MAX_VALUE)
                .addGroup(panLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panLoginInfos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panLoginMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(249, Short.MAX_VALUE))
        );
        panLoginLayout.setVerticalGroup(
            panLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLoginLayout.createSequentialGroup()
                .addContainerGap(74, Short.MAX_VALUE)
                .addComponent(panLoginInfos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panLoginMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(64, Short.MAX_VALUE))
        );

        tabPrincipal.addTab("Login", panLogin);

        panOutils.setBackground(new java.awt.Color(26, 46, 77));
        panOutils.setForeground(new java.awt.Color(255, 255, 255));

        barreOutils.setFloatable(false);
        barreOutils.setForeground(new java.awt.Color(153, 153, 153));
        barreOutils.setOrientation(javax.swing.SwingConstants.VERTICAL);
        barreOutils.setRollover(true);

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
            .addComponent(barreOutils, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        menu.setText("Fichier");

        menuSynchroniser.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        menuSynchroniser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG_Fees/Facture01.png"))); // NOI18N
        menuSynchroniser.setText("Back-up");
        menuSynchroniser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSynchroniserActionPerformed(evt);
            }
        });
        menu.add(menuSynchroniser);

        menuDeconnexion.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK));
        menuDeconnexion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        menuDeconnexion.setText("Déconnexion");
        menuDeconnexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuDeconnexionActionPerformed(evt);
            }
        });
        menu.add(menuDeconnexion);
        menu.add(jSeparator1);

        menuQuitter.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        menuQuitter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMG/Facture01.png"))); // NOI18N
        menuQuitter.setText("Quitter");
        menuQuitter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuQuitterActionPerformed(evt);
            }
        });
        menu.add(menuQuitter);

        jMenuBar1.add(menu);

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
                    .addComponent(tabPrincipal)
                    .addComponent(panOutils, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(barreEtat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
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
        UtilObjet.lancerPageWebAdmin(moi, session.getUtilisateur(), session.getEntreprise(), UtilObjet.ACTION_HOME_PAGE, icones.getAlarme_02());
    }//GEN-LAST:event_labLoginNouveauCompteMouseClicked

    private void btEtatLicenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEtatLicenceActionPerformed
        // TODO add your handling code here:
        UtilObjet.lancerPagePaiementEnLigne(session.getUtilisateur(), session.getEntreprise());
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

    private void backBoutonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBoutonActionPerformed
        // TODO add your handling code here:
        lf_synchroniser(true);
    }//GEN-LAST:event_backBoutonActionPerformed

    private void btEtatUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEtatUserActionPerformed
        // TODO add your handling code here:
        UtilObjet.lancerPageWebAdmin(moi, session.getUtilisateur(), session.getEntreprise(), UtilObjet.ACTION_LISTER_UTILISATEUR, icones.getAlarme_02());
    }//GEN-LAST:event_btEtatUserActionPerformed

    private void btEtatEntrepriseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEtatEntrepriseActionPerformed
        // TODO add your handling code here:
        UtilObjet.lancerPageWebAdmin(moi, session.getUtilisateur(), session.getEntreprise(), UtilObjet.ACTION_MODIFIER_INFO_ECOLE, icones.getAlarme_02());
    }//GEN-LAST:event_btEtatEntrepriseActionPerformed

    private void menuSynchroniserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSynchroniserActionPerformed
        // TODO add your handling code here:
        lf_synchroniser(true);
    }//GEN-LAST:event_menuSynchroniserActionPerformed

    private void tabPrincipalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabPrincipalStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_tabPrincipalStateChanged

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
    private javax.swing.JButton backBouton;
    private javax.swing.JLabel backLabel;
    private javax.swing.JProgressBar backProgress;
    private javax.swing.JToolBar backUpToolbar;
    private javax.swing.JPanel barreEtat;
    private javax.swing.JToolBar barreOutils;
    private javax.swing.JButton btEtatEntreprise;
    private javax.swing.JButton btEtatLicence;
    private javax.swing.JButton btEtatUser;
    private javax.swing.JButton btLoginConnexion;
    private UI.JS2bTextField chLoginEmail;
    private UI.JS2bTextField chLoginIDEcole;
    private UI.JS2BPassword chLoginMotDePasse;
    private javax.swing.JComboBox<String> comboListeAnneesScolaires;
    private javax.swing.JButton jButton2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel labInfoEtat;
    private javax.swing.JLabel labLoginMessage;
    private javax.swing.JLabel labLoginNouveauCompte;
    private javax.swing.JMenu menu;
    private javax.swing.JMenuItem menuDeconnexion;
    private javax.swing.JMenuItem menuQuitter;
    private javax.swing.JMenuItem menuSynchroniser;
    private javax.swing.JPanel panLogin;
    private javax.swing.JPanel panLoginInfos;
    private javax.swing.JPanel panLoginMessage;
    private javax.swing.JPanel panOutils;
    private javax.swing.JProgressBar progressEtat;
    private javax.swing.JProgressBar progressLogin;
    private javax.swing.JTabbedPane tabPrincipal;
    // End of variables declaration//GEN-END:variables
}
