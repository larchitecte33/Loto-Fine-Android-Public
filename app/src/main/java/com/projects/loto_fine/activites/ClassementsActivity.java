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

public class ClassementsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, ValidationDialogFragment.ValidationDialogListener {

    private LinkedList<ParticipantClasse> participantsClasses = new LinkedList<>();
    private String critere = "";
    private ListView listParticipants;
    private SharedPreferences sharedPref;
    private String adresseServeur;
    private int annee, mois, jour;
    private String nameFieldDateSet = "";
    private EditText edDateDebut, edDateFin;

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;

        if (source == "recuperationClassements") {
            if (isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la récupération du classement : " + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    JSONArray ja = new JSONArray(reponse);

                    if (ja.length() == 0) {
                        AccueilActivity.afficherMessage("Aucune donnée trouvée.", false, getSupportFragmentManager());
                    } else {
                        JSONObject jo = ja.getJSONObject(0);

                        if (jo.opt("erreur") != null) {
                            AccueilActivity.afficherMessage("Erreur lors de la recherche du classement : " + jo.getString("erreur"), false, getSupportFragmentManager());
                        }
                        else {
                            listParticipants = (ListView) findViewById(R.id.classements_scroll_participants);
                            listParticipants.setVisibility(View.VISIBLE);

                            participantsClasses.clear();

                            for(int i = 0 ; i < ja.length() ; i++) {
                                jo = ja.getJSONObject(i);

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

                                participantsClasses.add(participantClasse);
                            }

                            ParticipantClasseAdapter adapter = new ParticipantClasseAdapter(this, this, R.layout.activity_classements_adapter,
                                    participantsClasses, critere);
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

        edDateDebut = (EditText) findViewById(R.id.classements_edit_date_debut);
        edDateFin = (EditText) findViewById(R.id.classements_edit_date_fin);
        Spinner spCritere = (Spinner) findViewById(R.id.classements_spin_critere);
        Button btnAnnulerVisualisationClassements = (Button) findViewById(R.id.bouton_annuler_visualisation_classements);
        Button btnValiderVisualisationClassements = (Button) findViewById(R.id.bouton_valider_visualisation_classements);

        spCritere.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("position = " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnAnnulerVisualisationClassements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        btnValiderVisualisationClassements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemSelectedPosition = spCritere.getSelectedItemPosition();

                if((itemSelectedPosition < 0) || (itemSelectedPosition > 2)) {
                    AccueilActivity.afficherMessage("Veuillez sélectionner un critère.", false, getSupportFragmentManager());
                }
                else {
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
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        dateDebut = sdf.parse(edDateDebut.getText().toString());
                        dateDebutLong = dateDebut.getTime();
                        dateFin = sdf.parse(edDateFin.getText().toString());
                        dateFinLong = dateFin.getTime();

                        String email = sharedPref.getString("emailUtilisateur", "");
                        String mdp = sharedPref.getString("mdpUtilisateur", "");

                        String adresse = adresseServeur + ":" + Constants.portMicroserviceStatistiquesEtClassements +
                                "/statistiques-classements/recuperation-classements?email=" + email + "&mdp=" + mdp + "&dateDebut=" + dateDebutLong +
                                "&dateFin=" + dateFinLong + "&critere=" + critere;

                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                                adresse, ClassementsActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSONArray(ClassementsActivity.this, "RecuperationClassements", "GET", getSupportFragmentManager());
                    }
                    catch (ParseException e) {
                        AccueilActivity.afficherMessage("Erreur dans le format de la date/heure : " + e.getMessage(), false, getSupportFragmentManager());
                    }
                }
            }
        });

        edDateDebut.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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

        edDateFin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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

    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
            startActivity(intent);
        }
    }
}