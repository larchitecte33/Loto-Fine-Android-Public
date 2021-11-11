package com.projects.loto_fine.classes_metier;

/**
 * Classe qui repr�sente un participant class�.
 * Cette classe fait partie du mod�le.
 */
public class ParticipantClasse extends Personne {
    private int numeroClassement; // Num�ro du classement
    private int nbLotsGagnes; // Nombre de lots gagn�s
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
