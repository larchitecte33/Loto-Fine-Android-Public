package com.projects.loto_fine.classes_metier;

/**
 * Classe qui représente l'inscription d'un participant à une partie.
 * Cette classe fait partie du modèle.
 */
public class Inscription {
    private int idPartie; // L'identifiant de la partie
    private boolean isValidee; // L'inscription est-elle validée ?
    private double montant; // Le montant de l'inscription

    /* Getters et setters */
    public int getIdPartie() {
        return idPartie;
    }

    public void setIdPartie(int idPartie) {
        this.idPartie = idPartie;
    }

    public boolean isValidee() {
        return isValidee;
    }

    public void setValidee(boolean validee) {
        isValidee = validee;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }
}
