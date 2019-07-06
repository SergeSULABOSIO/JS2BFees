/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.OBJETS;

import SOURCES.Interfaces.InterfaceCharge;

/**
 *
 * @author HP Pavilion
 */
public class Charge implements InterfaceCharge{
    
    public int id;
    public int idEntreprise;
    public int idUtilisateur;
    public int idExercice;
    public String nom;
    public double limiteAnnuelle;
    public int idMonnaie;
    public long signatureMonnaie;
    public String monnaie;
    public int beta;

    public Charge(int id, int idEntreprise, int idUtilisateur, int idExercice, String nom, double limiteAnnuelle, int idMonnaie, long signatureMonnaie, String monnaie, int beta) {
        this.id = id;
        this.idEntreprise = idEntreprise;
        this.idUtilisateur = idUtilisateur;
        this.idExercice = idExercice;
        this.nom = nom;
        this.limiteAnnuelle = limiteAnnuelle;
        this.idMonnaie = idMonnaie;
        this.monnaie = monnaie;
        this.signatureMonnaie = signatureMonnaie;
        this.beta = beta;
    }

    public Charge() {
    }
    
    

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEntreprise() {
        return idEntreprise;
    }

    public void setIdEntreprise(int idEntreprise) {
        this.idEntreprise = idEntreprise;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
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

    public double getLimiteAnnuelle() {
        return limiteAnnuelle;
    }

    public void setLimiteAnnuelle(double limiteAnnuelle) {
        this.limiteAnnuelle = limiteAnnuelle;
    }

    public int getIdMonnaie() {
        return idMonnaie;
    }

    public void setIdMonnaie(int idMonnaie) {
        this.idMonnaie = idMonnaie;
    }

    public long getSignatureMonnaie() {
        return signatureMonnaie;
    }

    public void setSignatureMonnaie(long signatureMonnaie) {
        this.signatureMonnaie = signatureMonnaie;
    }

    public String getMonnaie() {
        return monnaie;
    }

    public void setMonnaie(String monnaie) {
        this.monnaie = monnaie;
    }

    @Override
    public String toString() {
        return "XX_Charge{" + "id=" + id + ", idEntreprise=" + idEntreprise + ", idUtilisateur=" + idUtilisateur + ", idExercice=" + idExercice + ", nom=" + nom + ", limiteAnnuelle=" + limiteAnnuelle + ", idMonnaie=" + idMonnaie + ", signatureMonnaie=" + signatureMonnaie + ", monnaie=" + monnaie + ", beta=" + beta + '}';
    }
}
