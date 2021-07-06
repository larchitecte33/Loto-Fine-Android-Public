package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.projects.loto_fine.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.RequeteHTTP;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ChoixLieuRetraitActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;

        if (source == "definirLieuRetraitLot") {
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la modification du lot : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        AccueilActivity.afficherMessage("Le lieu de retrait du lot a été validé.", true, getSupportFragmentManager());
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
        setContentView(R.layout.activity_choix_lieu_retrait);

        Intent intent = getIntent();
        int idLot = intent.getIntExtra("idLot", -1);
        String nomLot = intent.getStringExtra("nomLot");

        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String adresseServeur = sharedPref.getString("AdresseServeur", "");

        EditText editAdresse = (EditText) findViewById(R.id.choix_lieu_retrait_edit_saisir_adresse);
        EditText editCP = (EditText) findViewById(R.id.choix_lieu_retrai_edit_saisir_code_postal);
        EditText editVille = (EditText) findViewById(R.id.choix_lieu_retrai_edit_saisir_ville);
        Button btnValider = (Button) findViewById(R.id.bouton_valider_choix_lieu_retrait);
        Button btnAnnuler = (Button) findViewById(R.id.bouton_annuler_choix_lieu_retrait);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        // Clic sur le bouton Annuler
        btnAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MesLotsActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le bouton valider
        btnValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editAdresse.getText().toString().trim() == "") {
                    AccueilActivity.afficherMessage("Veuillez renseigner une adresse.", false, getSupportFragmentManager());
                }
                else if(editCP.getText().toString().trim() == "") {
                    AccueilActivity.afficherMessage("Veuillez renseigner un code postal.", false, getSupportFragmentManager());
                }
                else if(editVille.getText().toString().trim() == "") {
                    AccueilActivity.afficherMessage("Veuillez renseigner une ville.", false, getSupportFragmentManager());
                }
                else {
                    // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                    String email = sharedPref.getString("emailUtilisateur", "");
                    String mdp = sharedPref.getString("mdpUtilisateur", "");

                    String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/definir-lieu-retrait?email=" + email +
                            "&mdp=" + mdp + "&idLot=" + idLot + "&adresse=" + editAdresse.getText().toString() + "&cp=" + editCP.getText().toString() +
                            "&ville=" + editVille.getText().toString();

                    RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                            adresse, ChoixLieuRetraitActivity.this);
                    requeteHTTP.traiterRequeteHTTPJSON(ChoixLieuRetraitActivity.this, "DefinirLieuRetrait", "POST", "", getSupportFragmentManager());
                }
            }
        });
    }

    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if (revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), MesLotsActivity.class);
            startActivity(intent);
        }
    }
}