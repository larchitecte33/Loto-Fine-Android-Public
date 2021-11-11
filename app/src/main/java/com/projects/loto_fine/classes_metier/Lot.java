package com.projects.loto_fine.classes_metier;

/**
 * Classe qui représente un lot d'une partie de loto.
 * Cette classe fait partie du modèle.
 */
public class Lot {
    private int id; // Identifiant du lot
    private String nom; // Libellé du lot
    private double valeur; // Valeur du lot en euros
    private int position; // Position du lot dans la partie
    private boolean auCartonPlein; // Le lot est-il au carton plein ?
    private boolean enCoursDeJeu; // Le lot est-il en cours de jeu ?
    private String adresseRetrait, cpRetrait, villeRetrait; // Adresse, code postal et ville de retrait du lot.
    private Partie partie; // Partie dans laquelle est mis en jeu le lot.
    private Personne gagnant; // Personne ayant remporté le lot.

    // Constructeur
    public Lot(int id, String nom, double valeur, int position, boolean auCartonPlein, boolean enCoursDeJeu, Partie partie) {
        this.id = id;
        this.nom = nom;
        this.valeur = valeur;
        this.position = position;
        this.auCartonPlein = auCartonPlein;
        this.enCoursDeJeu = enCoursDeJeu;
        this.partie = partie;
    }

    // Constructeur par défaut
    public Lot() {
    }

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

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isAuCartonPlein() {
        return auCartonPlein;
    }

    public void setAuCartonPlein(boolean auCartonPlein) {
        this.auCartonPlein = auCartonPlein;
    }

    public boolean isEnCoursDeJeu() {
        return enCoursDeJeu;
    }

    public void setEnCoursDeJeu(boolean enCoursDeJeu) {
        this.enCoursDeJeu = enCoursDeJeu;
    }

    public String getAdresseRetrait() {
        return adresseRetrait;
    }

    public void setAdresseRetrait(String adresseRetrait) {
        this.adresseRetrait = adresseRetrait;
    }

    public String getCpRetrait() {
        return cpRetrait;
    }

    public void setCpRetrait(String cpRetrait) {
        this.cpRetrait = cpRetrait;
    }

    public String getVilleRetrait() {
        return villeRetrait;
    }

    public void setVilleRetrait(String villeRetrait) {
        this.villeRetrait = villeRetrait;
    }

    public Partie getPartie() {
        return partie;
    }

    public void setPartie(Partie partie) {
        this.partie = partie;
    }

    public Personne getGagnant() {
        return gagnant;
    }

    public void setGagnant(Personne gagnant) {
        this.gagnant = gagnant;
    }
}
