package com.projects.loto_fine.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Cette activité est celle qui est affichée lors du lancement de l'application.
 * Elle permet d'accéder aux fonctionnalités suivantes :
 *  - Inscription / Modification compte
 *  - Désinscription
 *  - Connexion / Déconnexion
 *  - Recherche de paries
 *  - Création d'une partie
 *  - Visualisation des inscriptions
 *  - Visualisation des lots
 *  - Visualisation des statistiques
 *  - Visualisation des classements
 *  - Envoi du feedback
 *  - Visualisation et modification des paramètres
 */
public class AccueilActivity extends AppCompatActivity implements ValidationDialogFragment.ValidationDialogListener {

    /**
     * Fonction permettant d'afficher un message à l'utilisateur.
     * @param message : message à afficher
     * @param quitter : true si on doit quitter l'activité actuelle après avoir fermé le message, false sinon
     * @param fragmentManager : fragmentManager s'occupant de l'affichage du message.
     */
    public static void afficherMessage(String message, boolean quitter, FragmentManager fragmentManager) {
        try {
            final ValidationDialogFragment fmdf = new ValidationDialogFragment(message, quitter);
            fmdf.show(fragmentManager, "");
        }
        catch(IllegalStateException e) {
            Log.d("ErrorAffichageMessage", e.getMessage());
        }
    }

    /**
     * Fonction permettant d'encoder une URL
     * @param urlAEncoder : URL à encoder
     * @param supportFragmentManager : fragmentManager permettant d'afficher les messages d'erreur
     * @return l'URL encodé si elle a pu l'être, l'URL non encodée sinon.
     */
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

    /**
     * Fonction permettant d'encoder le & par son code hexadécimal.
     * @param chaineATraiter : chaine dans laquelle rechercher les &
     * @return la chaine avec tous les & encodés.
     */
    public static String encoderECommercial(String chaineATraiter) {
        String chaineRetour = chaineATraiter.replace("&", "%26");
        return chaineRetour;
    }

    /**
     * Fonction appelée quand l'activité est lancée.
     * @param savedInstanceState : si l'activité est réinitialisée après avoir été fermée, ce Bundle contient les données
     *                             qu'il a le plus récemment fournies.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_accueil);

        // Récupération des composants
        Button btnQuitter = (Button)findViewById(R.id.bouton_quitter);
        Button btnMesInscriptions = (Button)findViewById(R.id.bouton_mes_inscriptions);
        Button btnMInscrire = (Button)findViewById(R.id.bouton_minscrire);
        Button btnMeDesinscrire = (Button)findViewById(R.id.bouton_me_desinscrire);
        Button btnMeConnecter = (Button)findViewById(R.id.bouton_me_connecter);
        Button btnParametres = (Button)findViewById(R.id.bouton_parametres);
        Button btnRechercherPartie = (Button)findViewById(R.id.bouton_rechercher_partie);
        Button btnCreerPartie = (Button)findViewById(R.id.bouton_creer_partie);
        Button btnMesLots = (Button)findViewById(R.id.bouton_mes_lots);
        Button btnStatistiques = (Button)findViewById(R.id.bouton_statistiques);
        Button btnClassements = (Button)findViewById(R.id.bouton_classements);
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
        btnStatistiques.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnStatistiques.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnClassements.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnClassements.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        btnFeedback.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        btnFeedback.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));


        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        // On récupère le nom et l'e-mail de l'utilisateur dans les SharedPreferences.
        String nomUtilisateur = sharedPref.getString("nomUtilisateur", "anonymous");
        String emailUtilisateur = sharedPref.getString("emailUtilisateur", "");

        // Si le nom d'utilisateur est anonymous, l'utilisateur est considéré comme étant non connecté.
        if(nomUtilisateur.equals("anonymous")) {
            tvEtatConnection.setText("Non connecté");
            btnMeConnecter.setText(R.string.texte_me_connecter);
            btnMInscrire.setText(R.string.texte_minscrire);
        }
        // Sinon, l'utilisateur est connecté.
        else {
            tvEtatConnection.setText("Connecté en tant que " + nomUtilisateur);
            btnMeConnecter.setText(R.string.texte_me_deconnecter);
            btnMInscrire.setText(R.string.texte_modifier_compte);
        }

        // Clic sur le bouton "QUITTER".
        btnQuitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });

        // Clic sur le bouton "MES INSCRIPTIONS"
        btnMesInscriptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si l'utilisateur n'est pas connecté
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                // Si l'utilisateur est connecté
                else {
                    Intent intent;
                    intent = new Intent(getApplicationContext(), MesInscriptionsActivity.class);

                    startActivity(intent);
                }
            }
        });

        // Clic sur le bouton "M'INSCRIRE"
        btnMInscrire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreationCompteActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le bouton "ME DÉSINSCRIRE"
        btnMeDesinscrire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si l'utilisateur n'est pas connecté
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                // Si l'utilisateur est connecté
                else {
                    Intent intent = new Intent(getApplicationContext(), SuppressionCompteActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Clis sur le bouton "ME CONNECTER"
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
                    // On affecte à la sharedPreference nomUtilisateur la valeur anonymous (utilisateur non connecté).
                    edit.putString("nomUtilisateur", "anonymous");
                    edit.putString("emailUtilisateur", "");
                    edit.commit();

                    btnMeConnecter.setText(R.string.texte_me_connecter);
                    btnMInscrire.setText(R.string.texte_minscrire);
                    tvEtatConnection.setText("Non connecté");
                }
            }
        });

        // Clic sur le bouton "RECHERCHER UNE PARTIE À VENIR"
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

        // Clic sur le bouton "CRÉER UNE PARTIE"
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

        // Clic sur le bouton "PARAMÈTRES"
        btnParametres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ParametresActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le bouton "MES LOTS"
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

        // Clic sur le bouton "STATISTIQUES"
        btnStatistiques.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), StatistiquesActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Clic sur le bouton "CLASSEMENTS"
        btnClassements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nomUtilisateur.equals("anonymous")) {
                    afficherMessage("Vous devez être connecté pour effectuer cette action.", false, getSupportFragmentManager());
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), ClassementsActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Clic sur le bouton "FEEDBACK"
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

    /**
     * Implémentation de la fonction onFinishEditDialog de l'interface ValidationDialogListener.
     * @param revenirAAccueil : true si on doit revenir à l'activité appelante, false sinon.
     */
    @Override
    public void onFinishEditDialog(boolean revenirAAccueil) {
        if (revenirAAccueil) {
            // On est déjà sur l'accueil.
        }
    }
}