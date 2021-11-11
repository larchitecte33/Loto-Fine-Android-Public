package com.projects.loto_fine.classes_metier;

/**
 * Classe qui représente un participant classé.
 * Cette classe fait partie du modèle.
 */
public class ParticipantClasse extends Personne {
    private int numeroClassement; // Numéro du classement
    private int nbLotsGagnes; // Nombre de lots gagnés
    private int nbParticipations; // Nombre de participations
    private float nbMoyenCartons; // Nombre moyen de cartons

    // Accesseurs
    public int getNumeroClassement() {
        return numeroClassement;
    }

    public void setNumeroClassement(int numeroClassement) {
        this.numeroClassement = numeroClassement;
    }

    public int getNbLotsGagnes() {
        return nbLotsGagnes;
    }

    public void setNbLotsGagnes(int nbLotsGagnes) {
        this.nbLotsGagnes = nbLotsGagnes;
    }

    public int getNbParticipations() {
        return nbParticipations;
    }

    public void setNbParticipations(int nbParticipations) {
        this.nbParticipations = nbParticipations;
    }

    public float getNbMoyenCartons() {
        return nbMoyenCartons;
    }

    public void setNbMoyenCartons(float nbMoyenCartons) {
        this.nbMoyenCartons = nbMoyenCartons;
    }
}
