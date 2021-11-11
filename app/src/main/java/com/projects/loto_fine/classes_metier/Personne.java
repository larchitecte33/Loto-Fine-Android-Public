package com.projects.loto_fine.classes_metier;

/**
 * Classe qui représente une personne.
 * Cette classe fait partie du modèle.
 */
public class Personne {
    private int id; // Identifiant de la personne
    private String nom; // Nom de la personne
    private String prenom; // Prénom de la personne
    private String email; // E-mail de la personne
    private String mdp; // Mot de passe de la personne
    private String adresse; // Adresse de résidence de la personne
    private String ville; // Ville de résidence de la personne
    private String cp; // Code postal du lieu de résidence de la personne
    private String numtel; // numéro de téléphone de la personne

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

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getNumtel() {
        return numtel;
    }

    public void setNumtel(String numtel) {
        this.numtel = numtel;
    }
}
