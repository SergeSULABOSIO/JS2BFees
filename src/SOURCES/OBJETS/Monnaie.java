/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.OBJETS;

import SOURCES.Interfaces.InterfaceMonnaie;

/**
 *
 * @author user
 */
public class Monnaie implements InterfaceMonnaie{
    public int id;
    public int idEntreprise;
    public int idUtilisateur;
    public int idExercice;
    public String nom;
    public String code;
    public int nature;    //Monnaie locale = 0, Monnaie étrangère = 1; 
    public double tauxMonnaieLocale;
    public long signature;
    public int beta;

    public Monnaie(int id, int idEntreprise, int idUtilisateur, int idExercice, String nom, String code, int nature, double tauxMonnaieLocale, long signature, int beta) {
        this.id = id;
        this.idEntreprise = idEntreprise;
        this.idUtilisateur = idUtilisateur;
        this.idExercice = idExercice;
        this.nom = nom;
        this.code = code;
        this.nature = nature;
        this.tauxMonnaieLocale = tauxMonnaieLocale;
        this.signature = signature;
        this.beta = beta;
    }

    public Monnaie() {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getNature() {
        return nature;
    }

    public void setNature(int nature) {
        this.nature = nature;
    }

    public double getTauxMonnaieLocale() {
        return tauxMonnaieLocale;
    }

    public void setTauxMonnaieLocale(double tauxMonnaieLocale) {
        this.tauxMonnaieLocale = tauxMonnaieLocale;
    }

    public long getSignature() {
        return signature;
    }

    public void setSignature(long signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "XX_Monnaie{" + "id=" + id + ", idEntreprise=" + idEntreprise + ", idUtilisateur=" + idUtilisateur + ", idExercice=" + idExercice + ", nom=" + nom + ", code=" + code + ", nature=" + nature + ", tauxMonnaieLocale=" + tauxMonnaieLocale + ", signature=" + signature + '}';
    }
    
    
}
