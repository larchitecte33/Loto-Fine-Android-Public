package com.projects.loto_fine.classes_metier;

/**
 * Classe qui représente une case d'un carton de loto
 * Cette classe fait partie du modèle.
 */
public class CaseCarton {
    private int id; // L'identifiant de la case.
    private int valeur; // La valeur de la case (-1 pour une case vide).
    private int posX; // La position en X de la case.
    private int posY; // La position en Y de la case.
    private boolean isOccupee; // True si la case occupée, false sinon.

    // Constructeur par défaut
    public CaseCarton() {
    }

    // Constructeur
    public CaseCarton(int id, int valeur, int posX, int posY, boolean isOccupee) {
        this.id = id;
        this.valeur = valeur;
        this.posX = posX;
        this.posY = posY;
        this.isOccupee = isOccupee;
    }

    // Accesseurs
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getValeur() {
        return valeur;
    }

    public void setValeur(int valeur) {
        this.valeur = valeur;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public boolean isOccupee() {
        return isOccupee;
    }

    public void setOccupee(boolean occupee) {
        isOccupee = occupee;
    }
}
