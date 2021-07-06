package com.projects.loto_fine.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;
import com.projects.loto_fine.stomp_client.ClientStomp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

public class AccueilActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    public static void afficherMessage(String message, boolean quitter, FragmentManager fragmentManager) {
        try {
            final ValidationDialogFragment fmdf = new ValidationDialogFragment(message, quitter);
            fmdf.show(fragmentManager, "");
        }
        catch(IllegalStateException e) {
            Log.d("ErrorAffichageMessage", e.getMessage());
        }
    }

    public static String encoderURL(String urlAEncoder, FragmentManager supportFragmentManager) {
        try {
            URL url = new URL(urlAEncoder);
            String query = url.getQuery();
            String urlRetour = "";

            // Si l'url n'a pas de paramètres
            if (query.trim().equals("")) {
                urlRetour = url.toString();
            }
            // Si l'url a des paramètres
            else {
                String hostProtocolePort;
                String host = url.getHost();
                String protocole = url.getProtocol();
                String path = url.getPath();
                int port = url.getPort();

                if (port == -1) {
                    hostProtocolePort = String.format("%s://%s%s", protocole, host, path);
                } else {
                    hostProtocolePort = String.format("%s://%s:%d%s", protocole, host, port, path);
                }

                // On extrait tous les paramètres.
                String[] parametres = query.split("&");
                String parametresEncodes = "";

                // On parcourt tous les paramètres
                for (int i = 0; i < parametres.length; i++) {
                    String parametre = parametres[i];
                    String[] nomValeur = parametre.split("=");

                    if (i > 0) {
                        parametresEncodes = parametresEncodes + "&";
                    }

                    if (nomValeur.length > 1) {
                        parametresEncodes = parametresEncodes + nomValeur[0] + "=" + URLEncoder.encode(nomValeur[1], "UTF-8");
                    } else {
                        parametresEncodes = parametresEncodes + parametre;
                    }
                }

                urlRetour = hostProtocolePort + "?" + parametresEncodes;
            }

            url = new URL(urlRetour);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            urlRetour = uri.toString();

            return urlRetour;
        }
        catch(URISyntaxException e) {
            AccueilActivity.afficherMessage(e.getMessage(), false, supportFragmentManager);
            return urlAEncoder;
        }
        catch(MalformedURLException e) {
            AccueilActivity.afficherMessage(e.getMessage(), false, supportFragmentManager);
            return urlAEncoder;
        }
        catch(UnsupportedEncodingException e) {
            AccueilActivity.afficherMessage(e.getMessage(), false, supportFragmentManager);
            return urlAEncoder;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_accueil);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        Button btnQuitter = (Button)findViewById(R.id.bouton_quitter);
        Button btnMesInscriptions = (Button)findViewById(R.id.bouton_mes_inscriptions);
        Button btnMInscrire = (Button)findViewById(R.id.bouton_minscrire);
        Button btnMeDesinscrire = (Button)findViewById(R.id.bouton_me_desinscrire);
        Button btnMeConnecter = (Button)findViewById(R.id.bouton_me_connecter);
        Button btnParametres = (Button)findViewById(R.id.bouton_parametres);
        Button btnRechercherPartie = (Button)findViewById(R.id.bouton_rechercher_partie);
        Button btnCreerPartie = (Button)findViewById(R.id.bouton_creer_partie);
        Button btnMesLots = (Button)findViewById(R.id.bouton_mes_lots);
        Button btnFeedback = (Button)findViewById(R.id.bouton_feedback);
        TextView tvEtatConnection = (TextView)findViewById(R.id.text_connection);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnQuitter.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnQuitter.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnMesInscriptions.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnMesInscriptions.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnMInscrire.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnMInscrire.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnMeDesinscrire.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnMeDesinscrire.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnMeConnecter.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnMeConnecter.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnParametres.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnParametres.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnRechercherPartie.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnRechercherPartie.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnCreerPartie.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnCreerPartie.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnMesLots.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnMesLots.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnFeedback.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnFeedback.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));


        /*if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            Toast.makeText(getApplicationContext(), "La taille de l'écran est LARGE.", 5).show();
        }
        else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Toast.makeText(getApplicationContext(), "La taille de l'écran est SMALL.", 5).show();
        }
        else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            Toast.makeText(getApplicationContext(), "La taille de l'écran est NORMAL.", 5).show();
        }
        else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
            Toast.makeText(getApplicationContext(), "La taille de l'écran est UNDEFINED.", 5).show();
        }
        else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            Toast.makeText(getApplicationContext(), "La taille de l'écran est LARGE.", 5).show();
        }*/


        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String nomUtilisateur = sharedPref.getString("nomUtilisateur", "anonymous");
        String emailUtilisateur = sharedPref.getString("emailUtilisateur", "");

        /*SharedPreferences.Editor edit = sharedPref.edit();
        edit.clear();

        String adresseServeur = sharedPref.getString("AdresseServeur", "http://localhost:8081");
        edit.putString("AdresseServeur", "http://192.168.1.17:8081");
        edit.apply();*/

        final boolean utilisateurEstConnecte;

        if(nomUtilisateur.equals("anonymous")) {
            tvEtatConnection.setText("Non connecté");
            btnMeConnecter.setText(R.string.texte_me_connecter);

            utilisateurEstConnecte = false;
        }
        else {
            tvEtatConnection.setText("Connecté en tant que " + nomUtilisateur);
            btnMeConnecter.setText(R.string.texte_me_deconnecter);
            utilisateurEstConnecte = true;
        }

        btnQuitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                finishAffinity();
            }
        });

        btnMesInscriptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    Intent intent;
                    intent = new Intent(getApplicationContext(), MesInscriptionsActivity.class);

                    startActivity(intent);
                }
            }
        });

        btnMInscrire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreationCompteActivity.class);
                startActivity(intent);
            }
        });

        btnMeDesinscrire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), SuppressionCompteActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnMeConnecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomUtilisateur = sharedPref.getString("nomUtilisateur", "anonymous");

                // Si l'utilisateur n'est pas connecté, on va ouvrir l'activité de connexion.
                if(nomUtilisateur.equals("anonymous")) {
                    Intent intent = new Intent(getApplicationContext(), ConnectionActivity.class);
                    startActivity(intent);
                }
                // Si l'utilisateur est connecté, on va le déconnecter.
                else {
                    SharedPreferences.Editor edit = sharedPref.edit();
                    //edit.clear();
                    edit.putString("nomUtilisateur", "anonymous");
                    edit.putString("emailUtilisateur", "");
                    edit.commit();

                    btnMeConnecter.setText(R.string.texte_me_connecter);
                    tvEtatConnection.setText("Non connecté");
                }
            }
        });

        btnRechercherPartie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), RecherchePartieActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnCreerPartie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), CreerPartieActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnParametres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ParametresActivity.class);
                startActivity(intent);
            }
        });

        btnMesLots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), MesLotsActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), FeedBackActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if (revenirAAccueil) {
            // On est déjà sur l'accueil.
        }
    }
}