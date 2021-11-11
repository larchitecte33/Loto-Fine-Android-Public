package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.classes_metier.Carton;
import com.projects.loto_fine.classes_metier.CaseCarton;
import com.projects.loto_fine.classes_utilitaires.ElementCliquable;
import com.projects.loto_fine.classes_utilitaires.OuiNonDialogFragment;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;
import com.projects.loto_fine.stomp_client.ClientStomp;
import com.projects.loto_fine.vues.GrilleNumerosTiresEtCartons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Cette activité est affichée lors du clic sur le bouton DEMARRER LA PARTIE de l'adapter PartieAdapter.
 * Elle permet à l'utilisateur de lancer l'animation d'une partie.
 */
public class GestionPartieActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener,
        OuiNonDialogFragment.OuiNonDialogListener {

    GrilleNumerosTiresEtCartons grilleNumerosTiresEtCarton;
    private int numeroTire, numCartonEnCours, idPartie = -1;
    private ArrayList<Carton> cartons = new ArrayList<>();
    private ArrayList<Integer> numerosTires = new ArrayList<>();
    private SharedPreferences sharedPref;
    private String sharedPrefAdresseServeur, sharedPrefEmail, sharedPrefMdp;
    private ServerSocket serverSocket;
    private boolean isLotPrecedentCartonPlein = false;
    // Stocke l'identifiant des personnes connectées à la partie.
    private List<Integer> idPersonnesConnectees = new ArrayList<>();
    private ClientStomp clientStomp;

    // ****************************************************** //
    //                  Getters et setters                    //
    // ****************************************************** //
    public int getNumeroTire() {
        return numeroTire;
    }

    public void setNumeroTire(int numeroTire) {
        this.numeroTire = numeroTire;
    }

    // Ajout de l'identifiant d'une personne aux identifiants des personnes connectées.
    public void addIdPersonneConnectee(int id) {
        if(idPersonnesConnectees.indexOf(id) == -1) {
            idPersonnesConnectees.add(id);
            grilleNumerosTiresEtCarton.setNbParticipantsConnectes(idPersonnesConnectees.size());
            grilleNumerosTiresEtCarton.invalidate();
        }
    }

    // Suppression de l'identifiant d'une personne des identifiants des personnes connectées.
    public void deleteIdPersonneConnectee(int id) {
        int indexPersonneConnectee = idPersonnesConnectees.indexOf(id);

        if(indexPersonneConnectee != -1) {
            idPersonnesConnectees.remove(indexPersonneConnectee);
            grilleNumerosTiresEtCarton.setNbParticipantsConnectes(idPersonnesConnectees.size());
            grilleNumerosTiresEtCarton.invalidate();
        }
    }

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : tirerNumero, recuperationQuines, validationQuine, majInfosTablette et suppressionNumerosTires.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;

        if(source == "tirerNumero") {
            // Si le serveur a renvoyé un erreur lors du tirage d'un numéro, alors on l'affiche.
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors du tirage de numéro : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on affiche cette erreur.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        numeroTire = Integer.valueOf(jo.getString("numero"));

                        // Fin de partie
                        if(numeroTire == -1) {
                            AccueilActivity.afficherMessage("La partie est terminée", true, getSupportFragmentManager());
                        }
                        else {
                            // On ajoute le numéro tiré à la liste des numéros tirés.
                            numerosTires.add(numeroTire);
                            grilleNumerosTiresEtCarton.setNumeroEnCours(numeroTire);
                            grilleNumerosTiresEtCarton.setNumerosTires(numerosTires);
                            // On redessine la grille des numéros tirés.
                            grilleNumerosTiresEtCarton.invalidate();

                            // On créer et envoie une requête permettant de mettre à jour le numéro en cours et les informations concernant le lot en cours.
                            String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" +
                                    sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
                            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                            requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "MAJInfosTabletteAnimateur", "GET", "", getSupportFragmentManager());
                        }
                    }
                }
                catch(JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if (source == "recuperationQuines") {
            // Si le serveur a renvoyé un erreur lors de la récupération des quines, alors on l'affiche.
            if(isErreur) {
                message = "Erreur lors de la récupération des quine : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
            }
            else {
                // On commence par tenter de convertir la réponse en JSONArray (comportement par défaut).
                try {
                    // On récupère tous les cartons pour lesquels une quine a été déclrée.
                    JSONArray ja = new JSONArray(reponse);
                    int idJoueur, idCarton;
                    CaseCarton[][] casesCarton;

                    // On supprime les données des cartons récupérés précédemment.
                    cartons.clear();

                    // On parcourt chacun des cartons.
                    for(int i = 0 ; i < ja.length() ; i++) {
                        // Extraction des données du carton
                        JSONObject jo = ja.getJSONObject(i);
                        JSONObject joCarton;
                        JSONObject joCases;
                        JSONArray jaLigne;
                        casesCarton = new CaseCarton[Constants.NB_LIGNES_CARTON][Constants.NB_COLONNES_CARTON];

                        // Extraction de l'identifiant du joueur
                        idJoueur = jo.getInt("idPersonne");
                        // Extraction de l'identifiant du carton
                        idCarton = jo.getInt("idCarton");
                        // Extraction du JSONObject contenant les informations du carton
                        joCarton = jo.getJSONObject("carton");
                        // Extraction du JSONObject contenant les informations des cases du carton
                        joCases = joCarton.getJSONObject("cases");
                        // Extraction du JSONArray contenant la valeur de chaque cases de la première ligne
                        jaLigne = joCases.getJSONArray("ligne1");

                        // On parcourt la valeur de chaque cases de la première ligne.
                        for(int j = 0 ; j < jaLigne.length() ; j++) {
                            CaseCarton caseCarton = new CaseCarton();
                            caseCarton.setValeur(jaLigne.getInt(j));
                            casesCarton[0][j] = caseCarton;
                        }

                        // Extraction du JSONArray contenant la valeur de chaque cases de la deuxième ligne
                        jaLigne = joCases.getJSONArray("ligne2");

                        // On parcourt la valeur de chaque cases de la deuxième ligne.
                        for(int j = 0 ; j < jaLigne.length() ; j++) {
                            CaseCarton caseCarton = new CaseCarton();
                            caseCarton.setValeur(jaLigne.getInt(j));
                            casesCarton[1][j] = caseCarton;
                        }

                        // Extraction du JSONArray contenant la valeur de chaque cases de la troisième ligne
                        jaLigne = joCases.getJSONArray("ligne3");

                        // On parcourt la valeur de chaque cases de la troisième ligne.
                        for(int j = 0 ; j < jaLigne.length() ; j++) {
                            CaseCarton caseCarton = new CaseCarton();
                            caseCarton.setValeur(jaLigne.getInt(j));
                            casesCarton[2][j] = caseCarton;
                        }

                        Personne personne = new Personne();
                        personne.setId(idJoueur);

                        Carton carton = new Carton(casesCarton);
                        carton.setId(idCarton);
                        carton.setPersonne(personne);

                        // Ajout du carton à l'array des cartons (pour affichage).
                        cartons.add(carton);
                    }

                    if(cartons.size() > 0) {
                        numCartonEnCours = 0;
                        grilleNumerosTiresEtCarton.setCarton(cartons.get(numCartonEnCours));

                        // Si il y a plus de deux cartons à valider, on active le bouton "Carton suivant".
                        if(cartons.size() > 1) {
                            grilleNumerosTiresEtCarton.setBoutonCartonSuivantActif(true);
                        }
                        // Sinon, on le désactive.
                        else {
                            grilleNumerosTiresEtCarton.setBoutonCartonSuivantActif(false);
                        }

                        // On demande à grilleNumerosTiresEtCarton de se redessiner.
                        grilleNumerosTiresEtCarton.invalidate();
                    }
                }
                catch(JSONException e) {
                    // Ici, on a pas pu convertir la réponse en JSONArray. On essaie de la convertir en JSONObject (cas où le
                    // serveur a renvoyé une erreur.
                    try {
                        JSONObject jo = new JSONObject(reponse);
                        Object objErreur = jo.opt("erreur");

                        // Si on a bien récupéré une erreur, on l'affiche.
                        if (objErreur != null) {
                            message = (String) objErreur;
                            AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        }
                    }
                    catch(JSONException ex) {
                        AccueilActivity.afficherMessage(ex.getMessage(), false, getSupportFragmentManager());
                    }
                }
            }
        }
        else if (source == "validationQuine") {
            // Si le serveur a renvoyé un erreur lors de la validation de la quine, alors on l'affiche.
            if(isErreur) {
                message = "Erreur lors de la validation de la quine : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);

                    Object objErreur = jo.opt("erreur");

                    // Si on a récupéré une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }
                    // Si la valeur rattachée à la clé reponse est OK, alors on affiche un message indiquant que la quine a été validée.
                    else if(jo.getString("reponse").equals("OK")) {
                        message = "La quine a été validée.";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        // On supprime le carton concerné par la quine de la liste des cartons.
                        cartons.remove(numCartonEnCours);

                        // Si le lot en-cours est à la ligne et si le lot précédent était au carton plein,
                        // alors on vide la liste des numéros tirés.
                        if(isLotPrecedentCartonPlein) {
                            numerosTires.clear();
                            grilleNumerosTiresEtCarton.setNumerosTires(numerosTires);

                            String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur +
                                    "/animateur/supprimer-numeros-tires?email=" + sharedPrefEmail +
                                    "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
                            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                            requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "SupprimerNumerosTires", "DELETE", "", getSupportFragmentManager());
                        }

                        // Si il y a plus de deux cartons à valider, on active le bouton "Carton suivant".
                        if(cartons.size() > 1) {
                            grilleNumerosTiresEtCarton.setBoutonCartonSuivantActif(true);
                        }
                        // Sinon, on le désactive.
                        else {
                            grilleNumerosTiresEtCarton.setBoutonCartonSuivantActif(false);
                        }

                        // On crée et envoie une requête permettant de récupérer les informations de la partie.
                        String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" +
                                sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "MAJInfosTabletteAnimateur", "GET", "", getSupportFragmentManager());
                    }
                    // Si la valeur rattachée à la clé reponse est KO, alors on affiche un message indiquant que la quine a été invalidée.
                    else if(jo.getString("reponse").equals("KO")) {
                        message = "La quine a été invalidée.";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        cartons.remove(numCartonEnCours);
                    }
                }
                catch(JSONException ex) {
                    AccueilActivity.afficherMessage(ex.getMessage(), false, getSupportFragmentManager());
                }
                catch(Exception ex2) {
                    AccueilActivity.afficherMessage("ExceptionDansValidationQuine" + ex2.toString(), false, getSupportFragmentManager());
                }
            }

            // S'il y a encore des cartons à valider,
            if(cartons.size() > 0) {
                numCartonEnCours = 0;
                grilleNumerosTiresEtCarton.setCarton(cartons.get(numCartonEnCours));
                grilleNumerosTiresEtCarton.invalidate();
            }
            else {
                grilleNumerosTiresEtCarton.setCarton(null);
                grilleNumerosTiresEtCarton.invalidate();
            }
        }
        else if(source == "majInfosTablette") {
            // Si le serveur a renvoyé un erreur lors de la récupération des informations de la partie, alors on l'affiche.
            if(isErreur) {
                message = "Erreur lors de la mise à jour des informations de la tablette : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On caste la réponse en JSObject.
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on affiche cette erreur.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }
                    else {
                        String lotEnCours = "";
                        boolean isLotCartonPlein;

                        lotEnCours = jo.getString("lotEnCours");

                        if(jo.get("lotCartonPlein").toString().equals("")) {
                            isLotCartonPlein = false;
                        }
                        else {
                            isLotCartonPlein = jo.getBoolean("lotCartonPlein");
                        }

                        isLotPrecedentCartonPlein = isLotCartonPlein;

                        // On met à jour le lot en cours et l'information indiquant si le lot en cours est à la ligne ou au carton plein
                        // sur la vue grilleNumerosTiresEtCarton.
                        grilleNumerosTiresEtCarton.setLotEnCours(lotEnCours);
                        grilleNumerosTiresEtCarton.setLotCartonPlein(isLotCartonPlein);
                        grilleNumerosTiresEtCarton.invalidate();
                    }
                }
                catch (JSONException e) {
                    message = e.getMessage();
                    vdf = new ValidationDialogFragment(message, false);
                    vdf.show(getSupportFragmentManager(), "");
                }
            }
        }
        else if(source == "suppressionNumerosTires") {
            // Si le serveur a renvoyé un erreur lors de la suppression des numéros tirés, alors on l'affiche.
            if(isErreur) {
                message = "Erreur lors de la suppression des numéros tirés : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
            }
            else {
                try {
                    // On caste la réponse en JSObject.
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    // Si on a une erreur, on l'affiche.
                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    }
                }
                catch (JSONException e) {
                    message = e.getMessage();
                    vdf = new ValidationDialogFragment(message, false);
                    vdf.show(getSupportFragmentManager(), "");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        // Récupération de l'identifiant de la partie dans l'intent.
        idPartie = intent.getIntExtra("idPartie", -1);

        String messageErreur = "";

        grilleNumerosTiresEtCarton = new GrilleNumerosTiresEtCartons(this);
        // Par défaut, on désactive le bouton "Cartons suivants".
        grilleNumerosTiresEtCarton.setBoutonCartonSuivantActif(false);

        setContentView(grilleNumerosTiresEtCarton);

        // Récupération des SharedPreferences
        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        sharedPrefAdresseServeur = sharedPref.getString("AdresseServeur", "");
        sharedPrefEmail = sharedPref.getString("emailUtilisateur", "");
        sharedPrefMdp = sharedPref.getString("mdpUtilisateur", "");


        // Si l'adresse du serveur n'est pas renseignée, on affiche une erreur.
        if(sharedPrefAdresseServeur.trim().equals("")) {
            messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        }
        else {
            // On crée et envoie une requête permettant d'obtenir les informations de la partie.
            String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" +
                    sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
            requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "MAJInfosTabletteAnimateur", "GET", "", getSupportFragmentManager());
        }

        // On initialise le client STOMP.
        clientStomp = new ClientStomp(getApplicationContext(), idPartie, null, this, sharedPrefAdresseServeur, getSupportFragmentManager(), sharedPref);

        // Gestion du clic sur la vue grilleNumerosTiresEtCarton
        grilleNumerosTiresEtCarton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX(); // On récupère la position en X du clic
                float y = event.getY(); // On récupère la position en Y du clic
                String id;
                boolean elementTrouve = false;
                int i = 0;
                // On récupère la liste des éléments qui réagissent aux clics.
                ArrayList<ElementCliquable> listeElementsCliquables = grilleNumerosTiresEtCarton.getListeElementsCliquables();
                String messageErreur = "";

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        // Tant qu'on a pas déterminé l'élément sur lequel on a cliqué et tant qu'on a pas parcouru
                        // l'ensemble des élément cliquables
                        while((!elementTrouve) && (i < listeElementsCliquables.size())) {
                            // Si la position de l'élement cliquable correspond à la position du clic
                            if ((listeElementsCliquables.get(i).getPosX() <= x) && (listeElementsCliquables.get(i).getPosX() + listeElementsCliquables.get(i).getWidth() > x)
                                    && (listeElementsCliquables.get(i).getPosY() <= y) && (listeElementsCliquables.get(i).getPosY() + listeElementsCliquables.get(i).getHeight() > y)) {
                                // Récupération de l'identifiant de l'élément cliqué.
                                id = listeElementsCliquables.get(i).getId();

                                // Si l'élément cliqué est le bouton servant à tirer des numéros
                                if (id == "boutonTirerNumero") {
                                    // On active le bouton servant à récupérer les quines.
                                    grilleNumerosTiresEtCarton.setRecupQuinesVisible(true);

                                    if(sharedPrefAdresseServeur.trim() != "") {
                                        // Création et envoi d'une requête permettant de tirer un numéro.
                                        String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur
                                                + "/animateur/tirer-le-numero?email=" + sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
                                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                                        requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "TirerNumero", "GET", "", getSupportFragmentManager());
                                    }

                                    elementTrouve = true;
                                    // On demande à la vue grilleNumerosTiresEtCarton de se redessiner.
                                    grilleNumerosTiresEtCarton.invalidate();
                                }
                                // Si l'élément cliqué est le bouton servant à récupérer les quines
                                else if (id == "boutonRecupQuines") {
                                    // On désactive le bouton servant à récupérer les quines.
                                    grilleNumerosTiresEtCarton.setRecupQuinesVisible(false);

                                    if(sharedPrefAdresseServeur.trim() != "") {
                                        // Création et envoi d'une requête permettant de récupérer les quines.
                                        String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur
                                                + "/animateur/recuperation-quines?email=" + sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
                                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                                        requeteHTTP.traiterRequeteHTTPJSONArray(GestionPartieActivity.this, "RecuperationQuines", "GET", getSupportFragmentManager());
                                    }

                                    elementTrouve = true;
                                    grilleNumerosTiresEtCarton.invalidate();
                                }
                                // Si l'élément cliqué est le bouton servant à valider une quine ou le bouton servant à invalider une quine
                                else if ((id == "boutonValiderQuine") || (id == "boutonInvaliderQuine")) {
                                    // S'il n'y a aucun carton à valider ou invalider, on affiche une erreur.
                                    if(cartons.size() <= 0) {
                                        messageErreur = "Aucun carton trouvé.";
                                        AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                                    }
                                    // Si l'indice du carton à valider dépasse la taille de la liste des cartons, on affiche une erreur.
                                    else if(cartons.size() < numCartonEnCours - 1) {
                                        messageErreur = "Le carton n'a pas été trouvé.";
                                        AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                                    }
                                    else if (sharedPrefAdresseServeur.trim() != "") {
                                        String adresse = "";

                                        // Si l'élément cliqué est le bouton servant à valider une quine, on crée l'adresse permettant de demander la
                                        // validation de la quine.
                                        if(id == "boutonValiderQuine") {
                                            adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur +
                                                    "/animateur/validation-quine?email=" + sharedPrefEmail + "&mdp=" + sharedPrefMdp +
                                                    "&idCarton=" + cartons.get(numCartonEnCours).getId() + "&isValidee=1";
                                        }
                                        // Si l'élément cliqué est le bouton servant à invalider une quine, on crée l'adresse permettant de demander
                                        // l'invalidation de la quine.
                                        else {
                                            adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur +
                                                    "/animateur/validation-quine?email=" + sharedPrefEmail + "&mdp=" + sharedPrefMdp +
                                                    "&idCarton=" + cartons.get(numCartonEnCours).getId() + "&isValidee=0";
                                        }

                                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                                        // On envoie la requête permettant de valider ou d'invalider la quine.
                                        requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "ValiderQuine", "POST", "", getSupportFragmentManager());
                                    }

                                    elementTrouve = true;
                                }
                                // Si l'élément cliqué est le bouton servant à visualiser le carton suivant
                                else if(id == "boutonCartonSuivant") {
                                    // Si on est pas à la fin de la liste des cartons à valider, on incrémente le numéro du carton à valider.
                                    if(cartons.size() > numCartonEnCours + 1) {
                                        numCartonEnCours++;
                                        grilleNumerosTiresEtCarton.setCarton(cartons.get(numCartonEnCours));
                                        grilleNumerosTiresEtCarton.invalidate();
                                    }
                                    // Sinon, s'il y a des cartons à valider, on repasse au premier carton.
                                    else if(cartons.size() > 0) {
                                        numCartonEnCours = 0;
                                        grilleNumerosTiresEtCarton.setCarton(cartons.get(numCartonEnCours));
                                        grilleNumerosTiresEtCarton.invalidate();
                                    }
                                }
                                // Si l'élément cliqué est le bouton Quitter, alors on affiche une boite de dialogue permettant à l'utilisateur de confirmer
                                // qu'il souhaite quitter la partie.
                                else if(id == "boutonQuitter") {
                                    elementTrouve = true;
                                    HashMap<String, String> args = new HashMap<>();
                                    OuiNonDialogFragment ondf = new OuiNonDialogFragment("Etes-vous sûr de vouloir quitter ?", "quitter", "nePasQuitter", args);
                                    ondf.show(getSupportFragmentManager(), "");
                                }
                            }

                            i++;
                        }

                        // Si le client stomp n'est pas créé ou s'il n'est pas connecté, on va le créer/re-créer.
                        if(clientStomp.equals(null) || (!clientStomp.isConnected())) {
                            clientStomp = new ClientStomp(getApplicationContext(), idPartie, null, GestionPartieActivity.this,
                                    sharedPrefAdresseServeur, getSupportFragmentManager(), sharedPref);
                        }

                        break;
                }

                return true;
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

    /**
     * Surcharge de la fonction onDestroy de AppCompatActivity.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(!clientStomp.equals(null))
            clientStomp.deconnecterClientStomp();
    }
}