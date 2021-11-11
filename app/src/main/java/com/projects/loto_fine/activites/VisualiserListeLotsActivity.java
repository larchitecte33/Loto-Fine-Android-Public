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
import com.projects.loto_fine.adapters.LotAdapter;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Lot;
import com.projects.loto_fine.classes_utilitaires.OuiNonDialogFragment;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Cette activité est affichée lors du clic sur le bouton VISUALISER LISTE LOTS de l'adapter PartieAdapter.
 * Elle permet à l'utilisateur de visualiser la liste des inscrits dans une partie qu'il anime.
 */
public class VisualiserListeLotsActivity extends AppCompatActivity implements OuiNonDialogFragment.OuiNonDialogListener,
        ValidationDialogFragment.ValidationDialogListener {

    private ListView listeLots; // ListView affichant la liste des lots.
    private int idPartie = -1, idLotSupprime;
    private String emailAnimateur = "";
    private SharedPreferences sharedPref;
    private String adresseServeur;
    private LinkedList<Lot> lots = new LinkedList<>(); // Liste des lots.
    private LinearLayout layoutAttente;

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : rechercheListeLots et suppressionLot.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;
        JSONObject jo = null;
        JSONArray ja = null;

        if (source == "rechercheListeLots") {
            // S'il y a une erreur lors de la recherche de la liste des lots, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On tente de caster la réponse en JSONArray.
                    ja = new JSONArray(reponse);

                    // On parcourt le JSONArray
                    for(int i = 0 ; i < ja.length() ; i++) {
                        // On extrait le JSONObject contenant les informations du lot.
                        jo = (JSONObject)ja.get(i);

                        // On crée un nouveau Lot et on lui affecte les informations extraites du JSONObject.
                        Lot lot = new Lot();
                        lot.setId(jo.getInt("id"));
                        lot.setNom(jo.getString("nom"));
                        lot.setValeur((float)jo.getDouble("valeur"));
                        lot.setPosition(jo.getInt("position"));

                        if(jo.getInt("cartonPlein") == 1)
                            lot.setAuCartonPlein(true);
                        else
                            lot.setAuCartonPlein(false);

                        if(jo.getInt("enCoursDeJeu") == 1)
                            lot.setEnCoursDeJeu(true);
                        else
                            lot.setEnCoursDeJeu(false);

                        // Si le lot a été gagné, on lui affecte son gagnant.
                        if(!jo.isNull("gagnant")) {
                            Personne gagnant = new Personne();
                            JSONObject joGagnant = jo.getJSONObject("gagnant");
                            gagnant.setId(joGagnant.getInt("id"));
                            gagnant.setNom(joGagnant.getString("nom"));
                            gagnant.setPrenom(joGagnant.getString("prenom"));
                            gagnant.setEmail(joGagnant.getString("email"));
                            lot.setGagnant(gagnant);
                        }
                        else {
                            lot.setGagnant(null);
                        }

                        if(jo.getString("adresseRetrait").equals("null")) {
                            lot.setAdresseRetrait(null);
                        }
                        else {
                            lot.setAdresseRetrait(jo.getString("adresseRetrait"));
                        }

                        if(jo.getString("cpRetrait").equals("null")) {
                            lot.setCpRetrait(null);
                        }
                        else {
                            lot.setCpRetrait(jo.getString("cpRetrait"));
                        }

                        if(jo.getString("villeRetrait").equals("null")) {
                            lot.setVilleRetrait(null);
                        }
                        else {
                            lot.setVilleRetrait(jo.getString("villeRetrait"));
                        }

                        // On ajoute le lot à la liste des lots.
                        lots.add(lot);
                    }

                    // On crée un nouvel ArrayAdapter de type LotAdapter et on lui passe la liste des lots.
                    LotAdapter adapter = new LotAdapter(this, this, R.layout.activity_visualiser_liste_lots_adapter, lots, idPartie, emailAnimateur, source);
                    listeLots = (ListView)findViewById(R.id.visu_liste_lots_scroll_lots);
                    listeLots.setAdapter(adapter);

                    layoutAttente.setVisibility(View.GONE);
                    listeLots.setVisibility(View.VISIBLE);
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
        else if(source == "suppressionLot") {
            // S'il y a une erreur lors de la suppression du lot, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspon à une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        AccueilActivity.afficherMessage("Le lot a été supprimé.", false, getSupportFragmentManager());

                        boolean lotEstSupprimeDeListe = false;

                        // On supprime le lot de la liste des lots affichée pour être en adéquation avec la base de données.
                        for(int i = 0 ; i < lots.size() ; i++) {
                            if(lots.get(i).getId() == idLotSupprime) {
                                lots.remove(i);
                                lotEstSupprimeDeListe = true;
                            }
                        }

                        // Si le lot a été supprimé, on met à jour la liste des lots affichée.
                        if(lotEstSupprimeDeListe)
                            ((LotAdapter)listeLots.getAdapter()).notifyDataSetChanged();
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

        setContentView(R.layout.activity_visualiser_liste_lots);

        Intent intent = getIntent();
        // On récupère l'identifiant de la partie.
        idPartie = intent.getIntExtra("idPartie", -1);
        // On récupère l'email de l'animateur.
        emailAnimateur = intent.getStringExtra("emailAnimateur");
        // On récupère le nom de l'activity source
        String source = intent.getStringExtra("source");
        // On récupère le type de recherche et la recherche effectuée pour arriver sur cette activité.
        int typeRecherche = intent.getIntExtra("typeRecherche", -1);
        String recherche = intent.getStringExtra("recherche");

        // Récupération des composants.
        Button btnAjouterLot = findViewById(R.id.visu_liste_lots_btn_ajouter_lot);
        Button btnRetour = findViewById(R.id.visu_liste_lots_btn_retour);
        layoutAttente = (LinearLayout) findViewById(R.id.visu_liste_lots_layout_attente);
        LinearLayout layoutAjouterLot = (LinearLayout) findViewById(R.id.visu_liste_lots_layout_ajouter_lot);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnAjouterLot.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnAjouterLot.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnRetour.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnRetour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        // On va chercher l'adresse du serveur dans les SharedPreferences
        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        adresseServeur = sharedPref.getString("AdresseServeur", "");
        String emailUtilisateur = sharedPref.getString("emailUtilisateur", "");

        String messageErreur = "";
        idLotSupprime = -1;

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

            // On va chercher la liste des lots pour la partie.
            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/recuperation-lots?email=" +
                    AccueilActivity.encoderECommercial(email) +
                    "&mdp=" + AccueilActivity.encoderECommercial(mdp) + "&idPartie=" + idPartie;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                    adresse, VisualiserListeLotsActivity.this);
            requeteHTTP.traiterRequeteHTTPJSONArray(VisualiserListeLotsActivity.this, "RechercheListeLots", "GET", getSupportFragmentManager());
        }

        // Si l'utilisateur est l'animateur de la partie, alors il peut ajouter des lots.
        if(emailUtilisateur.equals(emailAnimateur)) {
            btnAjouterLot.setVisibility(View.VISIBLE);
            btnAjouterLot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), AjoutLotActivity.class);
                    intent.putExtra("idPartie", idPartie);
                    intent.putExtra("emailAnimateur", emailAnimateur);
                    intent.putExtra("source", source);
                    startActivity(intent);
                    }
            });
        }
        // Sinon, il ne peut pas ajouter de lots.
        else {
            layoutAjouterLot.setVisibility(View.GONE);
        }

        // Gestion du clic sur le bouton Retour.
        btnRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(source.equals("RecherchePartieActivity")) {
                    Intent intent = new Intent(getApplicationContext(), RecherchePartieActivity.class);
                    intent.putExtra("source", "RecherchePartieActivity");
                    intent.putExtra("typeRecherche", typeRecherche);
                    intent.putExtra("recherche", recherche);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), MesInscriptionsActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public void onFinishEditDialog(String nomActionOui, String nomActionNon, boolean isChoixOui, HashMap<String, String> args) {
        // Si choix = Oui
        if(isChoixOui) {
            if(nomActionOui == "supprimerLot") {
                String email = sharedPref.getString("emailUtilisateur", "");
                String mdp = sharedPref.getString("mdpUtilisateur", "");

                this.idLotSupprime = Integer.valueOf(args.get("idLot"));

                // On demande la suppression du lot.
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur + "/animateur/suppression-lot?email=" +
                        AccueilActivity.encoderECommercial(email) +
                        "&mdp=" + AccueilActivity.encoderECommercial(mdp) + "&idLot=" + args.get("idLot");
                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, VisualiserListeLotsActivity.this);

                requeteHTTP.traiterRequeteHTTPJSON(VisualiserListeLotsActivity.this, "SuppressionLot", "DELETE", "", getSupportFragmentManager());
            }
        }
    }

    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        //
    }
}