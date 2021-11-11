package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.adapters.ParticipantClasseAdapter;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.ParticipantClasse;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 * Cette activité est affichée lors du clic sur le bouton CLASSEMENTS de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de visualiser le classement des participants sur une période donnée et suivant un critère donné (nb lots gagnés,
 * nb participations et nb moyen de cartons.
 */
public class ClassementsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, ValidationDialogFragment.ValidationDialogListener {

    private LinkedList<ParticipantClasse> participantsClasses = new LinkedList<>();
    private String critere = "";
    private ListView listParticipants;
    private SharedPreferences sharedPref;
    private String adresseServeur;
    private int annee, mois, jour;
    private String nameFieldDateSet = "";
    private EditText edDateDebut, edDateFin;

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponse traitée : recuperationClassements
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;

        if (source == "recuperationClassements") {
            if (isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la récupération du classement : " + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On tente de caster la réponse en JSONArray.
                    JSONArray ja = new JSONArray(reponse);

                    // Si on a réussi à caster la réponse en JSONArray et si le JSONArray ne possède aucun élément, on affiche une message.
                    if (ja.length() == 0) {
                        AccueilActivity.afficherMessage("Aucune donnée trouvée.", false, getSupportFragmentManager());
                    }
                    // Si le JSONArray possède au moins un élément
                    else {
                        // On va chercher le premier élément.
                        JSONObject jo = ja.getJSONObject(0);

                        // Si le premier élément correspond à une erreur, on l'affiche.
                        if (jo.opt("erreur") != null) {
                            AccueilActivity.afficherMessage("Erreur lors de la recherche du classement : " + jo.getString("erreur"), false, getSupportFragmentManager());
                        }
                        // Sinon, on peut afficher le classement.
                        else {
                            // On récupère la liste des participants et on la rend visible.
                            listParticipants = (ListView) findViewById(R.id.classements_scroll_participants);
                            listParticipants.setVisibility(View.VISIBLE);

                            participantsClasses.clear();

                            // Pour chacun des éléments du JSON correspondant aux participants classés
                            for(int i = 0 ; i < ja.length() ; i++) {
                                // On récupère le JSONObject qui correspond au participant classé courant.
                                jo = ja.getJSONObject(i);

                                // On construit un objet ParticipantClasse à partir des informations du JSON.
                                ParticipantClasse participantClasse = new ParticipantClasse();
                                participantClasse.setNumeroClassement(i + 1);
                                participantClasse.setNom(jo.getString("nom"));
                                participantClasse.setPrenom(jo.getString("prenom"));

                                if(critere.equals(Constants.CRITERE_NB_LOTS_GAGNES)) {
                                    participantClasse.setNbLotsGagnes(jo.getInt("nbLotsGagnes"));
                                    participantClasse.setNbMoyenCartons(0);
                                    participantClasse.setNbParticipations(0);
                                }
                                else if(critere.equals(Constants.CRITERE_NB_PARTICIPATIONS)) {
                                    participantClasse.setNbParticipations(jo.getInt("nbParticipations"));
                                    participantClasse.setNbLotsGagnes(0);
                                    participantClasse.setNbMoyenCartons(0);
                                }
                                else {
                                    participantClasse.setNbMoyenCartons((float)(jo.getDouble("nbMoyensCartons")));
                                    participantClasse.setNbLotsGagnes(0);
                                    participantClasse.setNbParticipations(0);
                                }

                                // On ajoute l'objet ParticipantClasse à la liste des participants classés.
                                participantsClasses.add(participantClasse);
                            }

                            // On construit un array adapter de type ParticipantClasseAdapter en lui fournissant la liste de participants classés.
                            ParticipantClasseAdapter adapter = new ParticipantClasseAdapter(this, this, R.layout.activity_classements_adapter,
                                    participantsClasses, critere);
                            // On assigne à la ListView listParticipants l'array adapter.
                            listParticipants.setAdapter(adapter);
                        }
                    }
                }
                catch (JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                } catch (Exception e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classements);

        // Pour que l'écran reste en mode portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        adresseServeur = sharedPref.getString("AdresseServeur", "");

        // Récupération des composants depuis le layout.
        edDateDebut = (EditText) findViewById(R.id.classements_edit_date_debut);
        edDateFin = (EditText) findViewById(R.id.classements_edit_date_fin);
        Spinner spCritere = (Spinner) findViewById(R.id.classements_spin_critere);
        Button btnAnnulerVisualisationClassements = (Button) findViewById(R.id.bouton_annuler_visualisation_classements);
        Button btnValiderVisualisationClassements = (Button) findViewById(R.id.bouton_valider_visualisation_classements);

        // Clis sur le bouton annuler
        btnAnnulerVisualisationClassements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le bouton valider
        btnValiderVisualisationClassements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On récupère l'indice du critère sélectionné.
                int itemSelectedPosition = spCritere.getSelectedItemPosition();

                // Si l'indice du critère sélectionné n'est pas compris entre 0 et 2 inclus, on affiche un message d'erreur.
                if((itemSelectedPosition < 0) || (itemSelectedPosition > 2)) {
                    AccueilActivity.afficherMessage("Veuillez sélectionner un critère.", false, getSupportFragmentManager());
                }
                else {
                    // Récupération du critère sélectionné
                    if(itemSelectedPosition == 0) {
                        critere = Constants.CRITERE_NB_LOTS_GAGNES;
                    }
                    else if(itemSelectedPosition == 1) {
                        critere = Constants.CRITERE_NB_PARTICIPATIONS;
                    }
                    else if(itemSelectedPosition == 2) {
                        critere = Constants.CRITERE_NB_MOYEN_CARTONS;
                    }

                    Date dateDebut = null, dateFin = null;
                    long dateDebutLong = 0, dateFinLong = 0;

                    try {
                        // On essaie de convertir le texte représentant les dates sélectionnées en objets de type Date.
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        dateDebut = sdf.parse(edDateDebut.getText().toString());
                        dateDebutLong = dateDebut.getTime();
                        dateFin = sdf.parse(edDateFin.getText().toString());
                        dateFinLong = dateFin.getTime();

                        String email = sharedPref.getString("emailUtilisateur", "");
                        String mdp = sharedPref.getString("mdpUtilisateur", "");

                        // Construction de l'URL permettant de récupérer le classement.
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceStatistiquesEtClassements +
                                "/statistiques-classements/recuperation-classements?email=" + email + "&mdp=" + mdp + "&dateDebut=" + dateDebutLong +
                                "&dateFin=" + dateFinLong + "&critere=" + critere;

                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                                adresse, ClassementsActivity.this);
                        // Envoi de la requête HTTP permettant de récupérer le classement.
                        requeteHTTP.traiterRequeteHTTPJSONArray(ClassementsActivity.this, "RecuperationClassements", "GET", getSupportFragmentManager());
                    }
                    catch (ParseException e) {
                        AccueilActivity.afficherMessage("Erreur dans le format de la date/heure : " + e.getMessage(), false, getSupportFragmentManager());
                    }
                }
            }
        });

        // Gestion du clic sur l'EditText de saisie de la date de début de la période du classment.
        edDateDebut.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Si le champ edDateDebut a le focus, on va afficher un calendrier permettant de choisir la date de début de la période du classement.
                if(hasFocus) {
                    Calendar calendar = Calendar.getInstance();
                    annee = calendar.get(Calendar.YEAR);
                    mois = calendar.get(Calendar.MONTH);
                    jour = calendar.get(Calendar.DAY_OF_MONTH);
                    nameFieldDateSet = "edDateDebut";
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ClassementsActivity.this, ClassementsActivity.this, annee, mois, jour);
                    datePickerDialog.show();
                }
            }
        });

        // Gestion du clic sur l'EditText de saisie de la date de fin de la période du classment.
        edDateFin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Si le champ edDateFin a le focus, on va afficher un calendrier permettant de choisir la date de fin de la période du classement.
                if(hasFocus) {
                    Calendar calendar = Calendar.getInstance();
                    annee = calendar.get(Calendar.YEAR);
                    mois = calendar.get(Calendar.MONTH);
                    jour = calendar.get(Calendar.DAY_OF_MONTH);
                    nameFieldDateSet = "edDateFin";
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ClassementsActivity.this, ClassementsActivity.this, annee, mois, jour);
                    datePickerDialog.show();
                }
            }
        });
    }

    // Ceci est le listener déclenché lorsque l'utilisateur a fini de sélectionner une date.
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Si la date qui vient d'être définie est la date de début, on l'affecte à edDateDebut.
        if(nameFieldDateSet.equals("edDateDebut")) {
            edDateDebut.setText(CreerPartieActivity.padLeftZeros(String.valueOf(dayOfMonth), 2) + "/" +
                    CreerPartieActivity.padLeftZeros(String.valueOf(month + 1), 2) + "/" +
                    CreerPartieActivity.padLeftZeros(String.valueOf(year), 4));
        }
        // Si la date qui vient d'être définie est la date de fin, on l'affecte à edDateFin.
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