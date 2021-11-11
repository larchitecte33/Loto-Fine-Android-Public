package com.projects.loto_fine.stomp_client;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.projects.loto_fine.activites.AccueilActivity;
import com.projects.loto_fine.activites.GestionPartieActivity;
import com.projects.loto_fine.activites.MainActivity;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.constantes.Constants;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

// Classe qui permet d'utiliser un client STOMP (Simple Text Orientated Messaging Protocol) de fa�on � s'abonner � des topics et ainsi
// recevoir les message qui sont envoy�s vers ces topics.
public class ClientStomp {
    private StompClient stompClient; // StompClient impl�ment� par NaikSoftware.
    private CompositeDisposable compositeDisposable; // CompositeDisposable dans lequel on va ajouter tous nos disposables.
    private int idPartie; // Identifiant de la partie
    private String adresseServeur; // Adresse du serveur
    private FragmentManager supportFragmentManager; // FragmentManager utilis� pour l'affichage de messages
    private SharedPreferences sharedPref; // Utilis� pour r�cup�r� l'e-mail et le mot de passe de l'utilisateur stock�s dans les SharedPreferences
    private Context context;
    private MainActivity mainActivity;
    private GestionPartieActivity gestionPartieActivity;

    // Constructeur
    public ClientStomp(Context context, int idPartie, MainActivity mainActivity, GestionPartieActivity gestionPartieActivity,
                       String adresseServeur, FragmentManager supportFragmentManager, SharedPreferences sharedPref) {
        String adresseSocket;

        this.idPartie = idPartie;
        this.supportFragmentManager = supportFragmentManager;
        this.sharedPref = sharedPref;
        this.context = context;
        this.mainActivity = mainActivity;
        this.gestionPartieActivity = gestionPartieActivity;
        this.adresseServeur = adresseServeur;

        adresseSocket = "";

        // Pour acc�der � la WebSocket, on va remplacer http:// ou https:// par ws://
        if(this.adresseServeur.startsWith("http://")) {
            adresseSocket = adresseServeur.replace("http://", "ws://");
        }
        else if(adresseServeur.startsWith("https://")) {
            adresseSocket = adresseServeur.replace("https://", "ws://");
        }
        else {
            adresseSocket = adresseServeur;
        }

        adresseSocket = adresseSocket + ":" + Constants.portWebSocket + "/lotofine-websocket";

        // On cr�e une connexion � la WebSocket via STOMP.
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, adresseSocket);

        stompClient.withClientHeartbeat(1000).withServerHeartbeat(1000);

        // On annule les subscriptions existantes.
        resetSubscriptions();

        // Disposable : interface repr�sentant une ressource qui peut �tre supprim�e.
        Disposable dispLifecycle = stompClient.lifecycle()
                // Schedulers.io() : Renvoie une instance de planificateur partag�e par d�faut destin�e au travail li� aux E/S.
                // Cela peut �tre utilis� pour effectuer de mani�re asynchrone des E/S bloquantes
                .subscribeOn(Schedulers.io())
                // Modifie un Publisher pour effectuer ses �missions et notifications sur un Scheduler sp�cifi�, de mani�re asynchrone
                // avec un tampon limit� de slots bufferSize().
                .observeOn(AndroidSchedulers.mainThread())
                // S'abonne � un Publisher et fournit un rappel pour g�rer les �l�ments qu'il �met.
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Toast.makeText(context, "Stomp connection opened", 5000);

                            if(mainActivity != null) {
                                // Quand on va ouvrir la connexion au client STOMP, on va enregistrer cette connexion dans la base.
                                enregistrerConnexion();
                            }

                            break;
                        case ERROR:
                            Log.e("AAA", "Stomp connection error", lifecycleEvent.getException());
                            Toast.makeText(context, "Stomp connection error", 5000);
                            break;
                        case CLOSED:
                            Toast.makeText(context, "Stomp connection closed", 5000);
                            // On annule les subscriptions existantes.
                            resetSubscriptions();

