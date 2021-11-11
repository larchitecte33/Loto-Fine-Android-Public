package com.projects.loto_fine.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.projects.loto_fine.activites.AccueilActivity;
import com.projects.loto_fine.activites.DescriptionMethodeReglementActivity;
import com.projects.loto_fine.activites.MesInscriptionsActivity;
import com.projects.loto_fine.activites.VisualiserListeInscritsActivity;
import com.projects.loto_fine.classes_metier.Inscription;
import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.activites.GestionPartieActivity;
import com.projects.loto_fine.activites.MainActivity;
import com.projects.loto_fine.activites.RecherchePartieActivity;
import com.projects.loto_fine.activites.VisualiserListeLotsActivity;
import com.projects.loto_fine.classes_metier.Lot;
import com.projects.loto_fine.classes_utilitaires.NumberPickerDialogFragment;
import com.projects.loto_fine.classes_metier.Partie;
import com.projects.loto_fine.classes_metier.Personne;
import com.projects.loto_fine.classes_utilitaires.RequeteHTTP;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * ArrayAdapter permettant l'affichage des éléments de la liste des parties.
 */
public class PartieAdapter extends ArrayAdapter<Partie> implements NumberPicker.OnValueChangeListener {
    private final Context context;
    private LinkedList<Partie> parties; // Liste des parties à afficher.
    private SharedPreferences sharedPref;
    private String adresseServeur, nomActivitySource;
    private int idPartie, idPartieInscription = -1;
    private AppCompatActivity activity;
    private ArrayList<Inscription> listeInscriptions; // Liste des inscriptions de la personne.
    private Personne animateur;
    private boolean isInscriptionValidee;
    private int typeRecherche;
    private String recherche;
    private FragmentManager fragmentManager;

