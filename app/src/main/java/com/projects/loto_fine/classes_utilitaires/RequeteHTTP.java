package com.projects.loto_fine.classes_utilitaires;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.projects.loto_fine.activites.AccueilActivity;
import com.projects.loto_fine.activites.AjoutLotActivity;
import com.projects.loto_fine.activites.ChoixLieuRetraitActivity;
import com.projects.loto_fine.activites.ClassementsActivity;
import com.projects.loto_fine.activites.ConnectionActivity;
import com.projects.loto_fine.activites.CreationCompteActivity;
import com.projects.loto_fine.activites.CreerPartieActivity;
import com.projects.loto_fine.activites.DescriptionMethodeReglementActivity;
import com.projects.loto_fine.activites.FeedBackActivity;
import com.projects.loto_fine.activites.GestionPartieActivity;
import com.projects.loto_fine.activites.MainActivity;
import com.projects.loto_fine.activites.MesInscriptionsActivity;
import com.projects.loto_fine.activites.MesLotsActivity;
import com.projects.loto_fine.activites.ModificationLotActivity;
import com.projects.loto_fine.activites.RecherchePartieActivity;
import com.projects.loto_fine.activites.StatistiquesActivity;
import com.projects.loto_fine.activites.SuppressionCompteActivity;
import com.projects.loto_fine.activites.VisualiserListeInscritsActivity;
import com.projects.loto_fine.activites.VisualiserListeLotsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequeteHTTP {

    private Context context;
    private String url;
    private AppCompatActivity classTraitementReponse;

    public RequeteHTTP(Context context, String url, AppCompatActivity classTraitementReponse) {
        this.context = context;
        this.url = url;
        this.classTraitementReponse = classTraitementReponse;
    }

    public String traiterRequeteHTTPJSON(AppCompatActivity classeAppelante, String action, String method, String jsonToPostStr,
                                         FragmentManager fragmentManager) {
        RequestQueue queue = RequestQueueSingleton.getInstance(this.context).getRequestQueue(); // Volley.newRequestQueue(context);
        ReponseHTTP reponse = new ReponseHTTP();

        url = AccueilActivity.encoderURL(url, fragmentManager);
        Log.d("url : ", url);

        if(method == "GET") {
            // On prépare la requête
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // display response
                            Log.d("Response", response.toString());

                            if(action == "Connexion") {
                                final ConnectionActivity connectionActivity = (ConnectionActivity) classeAppelante;
                                connectionActivity.TraiterReponse("connexion", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == ConnectionActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final ConnectionActivity connectionActivity = (ConnectionActivity) classeAppelante;
                                connectionActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == MainActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == SuppressionCompteActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == StatistiquesActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final StatistiquesActivity statistiquesActivity = (StatistiquesActivity) classeAppelante;
                                statistiquesActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == CreationCompteActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                creationCompteActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            else if (action == "RecuperationQuine") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("recuperationQuine", response.toString(), false);
                            }
                            else if (action == "TirerNumero") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("tirerNumero", response.toString(), false);
                            }
                            // Mise à jour des informations sur la tablette/téléphone du participant.
                            else if(action == "MAJInfosTablette") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("majInfosTablette", response.toString(), false);
                            }
                            // Mise à jour des informations sur la tablette/téléphone de l'animateur.
                            else if(action == "MAJInfosTabletteAnimateur") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("majInfosTablette", response.toString(), false);
                            }
                            // Récupération de la méthode de réglement d'une partie
                            else if(action == "RecuperationMethodeReglement") {
                                final DescriptionMethodeReglementActivity descriptionMethodeReglementActivity = (DescriptionMethodeReglementActivity) classeAppelante;
                                descriptionMethodeReglementActivity.TraiterReponse("recuperationMethodeReglement", response.toString(), false);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());

                            if(action == "Connexion") {
                                final ConnectionActivity connectionActivity = (ConnectionActivity) classeAppelante;
                                connectionActivity.TraiterReponse("connexion", error.toString(), true);
                            }
                            else if((classeAppelante.getClass() == ConnectionActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final ConnectionActivity connectionActivity = (ConnectionActivity) classeAppelante;
                                connectionActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            else if((classeAppelante.getClass() == MainActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            else if((classeAppelante.getClass() == SuppressionCompteActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            else if((classeAppelante.getClass() == StatistiquesActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final StatistiquesActivity statistiquesActivity = (StatistiquesActivity) classeAppelante;
                                statistiquesActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            else if((classeAppelante.getClass() == CreationCompteActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                creationCompteActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            else if(action == "RecuperationQuine") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("recuperationQuine", error.toString(), true);
                            }
                            else if (action == "TirerNumero") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("tirerNumero", error.toString(), true);
                            }
                            // Mise à jour des informations sur la tablette/téléphone du participant.
                            else if(action == "MAJInfosTablette") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("majInfosTablette", error.toString(), true);
                            }
                            // Mise à jour des informations sur la tablette/téléphone de l'animateur.
                            else if(action == "MAJInfosTabletteAnimateur") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("majInfosTablette", error.toString(), true);
                            }
                            // Récupération de la méthode de réglement d'une partie
                            else if(action == "RecuperationMethodeReglement") {
                                final DescriptionMethodeReglementActivity descriptionMethodeReglementActivity = (DescriptionMethodeReglementActivity) classeAppelante;
                                descriptionMethodeReglementActivity.TraiterReponse("recuperationMethodeReglement", error.toString(), true);
                            }
                        }
                    }
            );

            getRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // add it to the RequestQueue
            queue.add(getRequest);
        }
        else if(method == "POST") {
            if(action == "EnvoyerReponsesFeedback") {
                JSONArray jsonToPost;

                try {
                    jsonToPost = new JSONArray(jsonToPostStr);
                }
                catch(JSONException e) {
                    System.out.println("Exception lors de la convertion en JSONArray : " + e.getMessage());
                    jsonToPost = null;
                }

                JsonArrayRequest postRequest = new JsonArrayRequest(Request.Method.POST, url, jsonToPost,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // Envoi des réponses pour le feedback
                                if (action == "EnvoyerReponsesFeedback") {
                                    final FeedBackActivity feedBackActivity = (FeedBackActivity) classeAppelante;
                                    feedBackActivity.TraiterReponse("envoyerReponsesFeedback", response.toString(), false);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Envoi des réponses pour le feedback
                                if (action == "EnvoyerReponsesFeedback") {
                                    final FeedBackActivity feedBackActivity = (FeedBackActivity) classeAppelante;
                                    feedBackActivity.TraiterReponse("envoyerReponsesFeedback", error.toString(), true);
                                }
                            }
                        });

                queue.add(postRequest);
            }
            else {
                JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Création d'un compte
                                if (action == "CreationCompte") {
                                    final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                    creationCompteActivity.TraiterReponse("creationCompte", response.toString(), false);
                                }
                                // Récupération des cartons
                                else if (action == "Main") {
                                    final MainActivity mainActivity = (MainActivity) classeAppelante;
                                    mainActivity.TraiterReponse("recuperationCartons", response.toString(), false);
                                }
                                // Envoi d'une demande de quine par un participant
                                else if (action == "EnvoiQuine") {
                                    final MainActivity mainActivity = (MainActivity) classeAppelante;
                                    mainActivity.TraiterReponse("envoiQuine", response.toString(), false);
                                }
                                // Validation d'une quine par l'animateur
                                else if (action == "ValiderQuine") {
                                    final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                    gestionPartieActivity.TraiterReponse("validationQuine", response.toString(), false);
                                }
                                // Inscription à une partie
                                else if (action == "InscriptionPartie") {
                                    final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                    recherchePartieActivity.TraiterReponse("inscriptionPartie", response.toString(), false);
                                }
                                // Création d'une partie
                                else if (action == "CreationPartie") {
                                    final CreerPartieActivity creerPartieActivity = (CreerPartieActivity) classeAppelante;
                                    creerPartieActivity.TraiterReponse("creationPartie", response.toString(), false);
                                }
                                // Ajout d'un lot
                                else if (action == "AjoutLot") {
                                    final AjoutLotActivity ajoutLotActivity = (AjoutLotActivity) classeAppelante;
                                    ajoutLotActivity.TraiterReponse("ajoutLot", response.toString(), false);
                                }
                                // Définition du lieu de retrait d'un lot
                                else if ((classeAppelante.getClass() == MesLotsActivity.class) && (action == "DefinirLieuRetraitLot")) {
                                    final MesLotsActivity mesLotsActivity = (MesLotsActivity) classeAppelante;
                                    mesLotsActivity.TraiterReponse("definirLieuRetraitLot", response.toString(), false);
                                }
                                // Définition du lieu de retrait d'un lot
                                else if ((classeAppelante.getClass() == ChoixLieuRetraitActivity.class) && (action == "DefinirLieuRetrait")) {
                                    final ChoixLieuRetraitActivity choixLieuRetraitActivity = (ChoixLieuRetraitActivity) classeAppelante;
                                    choixLieuRetraitActivity.TraiterReponse("definirLieuRetraitLot", response.toString(), false);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (action == "CreationCompte") {
                                    final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                    creationCompteActivity.TraiterReponse("creationCompte", error.toString(), true);
                                }
                                else if (action == "Main") {
                                    final MainActivity mainActivity = (MainActivity) classeAppelante;
                                    mainActivity.TraiterReponse("recuperationCartons", error.toString(), true);
                                }
                                else if (action == "EnvoiQuine") {
                                    final MainActivity mainActivity = (MainActivity) classeAppelante;
                                    mainActivity.TraiterReponse("envoiQuine", error.toString(), true);
                                }
                                else if (action == "ValiderQuine") {
                                    final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                    gestionPartieActivity.TraiterReponse("validationQuine", error.toString(), true);
                                }
                                // Inscription à une partie
                                else if (action == "InscriptionPartie") {
                                    final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                    recherchePartieActivity.TraiterReponse("inscriptionPartie", error.toString(), true);
                                }
                                // Création d'une partie
                                else if (action == "CreationPartie") {
                                    final CreerPartieActivity creerPartieActivity = (CreerPartieActivity) classeAppelante;
                                    creerPartieActivity.TraiterReponse("creationPartie", error.toString(), true);
                                }
                                // Ajout d'un lot
                                else if (action == "AjoutLot") {
                                    final AjoutLotActivity ajoutLotActivity = (AjoutLotActivity) classeAppelante;
                                    ajoutLotActivity.TraiterReponse("ajoutLot", error.toString(), true);
                                }
                                // Définition du lieu de retrait d'un lot
                                else if ((classeAppelante.getClass() == MesLotsActivity.class) && (action == "DefinirLieuRetraitLot")) {
                                    final MesLotsActivity mesLotsActivity = (MesLotsActivity) classeAppelante;
                                    mesLotsActivity.TraiterReponse("definirLieuRetraitLot", error.toString(), true);
                                }
                                // Définition du lieu de retrait d'un lot
                                else if ((classeAppelante.getClass() == ChoixLieuRetraitActivity.class) && (action == "DefinirLieuRetrait")) {
                                    final ChoixLieuRetraitActivity choixLieuRetraitActivity = (ChoixLieuRetraitActivity) classeAppelante;
                                    choixLieuRetraitActivity.TraiterReponse("definirLieuRetraitLot", error.toString(), true);
                                }
                            }
                        }
                );

                queue.add(postRequest);
            }
        }
        else if(method == "DELETE") {
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.DELETE, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "DesinscriptionPartie")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("desinscriptionPartie", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "DesinscriptionPartie")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("desinscriptionPartie", response.toString(), false);
                            }
                            else if(action == "SuppressionCompte") {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("suppressionCompte", response.toString(), false);
                            }
                            // Suppression d'un lot
                            else if (action == "SuppressionLot") {
                                final VisualiserListeLotsActivity visualiserListeLotsActivity = (VisualiserListeLotsActivity) classeAppelante;
                                visualiserListeLotsActivity.TraiterReponse("suppressionLot", response.toString(), false);
                            }
                            // Suppression d'une partie
                            else if((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "SuppressionPartie")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("suppressionPartie", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "SuppressionPartie")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("suppressionPartie", response.toString(), false);
                            }
                            // Suppression des numeros tirés pour une partie
                            else if (action == "SupprimerNumerosTires") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("suppressionNumerosTires", response.toString(), false);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(action == "DesinscriptionPartie") {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("desinscriptionPartie", error.toString(), true);
                            }
                            else if(action == "SuppressionCompte") {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("suppressionCompte", error.toString(), true);
                            }
                            // Suppression d'un lot
                            else if (action == "SuppressionLot") {
                                final VisualiserListeLotsActivity visualiserListeLotsActivity = (VisualiserListeLotsActivity) classeAppelante;
                                visualiserListeLotsActivity.TraiterReponse("suppressionLot", error.toString(), true);
                            }
                            // Suppression d'une partie
                            else if((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "SuppressionPartie")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("suppressionPartie", error.toString(), true);
                            }
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "SuppressionPartie")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("suppressionPartie", error.toString(), true);
                            }
                            // Suppression des numeros tirés pour une partie
                            else if (action == "SupprimerNumerosTires") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("suppressionNumerosTires", error.toString(), true);
                            }
                        }
                    }
            );

            queue.add(postRequest);
        }
        else if(method == "PUT") {
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Modification d'un lot
                            if(action == "ModificationLot") {
                                final ModificationLotActivity modificationLotActivity = (ModificationLotActivity) classeAppelante;
                                modificationLotActivity.TraiterReponse("modificationLot", response.toString(), false);
                            }
                            // Modification d'un compte
                            else if(action == "ModificationCompte") {
                                final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                creationCompteActivity.TraiterReponse("modificationCompte", response.toString(), false);
                            }
                            // Modification du statut de validation d'inscriptions de participants à une partie
                            else if(action == "ChangerStatutValidationInscriptions") {
                                final VisualiserListeInscritsActivity visualiserListeInscritsActivity = (VisualiserListeInscritsActivity) classeAppelante;
                                visualiserListeInscritsActivity.TraiterReponse("changerStatutValidationInscriptions", response.toString(), false);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Modification d'un lot
                            if(action == "ModificationLot") {
                                final ModificationLotActivity modificationLotActivity = (ModificationLotActivity) classeAppelante;
                                modificationLotActivity.TraiterReponse("modificationLot", error.toString(), true);
                            }
                            // Modification d'un compte
                            else if(action == "ModificationCompte") {
                                final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                creationCompteActivity.TraiterReponse("modificationCompte", error.toString(), true);
                            }
                            // Modification du statut de validation d'inscriptions de participants à une partie
                            else if(action == "ChangerStatutValidationInscriptions") {
                                final VisualiserListeInscritsActivity visualiserListeInscritsActivity = (VisualiserListeInscritsActivity) classeAppelante;
                                visualiserListeInscritsActivity.TraiterReponse("changerStatutValidationInscriptions", error.toString(), true);
                            }
                        }
                    }
            );

            queue.add(putRequest);
        }

        return "";
    }

    public String traiterRequeteHTTPJSONArray(AppCompatActivity classeAppelante, String action, String method,
                                              FragmentManager fragmentManager) {
        RequestQueue queue = RequestQueueSingleton.getInstance(this.context).getRequestQueue(); // Volley.newRequestQueue(context);
        ReponseHTTP reponse = new ReponseHTTP();

        url = AccueilActivity.encoderURL(url, fragmentManager);

        if(method == "GET") {
            // On prépare la requête
            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // display response
                            Log.d("Response", response.toString());

                            if (action == "Main") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("recuperationCartons", response.toString(), false);
                            }
                            else if (action == "RecuperationQuines") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("recuperationQuines", response.toString(), false);
                            }
                            else if (action == "RecherchePartie") {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("recherchePartie", response.toString(), false);
                            }
                            else if ((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "RechercheListeInscriptions")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("rechercheListeInscriptions", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "RechercheListeInscriptions")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("rechercheListeInscriptions", response.toString(), false);
                            }
                            else if((classeAppelante.getClass() == SuppressionCompteActivity.class) && (action == "RechercheListeInscriptions")) {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("rechercheListeInscriptions", response.toString(), false);
                            }
                            // Obtention des informations concernant les lots de la partie.
                            else if(action == "RecuperationInfosLots") {
                                final AjoutLotActivity ajoutLotActivity = (AjoutLotActivity) classeAppelante;
                                ajoutLotActivity.TraiterReponse("recuperationInfosLots", response.toString(), false);
                            }
                            else if(action == "RechercheListeLots") {
                                final VisualiserListeLotsActivity visualiserListeLotsActivity = (VisualiserListeLotsActivity) classeAppelante;
                                visualiserListeLotsActivity.TraiterReponse("rechercheListeLots", response.toString(), false);
                            }
                            else if(action == "RechercheLotsPersonne") {
                                final MesLotsActivity mesLotsActivity = (MesLotsActivity) classeAppelante;
                                mesLotsActivity.TraiterReponse("rechercheLotsPersonne", response.toString(), false);
                            }
                            else if(action == "RecuperationQuestionsFeedback") {
                                final FeedBackActivity feedBackActivity = (FeedBackActivity) classeAppelante;
                                feedBackActivity.TraiterReponse("recuperationQuestionsFeedback", response.toString(), false);
                            }
                            else if(action == "RecuperationStatistiques") {
                                final StatistiquesActivity statistiquesActivity = (StatistiquesActivity) classeAppelante;
                                statistiquesActivity.TraiterReponse("recuperationStatistiques", response.toString(), false);
                            }
                            else if(action == "RecuperationClassements") {
                                final ClassementsActivity classementsActivity = (ClassementsActivity) classeAppelante;
                                classementsActivity.TraiterReponse("recuperationClassements", response.toString(), false);
                            }
                            else if(action == "RechercheListeInscrits") {
                                final VisualiserListeInscritsActivity visualiserListeInscritsActivity = (VisualiserListeInscritsActivity) classeAppelante;
                                visualiserListeInscritsActivity.TraiterReponse("rechercheListeInscrits", response.toString(), false);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());

                            if (action == "Main") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("recuperationCartons", error.toString(), true);
                            }
                            else if (action == "RecuperationQuines") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("recuperationQuines", error.toString(), true);
                            }
                            else if (action == "RecherchePartie") {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("recherchePartie", error.toString(), true);
                            }
                            else if ((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "RechercheListeInscriptions")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("rechercheListeInscriptions", error.toString(), true);
                            }
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "RechercheListeInscriptions")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("rechercheListeInscriptions", error.toString(), true);
                            }
                            else if((classeAppelante.getClass() == SuppressionCompteActivity.class) && (action == "RechercheListeInscriptions")) {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("rechercheListeInscriptions", error.toString(), true);
                            }
                            // Obtention des informations concernant les lots de la partie.
                            else if(action == "RecuperationInfosLots") {
                                final AjoutLotActivity ajoutLotActivity = (AjoutLotActivity) classeAppelante;
                                ajoutLotActivity.TraiterReponse("recuperationInfosLots", error.toString(), true);
                            }
                            else if(action == "RechercheListeLots") {
                                final VisualiserListeLotsActivity visualiserListeLotsActivity = (VisualiserListeLotsActivity) classeAppelante;
                                visualiserListeLotsActivity.TraiterReponse("rechercheListeLots", error.toString(), true);
                            }
                            else if(action == "RechercheLotsPersonne") {
                                final MesLotsActivity mesLotsActivity = (MesLotsActivity) classeAppelante;
                                mesLotsActivity.TraiterReponse("rechercheLotsPersonne", error.toString(), true);
                            }
                            else if(action == "RecuperationQuestionsFeedback") {
                                final FeedBackActivity feedBackActivity = (FeedBackActivity) classeAppelante;
                                feedBackActivity.TraiterReponse("recuperationQuestionsFeedback", error.toString(), true);
                            }
                            else if(action == "RecuperationStatistiques") {
                                final StatistiquesActivity statistiquesActivity = (StatistiquesActivity) classeAppelante;
                                statistiquesActivity.TraiterReponse("recuperationStatistiques", error.toString(), true);
                            }
                            else if(action == "RecuperationClassements") {
                                final ClassementsActivity classementsActivity = (ClassementsActivity) classeAppelante;
                                classementsActivity.TraiterReponse("recuperationClassements", error.toString(), true);
                            }
                            else if(action == "RechercheListeInscrits") {
                                final VisualiserListeInscritsActivity visualiserListeInscritsActivity = (VisualiserListeInscritsActivity) classeAppelante;
                                visualiserListeInscritsActivity.TraiterReponse("rechercheListeInscrits", error.toString(), true);
                            }

                            //MessageBox msgBox = new MessageBox();
                            //msgBox.show("Erreur", error.toString(), classeAppelante.getApplicationContext());
                        }
                    }
            );

            getRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // add it to the RequestQueue
            queue.add(getRequest);
        }

        return "";
    }
}
