/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.UTILITAIRES;

import ICONES.Icones;
import SOURCES.Objets.Session;
import Source.Interface.InterfaceUtilisateur;
import Source.Objet.Utilisateur;
import java.awt.Color;
import java.awt.Desktop;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author user
 */
public class UtilFees {

    public static String nomApplication = "S2BFees";
    public static String pageWeb = "http://www.visiterlardc.com/s2b";

    public static Color COULEUR_BLEU = new Color(26, 45, 77);       //Pour plus d'infos visiter le lien https://www.colorhexa.com/1a2e4d
    public static Color COULEUR_BLEU_CLAIRE_1 = new Color(68, 117, 192);    //Une variante claire
    public static Color COULEUR_BLEU_CLAIRE_2 = new Color(141, 171, 217);    //Une variante claire
    public static Color COULEUR_ORANGE = new Color(251, 155, 12);   //Pour plus d'information, visiter le lien https://www.colorhexa.com/fb9b0c
    public static Color COULEUR_ROUGE = new Color(251, 36, 12);       //Une variante  
    //Les actions web
    public static int ACTION_LISTER_UTILISATEUR = 100;
    public static int ACTION_PAYER_LICENCE = 200;
    public static int ACTION_MODIFIER_LOGO = 300;
    public static int ACTION_MODIFIER_INFO_ECOLE = 400;
    public static int ACTION_MODIFIER_ARCHIVES = 500;
    public static int ACTION_HOME_PAGE = 0;

    public static Date convertDatePaiement(String Sdate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Sdate);
            System.out.println(Sdate + "\t" + date.toLocaleString());
            return date;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    public static String getSDroitAccess(int code) {
        switch (code) {
            case InterfaceUtilisateur.DROIT_PAS_ACCES:
                return "PAS D'ACCES";
            case InterfaceUtilisateur.DROIT_VISUALISER:
                return "VISUALISER";
            case InterfaceUtilisateur.DROIT_CONTROLER:
                return "CONTROLER";
            default:
                return null;
        }
    }

    public static boolean lancerPageWebAdmin(JFrame parent, Session session, int action) {
        boolean go = false;
        Utilisateur user = null;
        if (session != null) {
            user = session.getUtilisateur();
            boolean dExercice = user.getDroitExercice() == InterfaceUtilisateur.DROIT_CONTROLER;
            boolean dFacture = user.getDroitFacture() == InterfaceUtilisateur.DROIT_CONTROLER;
            boolean dInscription = user.getDroitInscription() == InterfaceUtilisateur.DROIT_CONTROLER;
            boolean dLitige = user.getDroitLitige() == InterfaceUtilisateur.DROIT_CONTROLER;
            boolean dPaie = user.getDroitPaie() == InterfaceUtilisateur.DROIT_CONTROLER;
            boolean dTresorerie = user.getDroitTresorerie() == InterfaceUtilisateur.DROIT_CONTROLER;
            boolean dUtilisateur = user.getDroitUtilisateur() == InterfaceUtilisateur.DROIT_CONTROLER;
            boolean typeUser = user.getType() == InterfaceUtilisateur.TYPE_ADMIN;

            if (typeUser == true && dExercice == true && dFacture == true && dInscription == true && dLitige == true && dPaie == true && dTresorerie == true && dUtilisateur == true && typeUser == true) {
                go = true;
            } else if (action == ACTION_HOME_PAGE) {
                go = true;
            }
        }

        if (go == true) {
            //http://www.visiterlardc.com/s2b/redirection.php?action=100&email=sulabosiog@gmail.com&motdepasse=abc&idEntreprise=2
            String parametres = "/redirection.php?action=" + action + "&email=" + session.getUtilisateur().getEmail() + "&motdepasse=" + session.getUtilisateur().getMotDePasse() + "&idEntreprise=" + session.getEntreprise().getId();
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URL(pageWeb + parametres).toURI());
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Icones icon = new Icones();
            JOptionPane.showMessageDialog(parent, "Désolé " + user.getPrenom() + ",\nVous ne pouvez pas accéder à cette fonctionnalité car vous avez un accès très limité!"
                    + "\nSi vous désirez élargir vos droits d'accès, merci de s'adresser à votre administrateur.", "Pas d'accès", JOptionPane.ERROR_MESSAGE, icon.getAlarme_02());
        }
        return false;
    }
    
    public static boolean isNewWorkAvailable(){
        try {
            URL url = new URL(pageWeb);//new URL("https://www.geeksforgeeks.org/"); 
            URLConnection connection = url.openConnection(); 
            connection.connect(); 
            return true;
        }catch (Exception e) { 
            return false;
        } 
    }

}

























