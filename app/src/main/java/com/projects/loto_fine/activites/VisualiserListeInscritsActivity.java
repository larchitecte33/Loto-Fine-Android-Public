package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.projects.loto_fine.R;
import com.projects.loto_fine.adapters.InscritAdapter;
import com.projects.loto_fine.classes_metier.Inscrit;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;
import com.projects.loto_fine.constantes.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Cette activité est affichée lors du clic sur le bouton VISUALISER LISTE INSCRITS de de l'adapter PartieAdapter.
 * Elle permet à l'utilisateur de visualiser la liste des inscrits dans une partie qu'il anime.
 */
public class VisualiserListeInscritsActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private LinkedList<Inscrit> inscrits = new LinkedList<>(); // Liste des inscrits.
    private ListView listeInscrits; // ListView affichant la liste des inscrits.
    private LinearLayout layoutAttente;
    private String adresseServeur;
    private SharedPreferences sharedPref;
    private List<Boolean> inscriptionsReglees = new ArrayList<>(); // Liste indiquant si l'inscrit a réglé son inscription.

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : rechercheListeInscrits et changerStatutValidationInscriptions.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;
        JSONObject jo = null;
        JSONArray ja = null;

        if (source == "rechercheListeInscrits") {
            // S'il y a une erreur lors de la recherche de la liste des inscrits, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On vide la liste des inscrits.
                    inscrits.clear();

                    // On tente de caster la réponse en JSONArray.
                    ja = new JSONArray(reponse);

                    // On parcourt le JSONArray
                    for (int i = 0; i < ja.length(); i++) {
                        // On extrait le JSONObject contenant les informations de l'inscrit.
                        jo = (JSONObject) ja.get(i);

                        // On crée un nouvel Inscrit et on lui affecte les informations extraites du JSONObject.
                        Inscrit inscrit = new Inscrit();
                        inscrit.setId(jo.getInt("id"));
                        inscrit.setNom(jo.getString("nom"));
                        inscrit.setPrenom(jo.getString("prenom"));
                        inscrit.setEmail(jo.getString("email"));
                        inscrit.setNumtel(jo.getString("numTel"));
                        inscrit.setInscriptionValidee(jo.getBoolean("isInscriptionReglee"));

                        // On ajoute l'inscrit à la liste des inscrits.
                        inscrits.add(inscrit);
                        // On ajoute le statut du règlement de l'inscription à la liste indiquant si l'inscrit a réglé son inscription.
                        inscriptionsReglees.add(jo.getBoolean("isInscriptionReglee"));
                    }

                    // On crée un nouvel ArrayAdapter de type InscritAdapter et on lui passe la liste des inscrits.
                    InscritAdapter adapter = new InscritAdapter(this, this, R.layout.activity_visualiser_liste_inscrits_adapter, inscrits);
                    listeInscrits = (ListView)findViewById(R.id.visu_liste_inscrits_scroll_inscrits);
                    listeInscrits.setAdapter(adapter);

                    layoutAttente.setVisibility(View.GONE);
                    listeInscrits.setVisibility(View.VISIBLE);
                }
                catch(JSONException e) {
                    // Ici, on a pas pu convertir la réponse en JSONArray. On essaie de la convertir en JSONObject (cas où le
                    // serveur a renvoyé une erreur.
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
        else if(source == "changerStatutValidationInscriptions") {
            // S'il y a une erreur lors du changement de statut de valiation des inscriptions, alors on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        AccueilActivity.afficherMessage("Le statut des inscriptions a été modifié.", true, getSupportFragmentManager());
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
        setContentView(R.layout.activity_visualiser_liste_inscrits);

        Intent intent = getIntent();
        // On récupère l'identifiant de la partie.
        int idPartie = intent.getIntExtra("idPartie", -1);

        // On va chercher l'adresse du serveur dans les SharedPreferences
        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        adresseServeur = sharedPref.getString("AdresseServeur", "");

        String messageErreur = "";

        // Si l'identifiant de la partie n'est pas renseigné
        if(idPartie == -1) {
            messageErreur = "L'identifiant de la partie n'est pas renseigné.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        }
        // Si l'adresse du serveur n'est pas renseignée
        else if(adresseServeur.trim().equals("")) {
            messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        }
        else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            // On va chercher la liste des inscrits pour la partie.
            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/get-participants-inscrits-a-partie?email=" +
                    AccueilActivity.encoderECommercial(email) + "&mdp=" + AccueilActivity.encoderECommercial(mdp) + "&idPartie=" + idPartie;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                    adresse, VisualiserListeInscritsActivity.this);
            requeteHTTP.traiterRequeteHTTPJSONArray(VisualiserListeInscritsActivity.this, "RechercheListeInscrits", "GET", getSupportFragmentManager());

            Button btnRetour = (Button) findViewById(R.id.visu_liste_inscrits_btn_retour);
            Button btnValider = (Button) findViewById(R.id.visu_liste_inscrits_btn_valider);
            layoutAttente = (LinearLayout) findViewById(R.id.visu_liste_inscrits_layout_attente);

            btnRetour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MesInscriptionsActivity.class);
                    startActivity(intent);
                }
            });

            btnValider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Chaine qui va contenir tous les identifiants des participants qui ont changé de statut d'inscription (ex : 5,34,45)
                    String strIdParticipants = "";

                    // On parcourt la liste des inscrits
                    for(int i = 0 ; i < inscrits.size() ; i++) {
                        // On va chercher l'inscrit en-cours de parcours
                        Inscrit inscrit = inscrits.get(i);

                        // Si le statut de l'inscription à changé
                        if((inscrit.isInscriptionValidee() && (inscriptionsReglees.get(i) == false))
                                || (!inscrit.isInscriptionValidee() && (inscriptionsReglees.get(i) == true))) {
                            if(!strIdParticipants.trim().equals(""))
                                strIdParticipants = strIdParticipants + "a";

                            strIdParticipants = strIdParticipants + inscrit.getId();
                        }
                    }

                    // Si aucune inscription n'a été modifiée
                    if (strIdParticipants.trim().equals("")) {
                        AccueilActivity.afficherMessage("Veuillez modifier au moins une inscription.", false, getSupportFragmentManager());
                    }
                    // Si au moins une inscription a été modifiée
                    else {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/changer-statut-validation-inscription?email=" +
                                email + "&mdp=" + mdp + "&idPartie=" + idPartie + "&identifiantsStr=" + strIdParticipants;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                                adresse, VisualiserListeInscritsActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(VisualiserListeInscritsActivity.this, "ChangerStatutValidationInscriptions", "PUT", "", getSupportFragmentManager());
                    }
                }
            });
        }
    }

    /**
     * Implémentation de la fonction onFinishEditDialog de l'interface ValidationDialogListener.
     * @param revenirAAccueil : true si on doit revenir à l'activité appelante, false sinon.
     */
    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), MesInscriptionsActivity.class);
            startActivity(intent);
        }
    }
}