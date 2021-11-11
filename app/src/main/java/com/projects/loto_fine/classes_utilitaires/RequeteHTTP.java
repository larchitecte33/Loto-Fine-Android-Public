package com.projects.loto_fine.classes_utilitaires;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.projects.loto_fine.R;
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

import java.util.HashMap;
import java.util.Map;

// Classe permettant d'envoyer des requêtes HTTP et de recevoir leur réponse.
public class RequeteHTTP {

    private Context context; // Contexte
    private String url; // URL vers laquelle envoyer la requête HTTP.
    private AppCompatActivity classTraitementReponse; // Classe (Activité) qui va traiter la réponse.

    // Constructeur
    public RequeteHTTP(Context context, String url, AppCompatActivity classTraitementReponse) {
        this.context = context;
        this.url = url;
        this.classTraitementReponse = classTraitementReponse;
    }

    /**
     * Fonction utilisée pour envoyer une requête HTTP qui va recevoir une réponse sous forme de JSON simple (pas d'array).
     * @param classeAppelante : Activité qui va traiter la réponse.
     * @param action : Action exécutée.
     * @param method : Méthode HTTP pour envoyer la requête.
     * @param jsonToPostStr : JSON à poster.
     * @param fragmentManager : FragmentManager passé à AccueilActivity.encoderURL pour afficher un message d'erreur.
     * @return ""
     */
    public String traiterRequeteHTTPJSON(AppCompatActivity classeAppelante, String action, String method, String jsonToPostStr,
                                         FragmentManager fragmentManager) {
        // On créé une instance s'il n'en existe pas ou on va chercher l'instance de la RequestQueue s'il en existe une.
        RequestQueue queue = RequestQueueSingleton.getInstance(this.context).getRequestQueue();
        ReponseHTTP reponse = new ReponseHTTP();

        // On encode l'URL
        url = AccueilActivity.encoderURL(url, fragmentManager);
        Log.d("url : ", url);

        // Si la méthode HTTP est GET
        if(method == "GET") {
            // On prépare la requête
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // display response
                            Log.d("Response", response.toString());

                            // Connexion de l'utilisateur à l'application
                            if(action == "Connexion") {
                                final ConnectionActivity connectionActivity = (ConnectionActivity) classeAppelante;
                                connectionActivity.TraiterReponse("connexion", response.toString(), false);
                            }
                            // Récupération des informations de la personne lors de la connexion
                            else if((classeAppelante.getClass() == ConnectionActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final ConnectionActivity connectionActivity = (ConnectionActivity) classeAppelante;
                                connectionActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            // Récupération des informations de la personne lors d'une partie de loto
                            else if((classeAppelante.getClass() == MainActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            // Récupération des informations de la personne lors de la suppression du compte
                            else if((classeAppelante.getClass() == SuppressionCompteActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            // Récupération des informations de la personne lors de l'affichage des statistiques
                            else if((classeAppelante.getClass() == StatistiquesActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final StatistiquesActivity statistiquesActivity = (StatistiquesActivity) classeAppelante;
                                statistiquesActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            // Récupération des informations de la personne lors de la création du compte
                            else if((classeAppelante.getClass() == CreationCompteActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                creationCompteActivity.TraiterReponse("obtentionInfosPersonne", response.toString(), false);
                            }
                            // Récupération d'une quine
                            else if (action == "RecuperationQuine") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("recuperationQuine", response.toString(), false);
                            }
                            // Tirage d'un numéro
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

                            // Connexion de l'utilisateur à l'application
                            if(action == "Connexion") {
                                final ConnectionActivity connectionActivity = (ConnectionActivity) classeAppelante;
                                connectionActivity.TraiterReponse("connexion", error.toString(), true);
                            }
                            // Récupération des informations de la personne lors de la connexion
                            else if((classeAppelante.getClass() == ConnectionActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final ConnectionActivity connectionActivity = (ConnectionActivity) classeAppelante;
                                connectionActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            // Récupération des informations de la personne lors d'une partie de loto
                            else if((classeAppelante.getClass() == MainActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            // Récupération des informations de la personne lors de la suppression du compte
                            else if((classeAppelante.getClass() == SuppressionCompteActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            // Récupération des informations de la personne lors de l'affichage des statistiques
                            else if((classeAppelante.getClass() == StatistiquesActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final StatistiquesActivity statistiquesActivity = (StatistiquesActivity) classeAppelante;
                                statistiquesActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            // Récupération des informations de la personne lors de la création du compte
                            else if((classeAppelante.getClass() == CreationCompteActivity.class) && (action == "ObtentionInfosPersonne")) {
                                final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                creationCompteActivity.TraiterReponse("obtentionInfosPersonne", error.toString(), true);
                            }
                            // Récupération d'une quine
                            else if(action == "RecuperationQuine") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("recuperationQuine", error.toString(), true);
                            }
                            // Tirage d'un numéro
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
            ){
                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String credentials = "toto:password";
                    String auth = "Basic "
                            + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }
            };

            // La requête est répétée un maximum de 3 fois avec un timeout de 5000 ms.
            getRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // On ajoute la requête à la RequestQueue
            queue.add(getRequest);
        }
        // Si la méthode HTTP est POST
        else if(method == "POST") {
            // Envoi des réponses au feedback
            if(action == "EnvoyerReponsesFeedback") {
                JSONArray jsonToPost;

                try {
                    // On essaie de créer un JSONArray à partir du String passé en paramètre.
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

                // La requête est répétée un maximum de 3 fois avec un timeout de 5000 ms.
                postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                // On ajoute la requête à la RequestQueue
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
                                // Enregistrement de la connexion de l'utilisateur à une partie
                                else if ((classeAppelante.getClass() == MainActivity.class) && (action == "EnregistrerConnexionAPartie")) {
                                    final MainActivity mainActivity = (MainActivity) classeAppelante;
                                    mainActivity.TraiterReponse("enregistrerConnexionAPartie", response.toString(), false);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Création d'un compte
                                if (action == "CreationCompte") {
                                    final CreationCompteActivity creationCompteActivity = (CreationCompteActivity) classeAppelante;
                                    creationCompteActivity.TraiterReponse("creationCompte", error.toString(), true);
                                }
                                // Récupération des cartons
                                else if (action == "Main") {
                                    final MainActivity mainActivity = (MainActivity) classeAppelante;
                                    mainActivity.TraiterReponse("recuperationCartons", error.toString(), true);
                                }
                                // Envoi d'une demande de quine par un participant
                                else if (action == "EnvoiQuine") {
                                    final MainActivity mainActivity = (MainActivity) classeAppelante;
                                    mainActivity.TraiterReponse("envoiQuine", error.toString(), true);
                                }
                                // Validation d'une quine par l'animateur
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
                                // Enregistrement de la connexion de l'utilisateur à une partie
                                else if ((classeAppelante.getClass() == MainActivity.class) && (action == "EnregistrerConnexionAPartie")) {
                                    final MainActivity mainActivity = (MainActivity) classeAppelante;
                                    mainActivity.TraiterReponse("enregistrerConnexionAPartie", error.toString(), true);
                                }
                            }
                        }
                );

                // La requête est répétée un maximum de 3 fois avec un timeout de 5000 ms.
                postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                // On ajoute la requête à la RequestQueue
                queue.add(postRequest);
            }
        }
        // Si la méthode HTTP est DELETE
        else if(method == "DELETE") {
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.DELETE, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Désinscription d'une partie depuis RechercherPartieActivity
                            if((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "DesinscriptionPartie")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("desinscriptionPartie", response.toString(), false);
                            }
                            // Désinscription d'une partie depuis MesInscriptionsActivity
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "DesinscriptionPartie")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("desinscriptionPartie", response.toString(), false);
                            }
                            // Suppression de compte
                            else if(action == "SuppressionCompte") {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("suppressionCompte", response.toString(), false);
                            }
                            // Suppression d'un lot
                            else if (action == "SuppressionLot") {
                                final VisualiserListeLotsActivity visualiserListeLotsActivity = (VisualiserListeLotsActivity) classeAppelante;
                                visualiserListeLotsActivity.TraiterReponse("suppressionLot", response.toString(), false);
                            }
                            // Suppression d'une partie depuis RecherchePartieActivity
                            else if((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "SuppressionPartie")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("suppressionPartie", response.toString(), false);
                            }
                            // Suppression d'une partie depuis MesInscriptionsActivity
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "SuppressionPartie")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("suppressionPartie", response.toString(), false);
                            }
                            // Suppression des numeros tirés pour une partie
                            else if (action == "SupprimerNumerosTires") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("suppressionNumerosTires", response.toString(), false);
                            }
                            // Suppression de l'enregistrement de la connexion à une partie
                            else if (action == "SupprimerEnregistrementConnexionAPartie") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("supprimerEnregistrementConnexionAPartie", response.toString(), false);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Désinscription d'une partie depuis RechercherPartieActivity
                            if(action == "DesinscriptionPartie") {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("desinscriptionPartie", error.toString(), true);
                            }
                            // Désinscription d'une partie depuis MesInscriptionsActivity
                            else if(action == "SuppressionCompte") {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("suppressionCompte", error.toString(), true);
                            }
                            // Suppression d'un lot
                            else if (action == "SuppressionLot") {
                                final VisualiserListeLotsActivity visualiserListeLotsActivity = (VisualiserListeLotsActivity) classeAppelante;
                                visualiserListeLotsActivity.TraiterReponse("suppressionLot", error.toString(), true);
                            }
                            // Suppression d'une partie depuis RecherchePartieActivity
                            else if((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "SuppressionPartie")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("suppressionPartie", error.toString(), true);
                            }
                            // Suppression d'une partie depuis MesInscriptionsActivity
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "SuppressionPartie")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("suppressionPartie", error.toString(), true);
                            }
                            // Suppression des numeros tirés pour une partie
                            else if (action == "SupprimerNumerosTires") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("suppressionNumerosTires", error.toString(), true);
                            }
                            // Suppression de l'enregistrement de la connexion à une partie
                            else if (action == "SupprimerEnregistrementConnexionAPartie") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("supprimerEnregistrementConnexionAPartie", error.toString(), true);
                            }
                        }
                    }
            );

            // La requête est répétée un maximum de 3 fois avec un timeout de 5000 ms.
            postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // On ajoute la requête à la RequestQueue
            queue.add(postRequest);
        }
        // Si la méthode HTTP est PUT
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

            // La requête est répétée un maximum de 3 fois avec un timeout de 5000 ms.
            putRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // On ajoute la requête à la RequestQueue
            queue.add(putRequest);
        }

