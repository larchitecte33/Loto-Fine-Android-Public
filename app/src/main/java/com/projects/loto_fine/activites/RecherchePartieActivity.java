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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * Cette activité est affichée lors du clic sur le bouton RECHERCHER UNE PARTIE A VENIR de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de rechercher une partie à venir.
 */
public class RecherchePartieActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {
    // Constantes
    private static final int RECHERCHE_PAR_VILLE = 1;
    private static final int RECHERCHE_PAR_DEPARTEMENT = 2;
    private static final int RECHERCHE_PAR_REGION = 3;

    // Type de recherche. Par défaut, c'est la recherche par ville.
    private int typeRecherche = RECHERCHE_PAR_VILLE;
    private ListView listParties; // ListView affichant la liste des parties.
    private LinkedList<Partie> parties = new LinkedList<Partie>(); // Liste des parties.
    private ArrayList<Inscription> inscriptions = new ArrayList<>(); // Liste des inscriptions.
    private LinearLayout layoutAttente; // Layout contenant un message d'attente.
    private TextView tvAttente; // TextView affichant un message d'attente.
    private String recherche = ""; // Chaine de caractères contenant la recherche saisie par l'utilisateur.

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : recherchePartie, inscriptionPartie, rechercheListeInscriptions, desinscriptionPartie et suppressionPartie.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        JSONObject jo = null, joPartie;
        JSONArray ja = null;

