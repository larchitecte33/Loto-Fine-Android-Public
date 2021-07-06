package com.projects.loto_fine.classes_metier;

/**
 * Classe représentant un élément cliquable.
 */
public class ElementCliquable {
    private String id; // Identidiant de l'élément
    private int posX; // Position en x de l'élément
    private int posY; // Position en y de l'élément
    private int width; // Largeur de l'élément
    private int height; // Hauteur de l'élément

    public ElementCliquable(String id, int posX, int posY, int width, int height) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
