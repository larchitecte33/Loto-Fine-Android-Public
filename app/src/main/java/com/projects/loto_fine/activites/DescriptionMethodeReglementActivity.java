package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;
import com.projects.loto_fine.constantes.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cette activité est affichée lors du clic sur le bouton COMMENT REGLER de l'adapter PartieAdapter.
 * Elle permet à l'utilisateur de visualiser la méthode de règlement des cartons.
 */
public class DescriptionMethodeReglementActivity extends AppCompatActivity {

    private TextView tvDescriptionMethodeReglement;

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponse traitée : recuperationMethodeReglement
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;

        if (source == "recuperationMethodeReglement") {
            // Si le serveur a renvoyé un erreur lors de la récupération de la méthode de règlement, alors on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la récupération de la méthode de réglement de la partie : " + reponse,
                        false, getSupportFragmentManager());
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
                    // Sinon, on affiche la méthode de règlement.
                    else {
                        tvDescriptionMethodeReglement.setText(jo.getString("methodeReglement"));
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
        setContentView(R.layout.activity_description_methode_reglement);

        Intent intent = getIntent();
        // On récupère l'identifiant de la partie.
        int idPartie = intent.getIntExtra("idPartie", -1);
        // On récupère le nom de l'activité source.
        String source = intent.getStringExtra("source");
        // On récupère le type de recherche et la recherche effectuée pour arriver sur cette activité.
        int typeRecherche = intent.getIntExtra("typeRecherche", -1);
        String recherche = intent.getStringExtra("recherche");

        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        // On va chercher l'adresse du serveur dans les SharedPreferences.
        String adresseServeur = sharedPref.getString("AdresseServeur", "");

        // Récupération des composants depuis le layout.
        tvDescriptionMethodeReglement = (TextView) findViewById(R.id.methode_reglement_tv_description_methode);
        Button btnRetour = (Button) findViewById(R.id.methode_reglement_btn_retour);

        // *************** Récupération de la méthode de réglement de la partie ***************** //
        // Si l'adresse du serveur n'est pas renseignée
        if (adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        } else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            // Si l'e-mail ou le mot de passe ne sont pas renseignés, on affiche une erreur.
            if((email.trim().length() == 0) || mdp.trim().length() == 0) {
                AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", true, getSupportFragmentManager());
            }
            else {
                // On construit l'URL permettant de récupérer la méthode de règlement de la partie.
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                        "/participant/recuperation-methode-reglement?email=" + AccueilActivity.encoderECommercial(email) +
                        "&mdp=" + AccueilActivity.encoderECommercial(mdp) + "&idPartie=" + idPartie;
                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                        adresse, DescriptionMethodeReglementActivity.this);
                // On envoie la requête permettant de récupérer la méthode de règlement de la partie.
                requeteHTTP.traiterRequeteHTTPJSON(DescriptionMethodeReglementActivity.this, "RecuperationMethodeReglement",
                        "GET", "", getSupportFragmentManager());
            }
        }

        // Gestion du clic sur le bouton Retour
        btnRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                // Si le nom de l'activité source est RecherchePartieActivity, on renvoie le type de recherche et la recherche de façon à pouvoir
                // ré-afficher la recherche.
                if(source.equals("RecherchePartieActivity")) {
                    intent = new Intent(getApplicationContext(), RecherchePartieActivity.class);
                    intent.putExtra("typeRecherche", typeRecherche);
                    intent.putExtra("recherche", recherche);
                }
                else {
                    intent = new Intent(getApplicationContext(), MesInscriptionsActivity.class);
                }

                startActivity(intent);
            }
        });
    }
}