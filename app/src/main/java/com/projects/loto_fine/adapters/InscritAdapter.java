package com.projects.loto_fine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Inscrit;

import java.util.LinkedList;

public class InscritAdapter extends ArrayAdapter<Inscrit> {
    private final Context context;
    private LinkedList<Inscrit> inscrits;
    private AppCompatActivity activity;

    public InscritAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<Inscrit> inscrits) {
        super(context, ressource, inscrits);
        this.context = context;
        this.inscrits = inscrits;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_visualiser_liste_inscrits_adapter, parent, false);
        } else {
            convertView = (LinearLayout) convertView;
        }

        TextView tvNomInscrit = (TextView) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_nom_inscrit);
        TextView tvPrenomInscrit = (TextView) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_prenom_inscrit);
        TextView tvEmailInscrit = (TextView) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_email_inscrit);
        TextView tvNumTelInscrit = (TextView) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_numtel_inscrit);
        CheckBox cbInscriptionValidee = (CheckBox) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_inscription_validee);

        String nomInscrit = inscrits.get(position).getNom();
        String prenomInscrit = inscrits.get(position).getPrenom();
        String emailInscrit = inscrits.get(position).getEmail();
        String numTelInscrit = inscrits.get(position).getNumtel();
        boolean isInscriptionValidee = inscrits.get(position).isInscriptionValidee();
        int idInscrit;

        tvNomInscrit.setText("Nom : " + nomInscrit);
        tvPrenomInscrit.setText("Prénom : " + prenomInscrit);
        tvEmailInscrit.setText("Adresse e-mail : " + emailInscrit);
        tvNumTelInscrit.setText("Numéro de téléphone : " + numTelInscrit);
        cbInscriptionValidee.setChecked(isInscriptionValidee);

        cbInscriptionValidee.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int idInscrit = inscrits.get(position).getId();

                if(isChecked) {
                    inscrits.get(position).setInscriptionValidee(true);
                }
                else {
                    inscrits.get(position).setInscriptionValidee(false);
                }
            }
        });

        return convertView;
    }
}
