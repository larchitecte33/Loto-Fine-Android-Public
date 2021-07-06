package com.projects.loto_fine.classes_metier;

public class Lot {
    private int id;
    private String nom;
    private double valeur;
    private int position;
    private boolean auCartonPlein;
    private boolean enCoursDeJeu;
    private String adresseRetrait, cpRetrait, villeRetrait;
    private Partie partie;

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
}
