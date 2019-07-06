/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.OBJETS;

import SOURCES.Interfaces.InterfaceCours;

/**
 *
 * @author HP Pavilion
 */
public class Cours implements InterfaceCours{
    public int id;
    public int idEntreprise;
    public int idUtilisateur;
    public int idExercice;
    public String nom;
    public String enseignant;
    public long signatureEnseignant;
    public int idEnseignant;
    public String classe;
    public long signatureClasse;
    public int idClasse;
    public int beta;

    public Cours(int id, int idEntreprise, int idUtilisateur, int idExercice, String nom, String enseignant, long signatureEnseignant, int idEnseignant, String classe, long signatureClasse, int idClasse, int beta) {
        this.id = id;
        this.idEntreprise = idEntreprise;
        this.idUtilisateur = idUtilisateur;
        this.idExercice = idExercice;
        this.nom = nom;
        this.enseignant = enseignant;
        this.signatureEnseignant = signatureEnseignant;
        this.idEnseignant = idEnseignant;
        this.classe = classe;
        this.signatureClasse = signatureClasse;
        this.idClasse = idClasse;
        this.beta = beta;
    }

    public Cours() {
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

    public String getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(String enseignant) {
        this.enseignant = enseignant;
    }

    public long getSignatureEnseignant() {
        return signatureEnseignant;
    }

    public void setSignatureEnseignant(long signatureEnseignant) {
        this.signatureEnseignant = signatureEnseignant;
    }

    public int getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(int idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public long getSignatureClasse() {
        return signatureClasse;
    }

    public void setSignatureClasse(long signatureClasse) {
        this.signatureClasse = signatureClasse;
    }

    public int getIdClasse() {
        return idClasse;
    }

    public void setIdClasse(int idClasse) {
        this.idClasse = idClasse;
    }

    @Override
    public String toString() {
        return "XX_Cours{" + "id=" + id + ", idEntreprise=" + idEntreprise + ", idUtilisateur=" + idUtilisateur + ", idExercice=" + idExercice + ", nom=" + nom + ", enseignant=" + enseignant + ", signatureEnseignant=" + signatureEnseignant + ", idEnseignant=" + idEnseignant + ", classe=" + classe + ", signatureClasse=" + signatureClasse + ", idClasse=" + idClasse + ", beta=" + beta + '}';
    }
}
