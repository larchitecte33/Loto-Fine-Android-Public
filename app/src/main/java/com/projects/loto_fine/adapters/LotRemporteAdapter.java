package com.projects.loto_fine.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.projects.loto_fine.R;
import com.projects.loto_fine.activites.ChoixLieuRetraitActivity;
import com.projects.loto_fine.activites.MesLotsActivity;
import com.projects.loto_fine.classes_metier.Lot;
import com.projects.loto_fine.classes_utilitaires.OuiNonDialogFragment;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * ArrayAdapter permettant l'affichage des éléments de la liste des lots remportés par un participant.
 */
public class LotRemporteAdapter extends ArrayAdapter<Lot> {
    private final Context context;
    private LinkedList<Lot> lots; // Liste des lots à afficher.
    private AppCompatActivity activity;
    private int idLot;
    private String adresseRetrait, cpRetrait, villeRetrait, nomLot;

    /**
     * Constructeur
     * @param activity : activité appelante.
     * @param context : contexte de l'activité appelante.
     * @param ressource : ID du layout affichant un élément de la liste des lots.
     * @param lots : liste des lots à afficher.
     */
    public LotRemporteAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<Lot> lots) {
        super(context, ressource, lots);
        this.activity = activity;
        this.context = context;
        this.lots = lots;
    }

    /**
     * Fonction utilisée pour convertir un objet de la liste des lots remportés en vue.
     * @param position : la position de l'item dans l'ensemble de données de l'adapter.
     * @param convertView : l'ancienne vue à réutiliser (si c'est possible, pour des questions de performances).
     * @param parent : vue contenant cette vue.
     * @return la vue qui affiche les données à la position d'index position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // S'il n'existe pas de vue qu'on peut réutiliser pour afficher la vue en cours d'affichage, on va la construire.
        if (convertView == null) {
            // On récupère une instance de LayoutInflater standard qui est déjà connectée au contexte actuel et correctement configurée pour l'appareil.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // On construit un objet View à partir du fichier XML R.layout.activity_mes_lots_adapter
            convertView = inflater.inflate(R.layout.activity_mes_lots_adapter, parent, false);
        }
        // Sinon, on va la réutiliser.
        else {
            convertView = (LinearLayout) convertView;
        }

        // Récupération des composants.
        TextView tvNomLot = (TextView)convertView.findViewById(R.id.mes_lots_adapter_nom_lot);
        TextView tvLieuRetrait = (TextView)convertView.findViewById(R.id.mes_lots_adapter_lieu_retrait);
        TextView tvLotExpedie = (TextView)convertView.findViewById(R.id.mes_lots_adapter_lot_expedie);
        LinearLayout llRetirerExpedierLot = (LinearLayout)convertView.findViewById(R.id.mes_lots_adapter_layout_retirer_expedier_lot);
        Button btRetirerSurPlace = (Button)convertView.findViewById(R.id.mes_lots_adapter_bouton_retirer_sur_place);
        Button btMExpedierLot = (Button)convertView.findViewById(R.id.mes_lots_adapter_bouton_me_lexpedier);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btRetirerSurPlace.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btRetirerSurPlace.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));
        btMExpedierLot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btMExpedierLot.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));

        // Récupération des valeurs de l'item affiché.
        idLot = lots.get(position).getId();
        nomLot = lots.get(position).getNom();
        adresseRetrait = lots.get(position).getAdresseRetrait();
        cpRetrait = lots.get(position).getCpRetrait();
        villeRetrait = lots.get(position).getVilleRetrait();

        tvNomLot.setText(nomLot);

        // Si l'adresse de retrait n'est pas définie
        if((adresseRetrait.equals("null")) || (cpRetrait.equals("null")) || (villeRetrait.equals("null"))) {
            llRetirerExpedierLot.setVisibility(View.VISIBLE);
            tvLotExpedie.setVisibility(View.GONE);
            tvLieuRetrait.setText("Lieu de retrait : " + lots.get(position).getPartie().getAdresse() + " " + lots.get(position).getPartie().getCp() +
                    " " + lots.get(position).getPartie().getVille());
        }
        // Si l'adresse de retrait est définie
        else if((!adresseRetrait.trim().equals("")) || (!cpRetrait.trim().equals("")) || (!villeRetrait.trim().equals(""))) {
            llRetirerExpedierLot.setVisibility(View.GONE);
            tvLotExpedie.setVisibility(View.VISIBLE);

            tvLieuRetrait.setText("Lieu de retrait : " + adresseRetrait + " " + cpRetrait + " " + villeRetrait);

            // Si l'adresse de retrait du lot correspond à l'adresse de la partie
            if((adresseRetrait.trim().equals(lots.get(position).getPartie().getAdresse().trim()))
                    && (cpRetrait.trim().equals(lots.get(position).getPartie().getCp().trim()))
                    && (villeRetrait.trim().equals(lots.get(position).getPartie().getVille().trim()))) {
                tvLotExpedie.setTextColor(R.color.vert);
                tvLotExpedie.setText("Lot à retirer sur place");
            }
            else {
                tvLotExpedie.setTextColor(R.color.vert);
                tvLotExpedie.setText("Lot expédié");
            }
        }
        // Sinon, on va laisser choisir le participant.
        else {
            llRetirerExpedierLot.setVisibility(View.VISIBLE);
            tvLotExpedie.setVisibility(View.GONE);
        }

        // Clic sur le bouton Retirer sur place.
        btRetirerSurPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupération des valeurs de l'item cliqué.
                idLot = lots.get(position).getId();
                adresseRetrait = lots.get(position).getPartie().getAdresse();
                cpRetrait = lots.get(position).getPartie().getCp();
                villeRetrait = lots.get(position).getPartie().getVille();

                // Le fragment OuiNonDialogFragment va être utilisé pour faire passer des valeurs à l'activité.
                HashMap<String, String> args = new HashMap<>();
                args.put("idLot", String.valueOf(idLot));
                args.put("adresseRetrait", adresseRetrait);
                args.put("cpRetrait", cpRetrait);
                args.put("villeRetrait", villeRetrait);

                OuiNonDialogFragment ondf = new OuiNonDialogFragment("Etes-vous sûr de vouloir retirer ce lot à l'adresse de la partie ?",
                        "retirerLotAAdressePartie", "nePasRetirerLot", args);
                ondf.show(((MesLotsActivity)getContext()).getSupportFragmentManager(), "");
            }
        });

        // Clic sur le bouton Me l'expédier.
        btMExpedierLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupération des valeurs de l'item cliqué.
                idLot = lots.get(position).getId();
                nomLot = lots.get(position).getNom();

                Intent intent = new Intent(getContext(), ChoixLieuRetraitActivity.class);
                intent.putExtra("idLot", idLot);
                intent.putExtra("nomLot", nomLot);
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }
}
