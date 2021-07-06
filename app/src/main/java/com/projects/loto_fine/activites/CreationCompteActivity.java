package com.projects.loto_fine.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.projects.loto_fine.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Cryptage;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;
import com.projects.loto_fine.classes_metier.RequeteHTTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class CreationCompteActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    private EditText edSaisirNom, edSaisirPrenom, edSaisirEmail, edSaisirMdp, edSaisirMdp2, edSaisirAdresse, edSaisirCP, edSaisirVille, edSaisirNumTel;
    private Button btnAnnuler, btnValider;

    public void TraiterReponse(String reponse, boolean isErreur) {
        String message;
        ValidationDialogFragment fmdf = null;

        // Si le serveur a renvoyé un erreur lors de la création du compte, alors on l'affiche
        if(isErreur) {
            AccueilActivity.afficherMessage("Erreur lors de la création du compte : " + reponse, false, getSupportFragmentManager());
        }
        else {
            try {
                JSONObject jo = new JSONObject(reponse);
                Object objErreur = jo.opt("erreur");

                if (objErreur != null) {
                    message = (String)objErreur;
                    AccueilActivity.afficherMessage(message, false, getSupportFragmentManager());
                } else {
                    message = getResources().getString(R.string.texte_compte_cree);
                    AccueilActivity.afficherMessage(message, true, getSupportFragmentManager());
                }
            }
            catch(JSONException e) {
                AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_compte);

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
        btnAnnuler = (Button) findViewById(R.id.bouton_annuler_creation_compte);
        btnValider = (Button) findViewById(R.id.bouton_valider_creation_compte);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        activityCreationCompte.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                /*int diffHeight = activityCreationCompte.getRootView().getHeight() - activityCreationCompte.getHeight();
                float htPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400,
                        getResources().getDisplayMetrics());

                Toast.makeText(getApplicationContext(), "diffHeight = " + diffHeight + ", htPx = " + htPx, Toast.LENGTH_LONG).show();

                if(diffHeight > htPx) {
                    Toast.makeText(getApplicationContext(), "Cacher boutons", Toast.LENGTH_LONG).show();
                }*/

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
                else if(mdp.compareTo("") == 0) {
                    messageErreur = "Le champ 'mot de passe' doit être renseigné.";
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
                else if(mdp.compareTo(verifMdp) != 0) {
                    messageErreur = "Les champs 'mot de passe' et 'vérification mot de passe' diffèrent. " +
                            mdp + " - " +
                            verifMdp;
                }

                if(messageErreur != "") {
                    AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                }
                else {
                    SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    Cryptage cryptage = new Cryptage();

                    String adresseServeur = sharedPref.getString("AdresseServeur", "");
                    //adresseServeur = "http://192.168.1.17:8081";

                    // Si l'adresse du serveur n'est pas renseignée
                    if(adresseServeur.trim().equals("")) {
                        messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";
                        AccueilActivity.afficherMessage(messageErreur, false, getSupportFragmentManager());
                    }
                    else {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/add?nom=" + edSaisirNom.getText() + "&prenom=" + edSaisirPrenom.getText() +
                                "&adresse=" + edSaisirAdresse.getText() + "&cp=" + edSaisirCP.getText() + "&ville=" + edSaisirVille.getText() +
                                "&email=" + edSaisirEmail.getText() + "&numTel=" + edSaisirNumTel.getText() +
                                "&mdp=" + edSaisirMdp.getText().toString(); // cryptage.getPbkdf2()
                        Log.d("Adresse = ", adresse);

                        RequeteHTTP requeteHTTP = new RequeteHTTP(getBaseContext(),
                                adresse, CreationCompteActivity.this);
                        requeteHTTP.traiterRequeteHTTPJSON(CreationCompteActivity.this, "CreationCompte", "POST", "", getSupportFragmentManager()); //
                    }
                }
            }
        });

        //RequeteHTTP requeteHTTP = new RequeteHTTP(this.getBaseContext(), "http://192.168.1.17:8080/cartons/2", this);
        //requeteHTTP.traiterRequeteHTTPJSONArray(this, "CreationCompte");
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