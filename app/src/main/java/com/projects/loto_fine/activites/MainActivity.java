package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Carton;
import com.projects.loto_fine.classes_metier.CaseCarton;
import com.projects.loto_fine.classes_utilitaires.ElementCliquable;
import com.projects.loto_fine.classes_utilitaires.OuiNonDialogFragment;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;
import com.projects.loto_fine.stomp_client.ClientStomp;
import com.projects.loto_fine.vues.Plateau;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Cette activité est affichée lors du clic sur le bouton DEMARRER LA PARTIE de l'adapter PartieAdapter.
 * Elle permet à l'utilisateur de participer à une partie.
 */
public class MainActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener,
        OuiNonDialogFragment.OuiNonDialogListener {

    private Carton carton;
    private ArrayList<Carton> cartons; // Liste des cartons du participant.
    private Plateau plateau; // Plateau de jeu du participant.
    private int idDernierCartonUtilise = -1; // Identifiant du dernier carton sur lequel l'utilisateur a déposé ou retiré un pion.
    private String cleRecuperationQuine = ""; // Clé de récupération de la quine.
    private boolean isQuineRecuperee = true; // Sert à indiquer si le statut de la quine a été récupéré ou non.
    private String adresseServeur = ""; // Adresse du serveur
    private SharedPreferences sharedPref; // Shared Preferences servant à récupérer/mettre à jour des valeurs stockées sur le téléphone/la tablette.
    private Future longRunningTaskFuture; // Utilisé pour récupérer le statut de la quine.
    private Personne joueur = null; // Stocke les informations du participant.
    private int idPartie = -1; // Stocke l'identifiant de la partie.
    private boolean isLotPrecedentCartonPlein = false; // true si le lot précédent était au carton plein, false sinon.
    private String lotEnCoursPrecedent = ""; // Lot précédent

    private int numeroTire = 0; // Numéro tiré
    private String lotEnCours = ""; // Lot en cours
    private boolean isLotEnCoursCartonPlein = false; // true si le lot en cours est au carton plein, false sinon.
    // Client STOMP qui va permettre de récupérer les informations de la partie en-cours (numéro en-cours,
    // lot en-cours et lot à la ligne ou au carton plein).
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

    /**
     * Fonction permettant de rafraichir le numéro en cours ainsi que les informations du lot en cours (libellé et au carton plein ou à la ligne)
     */
    public void rafraichirInfosNumeroLot() {
        // Un numéro tiré à -1 correspond à une fin de partie.
        if(numeroTire == -1) {
            AccueilActivity.afficherMessage("La partie est terminée", true, getSupportFragmentManager());
        }

        // On met à jour les informations de la vue Plateau.
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

        // On met à jour la vue Plateau.
        plateau.invalidate();
    }

    // Plus utilisé
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

    // ExecutorService servant à lancer le Runnable servant quant à lui à récupérer le statut de la quine.
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Runnable servant à récupérer le statut de la quine.
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Si l'adresse du serveur n'est pas renseignée, on affiche un message.
            if(adresseServeur.trim().equals("")) {
                AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", false, getSupportFragmentManager());
            }
            else {
                // On récupère l'e-mail et le mot de passe de l'utilisateur dans les SharedPreferences.
                String email = sharedPref.getString("emailUtilisateur", "");
                String mdp = sharedPref.getString("mdpUtilisateur", "");
                isQuineRecuperee = false;

                // Tant que le statut de la quine n'est pas récupéré
                while (!isQuineRecuperee) {
                    // On attend 2 secondes.
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Si le statut de la quine n'a pas encore été récupéré
                    if(!isQuineRecuperee) {
                        // On envoie une requête permettant de vérifier le statut de la quine (attente, valide ou invalide).
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-quine?email=" + email +
                                "&mdp=" + mdp + "&idPartie=" + idPartie + "&cle=" + cleRecuperationQuine;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "RecuperationQuine", "GET", "", getSupportFragmentManager());
                    }
                }
            }
        }
    };

    // Plus utilisé
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

    // Fonction servant à lancer la récupération du statut de la quine.
    public void runThread() {
       longRunningTaskFuture = executorService.submit(runnable);
    }

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : recuperationCartons, envoiQuine, recuperationQuine, obtentionInfosPersonne, majInfosTablette, enregistrerConnexionAPartie et
     *                     supprimerEnregistrementConnexionAPartie.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
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
                    JSONArray ligne1, ligne2, ligne3; // JSONArrays stockant chacun les informations d'une ligne d'un carton.
                    JSONObject cartonJSON, casesJSON; // cartonJSON : représentation JSON d'un carton, casesJSON : représentation JSON des cases d'un carton.
                    CaseCarton[][] casesCarton; // Cases d'un carton sous forme d'un tableau à deux dimensions.
                    int valeurCase = -1, ligne, colonne, idInt;
                    String id;

                    // On crée une nouvelle ArrayList vide pour les cartons.
                    cartons = new ArrayList<>();

                    // Pour chaque carton contenu dans le JSON
                    for (int i = 0; i < cartonsJSON.length(); i++) {
                        // On va chercher le carton sous format JSON.
                        cartonJSON = (JSONObject) cartonsJSON.get(i);
                        // On va chercher les cases du carton sous format JSON.
                        casesJSON = cartonJSON.getJSONObject("cases");

                        // On récupère les trois lignes du carton.
                        ligne1 = casesJSON.getJSONArray("ligne1");
                        ligne2 = casesJSON.getJSONArray("ligne2");
                        ligne3 = casesJSON.getJSONArray("ligne3");

                        // On crée un nouveau tableau servant à stocker les données des cases du carton.
                        casesCarton = new CaseCarton[Constants.NB_LIGNES_CARTON][Constants.NB_COLONNES_CARTON];

                        ligne = 0;
                        colonne = 0;

                        // Pour chaque valeurs de case de la ligne 1
                        for (int j = 0; j < ligne1.length(); j++) {
                            // On récupère la valeur de la case.
                            valeurCase = (int) ligne1.get(j);

                            // On affecte la valeur de la case à la matrice casesCarton.
                            casesCarton[ligne][colonne] = new CaseCarton();
                            casesCarton[ligne][colonne].setValeur(valeurCase);

                            if (colonne < Constants.NB_COLONNES_CARTON - 1) {
                                colonne++;
                            }
                        }

                        ligne++;
                        colonne = 0;

                        // Idem pour la ligne 2
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

                        // Idem pour la ligne 3
                        for (int j = 0; j < ligne1.length(); j++) {
                            valeurCase = (int) ligne3.get(j);

                            casesCarton[ligne][colonne] = new CaseCarton();
                            casesCarton[ligne][colonne].setValeur(valeurCase);

                            if (colonne < Constants.NB_COLONNES_CARTON - 1) {
                                colonne++;
                            }
                        }

                        // On crée un objet Carton avec les cases précédemment créées.
                        carton = new Carton(casesCarton);

                        // On extrait l'identifiant du carton depuis le JSON.
                        id = cartonJSON.get("id").toString();
                        idInt = Integer.valueOf(id);

                        // On affecte au carton l'identifiant extrait au dessus.
                        carton.setId(idInt);
                        // On affecte au carton le joueur.
                        carton.setPersonne(joueur);
                        // On ajoute le carton à la liste des cartons.
                        cartons.add(carton);
                    }

                    // On affecte les cartons au plateau.
                    plateau.setCartons(cartons);
                    // On affecte au plateau le joueur.
                    plateau.setJoueur(joueur);
                    // On redessine le plateau.
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
                    // On tente de caster la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String)objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        // Sinon, on va chaercher la clé permettant de récupérer le statut de la quine.
                        cleRecuperationQuine = jo.getString("cle");
                        message = getResources().getString(R.string.texte_demande_quine_envoyee);
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        // On lance le Runnable servant à récupérer le statut de la quine.
                        runThread();
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "recuperationQuine") {
            if(isErreur) {
                isQuineRecuperee = true;
            }
            else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on l'affiche et on indique que le statut de la quine a été récupéré.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        isQuineRecuperee = true;
                    } else {
                        // On va chercher l'état de validation de la quine.
                        String etat = jo.getString("etat");

                        // Si la quine est validée, on l'affiche à l'utilisateur et on indique que le statut de la quine a été récupéré.
                        if(etat.equals("valide")) {
                            isQuineRecuperee = true;
                            message = getResources().getString(R.string.texte_demande_quine_validee);
                            AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        }
                        // Si la quine est invalidée, on l'affiche à l'utilisateur et on indique que le statut de la quine a été récupéré.
                        else if(etat.equals("invalide")) {
                            isQuineRecuperee = true;
                            message = getResources().getString(R.string.texte_demande_quine_invalidee);
                            AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        }
                        // Si la quine n'est pas en attente de validation, on indique que le statut de la quine a été récupéré.
                        else if(!(etat.equals("attente"))) {
                            isQuineRecuperee = true;
                        }
                    }
                }
                catch (JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                    isQuineRecuperee = true;
                }
            }

            // Si le statut de la quine a été récupéré, on désactive le bouton Quine et on arrète la longRunningTask.
            if(isQuineRecuperee) {
                plateau.setBoutonQuineInactif();
                plateau.invalidate();
                longRunningTaskFuture.cancel(true);
            }

            // On envoie une requête permettant de mettre à jour les informations de la tablette/téléphone.
            String email = sharedPref.getString("emailUtilisateur", "");
            String mdp = sharedPref.getString("mdpUtilisateur", "");
            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" + email +
                    "&mdp=" + mdp + "&idPartie=" + idPartie;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
            requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "MAJInfosTablette", "GET", "", getSupportFragmentManager());

        }
        else if(source == "obtentionInfosPersonne") {
            // S'il y a une erreur lors de l'obtention des informations de la personne, on l'affiche.
            if(isErreur) {
                message = "Erreur lors de l'obtention des informations de la personne : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
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
                    } else {
                        // Sinon, on construit un objet Personne avec les informations reçues.
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
            // S'il y a une erreur lors de la mise à jour des informations de la tablette/téléphone, on l'affiche.
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
                    // On tente de caster la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }
                    else {
                        int numeroEnCours = 0;
                        String lotEnCours = "";
                        boolean isLotCartonPlein, isPartieTerminee = false;

                        try {
                            // On récupère le numéro en-cours.
                            numeroEnCours = jo.getInt("numeroEnCours");

                            // Si le numéro en-cours est égal à -1, la partie est terminée
                            if(numeroEnCours == -1) {
                                AccueilActivity.afficherMessage("La partie est terminée", true, getSupportFragmentManager());
                            }
                        }
                        catch(NumberFormatException ex) {
                            numeroEnCours = -1;
                        }

                        // On récupère le lot en cours et une information indiquant si le lot en cours est à la ligne ou au carton plein.
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
                        // On redessine la vue plateau.
                        plateau.invalidate();
                    }
                }
                catch (JSONException e) {
                    message = e.getMessage();
                    AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "enregistrerConnexionAPartie") {
            // S'il y a une erreur lors de l'enregistrement de la connexion à la partie, on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de l'enregistrement de la connexion à la partie : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On caste la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);

                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if (objErreur != null) {
                        AccueilActivity.afficherMessage((String) objErreur, false, getSupportFragmentManager());
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "supprimerEnregistrementConnexionAPartie") {
            // S'il y a une erreur lors de la suppression de l'enregistrement de la connexion à la partie, on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la suppression de l'enregistrement de la connexion à la partie : " + reponse,
                        false, getSupportFragmentManager());
            }
            else {
                try {
                    // On caste la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);

                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if (objErreur != null) {
                        AccueilActivity.afficherMessage((String) objErreur, false, getSupportFragmentManager());
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

        // On construit le plateau de jeu.
        plateau = new Plateau(this);

        Intent intent = getIntent();
        // On récupère l'identifiant de la partie.
        idPartie = intent.getIntExtra("idPartie", -1);

        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);

        // On va chercher l'adresse du serveur dans les SharedPreferences.
        adresseServeur = sharedPref.getString("AdresseServeur", "");

        // On initialise le client STOMP.
        clientStomp = new ClientStomp(getApplicationContext(), idPartie, this, null, adresseServeur, getSupportFragmentManager(), sharedPref);


        // Si l'adresse du serveur n'est pas renseignée, on affiche un message.
        if(adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        }
        else {
            String email = sharedPref.getString("emailUtilisateur", "");

            // On envoie une requête permettant de récupérer les informations du participant.
            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/get_infos_personne?email=" + email;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                    adresse, MainActivity.this);
            requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "ObtentionInfosPersonne", "GET", "", getSupportFragmentManager());
        }

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
                float x = event.getX(); // On récupère la position en X du clic.
                float y = event.getY(); // On récupère la position en Y du clic.
                String id;
                // On récupère tous les éléments cliquables du plateau.
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
                                    // Non implémenté
                                }
                                else if(id == "boutonAccelererPartie") {
                                    // Non implémenté
                                }
                                else if(id == "boutonQuine") {
                                    // Si l'adresse du serveur n'est pas renseignée
                                    if(adresseServeur.trim().equals("")) {
                                        String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
                                        AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                                    }
                                    else {
                                        // On active le bouton Quine.
                                        plateau.setBoutonQuineActif();

                                        // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
                                        String email = sharedPref.getString("emailUtilisateur", "");
                                        String mdp = sharedPref.getString("mdpUtilisateur", "");

                                        // On envoie une requête indiquant la déclaration de la quine.
                                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant
                                                + "/participant/envoi-quine?email=" + email + "&mdp=" + mdp + "&idPartie=" + idPartie
                                                + "&idCarton=" + idDernierCartonUtilise;
                                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MainActivity.this);
                                        requeteHTTP.traiterRequeteHTTPJSON(MainActivity.this, "EnvoiQuine", "POST", "", getSupportFragmentManager());
                                        // On redessine le plateau pour mettre à jour le bouton Quine.
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
                                    // Si on clique sur le bouton Quitter, on affiche un message demandant si on souhaite bien quitter.
                                    HashMap<String, String> args = new HashMap<>();
                                    OuiNonDialogFragment ondf = new OuiNonDialogFragment("Etes-vous sûr de vouloir quitter ?", "quitter", "nePasQuitter", args);
                                    ondf.show(getSupportFragmentManager(), "");
                                }
                            }

                            i++;
                        }

                        // Si le client stomp n'est pas créé ou s'il n'est pas connecté, on va le créer/re-créer.
                        if(clientStomp.equals(null) || (!clientStomp.isConnected())) {
                            clientStomp = new ClientStomp(getApplicationContext(), idPartie, MainActivity.this, null,
                                    adresseServeur, getSupportFragmentManager(), sharedPref);
                        }

                        break;
                }

                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Quand on va détruire l'activité, si le client STOMP est initialisé, on va demander sa déconnection.
        if(!clientStomp.equals(null))
            clientStomp.deconnecterClientStomp();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Sauvegarde de valeurs avant rotation écran.
        if(cartons != null)
            outState.putParcelableArrayList("cartons", cartons);

        outState.putInt("numeroTire", numeroTire);
        outState.putString("lotEnCours", lotEnCours);
        outState.putBoolean("isLotEnCoursCartonPlein", isLotEnCoursCartonPlein);
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

    /**
     * Implémentation de la fonction onFinishEditDialog de l'interface OuiNonDialogListener.
     * @param nomActionOui : nom de l'action déclenchée lors du clic sur le bouton Oui.
     * @param nomActionNon : nom de l'action déclenchée lors du clic sur le bouton Non.
     * @param isChoixOui : true si clic sur le bouton Oui, false sinon.
     * @param args : arguments.
     */
    @Override
    public void onFinishEditDialog(String nomActionOui, String nomActionNon, boolean isChoixOui, HashMap<String, String> args) {
        // Si choix = Oui
        if (isChoixOui) {
            finish();
        }
    }
}