        return "";
    }

    /**
     * Fonction utilisée pour envoyer une requête HTTP qui va recevoir une réponse sous forme d'un array JSON.
     * @param classeAppelante : Activité qui va traiter la réponse.
     * @param action : Action exécutée.
     * @param method : Méthode HTTP pour envoyer la requête.
     * @param fragmentManager : FragmentManager passé à AccueilActivity.encoderURL pour afficher un message d'erreur.
     * @return ""
     */
    public String traiterRequeteHTTPJSONArray(AppCompatActivity classeAppelante, String action, String method,
                                              FragmentManager fragmentManager) {
        // On créé une instance s'il n'en existe pas ou on va chercher l'instance de la RequestQueue s'il en existe une.
        RequestQueue queue = RequestQueueSingleton.getInstance(this.context).getRequestQueue();
        ReponseHTTP reponse = new ReponseHTTP();

        // On encode l'URL
        url = AccueilActivity.encoderURL(url, fragmentManager);

        // Si la méthode HTTP est GET
        if(method == "GET") {
            // On prépare la requête
            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // Récupération des cartons
                            if (action == "Main") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("recuperationCartons", response.toString(), false);
                            }
                            // Récupération des quines
                            else if (action == "RecuperationQuines") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("recuperationQuines", response.toString(), false);
                            }
                            // Recherche de parties
                            else if (action == "RecherchePartie") {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("recherchePartie", response.toString(), false);
                            }
                            // Recherche de la liste des inscriptions depuis RecherchePartieActivity
                            else if ((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "RechercheListeInscriptions")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("rechercheListeInscriptions", response.toString(), false);
                            }
                            // Recherche de la liste des inscriptions depuis MesInscriptionsActivity
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "RechercheListeInscriptions")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("rechercheListeInscriptions", response.toString(), false);
                            }
                            // Recherche de la liste des inscriptions depuis SuppressionCompteActivity
                            else if((classeAppelante.getClass() == SuppressionCompteActivity.class) && (action == "RechercheListeInscriptions")) {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("rechercheListeInscriptions", response.toString(), false);
                            }
                            // Obtention des informations concernant les lots de la partie.
                            else if(action == "RecuperationInfosLots") {
                                final AjoutLotActivity ajoutLotActivity = (AjoutLotActivity) classeAppelante;
                                ajoutLotActivity.TraiterReponse("recuperationInfosLots", response.toString(), false);
                            }
                            // Recherche de la liste des lots mis en jeu dans une partie
                            else if(action == "RechercheListeLots") {
                                final VisualiserListeLotsActivity visualiserListeLotsActivity = (VisualiserListeLotsActivity) classeAppelante;
                                visualiserListeLotsActivity.TraiterReponse("rechercheListeLots", response.toString(), false);
                            }
                            // Recherche des lots gagnés par une personne
                            else if(action == "RechercheLotsPersonne") {
                                final MesLotsActivity mesLotsActivity = (MesLotsActivity) classeAppelante;
                                mesLotsActivity.TraiterReponse("rechercheLotsPersonne", response.toString(), false);
                            }
                            // Récupération des questions du feedback
                            else if(action == "RecuperationQuestionsFeedback") {
                                final FeedBackActivity feedBackActivity = (FeedBackActivity) classeAppelante;
                                feedBackActivity.TraiterReponse("recuperationQuestionsFeedback", response.toString(), false);
                            }
                            // Récupération des statistiques
                            else if(action == "RecuperationStatistiques") {
                                final StatistiquesActivity statistiquesActivity = (StatistiquesActivity) classeAppelante;
                                statistiquesActivity.TraiterReponse("recuperationStatistiques", response.toString(), false);
                            }
                            // Récupération du classement
                            else if(action == "RecuperationClassements") {
                                final ClassementsActivity classementsActivity = (ClassementsActivity) classeAppelante;
                                classementsActivity.TraiterReponse("recuperationClassements", response.toString(), false);
                            }
                            // Recherche de la liste des inscrits pour une partie
                            else if(action == "RechercheListeInscrits") {
                                final VisualiserListeInscritsActivity visualiserListeInscritsActivity = (VisualiserListeInscritsActivity) classeAppelante;
                                visualiserListeInscritsActivity.TraiterReponse("rechercheListeInscrits", response.toString(), false);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Récupération des cartons
                            if (action == "Main") {
                                final MainActivity mainActivity = (MainActivity) classeAppelante;
                                mainActivity.TraiterReponse("recuperationCartons", error.toString(), true);
                            }
                            // Récupération des quines
                            else if (action == "RecuperationQuines") {
                                final GestionPartieActivity gestionPartieActivity = (GestionPartieActivity) classeAppelante;
                                gestionPartieActivity.TraiterReponse("recuperationQuines", error.toString(), true);
                            }
                            // Recherche de parties
                            else if (action == "RecherchePartie") {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("recherchePartie", error.toString(), true);
                            }
                            // Recherche de la liste des inscriptions depuis RecherchePartieActivity
                            else if ((classeAppelante.getClass() == RecherchePartieActivity.class) && (action == "RechercheListeInscriptions")) {
                                final RecherchePartieActivity recherchePartieActivity = (RecherchePartieActivity) classeAppelante;
                                recherchePartieActivity.TraiterReponse("rechercheListeInscriptions", error.toString(), true);
                            }
                            // Recherche de la liste des inscriptions depuis MesInscriptionsActivity
                            else if((classeAppelante.getClass() == MesInscriptionsActivity.class) && (action == "RechercheListeInscriptions")) {
                                final MesInscriptionsActivity mesInscriptionsActivity = (MesInscriptionsActivity) classeAppelante;
                                mesInscriptionsActivity.TraiterReponse("rechercheListeInscriptions", error.toString(), true);
                            }
                            // Recherche de la liste des inscriptions depuis SuppressionCompteActivity
                            else if((classeAppelante.getClass() == SuppressionCompteActivity.class) && (action == "RechercheListeInscriptions")) {
                                final SuppressionCompteActivity suppressionCompteActivity = (SuppressionCompteActivity) classeAppelante;
                                suppressionCompteActivity.TraiterReponse("rechercheListeInscriptions", error.toString(), true);
                            }
                            // Obtention des informations concernant les lots de la partie.
                            else if(action == "RecuperationInfosLots") {
                                final AjoutLotActivity ajoutLotActivity = (AjoutLotActivity) classeAppelante;
                                ajoutLotActivity.TraiterReponse("recuperationInfosLots", error.toString(), true);
                            }
                            // Recherche de la liste des lots mis en jeu dans une partie
                            else if(action == "RechercheListeLots") {
                                final VisualiserListeLotsActivity visualiserListeLotsActivity = (VisualiserListeLotsActivity) classeAppelante;
                                visualiserListeLotsActivity.TraiterReponse("rechercheListeLots", error.toString(), true);
                            }
                            // Recherche des lots gagnés par une personne
                            else if(action == "RechercheLotsPersonne") {
                                final MesLotsActivity mesLotsActivity = (MesLotsActivity) classeAppelante;
                                mesLotsActivity.TraiterReponse("rechercheLotsPersonne", error.toString(), true);
                            }
                            // Récupération des questions du feedback
                            else if(action == "RecuperationQuestionsFeedback") {
                                final FeedBackActivity feedBackActivity = (FeedBackActivity) classeAppelante;
                                feedBackActivity.TraiterReponse("recuperationQuestionsFeedback", error.toString(), true);
                            }
                            // Récupération des statistiques
                            else if(action == "RecuperationStatistiques") {
                                final StatistiquesActivity statistiquesActivity = (StatistiquesActivity) classeAppelante;
                                statistiquesActivity.TraiterReponse("recuperationStatistiques", error.toString(), true);
                            }
                            // Récupération du classement
                            else if(action == "RecuperationClassements") {
                                final ClassementsActivity classementsActivity = (ClassementsActivity) classeAppelante;
                                classementsActivity.TraiterReponse("recuperationClassements", error.toString(), true);
                            }
                            // Recherche de la liste des inscrits pour une partie
                            else if(action == "RechercheListeInscrits") {
                                final VisualiserListeInscritsActivity visualiserListeInscritsActivity = (VisualiserListeInscritsActivity) classeAppelante;
                                visualiserListeInscritsActivity.TraiterReponse("rechercheListeInscrits", error.toString(), true);
                            }
                        }
                    }
            );

            // La requête est répétée un maximum de 3 fois avec un timeout de 5000 ms.
            getRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // On ajoute la requête à la RequestQueue
            queue.add(getRequest);
        }

        return "";
    }
}
