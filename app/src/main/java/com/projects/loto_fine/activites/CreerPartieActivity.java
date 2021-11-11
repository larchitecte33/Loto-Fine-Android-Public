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

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Cette activité est affichée lors du clic sur le bouton CREER UNE PARTIE de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de créer une partie.
 */
public class CreerPartieActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
     ValidationDialogFragment.ValidationDialogListener {

    private int annee, mois, jour, heure, minutes;
    private Date datePartie;
    private EditText editSaisirDateHeure;
    private Calendar calendar = Calendar.getInstance();
    private String adresseServeur;
    private SharedPreferences sharedPref;
    private boolean timeIsSet = false;

    /**
     * Fonction permettant d'ajouter n 0 au début de la valeur inputString afin que la taille de cette valeur atteigne length caractères.
     * @param inputString : la chaine de caractères à laquelle ajouter les 0.
     * @param length : la longueur de la chaine en sortie.
     * @return la chaine avec length caractères
     */
    public static String padLeftZeros(String inputString, int length) {
        // Si la longueur de la chaine en entrée est supérieure ou égale length, alors elle ne doit pas être complétée.
        if (inputString.length() >= length) {
            return inputString;
        }

        // On utilise la classe StringBuilder qui permet de concaténer des chaines de caractères de manière optimisée.
        StringBuilder sb = new StringBuilder();

        // Tant qu'on a pas ajouté assez de 0 pour pour atteindre la taille length, on en ajoute.
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }

        // On ajoute la chaine.
        sb.append(inputString);

        // On retourne la chaine complétée par les 0.
        return sb.toString();
    }

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponse traitée : creationPartie
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;

        if (source == "creationPartie") {
            // Si le serveur a renvoyé un erreur lors de la création de la partie, alors on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la création de la partie : " + reponse, false, getSupportFragmentManager());
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
                    }
                    // Sinon, on affiche un message indiquant que la partie a été créée.
                    else {
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
        // On va chercher l'adresse du serveur dans les SharedPreferences.
        String adresseServeur = sharedPref.getString("AdresseServeur", "");
        String messageErreur;

        // Si l'adresse du serveur n'est pas renseignée, on affiche une erreur.
        if(adresseServeur.trim().equals("")) {
            AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", true, getSupportFragmentManager());
        }
        else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            // Si l'e-mail ou le mot de passe de l'utilisateur ne sont pas renseignés, on affiche une erreur.
            if ((email.trim().length() == 0) || mdp.trim().length() == 0) {
                AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", true, getSupportFragmentManager());
            }
        }

        // Récupération des composants depuis le layout.
        EditText editSaisirAdresse = findViewById(R.id.creer_partie_edit_saisir_adresse);
        editSaisirDateHeure = findViewById(R.id.creer_partie_edit_saisir_date_heure);
        EditText editSaisirCP = findViewById(R.id.creer_partie_edit_saisir_code_postal);
        EditText editSaisirVille = findViewById(R.id.creer_partie_edit_saisir_ville);
        EditText editSaisirPrixCarton = findViewById(R.id.creer_partie_edit_saisir_prix_carton);
        EditText editSaisirMethodeReglement = findViewById(R.id.creer_partie_edit_saisir_methode_reglement);
        Button boutonAnnuler = findViewById(R.id.bouton_annuler_creation_partie);
        Button boutonValider = findViewById(R.id.bouton_valider_creation_partie);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        boutonAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        boutonValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        // Gestion du clic sur le champ d'édition de la date/heure de la partie.
        editSaisirDateHeure.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Si editSaisirDateHeure a le focus, on va afficher un calendrier permettant de choisir la date de la partie.
                if(hasFocus) {
                    // On crée un Calendar initialisé avec la date actuelle.
                    Calendar calendar = Calendar.getInstance();
                    annee = calendar.get(Calendar.YEAR); // On récupère l'année du Calendar
                    mois = calendar.get(Calendar.MONTH); // On récupère le mois du Calendar
                    jour = calendar.get(Calendar.DAY_OF_MONTH); // On récupère le jour du Calendar

                    // On crée un DatePickerDialog en lui passant CreerPartieActivity.this pour Context et OnDateSetListener puisque le parent du
                    // DatePickerDialog est CreerPartieActivity et le listener à appeler pour la gestion des évènements liés à la gestion du clic sur
                    // le DatePickerDialog sont dans CreerPartieActivity.
                    DatePickerDialog datePickerDialog = new DatePickerDialog(CreerPartieActivity.this, CreerPartieActivity.this, annee, mois, jour);
                    // On affiche le DatePickerDialog pour la sélection du jour.
                    datePickerDialog.show();
                }
            }
        });

        // Gestion du clic sur le bouton valider
        boutonValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean continuer = false;
                double prixCarton = 0.0;
                Date date = null;
                String dateStr = "";
                long dateLong = 0;

                // Vérification remplissage champs
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

                        // On vérifie que la date saisie est bien dans le futur
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
                            // On vérifie que le prix du carton est bien un Double.
                            prixCarton = Double.valueOf(editSaisirPrixCarton.getText().toString());
                        }
                        catch (NumberFormatException e) {
                            AccueilActivity.afficherMessage("Erreur dans le prix du carton : " + e.getMessage(), false, getSupportFragmentManager());
                            continuer = false;
                        }
                    }
                }

                // Si toutes les données saisies sont correctes.
                if(continuer) {
                    // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                    String email = sharedPref.getString("emailUtilisateur", "");
                    String mdp = sharedPref.getString("mdpUtilisateur", "");

                    // On construit l'URL permettant de créer la partie.
                    String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/creation-partie?email=" +
                            AccueilActivity.encoderECommercial(email) + "&mdp=" + AccueilActivity.encoderECommercial(mdp) +
                            "&adresse=" + AccueilActivity.encoderECommercial(editSaisirAdresse.getText().toString()) + "&datePartie=" + dateLong + // + dateStr + ":00" +
                            "&cp=" + AccueilActivity.encoderECommercial(editSaisirCP.getText().toString()) +
                            "&ville=" + AccueilActivity.encoderECommercial(editSaisirVille.getText().toString()) +
                            "&prixCarton=" + editSaisirPrixCarton.getText().toString() +
                            "&methodeReglement=" + AccueilActivity.encoderECommercial(editSaisirMethodeReglement.getText().toString());
                    System.out.println("Création d'une partie : " + adresse);

                    RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                            adresse, CreerPartieActivity.this);
                    // On envoie la demande de création de la partie.
                    requeteHTTP.traiterRequeteHTTPJSON(CreerPartieActivity.this, "CreationPartie", "POST", "", getSupportFragmentManager());

                }
            }
        });

        // Gestion du clic sur le bouton Annuler
        boutonAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Gestion de la sélection de la date.
     * @param view : le sélecteur associé à la boite de dialogue.
     * @param year : l'année sélectionnée
     * @param month : le mois sélectionné
     * @param dayOfMonth : le jour du mois sélectionné.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Si la vue et tous ses ancètres sont visibles.
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

    /**
     * Gestion de la sélection de l'heure.
     * @param view : le sélecteur associé à la boite de dialogue.
     * @param hourOfDay : l'heure sélectionné
     * @param minute : les minutes sélectionnées.
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        heure = hourOfDay;
        minutes = minute;

        editSaisirDateHeure.setText(padLeftZeros(String.valueOf(jour), 2) + "/" + padLeftZeros(String.valueOf(mois + 1), 2) + "/" +
                padLeftZeros(String.valueOf(annee), 4) + " " + padLeftZeros(String.valueOf(heure), 2) + ":" +
                padLeftZeros(String.valueOf(minutes), 2));

        timeIsSet = true;
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