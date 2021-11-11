package com.projects.loto_fine.classes_metier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe qui repr�sente une partie de loto.
 * Cette classe fait partie du mod�le.
 */
public class Partie {
    private int id; // Identifiant de la partie
    private Date date; // Date de la partie
    private String adresse; // Adresse de la partie
    private String ville; // Ville dans laquelle est organis�e la partie (utile pour la d�finition du lieu de retrait par d�faut).
    private String cp; // Code postal de la ville dans laquelle est organis�e la partie.
    private ArrayList<Lot> lots; // Lots mis en jeu dans la partie.
    private double prixCarton; // Prix du carton
    private Personne animateur; // Animateur de la partie

    // Constructeur
    public Partie(int id, Date date, String adresse, String ville, String cp, ArrayList<Lot> lots, double prixCarton, Personne animateur) { // LocalDateTime
        this.id = id;
        this.date = date;
        this.adresse = adresse;
        this.ville = ville;
        this.cp = cp;
        this.lots = lots;
        this.prixCarton = prixCarton;
        this.animateur = animateur;
    }

    // Accesseurs
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public ArrayList<Lot> getLots() {
        return lots;
    }

    public void setLots(ArrayList<Lot> lots) {
        this.lots = lots;
    }

    public double getPrixCarton() {
        return prixCarton;
    }

    public void setPrixCarton(double prixCarton) {
        this.prixCarton = prixCarton;
    }

    public Personne getAnimateur() {
        return animateur;
    }

    public void setAnimateur(Personne animateur) {
        this.animateur = animateur;
    }
}

