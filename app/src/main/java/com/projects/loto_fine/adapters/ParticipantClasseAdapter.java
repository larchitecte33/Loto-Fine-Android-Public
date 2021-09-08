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

public class ParticipantClasseAdapter extends ArrayAdapter<ParticipantClasse> {

    private final Context context;
    private LinkedList<ParticipantClasse> participantsClasses;
    private AppCompatActivity activity;
    private String critere;

    public ParticipantClasseAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<ParticipantClasse> participantsClasses,
                                    String critere) {
        super(context, ressource, participantsClasses);
        this.activity = activity;
        this.context = context;
        this.participantsClasses = participantsClasses;
        this.critere = critere;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_classements_adapter, parent, false);
        }
        else {
            convertView = (LinearLayout)convertView;
        }

        TextView tvPosNomPrenom = (TextView) convertView.findViewById(R.id.classements_adapter_pos_nom_prenom);
        TextView tvNbLots = (TextView) convertView.findViewById(R.id.classements_adapter_nb_lots);
        TextView tvNbParticipations = (TextView) convertView.findViewById(R.id.classements_adapter_nb_participations);
        TextView tvNbMoyenCartons = (TextView) convertView.findViewById(R.id.classements_adapter_nb_moyen_cartons);
        int nbLotsGagnes, nbParticipations;
        float nbCartonsEnMoyenne;

        tvNbLots.setVisibility(View.GONE);
        tvNbParticipations.setVisibility(View.GONE);
        tvNbMoyenCartons.setVisibility(View.GONE);

        if(critere.equals(Constants.CRITERE_NB_PARTICIPATIONS)) {
            tvNbParticipations.setVisibility(View.VISIBLE);
            nbParticipations = participantsClasses.get(position).getNbParticipations();

            if(nbParticipations > 1) {
                tvNbParticipations.setText(String.valueOf(nbParticipations) + " participations");
            }
            else {
                tvNbParticipations.setText(String.valueOf(nbParticipations) + " participation");
            }
        }
        else if(critere.equals(Constants.CRITERE_NB_LOTS_GAGNES)) {
            tvNbLots.setVisibility(View.VISIBLE);
            nbLotsGagnes = participantsClasses.get(position).getNbLotsGagnes();

            if(nbLotsGagnes > 1) {
                tvNbLots.setText(String.valueOf(nbLotsGagnes) + " lots gagnés");
            }
            else {
                tvNbLots.setText(String.valueOf(nbLotsGagnes) + " lot gagné");
            }
        }
        else {
            tvNbMoyenCartons.setVisibility(View.VISIBLE);
            nbCartonsEnMoyenne = participantsClasses.get(position).getNbMoyenCartons();

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

        tvPosNomPrenom.setText(strPosNomPrenom + participantsClasses.get(position).getNom() + " " + participantsClasses.get(position).getPrenom());


        return convertView;
    }
}
