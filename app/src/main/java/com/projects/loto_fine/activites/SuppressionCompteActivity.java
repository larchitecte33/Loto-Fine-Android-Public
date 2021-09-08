package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SuppressionCompteActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private String email, mdp;
    private String adresseServeur;
    private SharedPreferences sharedPref;

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;
        JSONArray ja;
        JSONObject jo;

        if (source == "obtentionInfosPersonne") {
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la recherche des informations de la personne : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else if(jo.toString().equals("{}")) {
                        AccueilActivity.afficherMessage("Le compte n'a pas été trouvé.", false, getSupportFragmentManager());
                    }
                    else {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/obtenir_liste_inscriptions?" +
                                "email=" + AccueilActivity.encoderECommercial(email) +
                                "&mdp=" + AccueilActivity.encoderECommercial(mdp) +
                                "&inclurePartiesPassees=0&inclurePartiesAnimateur=1&inclurePartiesAnimeesDansPasse=0";
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, SuppressionCompteActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSONArray(SuppressionCompteActivity.this, "RechercheListeInscriptions", "GET", getSupportFragmentManager());
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if (source == "rechercheListeInscriptions") {
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    boolean existeInscriptionFuture = false, isExceptionDateHeurePartie;
                    Date dateHeurePartie = null;
                    String dateHeurePartieStr;

                    // On récupère la liste des inscriptions
                    ja = new JSONArray(reponse);

                    for(int i = 0 ; i < ja.length() ; i++) {
                        jo = ja.getJSONObject(i);
                        jo = jo.getJSONObject("partie");

                        isExceptionDateHeurePartie = false;

                        dateHeurePartieStr = jo.getString("date");

                        try {
                            dateHeurePartie = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(dateHeurePartieStr);
                        } catch (ParseException e) {
                            isExceptionDateHeurePartie = true;
                        }

                        // Si la date/heure de début de la partie est correcte.
                        if (!isExceptionDateHeurePartie) {
                            if(dateHeurePartie.after(new Date())) {
                                existeInscriptionFuture = true;
                            }
                        }
                    }

                    if(existeInscriptionFuture) {
                        AccueilActivity.afficherMessage("Vous êtes inscrit pour une partie qui n'a pas commencé. Veuillez vous désinscrire de cette partie.",
                                false, getSupportFragmentManager());
                    }
                    else {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/suppression-compte?" +
                                "email=" + AccueilActivity.encoderECommercial(email) +
                                "&mdp=" + AccueilActivity.encoderECommercial(mdp);
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, SuppressionCompteActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(SuppressionCompteActivity.this, "SuppressionCompte", "DELETE", "", getSupportFragmentManager());
                    }
                }
                catch(JSONException e) {
                    try {
                        ja = new JSONArray(reponse);

                        if(ja.length() == 1) {
                            JSONObject objErreur = ja.getJSONObject(0);

                            // Si on a bien récupéré une erreur, on l'affiche.
                            if (objErreur != null) {
                                message = "Erreur : " + objErreur.getString("erreur");
                                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                            }
                        }
                    }
                    catch(JSONException e2) {
                        AccueilActivity.afficherMessage(e2.getMessage(), false, getSupportFragmentManager());
                    }
                }
            }
        }
        else if (source == "suppressionCompte") {
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }
                    else if(jo.getString("message").equals("OK")) {
                        AccueilActivity.afficherMessage("Le compte a bien été supprimé.", false, getSupportFragmentManager());

                        SharedPreferences.Editor edit = sharedPref.edit();
                        edit.putString("nomUtilisateur", "anonymous");
                        edit.putString("emailUtilisateur", "");
                        edit.commit();

                        Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                        startActivity(intent);
                    }
                    else {
                        AccueilActivity.afficherMessage("Le compte n'a pas pu être supprimé.", false, getSupportFragmentManager());
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
        setContentView(R.layout.activity_suppression_compte);

        Button btnSupprimer = findViewById(R.id.suppression_compte_btn_supprimer);
        Button btnNePasSupprimer = findViewById(R.id.suppression_compte_btn_ne_pas_supprimer);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnSupprimer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnSupprimer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnNePasSupprimer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnNePasSupprimer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        adresseServeur = sharedPref.getString("AdresseServeur", "");

        if(adresseServeur.trim().equals("")) {
            AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", true, getSupportFragmentManager());
        }
        else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            email = sharedPref.getString("emailUtilisateur", "");
            mdp = sharedPref.getString("mdpUtilisateur", "");

            if ((email.trim().length() == 0) || mdp.trim().length() == 0) {
                AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", true, getSupportFragmentManager());
            }
        }

        btnNePasSupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        btnSupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On vérifie que le compte existe.
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/get_infos_personne?email=" + email;

                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, SuppressionCompteActivity.this);

                requeteHTTP.traiterRequeteHTTPJSON(SuppressionCompteActivity.this, "ObtentionInfosPersonne", "GET", "", getSupportFragmentManager());
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