    /**
     * Constructeur
     * @param activity : activité appelante.
     * @param context : contexte de l'activité appelante.
     * @param ressource : ID du layout affichant un élément de la liste des parties.
     * @param parties : liste des parties à afficher.
     * @param listeInscriptions : liste des inscriptions du participant.
     * @param typeRecherche : type de recherche (par ville, par département ou par région).
     * @param recherche : texte de la recherche
     * @param fragmentManager : FragmentManager
     */
    public PartieAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<Partie> parties,
                         ArrayList<Inscription> listeInscriptions,
                         int typeRecherche, String recherche, FragmentManager fragmentManager) {
        super(context, ressource, parties);
        this.context = context;
        this.parties = parties;
        this.activity = activity;
        this.listeInscriptions = listeInscriptions;
        this.typeRecherche = typeRecherche;
        this.recherche = recherche;
        this.fragmentManager = fragmentManager;
    }

    // Fonction visiblement non utilisée
    public void show() {
        final Dialog dialog = new Dialog(getContext());
    }

    /**
     * Fonction qui vérifie si un utilisateur est inscrit à une partie.
     * @param inscriptions : liste d'inscriptions de l'utilisateur.
     * @param idPartie : identifiant de la partie.
     * @return l'indice de l'inscription dans inscriptions si l'inscription a été trouvée, -1 sinon.
     */
    public int getIndiceInscription(ArrayList<Inscription> inscriptions, int idPartie) {
        boolean isInscriptionTrouvee = false;
        int j = 0;

        while((!isInscriptionTrouvee) && (j < inscriptions.size())) {
            if(inscriptions.get(j).getIdPartie() == idPartie) {
                isInscriptionTrouvee = true;
            }
            else {
                j++;
            }
        }

        // Si l'inscription a été trouvée, on retourne l'indice de l'inscription dans l'ArrayList inscriptions.
        if(isInscriptionTrouvee) {
            return j;
        }
        else {
            return -1;
        }
    }

    /**
     * Fonction utilisée pour convertir un objet de la liste des parties en vue.
     * @param position : la position de l'item dans l'ensemble de données de l'adapter.
     * @param convertView : l'ancienne vue à réutiliser (si c'est possible, pour des questions de performances).
     * @param parent : vue contenant cette vue.
     * @return la vue qui affiche les données à la position d'index position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // S'il n'existe pas de vue qu'on peut réutiliser pour afficher la vue en cours d'affichage, on va la construire.
        if(convertView == null) {
            // On récupère une instance de LayoutInflater standard qui est déjà connectée au contexte actuel et correctement configurée pour l'appareil.
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // On construit un objet View à partir du fichier XML R.layout.activity_recherche_partie_adapter.
            convertView = inflater.inflate(R.layout.activity_recherche_partie_adapter, parent, false);
        }
        else {
            // Sinon, on va la réutiliser.
            convertView = (LinearLayout)convertView;
        }

        // Récupération des composants.
        TextView tvdateHeure = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_date_heure);
        TextView tvLieuDeRetrait = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_lieu_retrait);
        TextView tvSommeTotaleLots = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_somme_totale_lots);
        TextView tvPrixCarton = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_prix_carton);
        TextView tvStatutParticipantAnimateur = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_statut_participant_animateur);
        TextView tvStatutInscription = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_statut_inscription);
        TextView tvEmailAnimateur = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_email_animateur);
        TextView tvNumTelAnimateur = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_numtel_animateur);
        TextView tvMontantARegler = (TextView)convertView.findViewById(R.id.recherche_partie_adapter_montant_a_regler);

        Button btnInscriptionPartie = (Button)convertView.findViewById(R.id.recherche_partie_adapter_btn_inscription_partie);
        Button btnDesinscriptionPartie = (Button)convertView.findViewById(R.id.recherche_partie_adapter_btn_desinscription_partie);
        Button btnMesInscriptionsSeDesinscrire = (Button)convertView.findViewById(R.id.recherche_partie_adapter_bouton_se_desinscrire);
        Button btnMesInscriptionsDemarrerPartie = (Button)convertView.findViewById(R.id.recherche_partie_adapter_bouton_demarrer_partie);
        Button btnVisualiserListeInscrits = (Button)convertView.findViewById(R.id.recherche_partie_adapter_bouton_visualiser_liste_inscrits);
        Button btnCommentRegler = (Button)convertView.findViewById(R.id.recherche_partie_adapter_bouton_comment_regler);
        LinearLayout layoutDemarrerPartie = (LinearLayout)convertView.findViewById(R.id.recherche_partie_adapter_layout_demarrer_partie);
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
        btnVisualiserListeInscrits.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnVisualiserListeInscrits.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));
        btnCommentRegler.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        btnCommentRegler.setTextColor(ContextCompat.getColor(getContext(), R.color.vert));

        // Récupération des valeurs de l'item affiché.
        idPartie = parties.get(position).getId();
        animateur = parties.get(position).getAnimateur();

        double sommeTotaleLots = 0.0;
        DecimalFormat df = new DecimalFormat("0.00");
        boolean isInscriptionTrouvee = false;
        int indiceInscription = 0;
        isInscriptionValidee = false;

        // On recherche l'indice de l'inscription dans la liste des inscriptions de l'utilisateur
        // correspondant à la partie.
        indiceInscription = getIndiceInscription(listeInscriptions, parties.get(position).getId());

        // Si l'indice de l'inscription a été trouvé, on va voir si cette dernière a été validée.
        if(indiceInscription > -1) {
            isInscriptionValidee = listeInscriptions.get(indiceInscription).isValidee();
        }

        // On affecte aux composants les valeurs de l'item affiché.
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        tvdateHeure.setText("Date et heure : " + simpleDateFormat.format(parties.get(position).getDate()));
        tvLieuDeRetrait.setText("Lieu de retrait : " + parties.get(position).getAdresse() + " " + parties.get(position).getCp() + " " + parties.get(position).getVille());

        ArrayList<Lot> lots = parties.get(position).getLots();

        System.out.println("sommeTotaleLots = " + sommeTotaleLots);

        for(int i = 0 ; i < lots.size() ; i++) {
            sommeTotaleLots += lots.get(i).getValeur();
            System.out.println("sommeTotaleLots = " + sommeTotaleLots);
        }

        // On arrondi vers le plus proche voisin, à moins que les deux voisins soit équidistants, auquel cas on arrondi vers le voisin supérieur.
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
            if (indiceInscription == -1) {
                btnInscriptionPartie.setVisibility(View.VISIBLE);
                btnDesinscriptionPartie.setVisibility(View.GONE);
                tvStatutParticipantAnimateur.setVisibility(View.GONE);
                tvStatutInscription.setVisibility(View.GONE);
                tvEmailAnimateur.setVisibility(View.GONE);
                tvNumTelAnimateur.setVisibility(View.GONE);
                tvMontantARegler.setVisibility(View.GONE);
            }
            // Sinon, on masque le bouton d'inscription et on affiche le bouton de désinscription.
            else {
                btnInscriptionPartie.setVisibility(View.GONE);
                btnDesinscriptionPartie.setVisibility(View.VISIBLE);

                // Si l'utilisateur est animateur de la partie, alors on va afficher l'information.
                if(sharedPref.getString("emailUtilisateur", "").equals(animateur.getEmail())) {
                    tvStatutParticipantAnimateur.setText("Vous êtes animateur de cette partie.");
                    tvStatutParticipantAnimateur.setVisibility(View.VISIBLE);
                    tvStatutInscription.setVisibility(View.GONE);
                    tvEmailAnimateur.setVisibility(View.GONE);
                    tvNumTelAnimateur.setVisibility(View.GONE);
                    tvMontantARegler.setVisibility(View.GONE);
                }
                // Sinon, on va afficher une information indiquant que l'utilisateur est participant.
                else {
                    tvStatutParticipantAnimateur.setVisibility(View.VISIBLE);
                    tvStatutParticipantAnimateur.setText("Vous êtes participant de cette partie.");

                    tvStatutInscription.setVisibility(View.VISIBLE);

                    if(isInscriptionValidee) {
                        tvStatutInscription.setText("Votre inscription est validée");
                    }
                    else {
                        tvStatutInscription.setText("Votre inscription n'est pas validée");
                    }

                    tvEmailAnimateur.setVisibility(View.VISIBLE);
                    tvNumTelAnimateur.setVisibility(View.VISIBLE);
                    tvEmailAnimateur.setText("E-mail de l'animateur : " + animateur.getEmail());
                    tvNumTelAnimateur.setText("N° tél de l'animateur : " + animateur.getNumtel());

                    tvMontantARegler.setText("Montant à régler : " + df.format(listeInscriptions.get(indiceInscription).getMontant()) + " €");
                }
            }

            btnMesInscriptionsDemarrerPartie.setVisibility(View.GONE);
            btnMesInscriptionsSeDesinscrire.setVisibility(View.GONE);
            btnVisualiserListeInscrits.setVisibility(View.GONE);
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
                tvStatutInscription.setVisibility(View.GONE);
                btnVisualiserListeInscrits.setVisibility(View.VISIBLE);

                tvEmailAnimateur.setVisibility(View.GONE);
                tvNumTelAnimateur.setVisibility(View.GONE);
                tvMontantARegler.setVisibility(View.GONE);
            }
            // Sinon, on va afficher une information indiquant que l'utilisateur est participant.
            else {
                tvStatutParticipantAnimateur.setText("Vous êtes participant de cette partie.");

                if(isInscriptionValidee) {
                    tvStatutInscription.setText("Votre inscription est validée");
                }
                else {
                    tvStatutInscription.setText("Votre inscription n'est pas validée");
                }

                btnVisualiserListeInscrits.setVisibility(View.GONE);

                tvEmailAnimateur.setVisibility(View.VISIBLE);
                tvNumTelAnimateur.setVisibility(View.VISIBLE);
                tvEmailAnimateur.setText("E-mail de l'animateur : " + animateur.getEmail());
                tvNumTelAnimateur.setText("N° tél de l'animateur : " + animateur.getNumtel());
                tvMontantARegler.setText("Montant à régler : " + df.format(listeInscriptions.get(indiceInscription).getMontant()) + " €");
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
                    // On affiche un number picker permettant de chosir le nombre de cartons.
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
                // Récupération des valeurs de l'item cliqué.
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
                        // Sinon, on va envoyer une requête pour désinscrire l'utilisateur de la partie.
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
                isInscriptionValidee = listeInscriptions.get(position).isValidee();

                String email = sharedPref.getString("emailUtilisateur", "");
                Intent intent = null;

                // Si l'e-mail de l'utilisateur correspond à l'e-mail de l'animateur, alors on va lancer l'activity de gestion de la partie.
                if(email.equals(animateur.getEmail())) {
                    intent = new Intent(getContext(), GestionPartieActivity.class);
                    intent.putExtra("idPartie", idPartieInscription);
                    getContext().startActivity(intent);
                }
                // Sinon, on lance l'activity de permettant de joueur à la partie de loto.
                else {
                    if(!isInscriptionValidee) {
                        String messageErreur = "Votre inscription à cette partie n'a pas encore été validée. Si vous avez effectué le réglement de la partie, " +
                                "veuillez contacter l'animateur afin qu'il valide votre inscription.";

                        ValidationDialogFragment vdf = new ValidationDialogFragment(messageErreur, false);
                        vdf.show(((MesInscriptionsActivity)context).getSupportFragmentManager(), "");
                    }
                    else {
                        intent = new Intent(getContext(), MainActivity.class);
                        intent.putExtra("idPartie", idPartieInscription);
                        getContext().startActivity(intent);
                    }
                }
            }
        });

        // Clic sur le bouton Visualiser liste lots.
        btnVisualiserListeLots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPartieInscription = parties.get(position).getId();
                animateur = parties.get(position).getAnimateur();

                Intent intent = new Intent(getContext(), VisualiserListeLotsActivity.class);
                intent.putExtra("idPartie", idPartieInscription);
                intent.putExtra("emailAnimateur", animateur.getEmail());
                intent.putExtra("source", nomActivitySource);
                // On passe en extra le type de recherche et la recherche. Ceci nous permettra de récupérer
                // ces valeurs quand on sortira de VisualiserListeInscritsActivity.
                intent.putExtra("typeRecherche", typeRecherche);
                intent.putExtra("recherche", recherche);
                getContext().startActivity(intent);
            }
        });

        // Clic sur le bouton Se désinscrire.
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
                        // Sinon, on va envoyer une requête pour désinscrire l'utilisateur de la partie.
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

        // Clic sur le bouton Visualiser liste inscrits.
        btnVisualiserListeInscrits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPartieInscription = parties.get(position).getId();
                Intent intent = new Intent(getContext(), VisualiserListeInscritsActivity.class);
                intent.putExtra("idPartie", idPartieInscription);
                getContext().startActivity(intent);
            }
        });

        // Clic sur le bouton Comment régler.
        btnCommentRegler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPartieInscription = parties.get(position).getId();
                Intent intent = new Intent(getContext(), DescriptionMethodeReglementActivity.class);
                intent.putExtra("idPartie", idPartieInscription);
                intent.putExtra("source", nomActivitySource);
                // On passe en extra le type de recherche et la recherche. Ceci nous permettra de récupérer
                // ces valeurs quand on sortira de DescriptionMethodeReglementActivity.
                intent.putExtra("typeRecherche", typeRecherche);
                intent.putExtra("recherche", recherche);
                getContext().startActivity(intent);
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

        // Envoi d'une requête permettant d'inscrire la parsonne à la partie sélectionnée.
        String adresse = adresseServeur + ":" + Constants.portMicroserviceGUIParticipant +
                "/participant/inscription-partie?email=" + AccueilActivity.encoderECommercial(email) + "&mdp=" +
                AccueilActivity.encoderECommercial(mdp) + "&idPartie=" + idPartieInscription + "&nbCartons=" + numberPicker.getValue();
        RequeteHTTP requeteHTTP = new RequeteHTTP(getContext(),
                adresse, activity);
        requeteHTTP.traiterRequeteHTTPJSON(activity, "InscriptionPartie", "POST", "", fragmentManager);
    }
}
