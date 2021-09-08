package com.projects.loto_fine.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;

import org.json.JSONException;
import org.json.JSONObject;

public class CreationCompteActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private EditText edSaisirNom, edSaisirPrenom, edSaisirEmail, edSaisirMdp, edSaisirMdp2, edSaisirAdresse, edSaisirCP, edSaisirVille, edSaisirNumTel;
    private Button btnAnnuler, btnValider;
    private boolean isModificationCompte;

    public void TraiterReponse(String source, String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment fmdf = null;

        if(source == "creationCompte") {
            // Si le serveur a renvoyé un erreur lors de la création du compte, alors on l'affiche
            if (isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la création du compte : " + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        message = getResources().getString(R.string.texte_compte_cree);
                        AccueilActivity.afficherMessage(message, true, getSupportFragmentManager());
                    }
                } catch (JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "obtentionInfosPersonne") {
            if (isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la création du compte : " + reponse, false, getSupportFragmentManager());
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
                        edSaisirNom.setText(jo.getString("nom"));
                        edSaisirPrenom.setText(jo.getString("prenom"));
                        edSaisirEmail.setText(jo.getString("email"));
                        //edSaisirMdp.setText(jo.getString("mdp"));
                        //edSaisirMdp2.setText(jo.getString("mdp"));
                        edSaisirAdresse.setText(jo.getString("adresse"));
                        edSaisirCP.setText(jo.getString("cp"));
                        edSaisirVille.setText(jo.getString("ville"));
                        edSaisirNumTel.setText(jo.getString("numTel"));
                    }
                } catch (JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
        else if(source == "modificationCompte") {
            // Si le serveur a renvoyé un erreur lors de la création du compte, alors on l'affiche
            if (isErreur) {
                AccueilActivity.afficherMessage("Erreur lors de la création du compte : " + reponse, false, getSupportFragmentManager());
            } else {
                try {
                    JSONObject jo = new JSONObject(reponse);
                    Object objErreur = jo.opt("erreur");

                    if (objErreur != null) {
                        message = (String) objErreur;
                        AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                    } else {
                        message = getResources().getString(R.string.texte_compte_modifie);
                        AccueilActivity.afficherMessage(message, true, getSupportFragmentManager());
                    }
                } catch (JSONException e) {
                    AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_compte);

        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String adresseServeur = sharedPref.getString("AdresseServeur", "");
        isModificationCompte = false;

        final View activityCreationCompte = findViewById(R.id.edit_saisir_adresse).getRootView();

        edSaisirNom = (EditText) findViewById(R.id.edit_saisir_nom);
        edSaisirPrenom = (EditText) findViewById(R.id.edit_saisir_prenom);
        edSaisirEmail = (EditText) findViewById(R.id.edit_saisir_email);
        edSaisirMdp = (EditText) findViewById(R.id.edit_saisir_mdp);
        edSaisirMdp2 = (EditText) findViewById(R.id.edit_saisir_mdp2);
        edSaisirAdresse = (EditText) findViewById(R.id.edit_saisir_adresse);
        edSaisirCP = (EditText) findViewById(R.id.edit_saisir_cp);
        edSaisirVille = (EditText) findViewById(R.id.edit_saisir_ville);
        edSaisirNumTel = (EditText) findViewById(R.id.edit_saisir_num_tel);
        TextView tvSaisirMdp = (TextView) findViewById(R.id.tv_saisir_mdp);
        TextView tvSaisirMdp2 = (TextView) findViewById(R.id.tv_saisir_mdp2);
        btnAnnuler = (Button) findViewById(R.id.bouton_annuler_creation_compte);
        btnValider = (Button) findViewById(R.id.bouton_valider_creation_compte);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        // Affichage ou masquage des boutons Annuler et Valider.
        activityCreationCompte.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                activityCreationCompte.getWindowVisibleDisplayFrame(r);
                int screenHeight = activityCreationCompte.getRootView().getHeight();

                int keypadHeight = screenHeight - r.bottom;

                Log.d("keypadHeight", "keypadHeight = " + keypadHeight);

                if (keypadHeight > screenHeight * 0.15) {
                    btnAnnuler.setVisibility(View.INVISIBLE);
                    btnValider.setVisibility(View.INVISIBLE);
                }
                else {
                    btnAnnuler.setVisibility(View.VISIBLE);
                    btnValider.setVisibility(View.VISIBLE);
                }
            }
        });

        // Clic sur le bouton annuler
        btnAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le bouton valider
        btnValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageErreur = "";

                String mdp = edSaisirMdp.getText().toString().trim();
                String verifMdp = edSaisirMdp2.getText().toString().trim();
                String nom = edSaisirNom.getText().toString().trim();

                if(nom.compareTo("") == 0) {
                    messageErreur = "Le champ 'nom' doit être renseigné.";
                }
                else if(edSaisirPrenom.getText().toString().trim().compareTo("") == 0) {
                    messageErreur = "Le champ 'prénom' doit être renseigné.";
                }
                else if(edSaisirEmail.getText().toString().trim().compareTo("") == 0) {
                    messageErreur = "Le champ 'e-mail' doit être renseigné.";
                }
                else if((!isModificationCompte) && (mdp.compareTo("") == 0)) {
                    messageErreur = "Le champ 'mot de passe' doit être renseigné.";
                }
                else if(isModificationCompte && (mdp.compareTo("") == 0)) {
                    messageErreur = "Le champ 'ancien mdp' doit être renseigné.";
                }
                else if(edSaisirAdresse.getText().toString().trim().compareTo("") == 0) {
                    messageErreur = "Le champ 'adresse' doit être renseigné.";
                }
                else if(edSaisirCP.getText().toString().trim().compareTo("") == 0) {
                    messageErreur = "Le champ 'code postal' doit être renseigné.";
                }
                else if(edSaisirVille.getText().toString().trim().compareTo("") == 0) {
                    messageErreur = "Le champ 'ville' doit être renseigné.";
                }
                else if(edSaisirNumTel.getText().toString().trim().compareTo("") == 0) {
                    messageErreur = "Le champ 'numéro de téléphone' doit être renseigné.";
                }
                // Si on est en création de compte, on vérifie que les champs mdp et verifMdp ont la même valeur.
                else if((!isModificationCompte) && (mdp.compareTo(verifMdp) != 0)) {
                    messageErreur = "Les champs 'mot de passe' et 'vérification mot de passe' diffèrent. " +
                            mdp + " - " +
                            verifMdp;
                }

                if(messageErreur != "") {
                    AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                }
                else {
                    // Si l'adresse du serveur n'est pas renseignée
                    if(adresseServeur.trim().equals("")) {
                        messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
                        AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                    }
                    else {
                        // Si on modifie le compte
                        if(isModificationCompte) {

                            if(verifMdp.compareTo("") == 0) {
                                edSaisirMdp2.setText(edSaisirMdp.getText());
                            }

                            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/modifier-compte?" +
                                    "email=" + AccueilActivity.encoderECommercial(edSaisirEmail.getText().toString()) +
                                    "&mdp=" + AccueilActivity.encoderECommercial(edSaisirMdp.getText().toString()) +
                                    "&nouveauMdp=" + AccueilActivity.encoderECommercial(edSaisirMdp2.getText().toString()) +
                                    "&nom=" + AccueilActivity.encoderECommercial(edSaisirNom.getText().toString()) +
                                    "&prenom=" + AccueilActivity.encoderECommercial(edSaisirPrenom.getText().toString()) +
                                    "&adresse=" + AccueilActivity.encoderECommercial(edSaisirAdresse.getText().toString()) +
                                    "&cp=" + AccueilActivity.encoderECommercial(edSaisirCP.getText().toString()) +
                                    "&ville=" + AccueilActivity.encoderECommercial(edSaisirVille.getText().toString()) +
                                    "&numTel=" + AccueilActivity.encoderECommercial(edSaisirNumTel.getText().toString());
                            RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                                    adresse, CreationCompteActivity.this);
                            requeteHTTP.traiterRequeteHTTPJSON(CreationCompteActivity.this, "ModificationCompte", "PUT", "", getSupportFragmentManager());
                        }
                        else {
                            String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/add?nom=" +
                                    AccueilActivity.encoderECommercial(edSaisirNom.getText().toString()) +
                                    "&prenom=" + AccueilActivity.encoderECommercial(edSaisirPrenom.getText().toString()) +
                                    "&adresse=" + AccueilActivity.encoderECommercial(edSaisirAdresse.getText().toString()) +
                                    "&cp=" + AccueilActivity.encoderECommercial(edSaisirCP.getText().toString()) +
                                    "&ville=" + AccueilActivity.encoderECommercial(edSaisirVille.getText().toString()) +
                                    "&email=" + AccueilActivity.encoderECommercial(edSaisirEmail.getText().toString()) +
                                    "&numTel=" + AccueilActivity.encoderECommercial(edSaisirNumTel.getText().toString()) +
                                    "&mdp=" + AccueilActivity.encoderECommercial(edSaisirMdp.getText().toString().toString());
                            Log.d("Adresse = ", adresse);

                            RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                                    adresse, CreationCompteActivity.this);
                            requeteHTTP.traiterRequeteHTTPJSON(CreationCompteActivity.this, "CreationCompte", "POST", "", getSupportFragmentManager());
                        }
                    }
                }
            }
        });

        if(adresseServeur.trim().equals("")) {
            AccueilActivity.afficherMessage("Veuillez renseigner l'adresse du serveur dans les paramètres.", true, getSupportFragmentManager());
        }
        else {
            // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
            String email = sharedPref.getString("emailUtilisateur", "");

            // Si l'adresse e-mail de l'utilisateur est renseignée, alors on va aller chercher les informations de l'utilisateur.
            if(email.trim().length() != 0) {
                isModificationCompte = true;

                tvSaisirMdp.setText("Ancien mdp : ");
                tvSaisirMdp2.setText("Nouveau mdp : ");
                edSaisirMdp.setHint("Saisissez votre mdp actuel");
                edSaisirMdp2.setHint("Saisissez votre nouveau mdp");

                String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/get_infos_personne?email=" + email;
                RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                        adresse, CreationCompteActivity.this);
                requeteHTTP.traiterRequeteHTTPJSON(CreationCompteActivity.this, "ObtentionInfosPersonne", "GET", "", getSupportFragmentManager());
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("nomUtilisateur", edSaisirNom.getText().toString());
        outState.putString("prenomUtilisateur", edSaisirPrenom.getText().toString());
        outState.putString("emailUtilisateur", edSaisirEmail.getText().toString());
        outState.putString("mdpUtilisateur", edSaisirMdp.getText().toString());
        outState.putString("mdp2Utilisateur", edSaisirMdp2.getText().toString());
        outState.putString("adresseUtilisateur", edSaisirAdresse.getText().toString());
        outState.putString("cpUtilisateur", edSaisirCP.getText().toString());
        outState.putString("villeUtilisateur", edSaisirVille.getText().toString());
        outState.putString("numTelUtilisateur", edSaisirNumTel.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        edSaisirNom.setText(savedInstanceState.getString("nomUtilisateur"));
    }

    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if(revenirAAccueil) {
            Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
            startActivity(intent);
        }
    }
}