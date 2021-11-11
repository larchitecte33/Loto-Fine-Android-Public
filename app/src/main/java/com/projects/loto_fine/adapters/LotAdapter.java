package com.projects.loto_fine.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.projects.loto_fine.activites.ModificationLotActivity;
import com.projects.loto_fine.activites.VisualiserListeLotsActivity;
import com.projects.loto_fine.classes_metier.Lot;
import com.projects.loto_fine.classes_utilitaires.OuiNonDialogFragment;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * ArrayAdapter permettant l'affichage des éléments de la liste des lots.
 */
public class LotAdapter extends ArrayAdapter<Lot> {
    private final Context context;
    private LinkedList<Lot> lots; // Liste des lots à afficher.
    private int idPartie, idLot, positionLot;
    private String emailAnimateurPartie, nomLot;
    private SharedPreferences sharedPref;
    private String adresseServeur;
    private AppCompatActivity activity;
    private double valeurLot;
    private boolean isCartonPlein;
    private String source;

    /**
     * Constructeur
     * @param activity : activité appelante.
     * @param context : contexte de l'activité appelante.
     * @param ressource : ID du layout affichant un élément de la liste des lots.
     * @param lots : liste des lots à afficher.
     * @param idPartie : identifiant de la partie (utilisé pour faire appel à l'activité ModificationLotActivity).
     * @param emailAnimateurPartie : e-mail de l'animateur de la partie.
     * @param source : précise l'activité qui a construit cet ArrayAdapter.
     */
    public LotAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<Lot> lots,
                      int idPartie, String emailAnimateurPartie, String source) {
        super(context, ressource, lots);
        this.activity = activity;
        this.context = context;
        this.lots = lots;
        this.idPartie = idPartie;
        this.emailAnimateurPartie = emailAnimateurPartie;
        this.source = source;
    }

    /**
     * Fonction utilisée pour convertir un objet de la liste des lots en vue.
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
            // On construit un objet View à partir du fichier XML R.layout.activity_visualiser_liste_lots_adapter.
            convertView = inflater.inflate(R.layout.activity_visualiser_liste_lots_adapter, parent, false);
        }
        // Sinon, on va la réutiliser.
        else {
            convertView = (LinearLayout) convertView;
        }

        sharedPref = getContext().getSharedPreferences("MyData", Context.MODE_PRIVATE);

        // Récupération des composants.
        TextView tvNomLot = (TextView)convertView.findViewById(R.id.visualiser_liste_lots_adapter_nom_lot);
        TextView tvValeurLot = (TextView)convertView.findViewById(R.id.visualiser_liste_lots_adapter_valeur_lot);
        TextView tvCartonPlein = (TextView)convertView.findViewById(R.id.visualiser_liste_lots_adapter_carton_plein);
        TextView tvPositionLot = (TextView)convertView.findViewById(R.id.visualiser_liste_lots_adapter_position_lot);
        TextView tvRemportePar = (TextView)convertView.findViewById(R.id.visualiser_liste_lots_adapter_remporte_par);
        TextView tvAdresseRetrait = (TextView)convertView.findViewById(R.id.visualiser_liste_lots_adapter_adresse_retrait);
        Button btnModifierLot = (Button)convertView.findViewById(R.id.visualiser_liste_lots_adapter_bouton_modifier_lot);
        Button btnSupprimerLot = (Button)convertView.findViewById(R.id.visualiser_liste_lots_adapter_bouton_supprimer_lot);
        LinearLayout layoutModifierSupprimerLot = (LinearLayout)convertView.findViewById(R.id.visualiser_liste_lots_adapter_layout_modifier_supprimer_lot);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnModifierLot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnModifierLot.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));
        btnSupprimerLot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnSupprimerLot.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));

        // Récupération des valeurs de l'item affiché.
        idLot = lots.get(position).getId();
        nomLot = lots.get(position).getNom();
        valeurLot = lots.get(position).getValeur();
        isCartonPlein = lots.get(position).isAuCartonPlein();
        positionLot = lots.get(position).getPosition();

        adresseServeur = sharedPref.getString("AdresseServeur", "");

        // On affecte aux composants les valeurs de l'item affiché.
        tvNomLot.setText("Designation du lot : " + nomLot);
        tvValeurLot.setText("Valeur du lot : " + (double)Math.round(valeurLot * 100) / 100);

        if(isCartonPlein) {
            tvCartonPlein.setText("Au carton plein");
        }
        else {
            tvCartonPlein.setText("A la ligne");
        }

        tvPositionLot.setText("Position du lot : " + positionLot);

        String email = sharedPref.getString("emailUtilisateur", "");

        // Si l'utilisateur connecté est l'animateur de la partie et si le lot n'a pas été gagné, alors
        // on affiche les boutons "Modifier le lot" et "Supprimer le lot".
        if(email.equals(emailAnimateurPartie) && (lots.get(position).getGagnant() == null)) {
            layoutModifierSupprimerLot.setVisibility(View.VISIBLE);
            btnModifierLot.setVisibility(View.VISIBLE);
            btnSupprimerLot.setVisibility(View.VISIBLE);
            tvRemportePar.setVisibility(View.GONE);
            tvAdresseRetrait.setVisibility(View.GONE);
        }
        // Si l'utilisateur connecté n'est pas l'animateur de la partie ou si le lot a été gagné, on masque ces boutons.
        else {
            layoutModifierSupprimerLot.setVisibility(View.GONE);
            btnModifierLot.setVisibility(View.GONE);
            btnSupprimerLot.setVisibility(View.GONE);

            // Si l'utilisateur connecté est l'animateur de la partie, on va afficher les coordonées de la personne ayant remporté le lot ainsi que
            // l'adresse de retrait choisie par celle-ci.
            if(email.equals(emailAnimateurPartie)) {
                tvRemportePar.setVisibility(View.VISIBLE);
                tvRemportePar.setText("Remporté par : " + lots.get(position).getGagnant().getNom() + " " + lots.get(position).getGagnant().getPrenom() +
                        " (email = " + lots.get(position).getGagnant().getEmail() + ")");

                if ((lots.get(position).getAdresseRetrait() != null) || (lots.get(position).getCpRetrait() != null)
                        || (lots.get(position).getVilleRetrait() != null)) {
                    tvAdresseRetrait.setVisibility(View.VISIBLE);

                    String adresseRetrait = "Adresse retrait :";

                    if (lots.get(position).getAdresseRetrait() != null) {
                        adresseRetrait += " ";
                        adresseRetrait += lots.get(position).getAdresseRetrait();
                    }

                    if (lots.get(position).getCpRetrait() != null) {
                        adresseRetrait += " ";
                        adresseRetrait += lots.get(position).getCpRetrait();
                    }

                    if (lots.get(position).getVilleRetrait() != null) {
                        adresseRetrait += " ";
                        adresseRetrait += lots.get(position).getVilleRetrait();
                    }

                    tvAdresseRetrait.setText(adresseRetrait);
                } else {
                    tvAdresseRetrait.setVisibility(View.GONE);
                }
            }
            else {
                tvRemportePar.setVisibility(View.GONE);
                tvAdresseRetrait.setVisibility(View.GONE);
            }
        }

        // ******************************** Gestion des clics sur boutons *********************** //
        // Clic sur le bouton Modifier le lot.
        btnModifierLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupération des valeurs de l'item cliqué.
                idLot = lots.get(position).getId();
                nomLot = lots.get(position).getNom();
                valeurLot = lots.get(position).getValeur();
                isCartonPlein = lots.get(position).isAuCartonPlein();
                positionLot = lots.get(position).getPosition();

                // On démarre l'activité ModificationLotActivity en lui passant toutes les valeurs dont elle a besoin en extra.
                Intent intent = new Intent(getContext(), ModificationLotActivity.class);
                intent.putExtra("idPartie", idPartie);
                intent.putExtra("emailAnimateur", emailAnimateurPartie);
                intent.putExtra("source", source);
                intent.putExtra("idLot", idLot);
                intent.putExtra("nomLot", nomLot);
                intent.putExtra("valeurLot", valeurLot);
                intent.putExtra("isCartonPlein", isCartonPlein);
                intent.putExtra("position", positionLot);
                getContext().startActivity(intent);
            }
        });

        // Clic sur le bouton Supprimer le lot.
        btnSupprimerLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupération de l'id de l'item cliqué.
                idLot = lots.get(position).getId();

                // Ici, on ne démarre pas de nouvelle activité. Donc l'identifiant du lot ne va pas être passé dans l'Intent.
                // A la place, le fragment OuiNonDialogFragment va être utilisé pour faire passer l'identifiant à l'activité.
                HashMap<String, String> args = new HashMap<>();
                args.put("idLot", String.valueOf(idLot));
                OuiNonDialogFragment ondf = new OuiNonDialogFragment("Etes-vous sûr de vouloir supprimer ce lot ?", "supprimerLot", "conserverLot", args);
                ondf.show(((VisualiserListeLotsActivity)getContext()).getSupportFragmentManager(), "");
            }
        });

        return convertView;
    }
}
