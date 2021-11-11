package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Cette activité est affichée lors du clic sur le bouton STATISTIQUES de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de visualiser des statistiques le concernant.
 */
public class StatistiquesActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, ValidationDialogFragment.ValidationDialogListener {

    private String nameFieldDateSet = ""; // Nom du champ date qui est modifié.
    private EditText edDateDebut, edDateFin; // EditText permettant de modifier la date de début et la date de fin.
    private int annee, mois, jour;
    private SharedPreferences sharedPref;
    private String adresseServeur;
    private TextView tvNbLotsGagnges, tvNbParticipations, tvNbMoyenCartons;

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : obtentionInfosPersonne et recuperationStatistiques.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;

        if (source == "obtentionInfosPersonne") {
            // S'il y a une erreur lors de la recherche des informations de la personne, on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la recherche des informations de l'utilisateur : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si on a une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        Date dateDebut = null, dateFin = null;
                        long dateDebutLong = 0, dateFinLong = 0;

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            dateDebut = sdf.parse(edDateDebut.getText().toString());
                            dateDebutLong = dateDebut.getTime();
                            dateFin = sdf.parse(edDateFin.getText().toString());
                            dateFinLong = dateFin.getTime();

                            // Récupération de l'email et du mot de passe de l'utilisateur dans les SharedPreferences.
                            String email = sharedPref.getString("emailUtilisateur", "");
                            String mdp = sharedPref.getString("mdpUtilisateur", "");

                            int idPersonne = jo.getInt("id");

                            // Envoi d'une requête permettant de récupérer les statistiques.
                            String adresse = adresseServeur + ":" + Constants.portMicroserviceStatistiquesEtClassements +
                                    "/statistiques-classements/recuperation-statistiques?email=" + email + "&mdp=" + mdp + "&dateDebut=" + dateDebutLong +
                                    "&dateFin=" + dateFinLong + "&idPersonne=" + idPersonne;

                            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                                    adresse, StatistiquesActivity.this);
                            requeteHTTP.traiterRequeteHTTPJSONArray(StatistiquesActivity.this, "RecuperationStatistiques", "GET", getSupportFragmentManager());
                        }
                        catch (ParseException e) {
                            AccueilActivity.afficherMessage("Erreur dans le format de la date/heure : " + e.getMessage(), false, getSupportFragmentManager());
                        }
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if (source == "recuperationStatistiques") {
            // S'il y a une erreur lors de la récupération des statistiques, on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la recherche des statistiques : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On tente de caster la réponse du serveur en JSONArray.
                    JSONArray ja = new JSONArray(reponse);

                    // Si on a aucun enregistrement dans le JSONArray, on affiche une erreur.
                    if (ja.length() == 0) {
                        AccueilActivity.afficherMessage("Erreur lors de la recherche des statistiques : aucune statistique renvoyée par le serveur", false, getSupportFragmentManager());
                    } else {
                        // Sinon, on va chercher le premier enregistrement du JSONArray.
                        JSONObject jo = ja.getJSONObject(0);

                        if (jo.opt("erreur") != null) {
                            AccueilActivity.afficherMessage("Erreur lors de la recherche des statistiques : " + jo.getString("erreur"), false, getSupportFragmentManager());
                        } else if (jo.opt("nbParticipations") == null) {
                            AccueilActivity.afficherMessage("Erreur lors de la recherche des statistiques : le nombre de participations n'est pas renseigné.", false, getSupportFragmentManager());
                        } else if (jo.opt("nbMoyensCartons") == null) {
                            AccueilActivity.afficherMessage("Erreur lors de la recherche des statistiques : le nombre moyen de cartons n'est pas renseigné.", false, getSupportFragmentManager());
                        } else if (jo.opt("nbLotsGagnes") == null) {
                            AccueilActivity.afficherMessage("Erreur lors de la recherche des statistiques : le nombre de lots gagnés n'est pas renseigné.", false, getSupportFragmentManager());
                        } else {
                            tvNbParticipations.setText("Nombre de participations : " + jo.getString("nbParticipations"));
                            tvNbMoyenCartons.setText("Nombre moyen de cartons : " + jo.getString("nbMoyensCartons"));
                            tvNbLotsGagnges.setText("Nombre de lots gagnés : " + jo.getString("nbLotsGagnes"));
                        }
                    }
                } catch (JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistiques);

        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        adresseServeur = sharedPref.getString("AdresseServeur", "");

        // Récupération des composants.
        edDateDebut = findViewById(R.id.visu_stats_edit_date_debut);
        edDateFin = findViewById(R.id.visu_stats_edit_date_fin);
        tvNbLotsGagnges = findViewById(R.id.visu_stats_lbl_nb_lots_gagnes);
        tvNbParticipations = findViewById(R.id.visu_stats_lbl_nb_participations);
        tvNbMoyenCartons = findViewById(R.id.visu_stats_lbl_nb_moyen_cartons);
        Button btValiderVisuStats = findViewById(R.id.bouton_valider_visualisation_statistiques);
        Button btAnnulerVisuStats = findViewById(R.id.bouton_annuler_visualisation_statistiques);

        // Clic sur l'EditText de la date de début.
        edDateDebut.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Si l'EditText de la date de début a le focus, alors on affiche un calendrier permettant de sélectionner la date.
                if(hasFocus) {
                    Calendar calendar = Calendar.getInstance();
                    annee = calendar.get(Calendar.YEAR);
                    mois = calendar.get(Calendar.MONTH);
                    jour = calendar.get(Calendar.DAY_OF_MONTH);
                    nameFieldDateSet = "edDateDebut";
                    DatePickerDialog datePickerDialog = new DatePickerDialog(StatistiquesActivity.this, StatistiquesActivity.this, annee, mois, jour);
                    datePickerDialog.show();
                }
            }
        });

        // Clic sur l'EditText de la date de fin.
        edDateFin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Si l'EditText de la date de fin a le focus, alors on affiche un calendrier permettant de sélectionner la date.
                if(hasFocus) {
                    Calendar calendar = Calendar.getInstance();
                    annee = calendar.get(Calendar.YEAR);
                    mois = calendar.get(Calendar.MONTH);
                    jour = calendar.get(Calendar.DAY_OF_MONTH);
                    nameFieldDateSet = "edDateFin";
                    DatePickerDialog datePickerDialog = new DatePickerDialog(StatistiquesActivity.this, StatistiquesActivity.this, annee, mois, jour);
                    datePickerDialog.show();
                }
            }
        });

        // Clic sur le bouton Annuler
        btAnnulerVisuStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le bouton Valider.
        btValiderVisuStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On va chercher l'email de l'utilisateur dans les SharedPreferences.
                String email = sharedPref.getString("emailUtilisateur", "");
                // On envoie une requête permettant de récupérer les informations de la personne.
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/get_infos_personne?email=" + email;

                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                        adresse, StatistiquesActivity.this);
                requeteHTTP.traiterRequeteHTTPJSON(StatistiquesActivity.this, "ObtentionInfosPersonne", "GET", "", getSupportFragmentManager());
            }
        });
    }

    /**
     * Déclenché quand une date est sélectionnée sur un DatePickerDialog.
     * @param view : le picker associé au dialog
     * @param year : l'année sélectionnée
     * @param month : le mois sélectionné
     * @param dayOfMonth : le jour sélectionné
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if(nameFieldDateSet.equals("edDateDebut")) {
            edDateDebut.setText(CreerPartieActivity.padLeftZeros(String.valueOf(dayOfMonth), 2) + "/" +
                    CreerPartieActivity.padLeftZeros(String.valueOf(month + 1), 2) + "/" +
                    CreerPartieActivity.padLeftZeros(String.valueOf(year), 4));
        }
        else if(nameFieldDateSet.equals("edDateFin")) {
            edDateFin.setText(CreerPartieActivity.padLeftZeros(String.valueOf(dayOfMonth), 2) + "/" +
                    CreerPartieActivity.padLeftZeros(String.valueOf(month + 1), 2) + "/" +
                    CreerPartieActivity.padLeftZeros(String.valueOf(year), 4));
        }
    }

    /**
     * Implémentation de la fonction onFinishEditDialog de l'interface ValidationDialogListener.
     * @param revenirAAccueil : true si on doit revenir à l'activité appelante, false sinon.
     */
    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
            startActivity(intent);
        }
    }
}