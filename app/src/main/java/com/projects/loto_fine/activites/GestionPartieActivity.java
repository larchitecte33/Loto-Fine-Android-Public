package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.projects.loto_fine.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Carton;
import com.projects.loto_fine.classes_metier.CaseCarton;
import com.projects.loto_fine.classes_metier.ElementCliquable;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_metier.RequeteHTTP;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;
import com.projects.loto_fine.vues.GrilleNumerosTiresEtCartons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ServerSocket;
import java.util.ArrayList;

public class GestionPartieActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    GrilleNumerosTiresEtCartons grilleNumerosTiresEtCarton;
    private int numeroTire, numCartonEnCours, idPartie = -1;
    private ArrayList<Carton> cartons = new ArrayList<>();
    private ArrayList<Integer> numerosTires = new ArrayList<>();
    private SharedPreferences sharedPref;
    private String sharedPrefAdresseServeur, sharedPrefEmail, sharedPrefMdp;
    private ServerSocket serverSocket;
    private boolean isLotPrecedentCartonPlein = false;

    // ****************************************************** //
    //                  Getters et setters                    //
    // ****************************************************** //
    public int getNumeroTire() {
        return numeroTire;
    }

    public void setNumeroTire(int numeroTire) {
        this.numeroTire = numeroTire;
    }

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message = "";
        ValidationDialogFragment vdf;

        if(source == "tirerNumero") {
            if(isErreur) {
                AccueilActivity.afficherMessage("Erreur lors du tirage de numéro : " + reponse, false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

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
                            numerosTires.add(numeroTire);
                            grilleNumerosTiresEtCarton.setNumeroEnCours(numeroTire);
                            grilleNumerosTiresEtCarton.setNumerosTires(numerosTires);
                            grilleNumerosTiresEtCarton.invalidate();

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

                        idJoueur = jo.getInt("idPersonne");
                        idCarton = jo.getInt("idCarton");
                        joCarton = jo.getJSONObject("carton");
                        joCases = joCarton.getJSONObject("cases");
                        jaLigne = joCases.getJSONArray("ligne1");

                        for(int j = 0 ; j < jaLigne.length() ; j++) {
                            CaseCarton caseCarton = new CaseCarton();
                            caseCarton.setValeur(jaLigne.getInt(j));
                            casesCarton[0][j] = caseCarton;
                        }

                        jaLigne = joCases.getJSONArray("ligne2");

                        for(int j = 0 ; j < jaLigne.length() ; j++) {
                            CaseCarton caseCarton = new CaseCarton();
                            caseCarton.setValeur(jaLigne.getInt(j));
                            casesCarton[1][j] = caseCarton;
                        }

                        jaLigne = joCases.getJSONArray("ligne3");

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
                    else if(jo.getString("reponse").equals("OK")) {
                        message = "La quine a été validée.";
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
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

                        String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" +
                                sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "MAJInfosTabletteAnimateur", "GET", "", getSupportFragmentManager());
                    }
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
            if(isErreur) {
                message = "Erreur lors de la mise à jour des informations de la tablette : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
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
            if(isErreur) {
                message = "Erreur lors de la suppression des numéros tirés : " + reponse;
                AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
            }
            else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

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


        // Si l'adresse du serveur n'est pas renseignée
        if(sharedPrefAdresseServeur.trim().equals("")) {
            messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
            AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
        }
        else {
            String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/recuperation-infos-partie?email=" +
                    sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
            RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
            requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "MAJInfosTabletteAnimateur", "GET", "", getSupportFragmentManager());
        }

        grilleNumerosTiresEtCarton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                String id;
                boolean elementTrouve = false;
                int i = 0;
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
                                id = listeElementsCliquables.get(i).getId();

                                if (id == "boutonTirerNumero") {
                                    grilleNumerosTiresEtCarton.setRecupQuinesVisible(true);

                                    if(sharedPrefAdresseServeur.trim() != "") {
                                        String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur
                                                + "/animateur/tirer-le-numero?email=" + sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
                                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                                        requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "TirerNumero", "GET", "", getSupportFragmentManager());
                                    }

                                    elementTrouve = true;
                                    grilleNumerosTiresEtCarton.invalidate();
                                }
                                else if (id == "boutonRecupQuines") {
                                    grilleNumerosTiresEtCarton.setRecupQuinesVisible(false);

                                    if(sharedPrefAdresseServeur.trim() != "") {
                                        String adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur
                                                + "/animateur/recuperation-quines?email=" + sharedPrefEmail + "&mdp=" + sharedPrefMdp + "&idPartie=" + idPartie;
                                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                                        requeteHTTP.traiterRequeteHTTPJSONArray(GestionPartieActivity.this, "RecuperationQuines", "GET", getSupportFragmentManager());
                                    }

                                    elementTrouve = true;
                                    grilleNumerosTiresEtCarton.invalidate();
                                }
                                else if ((id == "boutonValiderQuine") || (id == "boutonInvaliderQuine")) {
                                    if(cartons.size() <= 0) {
                                        messageErreur = "Aucun carton trouvé.";
                                        AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                                    }
                                    else if(cartons.size() < numCartonEnCours - 1) {
                                        messageErreur = "Le carton n'a pas été trouvé.";
                                        AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                                    }
                                    else if (sharedPrefAdresseServeur.trim() != "") {
                                        String adresse = "";

                                        if(id == "boutonValiderQuine") {
                                            adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur +
                                                    "/animateur/validation-quine?email=" + sharedPrefEmail + "&mdp=" + sharedPrefMdp +
                                                    "&idCarton=" + cartons.get(numCartonEnCours).getId() + "&isValidee=1";
                                        }
                                        else {
                                            adresse = sharedPrefAdresseServeur + ":" + Constants.portMicroserviceGUIAnimateur +
                                                    "/animateur/validation-quine?email=" + sharedPrefEmail + "&mdp=" + sharedPrefMdp +
                                                    "&idCarton=" + cartons.get(numCartonEnCours).getId() + "&isValidee=0";
                                        }

                                        RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, GestionPartieActivity.this);
                                        requeteHTTP.traiterRequeteHTTPJSON(GestionPartieActivity.this, "ValiderQuine", "POST", "", getSupportFragmentManager());
                                    }

                                    elementTrouve = true;
                                }
                                else if(id == "boutonCartonSuivant") {
                                    if(cartons.size() > numCartonEnCours + 1) {
                                        numCartonEnCours++;
                                        grilleNumerosTiresEtCarton.setCarton(cartons.get(numCartonEnCours));
                                        grilleNumerosTiresEtCarton.invalidate();
                                    }
                                    else if(cartons.size() > 0) {
                                        numCartonEnCours = 0;
                                        grilleNumerosTiresEtCarton.setCarton(cartons.get(numCartonEnCours));
                                        grilleNumerosTiresEtCarton.invalidate();
                                    }
                                }
                                else if(id == "boutonQuitter") {
                                    elementTrouve = true;
                                    finish();
                                }
                            }

                            i++;
                        }
                        break;
                }

                return true;
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