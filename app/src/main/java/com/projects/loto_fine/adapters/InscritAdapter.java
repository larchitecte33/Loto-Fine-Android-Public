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

/**
 * ArrayAdapter permettant l'affichage des éléments de la liste des inscrits.
 */
public class InscritAdapter extends ArrayAdapter<Inscrit> {
    private final Context context;
    private LinkedList<Inscrit> inscrits; // Liste des inscrits à afficher.
    private AppCompatActivity activity;

    /**
     * Constructeur
     * @param activity : activité appelante.
     * @param context : contexte de l'activité appelante.
     * @param ressource : ID du layout affichant un élément de la liste des inscrits.
     * @param inscrits : liste des inscrits à afficher.
     */
    public InscritAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<Inscrit> inscrits) {
        // Appel du constructeur parent.
        super(context, ressource, inscrits);
        this.context = context;
        this.inscrits = inscrits;
        this.activity = activity;
    }

    /**
     * Fonction utilisée pour convertir un objet de la liste des Inscrits en vue.
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
            // On construit un objet View à partir du fichier XML R.layout.activity_visualiser_liste_inscrits_adapter.
            convertView = inflater.inflate(R.layout.activity_visualiser_liste_inscrits_adapter, parent, false);
        }
        // Sinon, on va la réutiliser.
        else {
            convertView = (LinearLayout) convertView;
        }

        // Récupération des composants.
        TextView tvNomInscrit = (TextView) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_nom_inscrit);
        TextView tvPrenomInscrit = (TextView) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_prenom_inscrit);
        TextView tvEmailInscrit = (TextView) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_email_inscrit);
        TextView tvNumTelInscrit = (TextView) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_numtel_inscrit);
        CheckBox cbInscriptionValidee = (CheckBox) convertView.findViewById(R.id.visualiser_liste_inscrits_adapter_inscription_validee);

        // Récupération des valeurs de l'item affiché.
        String nomInscrit = inscrits.get(position).getNom();
        String prenomInscrit = inscrits.get(position).getPrenom();
        String emailInscrit = inscrits.get(position).getEmail();
        String numTelInscrit = inscrits.get(position).getNumtel();
        boolean isInscriptionValidee = inscrits.get(position).isInscriptionValidee();

        int idInscrit;

        // On affecte aux composants les valeurs de l'item affiché.
        tvNomInscrit.setText("Nom : " + nomInscrit);
        tvPrenomInscrit.setText("Prénom : " + prenomInscrit);
        tvEmailInscrit.setText("Adresse e-mail : " + emailInscrit);
        tvNumTelInscrit.setText("Numéro de téléphone : " + numTelInscrit);
        cbInscriptionValidee.setChecked(isInscriptionValidee);

        // Gestion du clic sur la CheckBox cbInscriptionValidee.
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
