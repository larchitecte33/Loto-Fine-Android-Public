package com.projects.loto_fine.classes_metier;

/**
 * Classe qui repr�sente un participant inscrit � une partie de loto.
 * Cette classe fait partie du mod�le.
 */
public class Inscrit {
    private int id; // Identifiant
    private String nom; // Nom du participant inscrit
    private String prenom; // Pr�nom du participant inscrit
    private String email; // E-mail du participant inscrit
    private String numtel; // Num�ro de t�l�phone du participant inscrit
    private boolean isInscriptionValidee; // L'inscription est-elle valid�e ?

    // Accesseurs
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumtel() {
        return numtel;
    }

    public void setNumtel(String numtel) {
        this.numtel = numtel;
    }

    public boolean isInscriptionValidee() {
        return isInscriptionValidee;
    }

    public void setInscriptionValidee(boolean inscriptionValidee) {
        isInscriptionValidee = inscriptionValidee;
    }
}
