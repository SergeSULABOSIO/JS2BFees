/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SOURCES.OBJETS;

import SOURCES.Interfaces.InterfaceAgent;

/**
 *
 * @author HP Pavilion
 */
public class Agent implements InterfaceAgent{
    public int id;
    public int idEntreprise;
    public int idUtilisateur;
    public int idExercice;
    public String nom;
    public String postnom;
    public String prenom;
    public int sexe;
    public int niveauEtude;
    public long signature;
    public int categorie;
    public int beta;

    public Agent(int id, int idEntreprise, int idUtilisateur, int idExercice, String nom, String postnom, String prenom, int sexe, int niveauEtude, long signature, int categorie, int beta) {
        this.id = id;
        this.idEntreprise = idEntreprise;
        this.idUtilisateur = idUtilisateur;
        this.idExercice = idExercice;
        this.nom = nom;
        this.postnom = postnom;
        this.prenom = prenom;
        this.sexe = sexe;
        this.niveauEtude = niveauEtude;
        this.signature = signature;
        this.categorie = categorie;
        this.beta = beta;
    }

    public Agent() {
    }
    
    

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }
    
    

    public int getCategorie() {
        return categorie;
    }

    public void setCategorie(int categorie) {
        this.categorie = categorie;
    }
    
    

    public long getSignature() {
        return signature;
    }

    public void setSignature(long signature) {
        this.signature = signature;
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

    public String getPostnom() {
        return postnom;
    }

    public void setPostnom(String postnom) {
        this.postnom = postnom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getSexe() {
        return sexe;
    }

    public void setSexe(int sexe) {
        this.sexe = sexe;
    }

    public int getNiveauEtude() {
        return niveauEtude;
    }

    public void setNiveauEtude(int niveauEtude) {
        this.niveauEtude = niveauEtude;
    }

    @Override
    public String toString() {
        return "XX_Agent{" + "id=" + id + ", idEntreprise=" + idEntreprise + ", idUtilisateur=" + idUtilisateur + ", idExercice=" + idExercice + ", nom=" + nom + ", postnom=" + postnom + ", prenom=" + prenom + ", sexe=" + sexe + ", niveauEtude=" + niveauEtude + ", signature=" + signature + ", categorie=" + categorie + ", beta=" + beta + '}';
    }
}
