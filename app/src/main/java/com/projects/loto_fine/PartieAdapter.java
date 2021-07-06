package com.projects.loto_fine;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.projects.loto_fine.activites.AccueilActivity;
import com.projects.loto_fine.activites.AjoutLotActivity;
import com.projects.loto_fine.activites.GestionPartieActivity;
import com.projects.loto_fine.activites.MainActivity;
import com.projects.loto_fine.activites.RecherchePartieActivity;
import com.projects.loto_fine.activites.VisualiserListeLotsActivity;
import com.projects.loto_fine.classes_metier.Lot;
import com.projects.loto_fine.classes_metier.NumberPickerDialogFragment;
import com.projects.loto_fine.classes_metier.Partie;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_metier.RequeteHTTP;
import com.projects.loto_fine.classes_metier.ValidationDialogFragment;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.time.temporal.ChronoUnit.*;

public class PartieAdapter extends ArrayAdapter<Partie> implements NumberPicker.OnValueChangeListener {
    private final Context context;
    private LinkedList<Partie> parties;
    private SharedPreferences sharedPref;
    private String adresseServeur, nomActivitySource;
    private int idPartie, idPartieInscription = -1;
    private AppCompatActivity activity;
    private ArrayList<Integer> listeInscriptions;
    private Personne animateur;
    private FragmentManager fragmentManager;

