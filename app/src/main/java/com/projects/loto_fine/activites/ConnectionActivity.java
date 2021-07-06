package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.projects.loto_fine.Constants;
import com.projects.loto_fine.MessageBox;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.RequeteHTTP;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class ConnectionActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private EditText edSaisirEmail;
    private String mdp = "";

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;

        if(source == "connexion") {
            try {
                if (isErreur) {
                    AccueilActivity.afficherMessage("Une erreur est survenue : " + reponse, false, getSupportFragmentManager());
                }
                else {
                    JSONObject jo = new JSONObject(reponse);
                    String result = jo.getString("result");

                    if (result.equals("false")) {
                        AccueilActivity.afficherMessage("Identifiants incorrects", false, getSupportFragmentManager());
                    } else {
                        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                        String adresseServeur = sharedPref.getString("AdresseServeur", "");

                        // Si l'adresse du serveur n'est pas renseignée
                        if(adresseServeur.trim().equals("")) {
                            AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", false, getSupportFragmentManager());
                        }
                        else {
                            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/get_infos_personne?email=" + edSaisirEmail.getText();
                            RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                                    adresse, ConnectionActivity.this);
                            requeteHTTP.traiterRequeteHTTPJSON(ConnectionActivity.this, "ObtentionInfosPersonne", "GET", "", getSupportFragmentManager());
                        }
                    }
                }
            } catch (JSONException e) {
                AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
            }
        }
        else if(source == "obtentionInfosPersonne") {
            try {
                JSONObject jo = new JSONObject(reponse);
                String nomUtilisateur = jo.getString("nom") + " " + jo.get("prenom");
                String emailUtilisateur = jo.getString("email");
                //String mdpUtilisateur = jo.getString("mdp");

                SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPref.edit();
                //edit.clear();
                edit.putString("nomUtilisateur", nomUtilisateur);
                edit.putString("emailUtilisateur", emailUtilisateur);
                edit.putString("mdpUtilisateur", mdp);

                if(edit.commit()) {
                    AccueilActivity.afficherMessage("Vous êtes maintenant connecté.", true, getSupportFragmentManager());
                }
                else {
                    AccueilActivity.afficherMessage("La connexion a échoué.", true, getSupportFragmentManager());
                }

            }
            catch (JSONException e) {
                Log.d("JSONException", e.getMessage() + ", reponse = " + reponse);
                AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        edSaisirEmail = findViewById(R.id.co_edit_saisir_email);
        EditText edSaisirMdp = findViewById(R.id.co_edit_saisir_mdp);
        Button btAnnuler = findViewById(R.id.bouton_annuler_connexion);
        Button btValider = findViewById(R.id.bouton_valider_connexion);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        btAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        btValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edSaisirEmail.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.txt_veuillez_saisir_email, Toast.LENGTH_LONG).show();
                }
                else if(edSaisirMdp.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.txt_veuillez_saisir_mdp, Toast.LENGTH_LONG).show();
                }
                else {
                    SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);

                    SharedPreferences.Editor edit = sharedPref.edit();
                    edit.clear();

                    //String adresseServeur = sharedPref.getString("AdresseServeur", "http://localhost:8081");
                    //edit.putString("AdresseServeur", "http://192.168.1.17:8081");
                    //edit.apply();

                    String adresseServeur = sharedPref.getString("AdresseServeur", "");

                    // Si l'adresse du serveur n'est pas renseignée
                    if(adresseServeur.trim().equals("")) {
                        AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", false, getSupportFragmentManager());
                    }
                    else {
                        mdp = edSaisirMdp.getText().toString();

                        //String adresseServeur = "http://192.168.1.17:8081";
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                                "/participant/verif-mdp?email=" + edSaisirEmail.getText() + "&mdp=" +
                                edSaisirMdp.getText();
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                                adresse, ConnectionActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(ConnectionActivity.this, "Connexion", "GET", "", getSupportFragmentManager());
                    }
                }
            }
        });
    }

    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
            startActivity(intent);
        }
    }
}