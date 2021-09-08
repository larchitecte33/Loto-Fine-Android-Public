package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;
import com.projects.loto_fine.constantes.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class DescriptionMethodeReglementActivity extends AppCompatActivity {

    private TextView tvDescriptionMethodeReglement;

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;

        if (source == "recuperationMethodeReglement") {
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la récupération de la méthode de réglement de la partie : " + reponse,
                        false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        tvDescriptionMethodeReglement.setText(jo.getString("methodeReglement"));
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
        setContentView(R.layout.activity_description_methode_reglement);

        Intent intent = getIntent();
        // On récupère l'identifiant de la partie.
        int idPartie = intent.getIntExtra("idPartie", -1);
        String source = intent.getStringExtra("source");

        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String adresseServeur = sharedPref.getString("AdresseServeur", "");

        tvDescriptionMethodeReglement = (TextView) findViewById(R.id.methode_reglement_tv_description_methode);
        Button btnRetour = (Button) findViewById(R.id.methode_reglement_btn_retour);

        // *************** Récupération de la méthode de réglement de la partie ***************** //
        // Si l'adresse du serveur n'est pas renseignée
        if (adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        } else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            if((email.trim().length() == 0) || mdp.trim().length() == 0) {
                AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", true, getSupportFragmentManager());
            }
            else {
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                        "/participant/recuperation-methode-reglement?email=" + AccueilActivity.encoderECommercial(email) +
                        "&mdp=" + AccueilActivity.encoderECommercial(mdp) + "&idPartie=" + idPartie;
                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                        adresse, DescriptionMethodeReglementActivity.this);
                requeteHTTP.traiterRequeteHTTPJSON(DescriptionMethodeReglementActivity.this, "RecuperationMethodeReglement",
                        "GET", "", getSupportFragmentManager());
            }
        }

        btnRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if(source.equals("RecherchePartieActivity")) {
                    intent = new Intent(getApplicationContext(), RecherchePartieActivity.class);
                }
                else {
                    intent = new Intent(getApplicationContext(), MesInscriptionsActivity.class);
                }

                startActivity(intent);
            }
        });
    }
}