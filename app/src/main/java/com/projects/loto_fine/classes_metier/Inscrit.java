package com.projects.loto_fine.classes_metier;

public class Inscrit {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String numtel;
    private boolean isInscriptionValidee;

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
