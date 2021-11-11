package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cette activité est affichée lors du clic sur le bouton CONNEXION de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de se connecter à l'application.
 */
public class ConnectionActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private EditText edSaisirEmail;
    private String mdp = "";

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : connexion et obtentionInfosPersonne
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;

        if(source == "connexion") {
            try {
                // Si une erreur a été renvoyée par le serveur, on l'affiche.
                if (isErreur) {
                    AccueilActivity.afficherMessage("Une erreur est survenue : " + reponse, false, getSupportFragmentManager());
                }
                else {
                    // On tente de caster la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);
                    String result = jo.getString("result");

                    // Si la valeur de result renvoyée dans la réponse est false, cela veut dire que les identifiants sont incorrects.
                    if (result.equals("false")) {
                        AccueilActivity.afficherMessage("Identifiants incorrects", false, getSupportFragmentManager());
                    } else {
                        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                        // On recherche l'adresse du serveur dans les SharedPreferences.
                        String adresseServeur = sharedPref.getString("AdresseServeur", "");

                        // Si l'adresse du serveur n'est pas renseignée, on affiche une erreur.
                        if(adresseServeur.trim().equals("")) {
                            AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", false, getSupportFragmentManager());
                        }
                        else {
                            // On construit l'URL permettant d'obtenir les informations de la personne.
                            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/get_infos_personne?email=" +
                                    AccueilActivity.encoderECommercial(edSaisirEmail.getText().toString());
                            RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                                    adresse, ConnectionActivity.this);
                            // On envoie la requête permettant d'obtenir les informations de la personne.
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
                // On tente de caster la réponse en JSONObject.
                JSONObject jo = new JSONObject(reponse);
                String nomUtilisateur = jo.getString("nom") + " " + jo.get("prenom");
                String emailUtilisateur = jo.getString("email");

                SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPref.edit();

                // On sauvegarde le nom de l'utilisateur, son e-mail et son mot de passe dans les SharedPreferences.
                edit.putString("nomUtilisateur", nomUtilisateur);
                edit.putString("emailUtilisateur", emailUtilisateur);
                edit.putString("mdpUtilisateur", mdp); // Le mot de passe est récupéré dans la saisie de l'utilisateur.

                // Si la sauvegarde s'est bien effectuée.
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

        // Récupération des composants depuis le layout.
        edSaisirEmail = findViewById(R.id.co_edit_saisir_email);
        EditText edSaisirMdp = findViewById(R.id.co_edit_saisir_mdp);
        Button btAnnuler = findViewById(R.id.bouton_annuler_connexion);
        Button btValider = findViewById(R.id.bouton_valider_connexion);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        // Gestion du clic sur le bouton Annuler.
        btAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        // Gestion du clic sur le bouton Valider.
        btValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si l'adresse e-mail n'est pas renseignée, on affiche une erreur.
                if(edSaisirEmail.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.txt_veuillez_saisir_email, Toast.LENGTH_LONG).show();
                }
                // Si le mot de passe n'est pas renseigné, on affiche une erreur.
                else if(edSaisirMdp.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.txt_veuillez_saisir_mdp, Toast.LENGTH_LONG).show();
                }
                else {
                    SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    String adresseServeur = sharedPref.getString("AdresseServeur", "");

                    // Si l'adresse du serveur n'est pas renseignée
                    if(adresseServeur.trim().equals("")) {
                        AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", false, getSupportFragmentManager());
                    }
                    else {
                        mdp = edSaisirMdp.getText().toString();

                        // On construit l'URL permettant de vérifier si le mot de passe correspond à l'e-mail.
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                                "/participant/verif-mdp?email=" + AccueilActivity.encoderECommercial(edSaisirEmail.getText().toString()) + "&mdp=" +
                                AccueilActivity.encoderECommercial(edSaisirMdp.getText().toString());
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                                adresse, ConnectionActivity.this);
                        // On envoie la requête permettant de vérifier si le mot de passe correspond à l'e-mail.
                        requeteHTTP.traiterRequeteHTTPJSON(ConnectionActivity.this, "Connexion", "GET", "", getSupportFragmentManager());
                    }
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