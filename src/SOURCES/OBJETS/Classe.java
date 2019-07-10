package SOURCES.OBJETS;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import SOURCES.Interfaces.InterfaceClasse;

/**
 *
 * @author HP Pavilion
 */
public class Classe implements InterfaceClasse{
    public int id;
    public int idUtilisateur;
    public int idEntreprise;
    public int idExercice;
    public String nom;
    public int capacite;
    public String nomLocal;
    public long signature;
    public int beta;

    public Classe(int id, int idUtilisateur, int idEntreprise, int idExercice, String nom, int capacite, String nomLocal, long signature, int beta) {
        this.id = id;
        this.idUtilisateur = idUtilisateur;
        this.idEntreprise = idEntreprise;
        this.idExercice = idExercice;
        this.nom = nom;
        this.capacite = capacite;
        this.nomLocal = nomLocal;
        this.signature = signature;
        this.beta = beta;
    }

    public Classe() {
    }
    
    

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }
    
    

    public long getSignature() {
        return signature;
    }

    public void setSignature(long signature) {
        this.signature = signature;
    }
    
    

    public String getNomLocal() {
        return nomLocal;
    }

    public void setNomLocal(String nomLocal) {
        this.nomLocal = nomLocal;
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

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    @Override
    public String toString() {
        return "XX_Classe{" + "id=" + id + ", idUtilisateur=" + idUtilisateur + ", idEntreprise=" + idEntreprise + ", idExercice=" + idExercice + ", nom=" + nom + ", capacite=" + capacite + ", nomLocal=" + nomLocal + ", signature=" + signature + ", beta=" + beta + '}';
    }
}
