/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.OBJETS;

import SOURCES.Interfaces.InterfaceClasse;
import SOURCES.Interfaces.InterfaceFrais;
import SOURCES.Interfaces.InterfacePeriode;
import SOURCES.Utilitaires.LiaisonClasseFrais;
import SOURCES.Utilitaires.LiaisonPeriodeFrais;
import java.util.Vector;

/**
 *
 * @author HP Pavilion
 */
public class Frais implements InterfaceFrais{

    public int id;
    public int idUtilisateur;
    public int idEntreprise;
    public int idExercice;
    public int idMonnaie;
    public double montantDefaut;
    public String nom;
    public String monnaie;
    public long signatureMonnaie;
    public Vector<LiaisonClasseFrais> liaisonsClasses = new Vector<LiaisonClasseFrais>();
    public Vector<LiaisonPeriodeFrais> liaisonsPeriodes = new Vector<LiaisonPeriodeFrais>();
    public int beta;

    public Frais(int id, int idUtilisateur, int idEntreprise, int idExercice, int idMonnaie, long signatureMonnaie, String nom, String monnaie, int nbTranches, Vector<LiaisonClasseFrais> liaisonsClasses, Vector<LiaisonPeriodeFrais> liaisonsPeriodes, double montantDefaut, int beta) {
        this.id = id;
        this.idUtilisateur = idUtilisateur;
        this.idEntreprise = idEntreprise;
        this.idExercice = idExercice;
        this.nom = nom;
        this.liaisonsClasses = liaisonsClasses;
        this.liaisonsPeriodes = liaisonsPeriodes;
        this.idMonnaie = idMonnaie;
        this.monnaie = monnaie;
        this.signatureMonnaie = signatureMonnaie;
        this.montantDefaut = montantDefaut;
        this.beta = beta;
    }

    public Frais() {
    }
    
    

    public double getMontantDefaut() {
        return montantDefaut;
    }

    public void setMontantDefaut(double montantDefaut) {
        this.montantDefaut = montantDefaut;
    }

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }

    public int getIdMonnaie() {
        return idMonnaie;
    }

    public void setIdMonnaie(int idMonnaie) {
        this.idMonnaie = idMonnaie;
    }

    public String getMonnaie() {
        return monnaie;
    }

    public void setMonnaie(String monnaie) {
        this.monnaie = monnaie;
    }

    public long getSignatureMonnaie() {
        return signatureMonnaie;
    }

    public void setSignatureMonnaie(long signatureMonnaie) {
        this.signatureMonnaie = signatureMonnaie;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdEntreprise() {
        return idEntreprise;
    }

    public void setIdEntreprise(int idEntreprise) {
        this.idEntreprise = idEntreprise;
    }

    public int getIdExercice() {
        return idExercice;
    }

    public void setIdExercice(int idExercice) {
        this.idExercice = idExercice;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Vector<LiaisonClasseFrais> getLiaisonsClasses() {
        return liaisonsClasses;
    }

    public void setLiaisonsClasses(Vector<LiaisonClasseFrais> liaisonsClasses) {
        this.liaisonsClasses = liaisonsClasses;
    }

    public Vector<LiaisonPeriodeFrais> getLiaisonsPeriodes() {
        return liaisonsPeriodes;
    }

    public void setLiaisonsPeriodes(Vector<LiaisonPeriodeFrais> liaisonsPeriodes) {
        this.liaisonsPeriodes = liaisonsPeriodes;
    }

    @Override
    public LiaisonPeriodeFrais getLiaisonPeriodes(InterfacePeriode periode) {
        for (LiaisonPeriodeFrais liaisonPeriodeFrais : liaisonsPeriodes) {
            if (liaisonPeriodeFrais.getIdPeriode()== periode.getId() && liaisonPeriodeFrais.getNomPeriode().equals(periode.getNom())) {
                return liaisonPeriodeFrais;
            }
        }
        return null;
    }

    @Override
    public LiaisonClasseFrais getLiaisonClasses(InterfaceClasse classe) {
        for (LiaisonClasseFrais liaisonClasseFrais : liaisonsClasses) {
            if (liaisonClasseFrais.getIdClasse() == classe.getId() && liaisonClasseFrais.getNomClasse().equals(classe.getNom())) {
                return liaisonClasseFrais;
            }
        }
        return null;
    }

    @Override
    public void ajouterLiaisonsPeriode(LiaisonPeriodeFrais liaisons) {
        if(liaisons != null){
            if(!this.liaisonsPeriodes.contains(liaisons)){
                this.liaisonsPeriodes.add(liaisons);
            }
        }
    }

    
    @Override
    public void ajouterLiaisonsClasse(LiaisonClasseFrais liaison) {
        if(liaison != null){
            if(!this.liaisonsClasses.contains(liaison)){
                this.liaisonsClasses.add(liaison);
            }
        }
    }

    @Override
    public void viderLiaisonsClasses() {
        this.liaisonsClasses.removeAllElements();
    }

    @Override
    public void viderLiaisonsPeriodes() {
        this.liaisonsPeriodes.removeAllElements();
    }


    @Override
    public String toString() {
        return "TEST_Frais{" + "id=" + id + ", idUtilisateur=" + idUtilisateur + ", idEntreprise=" + idEntreprise + ", idExercice=" + idExercice + ", idMonnaie=" + idMonnaie + ", montantDefaut=" + montantDefaut + ", nom=" + nom + ", monnaie=" + monnaie + ", signatureMonnaie=" + signatureMonnaie + ", liaisonsClasses=" + liaisonsClasses + ", liaisonsPeriodes=" + liaisonsPeriodes + ", beta=" + beta + '}';
    }
}










