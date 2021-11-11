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

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.adapters.LotRemporteAdapter;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Lot;
import com.projects.loto_fine.classes_utilitaires.OuiNonDialogFragment;
import com.projects.loto_fine.classes_metier.Partie;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Cette activité est affichée lors du clic sur le bouton MES LOTS de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de consulter la liste des lots qu'il a remporté.
 */
public class MesLotsActivity extends AppCompatActivity implements OuiNonDialogFragment.OuiNonDialogListener,
        ValidationDialogFragment.ValidationDialogListener {

    private ListView listLots; // ListView affichant les lots.
    private SharedPreferences sharedPref; // SharedPreferences utilisé pour récupérer des valeurs stockées sur le téléphone/tablette.
    private String adresseServeur, adresseRetraitLot, cpRetraitLot, villeRetraitLot;
    private int idLotARetirer; // Identifiant du lot à retirer.
    private LinkedList<Lot> lots = new LinkedList<>(); // Liste de lots.
    private LinearLayout layoutAttente; // Layout affichant un message d'attente pendant le chargement des lots.

    /**
     * Fonction qui traite les réponses aux requêtes HTTP.
     * Réponses traitées : rechercheLotsPersonne et definirLieuRetraitLot.
     * @param source : action ayant exécutée la requête.
     * @param reponse : reponse à la requête.
     * @param isErreur : y a-t-il eu une erreur lors de l'envoi de la requête.
     */
    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment vdf = null;
        JSONObject jo = null;
        JSONArray ja = null;

        if (source == "rechercheLotsPersonne") {
            // On cache le layout qui affiche le message d'attente.
            layoutAttente.setVisibility(View.GONE);
            // On affiche la liste des lots.
            listLots = (ListView)findViewById(R.id.mes_lots_scroll_lots);
            listLots.setVisibility(View.VISIBLE);

            // S'il y a une erreur lors de la recherche de la liste des lots, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    int idLot, positionLot;
                    String nomLot, dateHeurePartieStr;
                    double valeurLot;
                    boolean isAuCartonPlein, isEnCoursDeJeu, isExceptionDateHeurePartie;
                    Date dateHeurePartie = null;

                    // On tente de caster la réponse en JSONArray.
                    ja = new JSONArray(reponse);
                    JSONObject joPartie;
                    boolean isErreurTrouvee = false;

                    // Si la réponse ne contient qu'un seul enregistrement, c'est peut-être une erreur.
                    if(ja.length() == 1) {
                        jo = ja.getJSONObject(0);

                        // Si l'enregistrement correspond à une erreur, on l'affiche.
                        if(jo.optString("erreur") != null) {
                            isErreurTrouvee = true;
                            message = "Erreur : " + jo.optString("erreur");
                            AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                        }
                    }

                    // Si on a pas encor affiché d'erreur, on peut continuer.
                    if(!isErreurTrouvee) {
                        // On parcourt la liste des lots dans le JSONArray.
                        for (int i = 0; i < ja.length(); i++) {
                            Partie partie;

                            // On va chercher le JSONObject correspondant au lot en cours de parcours.
                            jo = ja.getJSONObject(i);

                            // On va chercher tous les attributs du lot.
                            idLot = jo.getInt("id");
                            nomLot = jo.getString("nom");
                            valeurLot = jo.getDouble("valeur");
                            positionLot = jo.getInt("position");

                            if (jo.getInt("cartonPlein") == 1)
                                isAuCartonPlein = true;
                            else
                                isAuCartonPlein = false;

                            if (jo.getInt("enCoursDeJeu") == 1)
                                isEnCoursDeJeu = true;
                            else
                                isEnCoursDeJeu = false;

                            joPartie = jo.getJSONObject("partie");

                            if (joPartie != null) {
                                dateHeurePartieStr = joPartie.getString("date");
                                isExceptionDateHeurePartie = false;

                                try {
                                    dateHeurePartie = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(dateHeurePartieStr);
                                } catch (ParseException e) {
                                    isExceptionDateHeurePartie = true;
                                }

                                if (!isExceptionDateHeurePartie) {
                                    partie = new Partie(joPartie.getInt("id"),
                                            dateHeurePartie,
                                            joPartie.getString("adresse"), joPartie.getString("ville"), joPartie.getString("cp"), null,
                                            joPartie.getDouble("prixCarton"), null);
                                } else {
                                    partie = null;
                                }
                            } else {
                                partie = null;
                            }

                            // On construit un objet lot avec tous les attributs récupérés dans le JSOObject.
                            Lot lot = new Lot(idLot, nomLot, valeurLot, positionLot, isAuCartonPlein, isEnCoursDeJeu, partie);
                            // On affecte les attributs qui ne font pas partie du constructeur.
                            lot.setAdresseRetrait(jo.getString("adresseRetrait"));
                            lot.setCpRetrait(jo.getString("cpRetrait"));
                            lot.setVilleRetrait(jo.getString("villeRetrait"));
                            // On ajoute le lot à la liste des lots.
                            lots.add(lot);
                        }

                        // On construit un ArrayAdapter de type LotRemporteAdapter et on l'affecte à la vue listLots.
                        LotRemporteAdapter adapter = new LotRemporteAdapter(this, this, R.layout.activity_mes_lots_adapter, lots);
                        listLots.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                } catch (Exception e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "definirLieuRetraitLot") {
            // S'il y a une erreur lors de la définition du lot de retrait du lot, on l'affiche.
            if (isErreur) {
                message = "Une erreur est survenue : ";
                AccueilActivity.afficherMessage(message + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    // On tente de caster la réponse en JSONObject.
                    jo = new JSONObject(reponse);

                    Object objErreur = jo.opt("erreur");

                    // Si la réponse correspond à une erreur, on l'affiche.
                    if(objErreur != null) {
                        AccueilActivity.afficherMessage((String)objErreur, false, getSupportFragmentManager());
                    }
                    else {
                        try {
                            // On parcourt la liste des lots.
                            for(int i = 0 ; i < lots.size() ; i++) {
                                // Si l'identifiant du lot parcouru est égal à l'identifiant du lot à retirer, alors on modifie son adresse de retrait,
                                // son code postal de retrait et sa ville de retrait avec les
                                if(lots.get(i).getId() == idLotARetirer) {
                                    lots.get(i).setAdresseRetrait(adresseRetraitLot);
                                    lots.get(i).setCpRetrait(cpRetraitLot);
                                    lots.get(i).setVilleRetrait(villeRetraitLot);
                                }
                            }

                            ((LotRemporteAdapter) listLots.getAdapter()).notifyDataSetChanged();
                        }
                        catch(Exception e1) {
                            AccueilActivity.afficherMessage(e1.getMessage(), false, getSupportFragmentManager());
                        }
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

        setContentView(R.layout.activity_mes_lots);

        Button btnRetour = (Button) findViewById(R.id.mes_lots_btn_retour);
        layoutAttente = findViewById(R.id.mes_lots_layout_attente);


        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnRetour.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnRetour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));


        sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        adresseServeur = sharedPref.getString("AdresseServeur", "");

        // ********************* Récupération des lots de la personne *************************** //
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
                        "/participant/obtenir-lots-personne?email=" + AccueilActivity.encoderECommercial(email) +
                        "&mdp=" + AccueilActivity.encoderECommercial(mdp);
                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(),
                        adresse, MesLotsActivity.this);
                requeteHTTP.traiterRequeteHTTPJSONArray(MesLotsActivity.this, "RechercheLotsPersonne", "GET", getSupportFragmentManager());
            }
        }

        btnRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onFinishEditDialog(String nomActionOui, String nomActionNon, boolean isChoixOui, HashMap<String, String> args) {
        // Si choix = Oui
        if(isChoixOui) {
            // Retirer sur place
            if(nomActionOui == "retirerLotAAdressePartie") {
                // Arguments envoyés lors du clic sur le bouton Retirer sur place de LotRemporteAdapter.
                idLotARetirer = Integer.valueOf(args.get("idLot"));
                adresseRetraitLot = args.get("adresseRetrait");
                cpRetraitLot = args.get("cpRetrait");
                villeRetraitLot = args.get("villeRetrait");

                String email = sharedPref.getString("emailUtilisateur", "");
                String mdp = sharedPref.getString("mdpUtilisateur", "");

                // On envoie une requête permettant la mise à jour du lieu de retrait du lot.
                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/definir-lieu-retrait?email=" + email +
                        "&mdp=" + mdp + "&idLot=" + args.get("idLot") + "&adresse=" + adresseRetraitLot +
                        "&cp=" + cpRetraitLot + "&ville=" + villeRetraitLot;

                RequeteHTTP requeteHTTP = new RequeteHTTP(getApplicationContext(), adresse, MesLotsActivity.this);
                requeteHTTP.traiterRequeteHTTPJSON(MesLotsActivity.this, "DefinirLieuRetraitLot", "POST", "", getSupportFragmentManager());
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