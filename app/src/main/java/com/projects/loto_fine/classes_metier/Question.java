package com.projects.loto_fine.classes_metier;

public class Question {
    private int id, reponseSelectionnee;
    private String texte;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public int getReponseSelectionnee() {
        return reponseSelectionnee;
    }

    public void setReponseSelectionnee(int reponseSelectionnee) {
        this.reponseSelectionnee = reponseSelectionnee;
    }
}
