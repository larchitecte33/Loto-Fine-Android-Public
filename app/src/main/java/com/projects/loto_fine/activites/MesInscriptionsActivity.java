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
import android.widget.RadioButton;

import com.projects.loto_fine.classes_metier.Inscription;
import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.adapters.PartieAdapter;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Lot;
import com.projects.loto_fine.classes_metier.Partie;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * Cette activité est affichée lors du clic sur le bouton MES INSCRIPTIONS de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de consulter la liste des ses inscriptions à des parties de loto.
 */
public class MesInscriptionsActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private ListView listParties; // ListView affichant les parties.
    private LinearLayout layoutAttente; // Layout affichant un message d'attente.
    private LinkedList<Partie> parties = new LinkedList<Partie>(); // Liste de parties.
    private ArrayList<Inscription> inscriptions = new ArrayList<>(); // Liste d'inscriptions.
    private SharedPreferences sharedPref; // SharedPreferences utilisé pour récupérer des valeurs stockées sur le téléphone/tablette.
    private String adresseServeur; // Adresse du serveur.

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : rechercheListeInscriptions, suppressionPartie et desinscriptionPartie.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;
        JSONObject jo = null;
        JSONArray ja = null;

        if (source == "rechercheListeInscriptions") {
            layoutAttente.setVisibility(View.GONE);

            // On vide la liste des parties et des inscriptions.
            parties.clear();
            inscriptions.clear();

            // On récupère la ListView affichant les parties.
            listParties = (ListView)findViewById(R.id.mes_inscriptions_scroll_parties);
            listParties.setVisibility(View.VISIBLE);

            // S'il y a une erreur lors de la recherche de la liste des inscriptions, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    int idPartie, animateurId;
                    Date dateHeurePartie = null;
                    String cp, ville, adresse, dateHeurePartieStr, patternDate = "dd/MM/yyyy", patternHeure = "HH", patternMinutes = "mm";
                    double prixCarton = 0.0;
                    boolean isExceptionDateHeurePartie = false;
                    ArrayList<Lot> lots = new ArrayList<>();

                    // On tente de caster la réponse en JSONArray.
                    ja = new JSONArray(reponse);
                    JSONArray jaLots;
                    JSONObject joLots;
                    JSONObject joAnimateur;
                    Personne animateur;

                    // On parcourt chaque partie trouvée dans le JSON.
                    for (int i = 0; i < ja.length(); i++) {
                        // On extrait l'objet JSON contenant les informations de la partie, le montant restant à régler et un booléen indiquant si la partie
                        // a été réglée.
                        jo = ja.getJSONObject(i);

                        Inscription inscription = new Inscription();
                        inscription.setMontant(jo.getDouble("montantARegler"));
                        inscription.setValidee(jo.getBoolean("isReglee"));

                        // On extrait l'objet JSON contenant les informations de la partie.
                        jo = jo.getJSONObject("partie");

                        isExceptionDateHeurePartie = false;

                        dateHeurePartieStr = jo.getString("date");

                        try {
                            dateHeurePartie = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(dateHeurePartieStr);
                        } catch (ParseException e) {
                            isExceptionDateHeurePartie = true;
                        }

                        // Si la date/heure de début de la partie est correcte.
                        if (!isExceptionDateHeurePartie) {
                            // On extrait tous les éléments dont on a besoin.
                            idPartie = jo.getInt("id");
                            cp = jo.getString("cp");
                            ville = jo.getString("ville");
                            adresse = jo.getString("adresse");
                            prixCarton = jo.getDouble("prixCarton");

                            joAnimateur = jo.getJSONObject("animateur");
                            animateur = new Personne();
                            animateur.setId(joAnimateur.getInt("id"));
                            animateur.setNom(joAnimateur.getString("nom"));
                            animateur.setPrenom(joAnimateur.getString("prenom"));
                            animateur.setAdresse(joAnimateur.getString("adresse"));
                            animateur.setCp(joAnimateur.getString("cp"));
                            animateur.setVille(joAnimateur.getString("ville"));
                            animateur.setEmail(joAnimateur.getString("email"));
                            animateur.setNumtel(joAnimateur.getString("numTel"));
                            animateur.setMdp(joAnimateur.getString("mdp"));

                            jaLots = jo.getJSONArray("lots");
                            lots = new ArrayList<>();

                            // On parcourt tous les lots.
                            for (int j = 0; j < jaLots.length(); j++) {
                                joLots = jaLots.getJSONObject(j);

                                Lot lot = new Lot();
                                lot.setId(joLots.getInt("id"));
                                lot.setNom(joLots.getString("nom"));
                                lot.setValeur((float)joLots.getDouble("valeur"));

                                lots.add(lot);
                            }

                            // On crée une partie avec les informations extraites au-dessus et on l'ajoute à la liste des parties.
                            parties.add(new Partie(idPartie, dateHeurePartie, adresse, ville, cp, lots, prixCarton, animateur));

                            // On ajoute la partie à la liste des inscriptions.
                            inscription.setIdPartie(idPartie);
                            inscriptions.add(inscription);
                        }
                    }

                    PartieAdapter adapter = new PartieAdapter(this, this, R.layout.activity_recherche_partie_adapter,
                            parties, inscriptions, 1, "", getSupportFragmentManager());
                    listParties.setAdapter(adapter);
                }
                catch(JSONException e) {
                    // Ici, on a pas pu convertir la réponse en JSONArray. On essaie de la convertir en JSONObject (cas où le
                    // serveur a renvoyé une erreur.
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
        else if(source == "suppressionPartie") {
            // S'il y a une erreur lors de la suppression de la partie, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On tente de caster la réponse en JSObject.
                    JSONObject jsonObject = new JSONObject(reponse);

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if (jsonObject.opt("erreur") != null) {
                        message = "Erreur : " + jsonObject.getString("erreur");
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }
                    // Sinon, on affiche un message indiquant que la partie a bien été supprimée.
                    else {
                        message = "La partie a été supprimée";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage("Une erreur est survenue : " + e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "desinscriptionPartie") {
            // S'il y a une erreur lors de la désinscription de la partie, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    JSONObject jsonObject = new JSONObject(reponse);

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if (jsonObject.opt("erreur") != null) {
                        message = "Erreur : " + jsonObject.getString("erreur");
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        // On récupère l'id de la partie supprimée.
                        int idPartie = jsonObject.getInt("idPartie");
                        message = "Vous avez été désinscrit de la partie";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());

                        // On parcourt les parties auquelles l'utilisateur était inscrit avant la désinscription.
                        for(int i = 0 ; i < parties.size() ; i++) {
                            // Si l'id de la partie parcouru correspond à l'id de la partie supprimée, alors on supprime la partie des parties
                            // auxquelles est inscrit l'utilisateur.
                            if(parties.get(i).getId() == idPartie) {
                                parties.remove(i);
                                inscriptions.remove(i);
                            }
                        }

                        // On construit un PartieAdapter avec la liste des parties auxquelles est inscrit l'utilisateur.
                        PartieAdapter adapter = new PartieAdapter(this, this, R.layout.activity_recherche_partie_adapter,
                                parties, inscriptions, 1, "", getSupportFragmentManager());
                        // On fournit l'adapter à la ListView listParties.
                        listParties.setAdapter(adapter);
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage("Une erreur est survenue : " + e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
    }

    /**
     * Fonction permettant de rechercher des parties.
     * @param inclurePartiesPassees : true si on doit inclure les parties passées, false sinon.
     * @param inclurePartiesAnimateur : true si on doit inclure les parties animées, false sinon.
     * @param inclurePartiesAnimeesDansPasse : true si on doit inclure les parties animées dans le passé, false sinon.
     */
    public void rechercherParties(boolean inclurePartiesPassees, boolean inclurePartiesAnimateur, boolean inclurePartiesAnimeesDansPasse) {
        int inclurePartiesPasseesInt, inclurePartiesAnimateurInt, inclurePartiesAnimeesDansPasseInt;

        if(inclurePartiesPassees) {
            inclurePartiesPasseesInt = 1;
        }
        else {
            inclurePartiesPasseesInt = 0;
        }

        if(inclurePartiesAnimateur) {
            inclurePartiesAnimateurInt = 1;
        }
        else {
            inclurePartiesAnimateurInt = 0;
        }

        if(inclurePartiesAnimeesDansPasse) {
            inclurePartiesAnimeesDansPasseInt = 1;
        }
        else {
            inclurePartiesAnimeesDansPasseInt = 0;
        }

        // Si l'adresse du serveur n'est pas renseignée
        if (adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        } else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            // Si l'e-mail ou le mot de passe n'est pas renseigné, on affiche une erreur.
            if((email.trim().length() == 0) || mdp.trim().length() == 0) {
                AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", true, getSupportFragmentManager());
            }
            else {
                // On envoie une requête permettant d'obtenir la liste des inscriptions de l'utilisateur.
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                        "/participant/obtenir_liste_inscriptions?email=" + AccueilActivity.encoderECommercial(email) +
                        "&mdp=" + AccueilActivity.encoderECommercial(mdp) +
                        "&inclurePartiesPassees=" + inclurePartiesPasseesInt +
                        "&inclurePartiesAnimateur=" + inclurePartiesAnimateurInt +
                        "&inclurePartiesAnimeesDansPasse=" + inclurePartiesAnimeesDansPasseInt;
                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                        adresse, MesInscriptionsActivity.this);
                requeteHTTP.traiterRequeteHTTPJSONArray(MesInscriptionsActivity.this, "RechercheListeInscriptions", "GET", getSupportFragmentManager());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pour que l'écran reste en mode portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_mes_inscriptions);

        // On vide la liste des inscriptions de l'utilisateur.
        inscriptions.clear();

        // Récupération des vues.
        Button boutonRetour = findViewById(R.id.mes_inscriptions_btn_retour);
        layoutAttente = findViewById(R.id.mes_inscriptions_layout_attente);
        RadioButton rbPartiesAVenir = (RadioButton) findViewById(R.id.mes_inscriptions_rb_parties_a_venir);
        RadioButton rbPartiesAVenirOuAnimees = (RadioButton) findViewById(R.id.mes_inscriptions_rb_parties_a_venir_ou_animees);
        RadioButton rbToutesLesParties = (RadioButton) findViewById(R.id.mes_inscriptions_rb_toutes_les_parties);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        boutonRetour.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonRetour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        adresseServeur = sharedPref.getString("AdresseServeur", "");

        // On recherche les parties à venir.
        rechercherParties(false, true, false);

        // Clic sur le bouton Retour
        boutonRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le radio bouton parties à venir
        rbPartiesAVenir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutAttente.setVisibility(View.VISIBLE);
                listParties.setVisibility(View.GONE);
                rechercherParties(false, true, false);
            }
        });

        // Clic sur le bouton parties à venir ou animées
        rbPartiesAVenirOuAnimees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutAttente.setVisibility(View.VISIBLE);
                listParties.setVisibility(View.GONE);
                rechercherParties(false, true, true);
            }
        });

        // Clic sur le bouton toutes les parties
        rbToutesLesParties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutAttente.setVisibility(View.VISIBLE);
                listParties.setVisibility(View.GONE);
                rechercherParties(true, true, true);
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