        if (source == "recherchePartie") {
            parties.clear();

            // On masque le layout d'attente et on affiche la liste des parties.
            layoutAttente.setVisibility(View.GONE);
            listParties.setVisibility(View.VISIBLE);

            // S'il y a eu une erreur lors de la recherche de la partie, on l'affiche.
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

                    System.out.println("ja.toString() = " + ja.toString());

                    // On parcourt chaque partie trouvée dans le JSON.
                    for (int i = 0; i < ja.length(); i++) {
                        jo = ja.getJSONObject(i);
                        joPartie = jo.getJSONObject("partie");

                        isExceptionDateHeurePartie = false;

                        dateHeurePartieStr = joPartie.getString("date");

                        try {
                            dateHeurePartie = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(dateHeurePartieStr);
                        } catch (ParseException e) {
                            isExceptionDateHeurePartie = true;
                        }

                        // Si la date/heure de début de la partie est correcte.
                        if (!isExceptionDateHeurePartie) {
                            // On extrait tous les éléments dont on a besoin.
                            idPartie = joPartie.getInt("id");
                            cp = joPartie.getString("cp");
                            ville = joPartie.getString("ville");
                            adresse = joPartie.getString("adresse");
                            prixCarton = joPartie.getDouble("prixCarton");

                            joAnimateur = joPartie.getJSONObject("animateur");
                            animateur = new Personne();
                            // On remplit les attributs de l'animateur.
                            animateur.setId(joAnimateur.getInt("id"));
                            animateur.setNom(joAnimateur.getString("nom"));
                            animateur.setPrenom(joAnimateur.getString("prenom"));
                            animateur.setAdresse(joAnimateur.getString("adresse"));
                            animateur.setCp(joAnimateur.getString("cp"));
                            animateur.setVille(joAnimateur.getString("ville"));
                            animateur.setEmail(joAnimateur.getString("email"));
                            animateur.setNumtel(joAnimateur.getString("numTel"));
                            animateur.setMdp(joAnimateur.getString("mdp"));

                            jaLots = joPartie.getJSONArray("lots");
                            lots = new ArrayList<>();

                            // On parcourt tous les lots.
                            for (int j = 0; j < jaLots.length(); j++) {
                                joLots = jaLots.getJSONObject(j);

                                // On construit un lot et on lui affecte les valeurs du JSONObject.
                                Lot lot = new Lot();
                                lot.setId(joLots.getInt("id"));
                                lot.setNom(joLots.getString("nom"));
                                lot.setValeur((float)joLots.getDouble("valeur"));

                                // On ajoute le lot à la liste des lots.
                                lots.add(lot);
                            }

                            // On crée un objet Partie et on l'ajoute à la liste des parties.
                            parties.add(new Partie(idPartie, dateHeurePartie, adresse, ville, cp, lots, prixCarton, animateur));
                        }
                    }

                    PartieAdapter adapter = new PartieAdapter(this, this, R.layout.activity_recherche_partie_adapter,
                            parties, inscriptions, typeRecherche, recherche, getSupportFragmentManager());
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
            // S'il y a eu une erreur lors de l'inscription à la partie, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    jo = new JSONObject(reponse);

                    Object objErreur = jo.opt("erreur");

                    // Si on a récupéré une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        message = "Votre inscription a été prise en compte.";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());

                        // On crée un objet Inscription et on lui affecte les valeurs retournées par le serveur.
                        Inscription inscription = new Inscription();
                        inscription.setValidee(false);
                        inscription.setIdPartie(jo.getInt("idPartie"));
                        inscription.setMontant(jo.getDouble("montantInscription"));
                        // On ajoute l'inscription à la liste des inscriptions.
                        inscriptions.add(inscription);

                        // On mes à jour la liste des parties.
                        ((PartieAdapter)listParties.getAdapter()).notifyDataSetChanged();
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if (source == "rechercheListeInscriptions") {
            // S'il y a eu une erreur lors de la recherche de la liste des inscriptions, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On tente de caster la réponse en JSONArray.
                    ja = new JSONArray(reponse);

                    // On parcourt chaque inscription contenue dans le JSONArray.
                    for(int i = 0 ; i < ja.length() ; i++) {
                        jo = ja.getJSONObject(i);
                        joPartie = jo.getJSONObject("partie");

                        // On crée un objet Inscription et on lui affecte les valeurs contenues dans le JSON.
                        Inscription inscription = new Inscription();
                        inscription.setMontant(jo.getDouble("montantARegler"));
                        inscription.setIdPartie(joPartie.getInt("id"));
                        inscription.setValidee(jo.getBoolean("isReglee"));
                        // On ajoute l'inscription à la liste des inscriptions.
                        inscriptions.add(inscription);
                    }
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
                    catch(JSONException e2) {
                        AccueilActivity.afficherMessage(e2.getMessage(), false, getSupportFragmentManager());
                    }
                }
            }
        }
        else if (source == "desinscriptionPartie") {
            // S'il y a eu une erreur lors de la désinscription, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si on a récupéré une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        message = "La désinscription est réussie.";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());

                        // On recherche la partie dans la liste des inscriptions
                        for(int i = 0 ; i < inscriptions.size() ; i++) {
                            // Si la partie est trouvée, on la supprime de la liste des inscriptions.
                            if(inscriptions.get(i).getIdPartie() == jo.getInt("idPartie")) {
                                inscriptions.remove(i);
                            }
                        }

                        // On notifie la liste des parties pour qu'elle se mette à jour.
                        ((PartieAdapter)listParties.getAdapter()).notifyDataSetChanged();
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        // Suppression d'une partie
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

                        // On recherche la partie dans la liste des parties
                        for(int i = 0 ; i < parties.size() ; i++) {
                            // Si la partie est trouvée, on la supprime de la liste des parties
                            if(parties.get(i).getId() == jo.getInt("idPartie")) {
                                parties.remove(i);
                            }
                        }

                        // On recherche la partie dans la liste des inscriptions
                        for(int i = 0 ; i < inscriptions.size() ; i++) {
                            // Si la partie est trouvée, on la supprime de la liste des inscriptions.
                            if(inscriptions.get(i).getIdPartie() == jo.getInt("idPartie")) {
                                inscriptions.remove(i);
                            }
                        }

                        // On notifie la liste des parties pour qu'elle se mette à jour.
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
        inscriptions.clear();

        // Récupération des composants.
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
                        "/participant/obtenir_liste_inscriptions?email=" + AccueilActivity.encoderECommercial(email) +
                        "&mdp=" + AccueilActivity.encoderECommercial(mdp) +
                        "&inclurePartiesPassees=0&inclurePartiesAnimateur=1&inclurePartiesAnimeesDansPasse=0";
                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                        adresse, RecherchePartieActivity.this);
                requeteHTTP.traiterRequeteHTTPJSONArray(RecherchePartieActivity.this, "RechercheListeInscriptions", "GET", getSupportFragmentManager());
            }
        }


        // Clic sur le bouton RETOUR.
        boutonRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le bouton RECHERCHER PAR VILLE
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

        // Clic sur le bouton RECHERCHER PAR DEPARTEMENT
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

        // Clic sur le bouton RECHERCHER PAR REGION
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

        // Clic sur le bouton RECHERCHER
        btnRechercherPartie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On affiche le layout d'attente et on cache la liste des parties.
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

                    recherche = AccueilActivity.encoderECommercial(editSaisirRecherche.getText().toString().trim());

                    // Envoi d'une requête permettant de rechercher des parties.
                    String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                            "/participant/rechercher_partie?email=" + AccueilActivity.encoderECommercial(email) + "&mdp=" +
                            AccueilActivity.encoderECommercial(mdp) + "&typeRecherche=" + typeRecherche +
                            "&recherche=" + recherche +
                            "&inclurePartiesPassees=0";
                    RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                            adresse, RecherchePartieActivity.this);
                    requeteHTTP.traiterRequeteHTTPJSONArray(RecherchePartieActivity.this, "RecherchePartie", "GET", getSupportFragmentManager());
                }
            }
        });

        Intent intent = getIntent();
        // On récupère le type de recherche et la recherche contenus dans l'intent (quand on vient de VISUALISER LISTE LOTS ou COMMENT REGLER).
        int typeRecherche = intent.getIntExtra("typeRecherche", RECHERCHE_PAR_VILLE);
        String laRecherche = intent.getStringExtra("recherche");

        // On affecte à editSaisirRecherche la recherche de l'intent.
        editSaisirRecherche.setText(laRecherche);

        if((laRecherche != null) && (!laRecherche.trim().equals(""))) {
            switch(typeRecherche) {
                case RECHERCHE_PAR_VILLE :
                    // On clique sur les boutons VILLE puis RECHERCHER par programmation.
                    btnRechercherParVille.callOnClick();
                    btnRechercherPartie.callOnClick();
                    break;
                case RECHERCHE_PAR_DEPARTEMENT :
                    // On clique sur les boutons DEPART. puis RECHERCHER par programmation.
                    btnRechercherParDepartement.callOnClick();
                    btnRechercherPartie.callOnClick();
                    break;
                case RECHERCHE_PAR_REGION :
                    // On clique sur les boutons REGION puis RECHERCHER par programmation.
                    btnRechercherParRegion.callOnClick();
                    btnRechercherPartie.callOnClick();
                    break;
                default :
                    break;
            }
        }
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