    public PartieAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<Partie> parties,
                         ArrayList<Integer> listeInscriptions, FragmentManager fragmentManager) {
        super(context, ressource, parties);
        this.context = context;
        this.parties = parties;
        this.activity = activity;
        this.listeInscriptions = listeInscriptions;
        this.fragmentManager = fragmentManager;
    }

    public void show() {
        final Dialog dialog = new Dialog(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_recherche_partie_adapter, parent, false);
        }
        else {
            convertView = (LinearLayout)convertView;
        }

        TextView tvdateHeure = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_date_heure);
        TextView tvLieuDeRetrait = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_lieu_retrait);
        TextView tvSommeTotaleLots = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_somme_totale_lots);
        TextView tvPrixCarton = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_prix_carton);
        TextView tvStatutParticipantAnimateur = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_statut_participant_animateur);
        Button btnInscriptionPartie = (Button)convertView.findViewById(R.id.recherche_partie_adapter_btn_inscription_partie);
        Button btnDesinscriptionPartie = (Button)convertView.findViewById(R.id.recherche_partie_adapter_btn_desinscription_partie);
        Button btnMesInscriptionsSeDesinscrire = (Button)convertView.findViewById(R.id.recherche_partie_adapter_bouton_se_desinscrire);
        Button btnMesInscriptionsDemarrerPartie = (Button)convertView.findViewById(R.id.recherche_partie_adapter_bouton_demarrer_partie);
        //Button btnModifierPartie = (Button)convertView.findViewById(R.id.recherche_partie_adapter_btn_modifier_partie);
        LinearLayout layoutDemarrerPartie = (LinearLayout)convertView.findViewById(R.id.recherche_partie_adapter_layout_demarrer_partie);
       // LinearLayout layoutAjouterSupprimerLot = (LinearLayout)convertView.findViewById(R.id.recherche_partie_adapter_layout_ajouter_supprimer_lots);
       // Button btnAjouterLot = (Button)convertView.findViewById(R.id.recherche_partie_adapter_btn_ajouter_lot);
       // Button btnSupprimerLot = (Button)convertView.findViewById(R.id.recherche_partie_adapter_btn_supprimer_lot);
        Button btnVisualiserListeLots = (Button)convertView.findViewById(R.id.recherche_partie_adapter_bouton_visualiser_liste_lots);


        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        btnInscriptionPartie.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnInscriptionPartie.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));
        btnDesinscriptionPartie.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnDesinscriptionPartie.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));
        btnMesInscriptionsSeDesinscrire.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnMesInscriptionsSeDesinscrire.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));
        btnMesInscriptionsDemarrerPartie.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnMesInscriptionsDemarrerPartie.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));
        btnVisualiserListeLots.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnVisualiserListeLots.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));


        idPartie = parties.get(position).getId();
        animateur = parties.get(position).getAnimateur();
        double sommeTotaleLots = 0.0;
        DecimalFormat df = new DecimalFormat("0.00");

        String patternDate = "dd/MM/yyyy HH:mm";
        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern(patternDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        tvdateHeure.setText("Date et heure : " + simpleDateFormat.format(parties.get(position).getDate())); // parties.get(position).getDate().format(dtf));
        tvLieuDeRetrait.setText(parties.get(position).getAdresse() + " " + parties.get(position).getCp() + " " + parties.get(position).getVille());

        ArrayList<Lot> lots = parties.get(position).getLots();

        System.out.println("sommeTotaleLots = " + sommeTotaleLots);

        for(int i = 0 ; i < lots.size() ; i++) {
            sommeTotaleLots += lots.get(i).getValeur();
            System.out.println("sommeTotaleLots = " + sommeTotaleLots);
        }

        df.setRoundingMode(RoundingMode.HALF_UP);

        tvSommeTotaleLots.setText("Somme totale des lots : " + df.format(sommeTotaleLots));
        tvPrixCarton.setText("Prix du carton : " + df.format(parties.get(position).getPrixCarton()) + " €");

        sharedPref = getContext().getSharedPreferences("MyData", Context.MODE_PRIVATE);

        System.out.println("this.activity.getClass().getSimpleName() = " + this.activity.getClass().getSimpleName());
        nomActivitySource = this.activity.getClass().getSimpleName();

        // RecherchePartieActivity
        if(nomActivitySource.equals("RecherchePartieActivity")) {
            // Si l'utilisateur n'est pas inscrit à la partie, alors on affiche le bouton d'inscription et on masque le bouton
            // de désinscription.
            if (listeInscriptions.indexOf(idPartie) == -1) {
                btnInscriptionPartie.setVisibility(View.VISIBLE);
                btnDesinscriptionPartie.setVisibility(View.GONE);
            }
            // Sinon, on masque le bouton d'inscription et on affiche le bouton de désinscription.
            else {
                btnInscriptionPartie.setVisibility(View.GONE);
                btnDesinscriptionPartie.setVisibility(View.VISIBLE);

                // Si l'utilisateur est animateur de la partie, alors on va afficher l'information.
                if(sharedPref.getString("emailUtilisateur", "").equals(animateur.getEmail())) {
                    tvStatutParticipantAnimateur.setText("Vous êtes animateur de cette partie.");
                }
                // Sinon, on va afficher une information indiquant que l'utilisateur est participant.
                else {
                    tvStatutParticipantAnimateur.setText("Vous êtes participant de cette partie.");
                }
            }

            btnMesInscriptionsDemarrerPartie.setVisibility(View.GONE);
            btnMesInscriptionsSeDesinscrire.setVisibility(View.GONE);
            layoutDemarrerPartie.setVisibility(View.GONE);
        }
        // MesInscriptionsActivity
        else {
            String email = sharedPref.getString("emailUtilisateur", "");

            btnInscriptionPartie.setVisibility(View.GONE);
            btnDesinscriptionPartie.setVisibility(View.GONE);

            layoutDemarrerPartie.setVisibility(View.VISIBLE);

            btnMesInscriptionsDemarrerPartie.setVisibility(View.VISIBLE);
            btnMesInscriptionsSeDesinscrire.setVisibility(View.VISIBLE);

            // Si l'utilisateur est animateur de la partie, alors on va afficher l'information.
            if(sharedPref.getString("emailUtilisateur", "").equals(animateur.getEmail())) {
                tvStatutParticipantAnimateur.setText("Vous êtes animateur de cette partie.");
            }
            // Sinon, on va afficher une information indiquant que l'utilisateur est participant.
            else {
                tvStatutParticipantAnimateur.setText("Vous êtes participant de cette partie.");
            }
        }


        // Clic sur le bouton d'inscription à la partie.
        btnInscriptionPartie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPartieInscription = parties.get(position).getId();

                adresseServeur = sharedPref.getString("AdresseServeur", "");

                // Si l'adresse du serveur n'est pas renseignée
                if(adresseServeur.trim().equals("")) {
                    String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";

                    ValidationDialogFragment vdf = new ValidationDialogFragment(messageErreur, false);
                    vdf.show(((RecherchePartieActivity)context).getSupportFragmentManager(), "");
                }
                else {
                    NumberPickerDialogFragment newFragment = new NumberPickerDialogFragment("Sélection du nombre de cartons",
                            "Choisissez un nombre de cartons.");
                    newFragment.setValueChangeListener(PartieAdapter.this::onValueChange);
                    newFragment.show(((RecherchePartieActivity)context).getSupportFragmentManager(), "time picker");
                }
            }
        });

        // Clic sur le bouton de désinscription de la partie
        btnDesinscriptionPartie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPartieInscription = parties.get(position).getId();
                adresseServeur = sharedPref.getString("AdresseServeur", "");

                // Si l'adresse du serveur n'est pas renseignée
                if(adresseServeur.trim().equals("")) {
                    String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";

                    ValidationDialogFragment vdf = new ValidationDialogFragment(messageErreur, false);
                    vdf.show(((RecherchePartieActivity)context).getSupportFragmentManager(), "");
                }
                else {
                    animateur = parties.get(position).getAnimateur();

                    String email = sharedPref.getString("emailUtilisateur", "");
                    String mdp = sharedPref.getString("mdpUtilisateur", "");

                    // Si l'e-mail de l'utilisateur correspond à l'e-mail de l'animateur, alors on va demander la suppression de la partie
                    if(email.equals(animateur.getEmail())) {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur +
                                "/animateur/suppression-partie?email=" + email + "&mdp=" +
                                mdp + "&idPartie=" + idPartieInscription;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getContext(),
                                adresse, activity);
                        requeteHTTP.traiterRequeteHTTPJSON(activity, "SuppressionPartie", "DELETE", "", fragmentManager);
                    }
                    else {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                                "/participant/desinscription-partie?email=" + email + "&mdp=" +
                                mdp + "&idPartie=" + idPartieInscription;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getContext(),
                                adresse, activity);
                        requeteHTTP.traiterRequeteHTTPJSON(activity, "DesinscriptionPartie", "DELETE", "", fragmentManager);
                    }
                }
            }
        });

        // Clic sur le bouton de démarrage de la partie
        btnMesInscriptionsDemarrerPartie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPartieInscription = parties.get(position).getId();
                animateur = parties.get(position).getAnimateur();

                String email = sharedPref.getString("emailUtilisateur", "");
                Intent intent = null;

                // Si l'e-mail de l'utilisateur correspond à l'e-mail de l'animateur, alors on va lancer l'activity de gestion de la partie.
                if(email.equals(animateur.getEmail())) {
                    intent = new Intent(getContext(), GestionPartieActivity.class);
                    intent.putExtra("idPartie", idPartieInscription);
                }
                // Sinon, on lance l'activity de permettant de joueur à la partie de loto.
                else {
                    intent = new Intent(getContext(), MainActivity.class);
                    intent.putExtra("idPartie", idPartieInscription);
                }

                getContext().startActivity(intent);
            }
        });

        btnVisualiserListeLots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPartieInscription = parties.get(position).getId();
                animateur = parties.get(position).getAnimateur();

                Intent intent = new Intent(getContext(), VisualiserListeLotsActivity.class);
                intent.putExtra("idPartie", idPartieInscription);
                intent.putExtra("emailAnimateur", animateur.getEmail());
                intent.putExtra("source", nomActivitySource);
                getContext().startActivity(intent);
            }
        });

        btnMesInscriptionsSeDesinscrire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPartieInscription = parties.get(position).getId();
                adresseServeur = sharedPref.getString("AdresseServeur", "");

                // Si l'adresse du serveur n'est pas renseignée
                if(adresseServeur.trim().equals("")) {
                    String messageErreur = "Veuillez renseigner l'adresse du serveur dans les paramètres.";

                    ValidationDialogFragment vdf = new ValidationDialogFragment(messageErreur, false);
                    vdf.show(((RecherchePartieActivity)context).getSupportFragmentManager(), "");
                }
                else {
                    animateur = parties.get(position).getAnimateur();

                    String email = sharedPref.getString("emailUtilisateur", "");
                    String mdp = sharedPref.getString("mdpUtilisateur", "");

                    // Si l'e-mail de l'utilisateur correspond à l'e-mail de l'animateur, alors on va demander la suppression de la partie
                    if(email.equals(animateur.getEmail())) {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIAnimateur +
                                "/animateur/suppression-partie?email=" + email + "&mdp=" +
                                mdp + "&idPartie=" + idPartieInscription;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getContext(),
                                adresse, activity);
                        requeteHTTP.traiterRequeteHTTPJSON(activity, "SuppressionPartie", "DELETE", "", fragmentManager);
                    }
                    else {
                        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                                "/participant/desinscription-partie?email=" + email + "&mdp=" +
                                mdp + "&idPartie=" + idPartieInscription;
                        RequeteHTTP requeteHTTP = new RequeteHTTP(getContext(),
                                adresse, activity);
                        requeteHTTP.traiterRequeteHTTPJSON(activity, "DesinscriptionPartie", "DELETE", "", fragmentManager);
                    }
                }
            }
        });

        return convertView;
    }

    // Choix du nombre de cartons
    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        // On va chercher l'email et le mot de passe de l'utilisateur dans les SharedPreferences.
        String email = sharedPref.getString("emailUtilisateur", "");
        String mdp = sharedPref.getString("mdpUtilisateur", "");

        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                "/participant/inscription-partie?email=" + email + "&mdp=" +
                mdp + "&idPartie=" + idPartieInscription + "&nbCartons=" + numberPicker.getValue();
        RequeteHTTP requeteHTTP = new RequeteHTTP(getContext(),
                adresse, activity);
        requeteHTTP.traiterRequeteHTTPJSON(activity, "InscriptionPartie", "POST", "", fragmentManager);
    }
}
