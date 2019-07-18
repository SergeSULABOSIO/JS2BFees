/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.UTILITAIRES;

import Source.Interface.InterfaceUtilisateur;
import java.awt.Color;
import java.awt.Desktop;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author user
 */
public class UtilFees {

    public static String nomApplication = "S2BFees";
    public static String pageWeb = "http://www.visiterlardc.com/s2b";
    public static String DOSSIER_ANNEE = "ANNEE";
    public static String DOSSIER_MONNAIE = "MONNAIE";
    public static String DOSSIER_CLASSE = "CLASSE";
    public static String DOSSIER_PERIODE = "PERIODE";
    public static String DOSSIER_AGENT = "AGENT";
    public static String DOSSIER_COURS = "COURS";
    public static String DOSSIER_REVENU = "REVENU";
    public static String DOSSIER_CHARGE = "CHARGE";
    public static String DOSSIER_FRAIS = "FRAIS";
    public static String DOSSIER_ELEVE = "ELEVE";
    public static String DOSSIER_AYANT_DROIT = "AYANT_DROIT";
    
    public static Color COULEUR_BLEU = new Color(26, 45, 77);       //Pour plus d'infos visiter le lien https://www.colorhexa.com/1a2e4d
    public static Color COULEUR_BLEU_CLAIRE_1 = new Color(68,117,192);    //Une variante claire
    public static Color COULEUR_BLEU_CLAIRE_2 = new Color(141,171,217);    //Une variante claire
    public static Color COULEUR_ORANGE = new Color(251, 155, 12);   //Pour plus d'information, visiter le lien https://www.colorhexa.com/fb9b0c
    public static Color COULEUR_ROUGE = new Color(251,36,12);       //Une variante  

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

    public static boolean lancerPageWebAdmin() {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URL(pageWeb).toURI());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}




























