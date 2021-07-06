package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.projects.loto_fine.Constants;
import com.projects.loto_fine.QuestionAdapter;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Question;
import com.projects.loto_fine.classes_metier.RequeteHTTP;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class FeedBackActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private LinkedList<Question> questions = new LinkedList<>();
    private ListView listeQuestions;
    private SharedPreferences sharedPref;
    private String adresseServeur;
    private LinearLayout layoutAttente;

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;
        JSONObject jo = null;
        JSONArray ja = null;

        if (source == "recuperationQuestionsFeedback") {
            listeQuestions = (ListView)findViewById(R.id.feedback_scroll_questions);
            layoutAttente.setVisibility(View.GONE);
            listeQuestions.setVisibility(View.VISIBLE);

            if (isErreur) {
                message = "Une erreur est survenue lors de la récupération des questions : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    ja = new JSONArray(reponse);
                    questions.clear();

                    // On parcourt le JSONArray
                    for (int i = 0; i < ja.length(); i++) {
                        jo = (JSONObject) ja.get(i);

                        Question question = new Question();
                        question.setId(jo.getInt("id"));
                        question.setTexte(jo.getString("texteQuestion"));

                        questions.add(question);
                    }

                    QuestionAdapter adapter = new QuestionAdapter(this, this, R.layout.activity_feed_back_adapter, questions);
                    listeQuestions.setAdapter(adapter);
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
                    catch(JSONException ex) {
                        AccueilActivity.afficherMessage(ex.getMessage(), false, getSupportFragmentManager());
                    }
                }
            }
        }
        else if (source == "envoyerReponsesFeedback") {
            if (isErreur) {
                message = "Une erreur est survenue lors de l'envoi des réponses : ";
                vdf = new ValidationDialogFragment(message + reponse, false);
                vdf.show(getSupportFragmentManager(), "");
            } else {
                try {
                    ja = new JSONArray(reponse);

                    if(ja.length() < 1) {
                        AccueilActivity.afficherMessage("Les réponses n'ont pas été envoyées.", false, getSupportFragmentManager());
                    }
                    else {
                        jo = ja.getJSONObject(0);

                        if(jo.opt("erreur") != null) {
                            AccueilActivity.afficherMessage(jo.getString("erreur"), false, getSupportFragmentManager());
                        }
                        else if(jo.getString("message").toString().equals("OK")) {
                            AccueilActivity.afficherMessage("Les réponses ont été envoyées.", true, getSupportFragmentManager());
                        }
                        else {
                            AccueilActivity.afficherMessage("Les réponses n'ont pas été envoyées.", false, getSupportFragmentManager());
                        }
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

        // Pour que l'écran reste en mode portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_feed_back);

        Button btnAnnulerFeedBack = findViewById(R.id.feedback_bouton_annuler_feedback);
        Button btnValiderFeedBack = findViewById(R.id.feedback_bouton_valider_feedback);
        layoutAttente = findViewById(R.id.feedback_layout_attente);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnAnnulerFeedBack.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnAnnulerFeedBack.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnValiderFeedBack.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnValiderFeedBack.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        // On va chercher l'adresse du serveur dans les SharedPreferences
        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        adresseServeur = sharedPref.getString("AdresseServeur", "");
        String emailUtilisateur = sharedPref.getString("emailUtilisateur", "");

        String messageErreur = "";

        // Si l'adresse du serveur n'est pas renseignée
        if(adresseServeur.trim().equals("")) {
            messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            ValidationDialogFragment fmdf = new ValidationDialogFragment(messageErreur, false);
            fmdf.show(getSupportFragmentManager(), "");
        }
        else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            // On va chercher la questions du feedback.
            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperer-questions-feedback?" +
                    "email=" + email + "&mdp=" + mdp;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                    adresse, FeedBackActivity.this);
            requeteHTTP.traiterRequeteHTTPJSONArray(FeedBackActivity.this, "RecuperationQuestionsFeedback", "GET", getSupportFragmentManager());
        }

        btnAnnulerFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        btnValiderFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jsonReponses = "[";

                for(int i = 0 ; i < questions.size() ; i++) {
                    System.out.println("Réponse à la question " + questions.get(i).getId() + " : " + questions.get(i).getReponseSelectionnee());

                    jsonReponses = jsonReponses + "{" +
                            "  \"numQuestion\": " + questions.get(i).getId() + ", " +
                            "  \"reponse\": " + questions.get(i).getReponseSelectionnee() +
                            "}";

                    if(i < questions.size() - 1) {
                        jsonReponses = jsonReponses + ",";
                    }
                }

                jsonReponses = jsonReponses + "]";

                // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                String email = sharedPref.getString("emailUtilisateur", "");
                String mdp = sharedPref.getString("mdpUtilisateur", "");

                if((email.trim().length() == 0) || mdp.trim().length() == 0) {
                    AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                            "/participant/envoyer-reponses-feedback?email=" + email + "&mdp=" + mdp;
                    RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                            adresse, FeedBackActivity.this);
                    requeteHTTP.traiterRequeteHTTPJSON(FeedBackActivity.this, "EnvoyerReponsesFeedback", "POST",
                            jsonReponses, getSupportFragmentManager());
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