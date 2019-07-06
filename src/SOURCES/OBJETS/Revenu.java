/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.OBJETS;

import SOURCES.Interfaces.InterfaceRevenu;

/**
 *
 * @author user
 */
public class Revenu implements InterfaceRevenu{
    public int id;
    public int idEntreprise;
    public int idUtilisateur;
    public int idExercice;
    public int idMonnaie;
    public long signatureMonnaie;
    public String nom;
    public String monnaie;
    public double volumeAnnuel;
    public int beta;

    public Revenu(int id, int idEntreprise, int idUtilisateur, int idExercice, int idMonnaie, long signatureMonnaie, String nom, String monnaie, double volumeAnnuel, int beta) {
        this.id = id;
        this.idEntreprise = idEntreprise;
        this.idUtilisateur = idUtilisateur;
        this.idExercice = idExercice;
        this.idMonnaie = idMonnaie;
        this.nom = nom;
        this.monnaie = monnaie;
        this.signatureMonnaie = signatureMonnaie;
        this.volumeAnnuel = volumeAnnuel;
        this.beta = beta;
    }

    public Revenu() {
    }
    
    

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }
    
    

    public long getSignatureMonnaie() {
        return signatureMonnaie;
    }

    public void setSignatureMonnaie(long signatureMonnaie) {
        this.signatureMonnaie = signatureMonnaie;
    }

    public double getVolumeAnnuel() {
        return volumeAnnuel;
    }

    public void setVolumeAnnuel(double volumeAnnuel) {
        this.volumeAnnuel = volumeAnnuel;
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

    public int getIdMonnaie() {
        return idMonnaie;
    }

    public void setIdMonnaie(int idMonnaie) {
        this.idMonnaie = idMonnaie;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getMonnaie() {
        return monnaie;
    }

    public void setMonnaie(String monnaie) {
        this.monnaie = monnaie;
    }

    @Override
    public String toString() {
        return "XX_Revenu{" + "id=" + id + ", idEntreprise=" + idEntreprise + ", idUtilisateur=" + idUtilisateur + ", idExercice=" + idExercice + ", idMonnaie=" + idMonnaie + ", signatureMonnaie=" + signatureMonnaie + ", nom=" + nom + ", monnaie=" + monnaie + ", volumeAnnuel=" + volumeAnnuel + ", beta=" + beta + '}';
    }

}
