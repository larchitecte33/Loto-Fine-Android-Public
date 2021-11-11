package com.projects.loto_fine.classes_metier;

/**
 * Classe qui représente une question.
 * Cette classe fait partie du modèle.
 */
public class Question {
    private int id; // Identifiant de la question
    private int reponseSelectionnee; // Identifiant de la réponse sélectionnée
    private String texte; // Texte de la question

    // Accesseurs
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
