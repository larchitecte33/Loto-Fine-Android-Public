package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class RecherchePartieActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private static int RECHERCHE_PAR_VILLE = 1;
    private static int RECHERCHE_PAR_DEPARTEMENT = 2;
    private static int RECHERCHE_PAR_REGION = 3;
    private int typeRecherche = RECHERCHE_PAR_VILLE;
    private RelativeLayout layoutListeParties;
    private ListView listParties;
    private ArrayList<Integer> idInscriptions = new ArrayList<>();
    private LinkedList<Partie> parties = new LinkedList<Partie>();
    private LinearLayout layoutAttente;
    private TextView tvAttente;

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        //ValidationDialogFragment vdf = null;
        JSONObject jo = null;
        JSONArray ja = null;

        if (source == "recherchePartie") {
            parties.clear();

            layoutAttente.setVisibility(View.GONE);
            listParties.setVisibility(View.VISIBLE);

            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    int idPartie;
                    Date dateHeurePartie = null;
                    String cp, ville, adresse, dateHeurePartieStr, patternDate = "dd/MM/yyyy", patternHeure = "HH", patternMinutes = "mm";
                    double prixCarton = 0.0;
                    boolean isExceptionDateHeurePartie = false;
                    DateFormat df = new SimpleDateFormat(patternDate), df2 = new SimpleDateFormat(patternHeure), df3 = new SimpleDateFormat(patternMinutes);
                    ArrayList<Lot> lots = new ArrayList<>();
                    Personne animateur;

                    TextView tvDateHeure, tvLieuDeRetrait;
                    LinearLayout layoutPartie;

                    ja = new JSONArray(reponse);
                    JSONArray jaLots;
                    JSONObject joLots;
                    JSONObject joAnimateur;

                    LinearLayout[] parent = new LinearLayout[5];
                    TextView[] tv = new TextView[5];
                    TextView[] tv2 = new TextView[5];

                    System.out.println("ja.toString() = " + ja.toString());

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
                            
                            parties.add(new Partie(idPartie, dateHeurePartie, // Instant.ofEpochMilli(dateHeurePartie.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                                    adresse, ville, cp, lots, prixCarton, animateur));
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
        else if(source == "inscriptionPartie") {
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    jo = new JSONObject(reponse);

                    Object objErreur = jo.opt("erreur");

                    // Si on a récupéré une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        message = "Votre inscription a été prise en compte.";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());

                        idInscriptions.add(jo.getInt("idPartie"));

                        ((PartieAdapter)listParties.getAdapter()).notifyDataSetChanged();
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if (source == "rechercheListeInscriptions") {
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    ja = new JSONArray(reponse);

                    for(int i = 0 ; i < ja.length() ; i++) {
                        jo = ja.getJSONObject(i);

                        idInscriptions.add(jo.getInt("id"));
                    }
                }
                catch(JSONException e) {
                    try {
                        jo = new JSONObject(reponse);

                        Object objErreur = jo.opt("erreur");

                        // Si on a récupéré une erreur, on l'affiche.
                        if (objErreur != null) {
                            message = (String) objErreur;
                            AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        }
                    }
                    catch(JSONException e2) {
                        AccueilActivity.afficherMessage(e2.getMessage(), false, getSupportFragmentManager());
                    }
                }
            }
        }
        else if (source == "desinscriptionPartie") {
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si on a récupéré une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        message = "La désinscription est réussie.";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());

                        if(idInscriptions.indexOf(jo.getInt("idPartie")) != -1) {
                            idInscriptions.remove(idInscriptions.indexOf(jo.getInt("idPartie")));
                            ((PartieAdapter)listParties.getAdapter()).notifyDataSetChanged();
                        }
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "suppressionPartie") {
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si on a récupéré une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        AccueilActivity.afficherMessage("La partie a été supprimée.", false, getSupportFragmentManager());

                        for(int i = 0 ; i < parties.size() ; i++) {
                            if(parties.get(i).getId() == jo.getInt("idPartie")) {
                                parties.remove(i);
                            }
                        }

                        if(idInscriptions.indexOf(jo.getInt("idPartie")) != -1) {
                            idInscriptions.remove(idInscriptions.indexOf(jo.getInt("idPartie")));
                        }

                        ((PartieAdapter)listParties.getAdapter()).notifyDataSetChanged();
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

        setContentView(R.layout.activity_recherche_partie);

        // On vide la liste des identifiants des inscriptions de l'utilisateur.
        idInscriptions.clear();

        Button btnRechercherParVille = findViewById(R.id.btn_rechercher_par_ville);
        Button btnRechercherParDepartement = findViewById(R.id.btn_rechercher_par_departement);
        Button btnRechercherParRegion = findViewById(R.id.btn_rechercher_par_region);
        Button btnRechercherPartie = findViewById(R.id.btn_rechercher_partie);
        Button boutonRetour = findViewById(R.id.rech_part_btn_retour);
        EditText editSaisirRecherche = findViewById(R.id.edit_saisir_recherche);
        layoutAttente = findViewById(R.id.rech_part_layout_attente);
        tvAttente = findViewById(R.id.rech_part_tv_attente);
        listParties = (ListView)findViewById(R.id.scroll_parties);

        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);


        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnRechercherPartie.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnRechercherPartie.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        boutonRetour.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonRetour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));


        // Par défaut, c'est la recherche par ville qui est active
        typeRecherche = RECHERCHE_PAR_VILLE;
        btnRechercherParVille.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnRechercherParVille.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnRechercherParDepartement.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnRechercherParDepartement.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnRechercherParRegion.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnRechercherParRegion.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        tvAttente.setText("");

        String adresseServeur = sharedPref.getString("AdresseServeur", "");

        // Si l'adresse du serveur n'est pas renseignée
        if(adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        }
        else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");

            if((email.trim().length() == 0) || mdp.trim().length() == 0) {
                AccueilActivity.afficherMessage("Veuillez vous connecter pour effectuer cette action.", true, getSupportFragmentManager());
            }
            else {
                // On va chercher la liste des inscriptions de la personne.
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                        "/participant/obtenir_liste_inscriptions?email=" + email + "&mdp=" + mdp +
                        "&inclurePartiesPassees=0&inclurePartiesAnimateur=1";
                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                        adresse, RecherchePartieActivity.this);
                requeteHTTP.traiterRequeteHTTPJSONArray(RecherchePartieActivity.this, "RechercheListeInscriptions", "GET", getSupportFragmentManager());
            }
        }


        boutonRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        btnRechercherParVille.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeRecherche = RECHERCHE_PAR_VILLE;
                btnRechercherParVille.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
                btnRechercherParVille.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
                btnRechercherParDepartement.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
                btnRechercherParDepartement.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
                btnRechercherParRegion.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
                btnRechercherParRegion.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
            }
        });

        btnRechercherParDepartement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeRecherche = RECHERCHE_PAR_DEPARTEMENT;
                btnRechercherParVille.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet)) ;
                btnRechercherParVille.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
                btnRechercherParDepartement.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
                btnRechercherParDepartement.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
                btnRechercherParRegion.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
                btnRechercherParRegion.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
            }
        });

        btnRechercherParRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeRecherche = RECHERCHE_PAR_REGION;
                btnRechercherParVille.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
                btnRechercherParVille.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
                btnRechercherParDepartement.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
                btnRechercherParDepartement.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
                btnRechercherParRegion.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
                btnRechercherParRegion.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
            }
        });

        btnRechercherPartie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutAttente.setVisibility(View.VISIBLE);
                listParties.setVisibility(View.GONE);
                tvAttente.setText("Chargement des parties en cours...");

                String adresseServeur = sharedPref.getString("AdresseServeur", "");

                // Si l'adresse du serveur n'est pas renseignée
                if(adresseServeur.trim().equals("")) {
                    String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
                    AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                }
                else {
                    // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                    String email = sharedPref.getString("emailUtilisateur", "");
                    String mdp = sharedPref.getString("mdpUtilisateur", "");

                    String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                            "/participant/rechercher_partie?email=" + email + "&mdp=" +
                            mdp + "&typeRecherche=" + typeRecherche + "&recherche=" + editSaisirRecherche.getText().toString().trim() +
                            "&inclurePartiesPassees=0";
                    RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                            adresse, RecherchePartieActivity.this);
                    requeteHTTP.traiterRequeteHTTPJSONArray(RecherchePartieActivity.this, "RecherchePartie", "GET", getSupportFragmentManager());
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