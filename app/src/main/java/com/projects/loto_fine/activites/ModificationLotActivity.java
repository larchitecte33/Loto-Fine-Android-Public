package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cette activité est affichée lors du clic sur le bouton MODIFIER LE LOT de l'array adapter LotAdapter.
 * Elle permet à l'utilisateur de modifier les informations du lot.
 */
public class ModificationLotActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private SharedPreferences sharedPref; // SharedPreferences utilisé pour récupérer des valeurs stockées sur le téléphone/tablette.
    private int idPartie; // Identifiant de la partie
    private String emailAnimateur; // Adresse e-mail de l'animateur
    private int idLot; // Identifiant du lot
    private String nomLot, source; // Libellé du lot et source d'appel de la modification du lot.
    private double valeurLot; // Valeur du lot.
    private boolean isCartonPlein; // Le lot est-il au carton plein ?
    private int position; // Position du lot dans la partie.

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponse traitée : modificationLot.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;

        if (source == "modificationLot") {
            // S'il y a eu une erreur lors de la modification du lot, on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la modification du lot : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        AccueilActivity.afficherMessage("Le lot a été modifié.", true, getSupportFragmentManager());
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modification_lot);

        Intent intent = getIntent();
        // On récupère les valeurs passées dans l'intent.
        idPartie = intent.getIntExtra("idPartie", -1);
        emailAnimateur = intent.getStringExtra("emailAnimateur");
        source = intent.getStringExtra("source");
        idLot = intent.getIntExtra("idLot", -1);
        nomLot = intent.getStringExtra("nomLot");
        valeurLot = intent.getDoubleExtra("valeurLot", -1);
        isCartonPlein = intent.getBooleanExtra("isCartonPlein", false);
        position = intent.getIntExtra("position", -1);


        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String adresseServeur = sharedPref.getString("AdresseServeur", "");

        // Récupération des composants
        EditText editNomLot = findViewById(R.id.modif_lot_edit_saisir_nom_lot);
        EditText editValeurLot = findViewById(R.id.modif_lot_edit_saisir_valeur_lot);
        CheckBox cbLotEstAuCartonPlein = findViewById(R.id.modif_lot_cb_carton_plein);
        NumberPicker numPickPositionLot = findViewById(R.id.modif_lot_numpick_position);
        Button btnAnnulerModifLot = findViewById(R.id.modif_lot_bouton_annuler_modif_lot);
        Button btnValiderModifLot = findViewById(R.id.modif_lot_bouton_valider_modif_lot);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnAnnulerModifLot.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnAnnulerModifLot.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnValiderModifLot.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnValiderModifLot.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        // Initialisation valeur composants
        editNomLot.setText(nomLot);
        editValeurLot.setText(String.valueOf((double)Math.round(valeurLot * 100) / 100));

        if(isCartonPlein) {
            cbLotEstAuCartonPlein.setChecked(true);
        }
        else {
            cbLotEstAuCartonPlein.setChecked(false);
        }

        numPickPositionLot.setMinValue(0);
        numPickPositionLot.setMaxValue(1000);
        numPickPositionLot.setValue(position);

        // Clic sur le bouton Annuler.
        btnAnnulerModifLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VisualiserListeLotsActivity.class);
                intent.putExtra("idPartie", idPartie);
                intent.putExtra("emailAnimateur", emailAnimateur);
                intent.putExtra("source", source);
                startActivity(intent);
            }
        });

        // Clic sur le bouton Valider.
        btnValiderModifLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean erreurTrouvee = false;
                int isALaLigne = 0;

                // Si le nom du lot n'est pas renseigné, on affiche une erreur.
                if(nomLot.equals("")) {
                    AccueilActivity.afficherMessage("Le nom du lot doit être renseigné.", false, getSupportFragmentManager());
                }
                else {
                    // Si la valeur du lot n'est pas correcte, on l'affiche.
                    try {
                        valeurLot = Double.valueOf(editValeurLot.getText().toString());
                    } catch (NumberFormatException e) {
                        AccueilActivity.afficherMessage("La valeur du lot est incorrecte.", false, getSupportFragmentManager());
                        erreurTrouvee = true;
                    }

                    if (cbLotEstAuCartonPlein.isChecked()) {
                        isALaLigne = 0;
                    } else {
                        isALaLigne = 1;
                    }

                    if (!erreurTrouvee) {
                        // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                        String email = sharedPref.getString("emailUtilisateur", "");
                        String mdp = sharedPref.getString("mdpUtilisateur", "");

                        // On envoie une requête pour demander la modification du lot.
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/modification-lot?email=" +
                                AccueilActivity.encoderECommercial(email) +
                                "&mdp=" + AccueilActivity.encoderECommercial(mdp) +
                                "&idPartie=" + idPartie + "&idLot=" + idLot + "&nomLot=" + AccueilActivity.encoderECommercial(nomLot) +
                                "&valeurLot=" + valeurLot + "&isALaLigne=" + isALaLigne +
                                "&position=" + numPickPositionLot.getValue();
                        System.out.println("Modification d'un lot(nomLot = " + nomLot + ", valeurLot = " + valeurLot + ") : " + adresse);

                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                                adresse, ModificationLotActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(ModificationLotActivity.this, "ModificationLot", "PUT", "", getSupportFragmentManager());
                    }
                }
            }
        });
    }

    /**
     * Implémentation de la fonction onFinishEditDialog de l'interface ValidationDialogListener.
     * @param revenirAAccueil : true si on doit revenir à l'activité appelante, false sinon.
     */
    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), VisualiserListeLotsActivity.class);
            intent.putExtra("idPartie", idPartie);
            intent.putExtra("emailAnimateur", emailAnimateur);
            intent.putExtra("source", source);
            startActivity(intent);
        }
    }
}