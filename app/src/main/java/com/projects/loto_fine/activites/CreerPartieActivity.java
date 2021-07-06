package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.projects.loto_fine.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.RequeteHTTP;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreerPartieActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
     ValidationDialogFragment.ValidationDialogListener {

    private int annee, mois, jour, heure, minutes;
    private Date datePartie;
    private EditText editSaisirDateHeure;
    private Calendar calendar = Calendar.getInstance();
    private String adresseServeur;
    private SharedPreferences sharedPref;
    private boolean timeIsSet = false;

    public String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }

        StringBuilder sb = new StringBuilder();

        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }

        sb.append(inputString);

        return sb.toString();
    }

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;

        if (source == "creationPartie") {
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la création de la partie : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        AccueilActivity.afficherMessage("La partie a été créée.", true, getSupportFragmentManager());
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
        setContentView(R.layout.activity_creer_partie);

        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String adresseServeur = sharedPref.getString("AdresseServeur", "");
        String messageErreur;

        if(adresseServeur.trim().equals("")) {
            AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", true, getSupportFragmentManager());
        }
        else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            if ((email.trim().length() == 0) || mdp.trim().length() == 0) {
                AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", true, getSupportFragmentManager());
            }
        }

        EditText editSaisirAdresse = findViewById(R.id.creer_partie_edit_saisir_adresse);
        editSaisirDateHeure = findViewById(R.id.creer_partie_edit_saisir_date_heure);
        EditText editSaisirCP = findViewById(R.id.creer_partie_edit_saisir_code_postal);
        EditText editSaisirVille = findViewById(R.id.creer_partie_edit_saisir_ville);
        EditText editSaisirPrixCarton = findViewById(R.id.creer_partie_edit_saisir_prix_carton);
        Button boutonAnnuler = findViewById(R.id.bouton_annuler_creation_partie);
        Button boutonValider = findViewById(R.id.bouton_valider_creation_partie);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        boutonAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        boutonValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        editSaisirDateHeure.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Si editSaisirDateHeure a le focus, on va afficher un calendrier.
                if(hasFocus) {
                    Calendar calendar = Calendar.getInstance();
                    annee = calendar.get(Calendar.YEAR);
                    mois = calendar.get(Calendar.MONTH);
                    jour = calendar.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(CreerPartieActivity.this, CreerPartieActivity.this, annee, mois, jour);
                    datePickerDialog.show();
                }
            }
        });

        boutonValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean continuer = false;
                double prixCarton = 0.0;
                Date date = null;
                String dateStr = "";
                long dateLong = 0;

                if(editSaisirAdresse.getText().length() == 0) {
                    AccueilActivity.afficherMessage(getResources().getString(R.string.erreur_doit_renseigner_adresse), false, getSupportFragmentManager());
                }
                else if(editSaisirDateHeure.getText().length() != 16) {
                    AccueilActivity.afficherMessage(getResources().getString(R.string.erreur_doit_renseigner_dateheure), false, getSupportFragmentManager());
                }
                else if(editSaisirCP.getText().length() == 0) {
                    AccueilActivity.afficherMessage(getResources().getString(R.string.erreur_doit_renseigner_cp), false, getSupportFragmentManager());
                }
                else if(editSaisirVille.getText().length() == 0) {
                    AccueilActivity.afficherMessage(getResources().getString(R.string.erreur_doit_renseigner_ville), false, getSupportFragmentManager());
                }
                else if(editSaisirPrixCarton.getText().length() == 0) {
                    AccueilActivity.afficherMessage(getResources().getString(R.string.erreur_doit_renseigner_prix_carton), false, getSupportFragmentManager());
                }
                else if(editSaisirAdresse.getText().length() > Constants.TAILLE_MAX_ADRESSE) {
                    AccueilActivity.afficherMessage(getResources().getString(R.string.erreur_taille_max_adresse) + Constants.TAILLE_MAX_ADRESSE, false, getSupportFragmentManager());
                }
                else if(editSaisirCP.getText().length() > Constants.TAILLE_MAX_CP) {
                    AccueilActivity.afficherMessage(getResources().getString(R.string.erreur_taille_max_cp) + Constants.TAILLE_MAX_CP, false, getSupportFragmentManager());
                }
                else if(editSaisirVille.getText().length() > Constants.TAILLE_MAX_VILLE) {
                    AccueilActivity.afficherMessage(getResources().getString(R.string.erreur_taille_max_ville) + Constants.TAILLE_MAX_VILLE, false, getSupportFragmentManager());
                }
                else {
                    continuer = true;

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                        date = sdf.parse(editSaisirDateHeure.getText().toString());
                        dateLong = date.getTime();

                        dateStr = sdf2.format(date);
                        System.out.println(dateStr);

                        if(date.before(new Date())) {
                            AccueilActivity.afficherMessage("La partie ne peut être créée dans le passé.", false, getSupportFragmentManager());
                            continuer = false;
                        }
                    }
                    catch (ParseException e) {
                        AccueilActivity.afficherMessage("Erreur dans le format de la date/heure : " + e.getMessage(), false, getSupportFragmentManager());
                        continuer = false;
                    }

                    if(continuer) {
                        try {
                            prixCarton = Double.valueOf(editSaisirPrixCarton.getText().toString());
                        }
                        catch (NumberFormatException e) {
                            AccueilActivity.afficherMessage("Erreur dans le prix du carton : " + e.getMessage(), false, getSupportFragmentManager());
                            continuer = false;
                        }
                    }
                }

                if(continuer) {
                    // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                    String email = sharedPref.getString("emailUtilisateur", "");
                    String mdp = sharedPref.getString("mdpUtilisateur", "");

                    String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/creation-partie?email=" + email + "&mdp=" + mdp +
                            "&adresse=" + editSaisirAdresse.getText().toString() + "&datePartie=" + dateLong + // + dateStr + ":00" +
                            "&cp=" + editSaisirCP.getText().toString() + "&ville=" + editSaisirVille.getText().toString() +
                            "&prixCarton=" + editSaisirPrixCarton.getText().toString();
                    System.out.println("Création d'une partie : " + adresse);

                    RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                            adresse, CreerPartieActivity.this);
                    requeteHTTP.traiterRequeteHTTPJSON(CreerPartieActivity.this, "CreationPartie", "POST", "", getSupportFragmentManager());

                }
            }
        });

        boutonAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
       if (view.isShown()) {
            annee = year;
            mois = month;
            jour = dayOfMonth;

            calendar.set(year, month, dayOfMonth);
            TimePickerDialog timePickerDialog = new TimePickerDialog(CreerPartieActivity.this, CreerPartieActivity.this, heure, minutes, true);
            // On affiche une horloge pour sélectionner l'heure.
            timePickerDialog.show();
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        heure = hourOfDay;
        minutes = minute;

        editSaisirDateHeure.setText(padLeftZeros(String.valueOf(jour), 2) + "/" + padLeftZeros(String.valueOf(mois + 1), 2) + "/" +
                padLeftZeros(String.valueOf(annee), 4) + " " + padLeftZeros(String.valueOf(heure), 2) + ":" +
                padLeftZeros(String.valueOf(minutes), 2));

        timeIsSet = true;
    }

    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
            startActivity(intent);
        }
    }
}