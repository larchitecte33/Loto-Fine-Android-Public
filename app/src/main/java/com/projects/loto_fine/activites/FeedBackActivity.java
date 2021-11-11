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

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.adapters.QuestionAdapter;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Question;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Cette activité est affichée lors du clic sur le bouton FEEDBACK de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de répondre au questionnaire de feedback.
 */
public class FeedBackActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private LinkedList<Question> questions = new LinkedList<>();
    private ListView listeQuestions;
    private SharedPreferences sharedPref;
    private String adresseServeur;
    private LinearLayout layoutAttente;

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : recuperationQuestionsFeedback et envoyerReponsesFeedback.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;
        JSONObject jo = null;
        JSONArray ja = null;

        if (source == "recuperationQuestionsFeedback") {
            listeQuestions = (ListView)findViewById(R.id.feedback_scroll_questions);
            layoutAttente.setVisibility(View.GONE);
            listeQuestions.setVisibility(View.VISIBLE);

            // Si le serveur a renvoyé un erreur lors de la récupération des questions, alors on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue lors de la récupération des questions : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On tente de caster la réponse en JSONArray.
                    ja = new JSONArray(reponse);
                    questions.clear();

                    // On parcourt le JSONArray contenant les questions du feedback.
                    for (int i = 0; i < ja.length(); i++) {
                        // On va chercher le JSONObject représentant la question en cours.
                        jo = (JSONObject) ja.get(i);

                        // On crée une Question à partir des données extraites du JSONObject.
                        Question question = new Question();
                        question.setId(jo.getInt("id"));
                        question.setTexte(jo.getString("texteQuestion"));

                        // On ajoute la question à la liste des questions.
                        questions.add(question);
                    }

                    // On construit un ArrayAdapter de type QuestionAdapter en lui passant la liste des questions.
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
            // Si le serveur a renvoyé un erreur lors de l'envoi des réponses, alors on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue lors de l'envoi des réponses : ";
                vdf = new ValidationDialogFragment(message + reponse, false);
                vdf.show(getSupportFragmentManager(), "");
            } else {
                try {
                    // On récupère la réponse sous forme d'un JSONArray.
                    ja = new JSONArray(reponse);

                    // Si le JSONArray n'a aucune entrée, on renvoie une erreur.
                    if(ja.length() < 1) {
                        AccueilActivity.afficherMessage("Les réponses n'ont pas été envoyées.", false, getSupportFragmentManager());
                    }
                    else {
                        // On va chercher le JSONObject contenant la réponse.
                        jo = ja.getJSONObject(0);

                        // Si la réponse correspond à une erreur, on affiche cette erreur.
                        if(jo.opt("erreur") != null) {
                            AccueilActivity.afficherMessage(jo.getString("erreur"), false, getSupportFragmentManager());
                        }
                        // Si la réponse à pour clé message et pour valeur OK, alors les réponses ont bien été reçues par le serveur.
                        else if(jo.getString("message").toString().equals("OK")) {
                            AccueilActivity.afficherMessage("Les réponses ont été envoyées.", true, getSupportFragmentManager());
                        }
                        // Sinon, il y a eu une erreur lors de la réception des réponses.
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

        // Gestion du clic sur le bouton Annuler.
        btnAnnulerFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        // Gestion du clic sur le bouton Valider.
        btnValiderFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Construction de l'array JSON contenant les réponses aux questions.
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

                // Si l'e-mail ou le mot de passe n'est pas renseigné, alors on affiche une erreur.
                if((email.trim().length() == 0) || mdp.trim().length() == 0) {
                    AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    // On construit l'URL permettant d'envoyer les réponses au feedback au serveur.
                    String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                            "/participant/envoyer-reponses-feedback?email=" + email + "&mdp=" + mdp;
                    RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                            adresse, FeedBackActivity.this);
                    // On envoie la requête permettant d'envoyer les réponses au feedback au serveur.
                    requeteHTTP.traiterRequeteHTTPJSON(FeedBackActivity.this, "EnvoyerReponsesFeedback", "POST",
                            jsonReponses, getSupportFragmentManager());
                }
            }
        });
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