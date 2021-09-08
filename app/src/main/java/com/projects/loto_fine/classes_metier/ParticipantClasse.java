package com.projects.loto_fine.classes_metier;

public class ParticipantClasse extends Personne {
    private int numeroClassement;
    private int nbLotsGagnes;
    private int nbParticipations;
    private float nbMoyenCartons;

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
