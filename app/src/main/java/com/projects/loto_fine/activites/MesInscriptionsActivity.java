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

import com.projects.loto_fine.Constants;
import com.projects.loto_fine.PartieAdapter;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Lot;
import com.projects.loto_fine.classes_metier.Partie;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_metier.RequeteHTTP;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class MesInscriptionsActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private ListView listParties;
    private ArrayList<Integer> idInscriptions = new ArrayList<>();
    private LinearLayout layoutAttente;
    private LinkedList<Partie> parties = new LinkedList<Partie>();

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;
        JSONObject jo = null;
        JSONArray ja = null;

        if (source == "rechercheListeInscriptions") {
            layoutAttente.setVisibility(View.GONE);
            listParties = (ListView)findViewById(R.id.mes_inscriptions_scroll_parties);
            listParties.setVisibility(View.VISIBLE);

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

                    ja = new JSONArray(reponse);
                    JSONArray jaLots;
                    JSONObject joLots;
                    JSONObject joAnimateur;
                    Personne animateur;

                    // On parcourt chaque partie trouvée dans le JSON.
                    for (int i = 0; i < ja.length(); i++) {
                        jo = ja.getJSONObject(i);

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

                            parties.add(new Partie(idPartie, dateHeurePartie, // .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                                    adresse, ville, cp, lots, prixCarton, animateur));
                            idInscriptions.add(idPartie);
                        }
                    }

                    PartieAdapter adapter = new PartieAdapter(this, this, R.layout.activity_recherche_partie_adapter,
                            parties, idInscriptions, getSupportFragmentManager());
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
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(reponse);

                    if (jsonObject.opt("erreur") != null) {
                        message = "Erreur : " + jsonObject.getString("erreur");
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
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
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(reponse);

                    if (jsonObject.opt("erreur") != null) {
                        message = "Erreur : " + jsonObject.getString("erreur");
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        int idPartie = jsonObject.getInt("idPartie");
                        message = "Vous avez été désinscrit de la partie";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());

                        for(int i = 0 ; i < parties.size() ; i++) {
                            if(parties.get(i).getId() == idPartie)
                                parties.remove(i);
                        }
                        idInscriptions.remove(idInscriptions.indexOf(idPartie));

                        PartieAdapter adapter = new PartieAdapter(this, this, R.layout.activity_recherche_partie_adapter,
                                parties, idInscriptions, getSupportFragmentManager());
                        listParties.setAdapter(adapter);
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage("Une erreur est survenue : " + e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pour que l'écran reste en mode portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_mes_inscriptions);

        idInscriptions.clear();

        Button boutonRetour = findViewById(R.id.mes_inscriptions_btn_retour);
        layoutAttente = findViewById(R.id.mes_inscriptions_layout_attente);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        boutonRetour.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonRetour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String adresseServeur = sharedPref.getString("AdresseServeur", "");

        // Si l'adresse du serveur n'est pas renseignée
        if (adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        } else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            if((email.trim().length() == 0) || mdp.trim().length() == 0) {
                AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", true, getSupportFragmentManager());
            }
            else {
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                        "/participant/obtenir_liste_inscriptions?email=" + email + "&mdp=" +
                        mdp + "&inclurePartiesPassees=0&inclurePartiesAnimateur=1";
                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                        adresse, MesInscriptionsActivity.this);
                requeteHTTP.traiterRequeteHTTPJSONArray(MesInscriptionsActivity.this, "RechercheListeInscriptions", "GET", getSupportFragmentManager());
            }
        }

        boutonRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
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