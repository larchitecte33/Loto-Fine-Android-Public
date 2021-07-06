package com.projects.loto_fine.classes_metier;

/**
 * Classe qui représente une case d'un carton de quine
 * Cette classe fait partie du modèle.
 */
public class CaseCarton {
    private int id;
    private int valeur;
    private int posX;
    private int posY;
    private boolean isOccupee;

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
