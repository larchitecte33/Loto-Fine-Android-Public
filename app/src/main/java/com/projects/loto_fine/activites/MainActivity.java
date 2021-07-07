package com.projects.loto_fine.activites;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.nfc.FormatException;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.projects.loto_fine.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Carton;
import com.projects.loto_fine.classes_metier.CaseCarton;
import com.projects.loto_fine.classes_metier.ElementCliquable;
import com.projects.loto_fine.classes_metier.OuiNonDialogFragment;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_metier.RequeteHTTP;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;
import com.projects.loto_fine.stomp_client.ClientStomp;
import com.projects.loto_fine.vues.Plateau;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kaazing.net.sse.SseEventReader;
import org.kaazing.net.sse.SseEventSource;
import org.kaazing.net.sse.SseEventSourceFactory;
import org.kaazing.net.sse.SseEventType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener,
        OuiNonDialogFragment.OuiNonDialogListener {

    private Carton monPremierCarton, monDeuxiemeCarton, carton;
    private boolean isCartonsArrives = false;
    private ArrayList<Carton> cartons;
    private Plateau plateau;
    private int idDernierCartonUtilise = -1;
    private String cleRecuperationQuine = "";
    private boolean isQuineRecuperee = true;
    private String adresseServeur = "";
    private SharedPreferences sharedPref;
    private Future longRunningTaskFuture, taskMAJInfosTablette;
    private Personne joueur = null;
    private InputStream input;
    private BufferedReader reader;
    private Socket socket;
    private boolean isMAJInfosTabletteEnCours = false;
    private Date dateDerniereMAJInfos;
    private int idPartie = -1;
    private boolean isLotPrecedentCartonPlein = false;
    private String lotEnCoursPrecedent = "";

    private int numeroTire = 0;
    private String lotEnCours = "";
    private boolean isLotEnCoursCartonPlein = false;
    private ClientStomp clientStomp;

    // Setters
    public void setNumeroTire(int numeroTire) {
        this.numeroTire = numeroTire;
    }

    public void setLotEnCours(String lotEnCours) {
        this.lotEnCours = lotEnCours;
    }

    public void setIsLotCartonPlein(boolean isLotEnCoursCartonPlein) {
        this.isLotEnCoursCartonPlein = isLotEnCoursCartonPlein;
    }

    public void rafraichirInfosNumeroLot() {
        if(numeroTire == -1) {
            AccueilActivity.afficherMessage("La partie est terminée", true, getSupportFragmentManager());
        }

        plateau.setNumeroEnCours(numeroTire);
        plateau.setLotEnCours(lotEnCours);
        plateau.setLotCartonPlein(isLotEnCoursCartonPlein);

        // Si le lot en-cous a changé et si le lot précédent était au carton plein, on enlève tous les pions
        // de tous les cartons
        if((!lotEnCoursPrecedent.equals(lotEnCours)) && isLotPrecedentCartonPlein) {
            for(int i = 0 ; i < plateau.getCartons().size() ; i++) {
                plateau.getCartons().get(i).enleverTousLesPions();
            }
        }

        lotEnCoursPrecedent = lotEnCours;
        isLotPrecedentCartonPlein = isLotEnCoursCartonPlein;

        plateau.invalidate();
    }

    Thread threadVerificationQuine = new Thread() {
        @Override
        public void run() {
            try {
                // Si l'adresse du serveur n'est pas renseignée, on affiche un message.
                if(adresseServeur.trim().equals("")) {
                    AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", false, getSupportFragmentManager());
                }
                else {
                    String email = sharedPref.getString("emailUtilisateur", "");
                    String mdp = sharedPref.getString("mdpUtilisateur", "");
                    isQuineRecuperee = false;

                    while (!isQuineRecuperee) {
                        sleep(2000);
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-quine?email=" + email +
                                "&mdp=" + mdp + "&idPartie=1&cle=" + cleRecuperationQuine;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "RecuperationQuine", "GET", "", getSupportFragmentManager());
                    }
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Si l'adresse du serveur n'est pas renseignée, on affiche un message.
            if(adresseServeur.trim().equals("")) {
                AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", false, getSupportFragmentManager());
            }
            else {
                String email = sharedPref.getString("emailUtilisateur", "");
                String mdp = sharedPref.getString("mdpUtilisateur", "");
                isQuineRecuperee = false;

                while (!isQuineRecuperee) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(!isQuineRecuperee) {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-quine?email=" + email +
                                "&mdp=" + mdp + "&idPartie=" + idPartie + "&cle=" + cleRecuperationQuine;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "RecuperationQuine", "GET", "", getSupportFragmentManager());
                    }
                }
            }
        }
    };

    Runnable runnableMAJInfosTablette = new Runnable() {
        @Override
        public void run() {
            // Si l'adresse du serveur n'est pas renseignée, on affiche un message.
            if(adresseServeur.trim().equals("")) {
                AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", false, getSupportFragmentManager());
            }
            else {
                String email = sharedPref.getString("emailUtilisateur", "");
                String mdp = sharedPref.getString("mdpUtilisateur", "");
                int nbSecondesAvantMAJInfosTablette = sharedPref.getInt("NbSecondesAvantMAJInfosTablette", Constants.NB_SECONDES_AVANT_MAJ_INFOS_TABLETTE_DEFAUT);

                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" + email +
                        "&mdp=" + mdp + "&idPartie=" + idPartie;

                while(true) {
                    try {
                        Thread.sleep(nbSecondesAvantMAJInfosTablette * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
                    requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "MAJInfosTablette", "GET", "", getSupportFragmentManager());
                }
            }
        }
    };

    public void runThread() {
       longRunningTaskFuture = executorService.submit(runnable);
    }

    public void runThreadMAJInfosTablette() {
    }


    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;
        boolean lancerThreadVerificationReponseQuine = false;

        if(source == "recuperationCartons") {
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la récupération des cartons : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // Déclarations
                    // Les carton sont envoyé sous la forme d'un JSONArray.
                    JSONArray cartonsJSON = new JSONArray(reponse);
                    JSONArray ligne1, ligne2, ligne3;
                    JSONObject cartonJSON, casesJSON;
                    CaseCarton[][] casesCarton;
                    int valeurCase = -1, ligne, colonne, idInt;
                    String id;

                    cartons = new ArrayList<>();

                    // Pour chaque carton contenu dans le JSON
                    for (int i = 0; i < cartonsJSON.length(); i++) {
                        cartonJSON = (JSONObject) cartonsJSON.get(i);
                        casesJSON = cartonJSON.getJSONObject("cases");

                        ligne1 = casesJSON.getJSONArray("ligne1");
                        ligne2 = casesJSON.getJSONArray("ligne2");
                        ligne3 = casesJSON.getJSONArray("ligne3");

                        casesCarton = new CaseCarton[Constants.NB_LIGNES_CARTON][Constants.NB_COLONNES_CARTON];

                        ligne = 0;
                        colonne = 0;

                        // Pour chaque valeurs de case du carton contenues dans le JSON
                        for (int j = 0; j < ligne1.length(); j++) {
                            valeurCase = (int) ligne1.get(j);

                            casesCarton[ligne][colonne] = new CaseCarton();
                            casesCarton[ligne][colonne].setValeur(valeurCase);

                            if (colonne < Constants.NB_COLONNES_CARTON - 1) {
                                colonne++;
                            }
                        }

                        ligne++;
                        colonne = 0;

                        for (int j = 0; j < ligne2.length(); j++) {
                            valeurCase = (int) ligne2.get(j);

                            casesCarton[ligne][colonne] = new CaseCarton();
                            casesCarton[ligne][colonne].setValeur(valeurCase);

                            if (colonne < Constants.NB_COLONNES_CARTON - 1) {
                                colonne++;
                            }
                        }

                        ligne++;
                        colonne = 0;

                        for (int j = 0; j < ligne1.length(); j++) {
                            valeurCase = (int) ligne3.get(j);

                            casesCarton[ligne][colonne] = new CaseCarton();
                            casesCarton[ligne][colonne].setValeur(valeurCase);

                            if (colonne < Constants.NB_COLONNES_CARTON - 1) {
                                colonne++;
                            }
                        }

                        carton = new Carton(casesCarton);

                        id = cartonJSON.get("id").toString();
                        idInt = Integer.valueOf(id);

                        carton.setId(idInt);
                        carton.setPersonne(joueur);
                        cartons.add(carton);
                    }

                    isCartonsArrives = true;
                    plateau.setCartons(cartons);
                    plateau.setJoueur(joueur);
                    plateau.invalidate();
                } catch (JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "envoiQuine") {
            // Si le serveur a renvoyé un erreur lors de l'envoi de la quine, alors on l'affiche.
            if(isErreur) {
                message = "Erreur lors de l'envoi de la quine : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String)objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        cleRecuperationQuine = jo.getString("cle");
                        message = getResources().getString(R.string.texte_demande_quine_envoyee);
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }

                    runThread();
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "recuperationQuine") {
            if(isErreur) {
                isQuineRecuperee = true;
                System.out.println("isQuineRecuperee(1)");
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        longRunningTaskFuture.cancel(true);
                       // runnableMAJInfosTablette.run();
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        isQuineRecuperee = true;
                        System.out.println("isQuineRecuperee(2)");
                    } else {
                        String etat = jo.getString("etat");

                        if(etat.equals("valide")) {
                            longRunningTaskFuture.cancel(true);
                            isQuineRecuperee = true;
                            System.out.println("isQuineRecuperee(3)");
                            message = getResources().getString(R.string.texte_demande_quine_validee);
                            AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                            //runThreadMAJInfosTablette();
                        }
                        else if(etat.equals("invalide")) {
                            longRunningTaskFuture.cancel(true);
                          //  runnableMAJInfosTablette.run();
                            isQuineRecuperee = true;
                            System.out.println("isQuineRecuperee(4)");
                            message = getResources().getString(R.string.texte_demande_quine_invalidee);
                            AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        }
                        else if(!(etat.equals("attente"))) {
                            longRunningTaskFuture.cancel(true);
                           // runnableMAJInfosTablette.run();
                            isQuineRecuperee = true;
                            System.out.println("isQuineRecuperee(5)");
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(isQuineRecuperee) {
                plateau.setBoutonQuineInactif();
                plateau.invalidate();
                longRunningTaskFuture.cancel(true);
            }

            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");
            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" + email +
                    "&mdp=" + mdp + "&idPartie=" + idPartie;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
            requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "MAJInfosTablette", "GET", "", getSupportFragmentManager());

        }
        else if(source == "obtentionInfosPersonne") {
            if(isErreur) {
                message = "Erreur lors de l'obtention des informations de la personne : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        joueur = new Personne();
                        joueur.setId(Integer.valueOf(jo.get("id").toString()));
                        joueur.setNom(jo.get("nom").toString());
                        joueur.setPrenom(jo.get("prenom").toString());
                        joueur.setAdresse(jo.get("adresse").toString());
                        joueur.setCp(jo.get("cp").toString());
                        joueur.setVille(jo.get("ville").toString());
                        joueur.setEmail(jo.get("email").toString());
                        joueur.setNumtel(jo.get("numTel").toString());
                        joueur.setMdp(jo.get("mdp").toString());
                    }
                }
                catch (JSONException e) {
                    message = e.getMessage();
                    AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "majInfosTablette") {
            if(isErreur) {
                message = "Erreur lors de la mise à jour des informations de la tablette : " + reponse;

                try {
                    AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                }
                catch(IllegalStateException e) {
                    //
                }
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }
                    else {
                        int numeroEnCours = 0;
                        String lotEnCours = "";
                        boolean isLotCartonPlein, isPartieTerminee = false;

                        try {
                            numeroEnCours = jo.getInt("numeroEnCours");

                            // Si le numéro en-cours est égal à -1, la partie est terminée
                            if(numeroEnCours == -1) {
                                AccueilActivity.afficherMessage("La partie est terminée", true, getSupportFragmentManager());
                            }
                        }
                        catch(NumberFormatException ex) {
                            numeroEnCours = -1;
                        }

                        lotEnCours = jo.getString("lotEnCours");
                        isLotCartonPlein = jo.getBoolean("lotCartonPlein");

                        // Si le lot en-cous a changé et si le lot précédent était au carton plein, on enlève tous les pions
                        // de tous les cartons
                        if((!lotEnCoursPrecedent.equals(lotEnCours)) && isLotPrecedentCartonPlein) {
                            for(int i = 0 ; i < plateau.getCartons().size() ; i++) {
                                plateau.getCartons().get(i).enleverTousLesPions();
                            }
                        }

                        lotEnCoursPrecedent = lotEnCours;
                        isLotPrecedentCartonPlein = isLotCartonPlein;

                        plateau.setNumeroEnCours(numeroEnCours);
                        plateau.setLotEnCours(lotEnCours);
                        plateau.setLotCartonPlein(isLotCartonPlein);
                        plateau.invalidate();
                    }
                }
                catch (JSONException e) {
                    message = e.getMessage();
                    AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                }
            }

            isMAJInfosTabletteEnCours = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On construit le plateau de jeu.
        plateau = new Plateau(this);
        dateDerniereMAJInfos = new Date();

        Intent intent = getIntent();
        idPartie = intent.getIntExtra("idPartie", -1);

        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);

        // On va chercher l'adresse du serveur dans les SharedPreferences.
        adresseServeur = sharedPref.getString("AdresseServeur", "");

        // On initialise le client STOMP.
        clientStomp = new ClientStomp(getApplicationContext(), idPartie, this);


        // Si l'adresse du serveur n'est pas renseignée, on affiche un message.
        if(adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        }
        else {
            String email = sharedPref.getString("emailUtilisateur", "");

            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/get_infos_personne?email=" + email;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                    adresse, MainActivity.this);
            requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "ObtentionInfosPersonne", "GET", "", getSupportFragmentManager());
        }

        // On lance le thread de mise à jour des informations de la tablette/téléphone.
        runThreadMAJInfosTablette();

        // S'il y a un savedInstanceState déjà enregistré (rotation d'écran)
        if(savedInstanceState != null) {
            // On va chercher les cartons enregistrés dans le savedInstanceState
            cartons = savedInstanceState.getParcelableArrayList("cartons");
            // On assigne au plateau les cartons enregistrés.
            plateau.setCartons(cartons);

            if(joueur != null) {
                plateau.setJoueur(joueur);
            }

            numeroTire = savedInstanceState.getInt("numeroTire", 0);
            lotEnCours = savedInstanceState.getString("lotEnCours", "");
            isLotEnCoursCartonPlein = savedInstanceState.getBoolean("isLotEnCoursCartonPlein", false);

            if(numeroTire > 0) {
                plateau.setNumeroEnCours(numeroTire);
            }

            if(!lotEnCours.equals("")) {
                plateau.setLotEnCours(lotEnCours);
            }

            plateau.setLotCartonPlein(isLotEnCoursCartonPlein);

            // On demande à la vue de se redessiner.
            plateau.invalidate();
        }
        else {
            // On va chercher l'adresse du serveur dans les SharedPreferences.
            adresseServeur = sharedPref.getString("AdresseServeur", "");

            // Si l'adresse du serveur n'est pas renseignée, on affiche un message.
            if(adresseServeur.trim().equals("")) {
                String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
                AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
            }
            else {
                // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                String email = sharedPref.getString("emailUtilisateur", "");
                String mdp = sharedPref.getString("mdpUtilisateur", "");

                // Lors de la création de l'activité, on envoie une requête au serveur pour récupérer les cartons de l'utilisateur
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation_cartons?email=" + email +
                        "&mdp=" + mdp + "&idPartie=" + idPartie;
                RequeteHTTP requeteHTTP = new RequeteHTTP(this.getBaseContext(), adresse, MainActivity.this);
                // Ici, on envoie la requête et on passe à la suite. Lorsque la réponse arrivera, on affichera les cartons.
                requeteHTTP.traiterRequeteHTTPJSONArray(this, "Main", "GET", getSupportFragmentManager());
            }
        }

        setContentView(plateau);

        // Déclenché lorsqu'on touche l'écran du terminal.
        plateau.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Déclarations
                float x = event.getX();
                float y = event.getY();
                String id;
                ArrayList<ElementCliquable> listeElementsCliquables = plateau.getListeElementsCliquables();
                boolean elementTrouve = false;
                int i = 0;


                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        Log.d("x = ", String.valueOf(x));
                        Log.d("y = ", String.valueOf(y));

                        // Tant qu'on a pas déterminé l'élément sur lequel on a cliqué et tant qu'on a pas parcouru
                        // l'ensemble des élément cliquables
                        while((!elementTrouve) && (i < listeElementsCliquables.size())) {
                            // Si la position de l'élement cliquable correspond à la position du clic
                            if((listeElementsCliquables.get(i).getPosX() <= x) && (listeElementsCliquables.get(i).getPosX() + listeElementsCliquables.get(i).getWidth() > x)
                                    && (listeElementsCliquables.get(i).getPosY() <= y) && (listeElementsCliquables.get(i).getPosY() + listeElementsCliquables.get(i).getHeight() > y)) {
                                id = listeElementsCliquables.get(i).getId();

                                if(id == "boutonRalentirPartie") {

                                }
                                else if(id == "boutonAccelererPartie") {

                                }
                                else if(id == "boutonQuine") {
                                    // Si l'adresse du serveur n'est pas renseignée
                                    if(adresseServeur.trim().equals("")) {
                                        String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
                                        AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                                    }
                                    else {
                                        plateau.setBoutonQuineActif();

                                        // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                                        String email = sharedPref.getString("emailUtilisateur", "");
                                        String mdp = sharedPref.getString("mdpUtilisateur", "");

                                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant
                                                + "/participant/envoi-quine?email=" + email + "&mdp=" + mdp + "&idPartie=" + idPartie
                                                + "&idCarton=" + idDernierCartonUtilise;
                                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
                                        requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "EnvoiQuine", "POST", "", getSupportFragmentManager());
                                        plateau.invalidate();
                                    }
                                }
                                else if(id.startsWith("carton")) {
                                    final String idCarton = id.replaceFirst("carton", "");

                                    // On parcourt tous les cartons du plateau.
                                    for(int j = 0 ; j < plateau.getCartons().size() ; j++) {
                                        // Si l'id du carton correspond à l'id du carton sur lequel on à cliqué
                                        if(plateau.getCartons().get(j).getId() == Integer.valueOf(idCarton)) {
                                            // On effectue sur la case positionnée en (x, y).
                                            plateau.getCartons().get(j).clicCase((int)x, (int)y);
                                            elementTrouve = true;
                                            idDernierCartonUtilise = plateau.getCartons().get(j).getId();
                                            // On demande à la vue de se redessiner.
                                            plateau.invalidate();
                                            break;
                                        }
                                    }
                                }
                                else if(id == "boutonCartonsPrecedents") {
                                    // Si le terminal est en orientation portrait
                                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                        // Si le numéro de la page courante en orientation portrait est supérieur à 1
                                        if (plateau.getPageCourantePortrait() > 1) {
                                            // On décrémente le numéro de la page courante en orientation portrait.
                                            plateau.setPageCourantePortrait(plateau.getPageCourantePortrait() - 1);
                                            // On demande à la vue de se redessiner.
                                            plateau.invalidate();
                                        }
                                    }
                                    // Si le terminal est en position paysage
                                    else {
                                        // Si le numéro de la page courante en orientation paysage est supérieur à 1
                                        if (plateau.getPageCourantePaysage() > 1) {
                                            // On décrémente le numéro de la page courante en orientation paysage.
                                            plateau.setPageCourantePaysage(plateau.getPageCourantePaysage() - 1);
                                            // On demande à la vue de se redessiner.
                                            plateau.invalidate();
                                        }
                                    }
                                }
                                else if(id == "boutonCartonsSuivants") {
                                    // Si le terminal est en orientation portrait
                                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                        // Si le numéro de la page courante en orientation portrait est inférieur au
                                        // nombre de pages en orientation portrait
                                        if (plateau.getPageCourantePortrait() < plateau.getNbPagesPortrait()) {
                                            // On incrémente le numéro de la page courante en orientation portrait.
                                            plateau.setPageCourantePortrait(plateau.getPageCourantePortrait() + 1);
                                            // On demande à la vue de se redessiner.
                                            plateau.invalidate();
                                        }
                                    }
                                    // Si le terminale est en position paysage
                                    else {
                                        // Si le numéro de la page courante en orientation paysage est inférieur au
                                        // nombre de pages en orientation paysage
                                        if (plateau.getPageCourantePaysage() < plateau.getNbPagesPaysage()) {
                                            // On incrémente le numéro de la page courante en orientation paysage.
                                            plateau.setPageCourantePaysage(plateau.getPageCourantePaysage() + 1);
                                            // On demande à la vue de se redessiner.
                                            plateau.invalidate();
                                        }
                                    }
                                }
                                else if(id == "boutonQuitter") {
                                    HashMap<String, String> args = new HashMap<>();
                                    OuiNonDialogFragment ondf = new OuiNonDialogFragment("Etes-vous sûr de vouloir quitter ?", "quitter", "nePasQuitter", args);
                                    ondf.show(getSupportFragmentManager(), "");
                                }
                            }

                            i++;
                        }

                        if(clientStomp.equals(null) || (!clientStomp.isConnected())) {
                            clientStomp = new ClientStomp(getApplicationContext(), idPartie, MainActivity.this);
                        }

                        /*Calendar maintenant = Calendar.getInstance();
                        maintenant.add(Calendar.SECOND, -10);

                        if((!MainActivity.this.isMAJInfosTabletteEnCours) || (dateDerniereMAJInfos.before(maintenant.getTime()))) {
                            MainActivity.this.isMAJInfosTabletteEnCours = true;
                            String email = sharedPref.getString("emailUtilisateur", "");
                            String mdp = sharedPref.getString("mdpUtilisateur", "");
                            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" + email +
                                    "&mdp=" + mdp + "&idPartie=" + idPartie;
                            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
                            Log.d("MAJInfosTablette", "Mise à jour des informmations de la tablette.");
                            requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "MAJInfosTablette", "GET", "", getSupportFragmentManager());
                        }*/

                        break;
                }

                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(taskMAJInfosTablette != null)
            taskMAJInfosTablette.cancel(true);

        if(!clientStomp.equals(null))
            clientStomp.deconnecterClientStomp();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(cartons != null)
            outState.putParcelableArrayList("cartons", cartons);

        outState.putInt("numeroTire", numeroTire);
        outState.putString("lotEnCours", lotEnCours);
        outState.putBoolean("isLotEnCoursCartonPlein", isLotEnCoursCartonPlein);
    }

    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onFinishEditDialog(String nomActionOui, String nomActionNon, boolean isChoixOui, HashMap<String, String> args) {
        // Si choix = Oui
        if (isChoixOui) {
            finish();
        }
    }
}