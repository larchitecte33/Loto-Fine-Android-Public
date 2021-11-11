package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cette activité est affichée lors du clic sur le bouton "AJOUTER UN LOT" de l'activité VisualiserListeLotsActivite.
 * Elle permet à l'utilisateur de saisir le informations d'un nouveau lot (nom du lot, valeur du lot et à la ligne ou au carton plein).
 */
public class AjoutLotActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private SharedPreferences sharedPref;
    private int idPartie = -1;
    private int positionMax = -1;
    private String emailAnimateur = "", source = "";

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : recuperationInfosLots, ajoutLot.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";

        if (source == "recuperationInfosLots") {
            // JSONArray qui va servir à stocker les données de la réponse.
            JSONArray ja;

            // Si une erreur a été renvoyée par le serveur, on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la récupération des informations concernant les lots : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On récupère le table de lot sous format JSON.
                    ja = new JSONArray(reponse);
                    JSONObject joLot;

                    // On parcourt le tableau de lots.
                    for(int i = 0 ; i < ja.length() ; i++) {
                        // On initialise joLot avec le lot en-cours de parcourt.
                        joLot = ja.getJSONObject(i);

                        // Si le lot en-cours de parcourt a une position supérieure à la position max, alors on change la position max.
                        if(joLot.getInt("position") > positionMax) {
                            positionMax = joLot.getInt("position");
                        }
                    }
                }
                // Cas où on a pas pu caster notre réponse ne JSONArray.
                catch(JSONException e) {
                    try {
                        ja = new JSONArray(reponse);

                        if (ja.length() == 1) {
                            JSONObject objErreur = ja.getJSONObject(0);

                            // Si on a bien récupéré une erreur, on l'affiche.
                            if (objErreur != null) {
                                message = "Erreur : " + objErreur.getString("erreur");
                                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                            }
                        }
                    } catch (JSONException ex) {
                        AccueilActivity.afficherMessage(ex.getMessage(), false, getSupportFragmentManager());
                    }
                }
            }
        }
        else if (source == "ajoutLot") {
            // Si une erreur a été renvoyée par le serveur, on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de l'ajout du lot : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    // On va voir si le JSON renvoyé correspond à une erreur.
                    Object objErreur = jo.opt("erreur");

                    // Si c'est le cas, on l'affiche.
                    if (objErreur != null) {
                        AccueilActivity.afficherMessage(objErreur.toString(), false, getSupportFragmentManager());
                    }
                    // Sinon, on affiche un message indiquant que tout s'est bien passé.
                    else {
                        AccueilActivity.afficherMessage("L'ajout du lot a été effectué.", true, getSupportFragmentManager());
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage("Erreur JSON lors de l'ajout du lot : " + e.getMessage(), false, getSupportFragmentManager());
                }
                catch(Exception e) {
                    AccueilActivity.afficherMessage("Erreur lors de l'ajout du lot : " + e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_lot);

        Intent intent = getIntent();
        // On récupère les valeurs passées dans l'intent.
        idPartie = intent.getIntExtra("idPartie", -1);
        emailAnimateur = intent.getStringExtra("emailAnimateur");
        source = intent.getStringExtra("source");

        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        // On récupère l'adrese du serveur dans les SharedPreferences.
        String adresseServeur = sharedPref.getString("AdresseServeur", "");
        String messageErreur;

        // Si l'adresse du serveur n'est pas définie, on affiche une erreur.
        if(adresseServeur.trim().equals("")) {
            messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        }
        else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            // Construction de l'adresse permettant de récupérer les lots de la partie.
            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/recuperation-lots?email="
                    + AccueilActivity.encoderECommercial(email) + "&mdp=" + AccueilActivity.encoderECommercial(mdp) + "&idPartie=" + idPartie;

            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                    adresse, AjoutLotActivity.this);
            // On envoi la requête qui doit renvoyer un array JSON.
            requeteHTTP.traiterRequeteHTTPJSONArray(AjoutLotActivity.this, "RecuperationInfosLots", "GET", getSupportFragmentManager());
        }

        // Récupération des composants
        EditText editNomLot = findViewById(R.id.ajout_lot_edit_saisir_nom_lot);
        EditText editValeurLot = findViewById(R.id.ajout_lot_edit_saisir_valeur_lot);
        CheckBox cbLotEstAuCartonPlein = findViewById(R.id.ajout_lot_cb_carton_plein);
        Button btnAnnulerAjoutLot = findViewById(R.id.ajout_lot_bouton_annuler_ajout_lot);
        Button btnValiderAjoutLot = findViewById(R.id.ajout_lot_bouton_valider_ajout_lot);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnAnnulerAjoutLot.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnAnnulerAjoutLot.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnValiderAjoutLot.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnValiderAjoutLot.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        // Clic sur le bouton "annuler"
        btnAnnulerAjoutLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VisualiserListeLotsActivity.class);
                intent.putExtra("idPartie", idPartie);
                intent.putExtra("emailAnimateur", emailAnimateur);
                intent.putExtra("source", source);
                startActivity(intent);
            }
        });

        // Clic sur le bouton "valider"
        btnValiderAjoutLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double valeurLot = 0.0;
                boolean erreurTrouvee = false;
                String nomLot = editNomLot.getText().toString();
                int isALaLigne = 1;

                // Si le nom du lot n'est pas renseigné, on affiche une erreur.
                if(nomLot.equals("")) {
                    AccueilActivity.afficherMessage("Le nom du lot doit être renseigné.", false, getSupportFragmentManager());
                }
                else {
                    // On va voir si la valeur du lot est bien un double.
                    try {
                        valeurLot = Double.valueOf(editValeurLot.getText().toString());
                    }
                    catch(NumberFormatException e) {
                        AccueilActivity.afficherMessage("La valeur du lot est incorrecte.", false, getSupportFragmentManager());
                        erreurTrouvee = true;
                    }

                    if(cbLotEstAuCartonPlein.isChecked()) {
                        isALaLigne = 0;
                    }
                    else {
                        isALaLigne = 1;
                    }

                    // S'il n'y a pas d'erreur dans la saisie, on continue.
                    if(!erreurTrouvee) {
                        // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                        String email = sharedPref.getString("emailUtilisateur", "");
                        String mdp = sharedPref.getString("mdpUtilisateur", "");

                        // On crée la requête permettant d'ajouter le lot à la partie.
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/creation-lot?email=" +
                                AccueilActivity.encoderECommercial(email) +
                                "&mdp=" + AccueilActivity.encoderECommercial(mdp) +
                                "&idPartie=" + idPartie + "&nomLot=" + AccueilActivity.encoderECommercial(nomLot) +
                                "&valeurLot=" + valeurLot + "&isALaLigne=" + isALaLigne +
                                "&position=" + (positionMax + 1);
                        System.out.println("Ajout d'un lot(nomLot = " + nomLot + ", valeurLot = " + valeurLot + ") : " + adresse);

                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                                adresse, AjoutLotActivity.this);
                        // On envoie la requête permettant d'ajouter le lot à la partie.
                        requeteHTTP.traiterRequeteHTTPJSON(AjoutLotActivity.this, "AjoutLot", "POST", "", getSupportFragmentManager());
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
        // Si on revient à l'activité appelante.
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), VisualiserListeLotsActivity.class);
            intent.putExtra("idPartie", idPartie);
            intent.putExtra("emailAnimateur", emailAnimateur);
            intent.putExtra("source", source);
            startActivity(intent);
        }
    }
}