                            if(mainActivity != null) {
                                // Quand on va fermer la connexion au client STOMP, on va supprimer l'enregistrement de cette connexion dans la base.
                                supprimerEnregistrementConnexion();
                            }

                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Toast.makeText(context, "Stomp failed server heartbeat", 5000);
                            break;
                    }
                });

        // On ajoute le disposable dans compositeDisposable.
        compositeDisposable.add(dispLifecycle);

        // Si le client STOMP a �t� initialis� par MainActivity
        if(mainActivity != null) {
            // On cr�� le topic pour l'obtention des information de la partie (num�ro en-cours, lot en-cours et lot au carton plein ou � la ligne).
            Disposable dispTopic = stompClient.topic("/topic/getInfosPartie/" + this.idPartie)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topicMessage -> {
                        Log.d("AAA", "Received " + topicMessage.getPayload());

                        // Si un message arrive depuis la WebSocket
                        if (!topicMessage.getPayload().equals("")) {
                            JsonObject jsonObject = new JsonParser().parse(topicMessage.getPayload()).getAsJsonObject();
                            mainActivity.setNumeroTire(jsonObject.get("numeroEnCours").getAsInt());
                            mainActivity.setLotEnCours(jsonObject.get("lotEnCours").getAsString());
                            mainActivity.setIsLotCartonPlein(jsonObject.get("lotCartonPlein").getAsBoolean());
                            mainActivity.rafraichirInfosNumeroLot();
                        }
                    }, throwable -> {
                        Log.e("AAA", "Error on subscribe topic", throwable);
                    });

            // On ajoute le topic dans compositeDisposable.
            compositeDisposable.add(dispTopic);
        }

        // Si le client STOMP a �t� initialis� par GestionPartieActivity
        if(gestionPartieActivity != null) {
            // On cr�e un topic pour la r�cup�ration de l'id d'un participant nouvellement connect� � la partie.
            Disposable dispTopic2 = stompClient.topic("/topic/getConnexionPartie/" + this.idPartie)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topicMessage -> {
                        if (!topicMessage.getPayload().equals("")) {
                            int idPersonne = Integer.valueOf(topicMessage.getPayload());
                            // On ajoute l'identifiant de la personne nouvellement connect�e � la liste des identifiants des personnes connect�es.
                            gestionPartieActivity.addIdPersonneConnectee(idPersonne);
                        }
                    });

            // On ajoute le topic dans compositeDisposable.
            compositeDisposable.add(dispTopic2);

            // On cr�e un topic pour la r�cup�ration de l'id d'un participant qui vient de se d�connecter de la partie.
            Disposable dispTopic3 = stompClient.topic("/topic/getDeconnexionPartie/" + this.idPartie)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topicMessage -> {
                        if (!topicMessage.getPayload().equals("")) {
                            int idPersonne = Integer.valueOf(topicMessage.getPayload());
                            // On supprime l'identifiant de la personne qui vient de se d�connecter de la liste des identifiants des personnes connect�es.
                            gestionPartieActivity.deleteIdPersonneConnectee(idPersonne);
                        }
                    });

            // On ajoute le topic dans compositeDisposable.
            compositeDisposable.add(dispTopic2);
        }

        // On se connecte � la WebSocket.
        stompClient.connect();
    }

    // Fonction permettant d'annuler toutes les connexions � des topics.
    private void resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }

        compositeDisposable = new CompositeDisposable();
    }

    // Fonction permettant de d�connecter le client STOMP du serveur.
    public void deconnecterClientStomp() {
        if(stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }

    // Retourne le statut de connexion du client STOMP.
    public boolean isConnected() {
        return stompClient.isConnected();
    }

    // Fonction permettant d'enregistrer la connexion du client � une partie.
    public void enregistrerConnexion() {
        // Si l'adresse du serveur n'est pas renseign�e, on affiche un message.
        if(this.adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les param�tres.";
            AccueilActivity.afficherMessage(messageErreur, false, this.supportFragmentManager);
        }
        else {
            // On va chercher l'e-mail et le mot de passe dans les SharedPreferences.
            String email = this.sharedPref.getString("emailUtilisateur", "");
            String mdp = this.sharedPref.getString("mdpUtilisateur", "");

            // On envoie un requ�te permettant d'enregistrer la connexion de l'utilisateur � une partie.
            String adresse = this.adresseServeur + ":" + Constants.portMicroserviceGUIParticipant + "/participant/enregistrer-connexion-a-partie?email=" +
                    email + "&mdp=" + mdp + "&idPartie=" + this.idPartie;
            RequeteHTTP requeteHTTP = new RequeteHTTP(context, adresse, this.mainActivity);
            requeteHTTP.traiterRequeteHTTPJSON(this.mainActivity, "EnregistrerConnexionAPartie", "POST", "", this.supportFragmentManager);
        }
    }

    // Fonction permettant de supprimer l'enregistrement de la connexion du client � une partie.
    public void supprimerEnregistrementConnexion() {
        // Si l'adresse du serveur n'est pas renseign�e, on affiche un message.
        if(this.adresseServeur.trim().equals("")) {
            String messageErreur = "Veuillez renseigner l'adresse du serveur dans les param�tres.";
            AccueilActivity.afficherMessage(messageErreur, false, this.supportFragmentManager);
        }
        else {
            // On va chercher l'e-mail et le mot de passe dans les SharedPreferences.
            String email = this.sharedPref.getString("emailUtilisateur", "");
            String mdp = this.sharedPref.getString("mdpUtilisateur", "");

            // On envoie un requ�te permettant de supprimer l'enregistrement de la connexion du client � une partie.
            String adresse = this.adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                    "/participant/supprimer-enregistrement-connexion-a-partie?email=" + email + "&mdp=" + mdp + "&idPartie=" + this.idPartie;
            RequeteHTTP requeteHTTP = new RequeteHTTP(context, adresse, this.mainActivity);
            requeteHTTP.traiterRequeteHTTPJSON(this.mainActivity, "SupprimerEnregistrementConnexionAPartie", "DELETE", "", this.supportFragmentManager);
        }
    }
}
