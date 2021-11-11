package com.projects.loto_fine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.ParticipantClasse;

import java.util.LinkedList;

/**
 * ArrayAdapter permettant l'affichage des éléments de la liste des participants classés.
 */
public class ParticipantClasseAdapter extends ArrayAdapter<ParticipantClasse> {

    private final Context context;
    private LinkedList<ParticipantClasse> participantsClasses; // Liste des particpants classés
    private AppCompatActivity activity;
    private String critere; // Critère du classement (nb participations, nb lots gagnés ou nb moyen de cartons).

    /**
     * Constructeur
     * @param activity : activité appelante.
     * @param context : contexte de l'activité appelante.
     * @param ressource : ID du layout affichant un élément de la liste des participants classés.
     * @param participantsClasses : liste des participants classés.
     * @param critere : Critère du classement (nb participations, nb lots gagnés ou nb moyen de cartons).
     */
    public ParticipantClasseAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<ParticipantClasse> participantsClasses,
                                    String critere) {
        super(context, ressource, participantsClasses);
        this.activity = activity;
        this.context = context;
        this.participantsClasses = participantsClasses;
        this.critere = critere;
    }

    /**
     * Fonction utilisée pour convertir un objet de la liste des participants classés en vue.
     * @param position : la position de l'item dans l'ensemble de données de l'adapter.
     * @param convertView : l'ancienne vue à réutiliser (si c'est possible, pour des questions de performances).
     * @param parent : vue contenant cette vue.
     * @return la vue qui affiche les données à la position d'index position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // S'il n'existe pas de vue qu'on peut réutiliser pour afficher la vue en cours d'affichage, on va la construire.
        if(convertView == null) {
            // On récupère une instance de LayoutInflater standard qui est déjà connectée au contexte actuel et correctement configurée pour l'appareil.
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // On construit un objet View à partir du fichier XML R.layout.activity_classements_adapter.
            convertView = inflater.inflate(R.layout.activity_classements_adapter, parent, false);
        }
        else {
            // Sinon, on va la réutiliser.
            convertView = (LinearLayout)convertView;
        }

        // Récupération des composants.
        TextView tvPosNomPrenom = (TextView) convertView.findViewById(R.id.classements_adapter_pos_nom_prenom);
        TextView tvNbLots = (TextView) convertView.findViewById(R.id.classements_adapter_nb_lots);
        TextView tvNbParticipations = (TextView) convertView.findViewById(R.id.classements_adapter_nb_participations);
        TextView tvNbMoyenCartons = (TextView) convertView.findViewById(R.id.classements_adapter_nb_moyen_cartons);
        int nbLotsGagnes, nbParticipations;
        float nbCartonsEnMoyenne;

        // Par défaut, on masque la vue affichant le nombre de lots remportés, celle affichant le nombre de participations, et celle affichant le nombre
        // moyen de cartons par partie.
        tvNbLots.setVisibility(View.GONE);
        tvNbParticipations.setVisibility(View.GONE);
        tvNbMoyenCartons.setVisibility(View.GONE);

        // Si le critère du classement est le nombre de participations
        if(critere.equals(Constants.CRITERE_NB_PARTICIPATIONS)) {
            // On affiche la vue affichant le nombre de participations.
            tvNbParticipations.setVisibility(View.VISIBLE);
            // On récupère le nombre de participations.
            nbParticipations = participantsClasses.get(position).getNbParticipations();

            // On modifie le texte de la vue avec le nombre de participations.
            if(nbParticipations > 1) {
                tvNbParticipations.setText(String.valueOf(nbParticipations) + " participations");
            }
            else {
                tvNbParticipations.setText(String.valueOf(nbParticipations) + " participation");
            }
        }
        // Si le critère du classement est le nombre de nombre de lots gagnés
        else if(critere.equals(Constants.CRITERE_NB_LOTS_GAGNES)) {
            // On affiche la vue affichant le nombre de lots gagnés.
            tvNbLots.setVisibility(View.VISIBLE);
            // On récupère le nombre de lots gagnés.
            nbLotsGagnes = participantsClasses.get(position).getNbLotsGagnes();

            // On modifie le texte de la vue avec le nombre de lots gagnés.
            if(nbLotsGagnes > 1) {
                tvNbLots.setText(String.valueOf(nbLotsGagnes) + " lots gagnés");
            }
            else {
                tvNbLots.setText(String.valueOf(nbLotsGagnes) + " lot gagné");
            }
        }
        // Si le critère du classement est le nombre moyen de cartons
        else {
            // On affiche la vue affichant le nombre moyen de cartons.
            tvNbMoyenCartons.setVisibility(View.VISIBLE);
            // On récuère le nombre moyen de cartons.
            nbCartonsEnMoyenne = participantsClasses.get(position).getNbMoyenCartons();

            // On modifie le texte de la vue avec le nombre moyen de cartons.
            if(nbCartonsEnMoyenne > 1) {
                tvNbMoyenCartons.setText(String.valueOf(nbCartonsEnMoyenne) + " cartons en moyenne");
            }
            else {
                tvNbMoyenCartons.setText(String.valueOf(nbCartonsEnMoyenne) + " carton en moyenne");
            }
        }

        String strPosNomPrenom;

        if(participantsClasses.get(position).getNumeroClassement() == 1) {
            strPosNomPrenom = participantsClasses.get(position).getNumeroClassement() + "er : ";
        }
        else {
            strPosNomPrenom = participantsClasses.get(position).getNumeroClassement() + "ème : ";
        }

        // Position + nom + prénom
        tvPosNomPrenom.setText(strPosNomPrenom + participantsClasses.get(position).getNom() + " " + participantsClasses.get(position).getPrenom());


        return convertView;
    }
}
