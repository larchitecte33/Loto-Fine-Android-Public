package com.projects.loto_fine.vues;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Carton;
import com.projects.loto_fine.classes_utilitaires.ElementCliquable;
import com.projects.loto_fine.classes_metier.Personne;

import java.util.ArrayList;

public class Plateau extends View {
    private ArrayList<ElementCliquable> listeElementsCliquables; // Liste des éléments qui vont réagir au clic.
    private ArrayList<Carton> cartons; // Liste des cartons présents sur le plateau (affichés ou non).
    private int pageCourantePortrait = 1; // Page courante dans l'affichage portrait.
    private int nbPagesPortrait = 1; // Nombre de pages dans l'affichage portrait.
    private int pageCourantePaysage = 1; // Page courante dans l'affichage paysage.
    private int nbPagesPaysage = 1; // Nombre de pages dans l'affichage paysage.
    private int numeroEnCours = -1; // Numéro en cours de jeu.
    private boolean boutonQuineIsActif = false; // Le bouton "Quine !!!" est-il actif.
    private boolean lotCartonPlein = false; // Le lot en cours de jeu est-il au carton plein ?
    private String lotEnCours = ""; // Libellé du lot en cours de jeu.
    private Personne joueur; // Joueur
    private Context context; // Contexte

    // Constructeur
    public Plateau(Context context) {
        // Appel du constructeur de View
        super(context);

        this.context = context;
        listeElementsCliquables = new ArrayList<>();
    }

    // Accesseurs
    public ArrayList<ElementCliquable> getListeElementsCliquables() {
        return this.listeElementsCliquables;
    }

    public void setCartons(ArrayList<Carton> cartons) {
        this.cartons = cartons;
    }

    public ArrayList<Carton> getCartons() {
        return this.cartons;
    }

    public int getPageCourantePortrait() {
        return pageCourantePortrait;
    }

    public void setPageCourantePortrait(int pageCourantePortrait) {
        this.pageCourantePortrait = pageCourantePortrait;
    }

    public int getPageCourantePaysage() {
        return pageCourantePaysage;
    }

    public void setPageCourantePaysage(int pageCourantePaysage) {
        this.pageCourantePaysage = pageCourantePaysage;
    }

    public int getNbPagesPortrait() {
        return nbPagesPortrait;
    }

    public int getNbPagesPaysage() {
        return nbPagesPaysage;
    }

    public void setBoutonQuineActif() {
        boutonQuineIsActif = true;
    }

    public void setBoutonQuineInactif() {
        boutonQuineIsActif = false;
    }

    public void setJoueur(Personne joueur) {
        this.joueur = joueur;
    }

    public void setNumeroEnCours(int numeroEnCours) {
        this.numeroEnCours = numeroEnCours;
    }

    public void setLotCartonPlein(boolean lotCartonPlein) {
        this.lotCartonPlein = lotCartonPlein;
    }

    public void setLotEnCours(String lotEnCours) {
        this.lotEnCours = lotEnCours;
    }

    // Fonction qui va être appelé quand la vue va se redessiner.
    @Override
    public void onDraw(Canvas canvas) {
        // Déclarations
        int pagePortrait; // Numéro de la page en portrait du carton en cours de parcours.
        int topCartons; // Position en Y du premier carton
        int topBoutonCartonsPrecedents; // Position en Y du bouton "Cartons précédents"
        int pagePaysage; // Numéro de la page en mode paysage.
        int top, bottom, left, right; // Coordonnées des éléments à afficher.
        int largeurEcran, hauteurEcran; // Largeur et hauteur de l'écran.

        // Appel du onDraw de View
        super.onDraw(canvas);

        Log.d("canvas.getWidth() = ", String.valueOf(canvas.getWidth()));
        Log.d("canvas.getHeight() = ", String.valueOf(canvas.getHeight()));

        // Récupération des images.
        Drawable drawableLogoSecondaire = ResourcesCompat.getDrawable(getResources(), R.drawable.logo_secondaire, null);
        Drawable drawableBoutonRalentirPartie = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_ralentir_partie, null);
        Drawable drawableBoutonAccelererPartie = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_accelerer_partie, null);
        Drawable drawableBoutonQuine;

        if(boutonQuineIsActif) {
            drawableBoutonQuine = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_quine_active, null);
        }
        else {
            drawableBoutonQuine = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_quine_desactive, null);
        }

        Drawable drawableBoutonCartonsPrecedents = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_cartons_precedents, null);
        Drawable drawableBoutonCartonsPrecedentsInactif = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_cartons_precedents_inactif, null);
        Drawable drawableBoutonCartonsSuivants = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_cartons_suivants, null);
        Drawable drawableBoutonCartonsSuivantsInactif = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_cartons_suivants_inactif, null);
        Drawable drawableBoutonQuitter = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_quitter, null);

        largeurEcran = canvas.getWidth();
        hauteurEcran = canvas.getHeight();

        Paint paint = new Paint();
        Rect rect = new Rect();

        // On vide la liste des éléments cliquables.
        listeElementsCliquables.clear();

        // Si le téléphone ou la tablette est en position portrait.
        if (canvas.getWidth() < canvas.getHeight()) {
            // Calcul échelle horizontale et verticale.
            float echelleHorizontale = (float) largeurEcran / (float) Constants.LARGEUR_ECRAN_MAQUETTE;
            float echelleVerticale = (float) hauteurEcran / (float) Constants.HAUTEUR_ECRAN_MAQUETTE;
            Log.d("echelleHorizontale = ", String.valueOf(echelleHorizontale));
            Log.d("echelleVerticale = ", String.valueOf(echelleVerticale));

            // Affichage logo secondaire
            top = Constants.TOP_LOGO_SECONDAIRE_MAQUETTE * hauteurEcran / Constants.HAUTEUR_ECRAN_MAQUETTE;
            bottom = Constants.BOTTOM_LOGO_SECONDAIRE_MAQUETTE * hauteurEcran / Constants.HAUTEUR_ECRAN_MAQUETTE;
            left = (largeurEcran / 2) - (int) (Constants.LOGO_SECONDAIRE_LARGEUR * echelleHorizontale / 2);
            Log.d("leftLogo = ", String.valueOf(left));
            right = (largeurEcran / 2) + (int) (Constants.LOGO_SECONDAIRE_LARGEUR * echelleHorizontale / 2);
            Log.d("rightLogo = ", String.valueOf(right));

            drawableLogoSecondaire.setBounds(left, top, right, bottom);
            drawableLogoSecondaire.draw(canvas);


            // Premier séparateur horizontal
            top = (int)(Constants.TOP_PREMIER_SEPARATEUR_HOROZONTAL_MAQUETTE * echelleVerticale); // * hauteurEcran / Constants.HAUTEUR_ECRAN_MAQUETTE;
            bottom = (int)(Constants.BOTTOM_PREMIER_SEPARATEUR_HOROZONTAL_MAQUETTE * echelleVerticale); //* hauteurEcran / Constants.HAUTEUR_ECRAN_MAQUETTE;

            paint.setColor(Constants.COULEUR_VIOLET);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, top, canvas.getWidth(), bottom, paint);

            // Si le lot en cours est définit, on affiche ses informations (nom du lot, à la ligne ou carton plein).
            if(!this.lotEnCours.trim().equals("")) {
                left = (int) ((Constants.LEFT_TEXTE_LOT) * echelleHorizontale);
                top = (int) ((Constants.TOP_TEXTE_LOT) * echelleVerticale);

                paint.setColor(Color.BLACK);
                paint.setTextSize((float)((Constants.TAILLE_CASE_CARTON * echelleHorizontale * 0.75) / 2));

                if (this.lotCartonPlein) {
                    canvas.drawText("Au carton plein :", left, top, paint);
                } else {
                    canvas.drawText("A la ligne :", left, top, paint);
                }

                top = top + (int)paint.getTextSize();
                canvas.drawText(this.lotEnCours, left, top, paint);
            }

            // Bouton ralentir partie
            top = (int) ((Constants.TOP_BOUTON_RALENTIR_PARTIE_MAQUETTE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_RALENTIR_PARTIE_MAQUETTE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_RALENTIR_PARTIE_MAQUETTE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_RALENTIR_PARTIE_MAQUETTE) * echelleHorizontale);

            drawableBoutonRalentirPartie.setBounds(left, top, right, bottom);
            drawableBoutonRalentirPartie.draw(canvas);

            ElementCliquable elemBoutonRalentirPartie = new ElementCliquable("boutonRalentirPartie", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonRalentirPartie);

            // Bouton accélerer partie
            top = (int) ((Constants.TOP_BOUTON_ACCELERER_PARTIE_MAQUETTE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_ACCELERER_PARTIE_MAQUETTE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_ACCELERER_PARTIE_MAQUETTE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_ACCELERER_PARTIE_MAQUETTE) * echelleHorizontale);

            Log.d("top bouton accélerer partie = ", String.valueOf(top));
            Log.d("bottom bouton accélerer partie = ", String.valueOf(bottom));

            drawableBoutonAccelererPartie.setBounds(left, top, right, bottom);
            drawableBoutonAccelererPartie.draw(canvas);

            ElementCliquable elemBoutonAccelererPartie = new ElementCliquable("boutonAccelererPartie", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonAccelererPartie);

            // Si le numéro en-cours de jeu est définit et s'il est valide, on l'affiche.
            if((numeroEnCours > 0) && (numeroEnCours <= 90)) {
                left = (int) ((Constants.LEFT_TEXTE_NUMERO_EN_COURS) * echelleHorizontale);

                paint.setColor(Color.BLACK);
                paint.setTextSize((float)((Constants.TAILLE_CASE_CARTON * echelleHorizontale * 0.75) / 2));
                String texte = "Numéro en-cours :";
                top = (int) (((Constants.TOP_TEXTE_NUMERO_EN_COURS) * echelleVerticale) + paint.getTextSize());
                canvas.drawText(texte, left, top, paint);

                left = left + (int) paint.measureText(texte) / 2;
                paint.setTextSize(paint.getTextSize() * 2);
                top = top + (int)paint.getTextSize();

                paint.setColor(Color.BLACK);
                canvas.drawText(String.valueOf(this.numeroEnCours), left, top, paint);
            }

            // Bouton quine
            top = (int) ((Constants.TOP_BOUTON_QUINE_MAQUETTE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_QUINE_MAQUETTE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_QUINE_MAQUETTE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_QUINE_MAQUETTE) * echelleHorizontale);

            drawableBoutonQuine.setBounds(left, top, right, bottom);
            drawableBoutonQuine.draw(canvas);

            ElementCliquable elemBoutonQuine = new ElementCliquable("boutonQuine", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonQuine);

            // Deuxième séparateur horizontal
            top = Constants.TOP_DEUXIEME_SEPARATEUR_HOROZONTAL_MAQUETTE * hauteurEcran / Constants.HAUTEUR_ECRAN_MAQUETTE;
            bottom = Constants.BOTTOM_DEUXIEME_SEPARATEUR_HOROZONTAL_MAQUETTE * hauteurEcran / Constants.HAUTEUR_ECRAN_MAQUETTE;

            paint.setColor(Constants.COULEUR_VIOLET);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(3);
            canvas.drawRect(0, top, canvas.getWidth(), bottom, paint);

            // Cartons
            top = Constants.TOP_PREMIER_CARTON_MAQUETTE * hauteurEcran / Constants.HAUTEUR_ECRAN_MAQUETTE;
            left = Constants.LEFT_PREMIER_CARTON_MAQUETTE * largeurEcran / Constants.LARGEUR_ECRAN_MAQUETTE;

            pagePortrait = 1;
            topCartons = top;
            topBoutonCartonsPrecedents = (int) ((Constants.TOP_BOUTON_CARTONS_PRECEDENTS_MAQUETTE) * echelleVerticale);
            nbPagesPortrait = 1;

            // Affichage des cartons
            if(cartons != null) {
                for (int i = 0; i < cartons.size(); i++) {
                    cartons.get(i).setHeight((int)(Constants.NB_LIGNES_CARTON * Constants.TAILLE_CASE_CARTON * echelleHorizontale));

                    int height = cartons.get(i).getTheHeight();

                    if(top + cartons.get(i).getTheHeight() > topBoutonCartonsPrecedents) {
                        top = topCartons;
                        pagePortrait++;
                        nbPagesPortrait++;
                    }

                    if(pagePortrait == pageCourantePortrait) {
                        cartons.get(i).initDonneesCarton(this.getContext(), left, top, pagePortrait, echelleHorizontale, joueur);
                        cartons.get(i).dessinerCarton(canvas, paint, rect);

                        int theWidth = cartons.get(i).getTheWidth();
                        int theHeight = cartons.get(i).getTheHeight();
                        int idCarton = cartons.get(i).getId();

                        ElementCliquable elemQuatriemeCarton = new ElementCliquable("carton" + idCarton, left, top,
                                theWidth, theHeight);
                        listeElementsCliquables.add(elemQuatriemeCarton);
                    }

                    top = cartons.get(i).getPosY() + cartons.get(i).getTheHeight() + 20;
                }
            }

            // Bouton cartons précédents
            top = (int) ((Constants.TOP_BOUTON_CARTONS_PRECEDENTS_MAQUETTE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_CARTONS_PRECEDENTS_MAQUETTE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_CARTONS_PRECEDENTS_MAQUETTE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_CARTONS_PRECEDENTS_MAQUETTE) * echelleHorizontale);

            if(pageCourantePortrait > 1) { // getPageCourantePortrait()
                drawableBoutonCartonsPrecedents.setBounds(left, top, right, bottom);
                drawableBoutonCartonsPrecedents.draw(canvas);
            }
            else {
                drawableBoutonCartonsPrecedentsInactif.setBounds(left, top, right, bottom);
                drawableBoutonCartonsPrecedentsInactif.draw(canvas);
            }

            ElementCliquable elemBoutonCartonsPrecedents = new ElementCliquable("boutonCartonsPrecedents", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonCartonsPrecedents);

            // Bouton cartons suivants
            top = (int) ((Constants.TOP_BOUTON_CARTONS_SUIVANTS_MAQUETTE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_CARTONS_SUIVANTS_MAQUETTE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_CARTONS_SUIVANTS_MAQUETTE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_CARTONS_SUIVANTS_MAQUETTE) * echelleHorizontale);

            if(getPageCourantePortrait() < getNbPagesPortrait()) {
                drawableBoutonCartonsSuivants.setBounds(left, top, right, bottom);
                drawableBoutonCartonsSuivants.draw(canvas);
            }
            else {
                drawableBoutonCartonsSuivantsInactif.setBounds(left, top, right, bottom);
                drawableBoutonCartonsSuivantsInactif.draw(canvas);
            }

            ElementCliquable elemBoutonCartonsSuivants = new ElementCliquable("boutonCartonsSuivants", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonCartonsSuivants);

            // Bouton cartons quitter
            top = (int) ((Constants.TOP_BOUTON_QUITTER_MAQUETTE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_QUITTER_MAQUETTE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_QUITTER_MAQUETTE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_QUITTER_MAQUETTE) * echelleHorizontale);

            drawableBoutonQuitter.setBounds(left, top, right, bottom);
            drawableBoutonQuitter.draw(canvas);

            ElementCliquable elemBoutonQuitter = new ElementCliquable("boutonQuitter", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonQuitter);
        }
        // Si le téléphone ou la tablette est en position paysage.
        else {
            // Calcul échelle horizontale et verticale.
            float echelleHorizontale = (float) largeurEcran / (float) Constants.LARGEUR_ECRAN_MAQUETTE_HORIZONTALE;
            float echelleVerticale = (float) hauteurEcran / (float) Constants.HAUTEUR_ECRAN_MAQUETTE_HORIZONTALE;

            // Affichage du logo secondaire
            top = (int) ((Constants.TOP_LOGO_SECONDAIRE_MAQUETTE_HORIZONTALE) * echelleVerticale);
            left = (int) ((Constants.LEFT_LOGO_SECONDAIRE_MAQUETTE_HORIZONTALE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_LOGO_SECONDAIRE_MAQUETTE_HORIZONTALE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_LOGO_SECONDAIRE_MAQUETTE_HORIZONTALE) * echelleHorizontale);

            drawableLogoSecondaire.setBounds(left, top, right, bottom);
            drawableLogoSecondaire.draw(canvas);

            // Premier séparateur horizontal
            top = (int) ((Constants.TOP_PREMIER_SEPARATEUR_HOROZONTAL_MAQUETTE_HORIZONTALE) * echelleVerticale);
            left = (int) ((Constants.LEFT_PREMIER_SEPARATEUR_HOROZONTAL_MAQUETTE_HORIZONTALE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_PREMIER_SEPARATEUR_HOROZONTAL_MAQUETTE_HORIZONTALE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_PREMIER_SEPARATEUR_HOROZONTAL_MAQUETTE_HORIZONTALE) * echelleHorizontale);

            paint.setColor(Constants.COULEUR_VIOLET);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(left, top, right, bottom, paint);


            // Si le lot en cours est définit, on affiche ses informations (nom du lot, à la ligne ou carton plein).
            if(!this.lotEnCours.trim().equals("")) {
                left = (int) ((Constants.LEFT_TEXTE_LOT_MAQUETTE_HORIZONTALE) * echelleHorizontale);
                top = (int) ((Constants.TOP_TEXTE_LOT_MAQUETTE_HORIZONTALE) * echelleVerticale);

                paint.setColor(Color.BLACK);
                paint.setTextSize((float) ((Constants.TAILLE_CASE_CARTON * echelleHorizontale * 0.75) / 2));

                if (this.lotCartonPlein) {
                    canvas.drawText("Au carton plein :", left, top, paint);
                } else {
                    canvas.drawText("A la ligne :", left, top, paint);
                }

                top = top + (int)paint.getTextSize();
                canvas.drawText(this.lotEnCours, left, top, paint);
            }

            // Si le numéro en-cours de jeu est définit et s'il est valide, on l'affiche.
            if((numeroEnCours > 0) && (numeroEnCours <= 90)) {
                top = (int) ((Constants.TOP_TEXTE_NUMERO_EN_COURS_MAQUETTE_HORIZONTALE) * echelleVerticale);
                left = (int) ((Constants.LEFT_TEXTE_NUMERO_EN_COURS_MAQUETTE_HORIZONTALE) * echelleHorizontale);

                paint.setColor(Color.BLACK);
                paint.setTextSize((float)((Constants.TAILLE_CASE_CARTON * echelleVerticale * 0.75) / 2));
                String texte = "Numéro";
                canvas.drawText(texte, left, top, paint);

                top = top + (int)paint.getTextSize();
                texte = "en-cours :";
                canvas.drawText(texte, left, top, paint);

                left = left + (int) paint.measureText(texte) / 2;
                paint.setTextSize(paint.getTextSize() * 2);
                left = left - (int)paint.measureText(String.valueOf(this.numeroEnCours)) / 2;
                top = top + (int)paint.getTextSize();

                paint.setColor(Color.BLACK);
                canvas.drawText(String.valueOf(this.numeroEnCours), left, top, paint);
            }

            // Bouton ralentir partie
            top = (int) ((Constants.TOP_BOUTON_RALENTIR_PARTIE_MAQUETTE_HORIZONTALE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_RALENTIR_PARTIE_MAQUETTE_HORIZONTALE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_RALENTIR_PARTIE_MAQUETTE_HORIZONTALE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_RALENTIR_PARTIE_MAQUETTE_HORIZONTALE) * echelleHorizontale);

            drawableBoutonRalentirPartie.setBounds(left, top, right, bottom);
            drawableBoutonRalentirPartie.draw(canvas);

            ElementCliquable elemBoutonRalentirPartie = new ElementCliquable("boutonRalentirPartie", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonRalentirPartie);

            // Bouton accélerer partie
            top = (int) ((Constants.TOP_BOUTON_ACCELERER_PARTIE_MAQUETTE_HORIZONTALE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_ACCELERER_PARTIE_MAQUETTE_HORIZONTALE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_ACCELERER_PARTIE_MAQUETTE_HORIZONTALE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_ACCELERER_PARTIE_MAQUETTE_HORIZONTALE) * echelleHorizontale);

            drawableBoutonAccelererPartie.setBounds(left, top, right, bottom);
            drawableBoutonAccelererPartie.draw(canvas);

            ElementCliquable elemBoutonAccelererPartie = new ElementCliquable("boutonAccelererPartie", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonAccelererPartie);

            // Bouton Quine
            top = (int) ((Constants.TOP_BOUTON_QUINE_MAQUETTE_HORIZONTALE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_QUINE_MAQUETTE_HORIZONTALE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_QUINE_MAQUETTE_HORIZONTALE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_QUINE_MAQUETTE_HORIZONTALE) * echelleHorizontale);

            drawableBoutonQuine.setBounds(left, top, right, bottom);
            drawableBoutonQuine.draw(canvas);

            ElementCliquable elemBoutonQuine = new ElementCliquable("boutonQuine", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonQuine);

            // Séparateur horizontal
            left = (int) (Constants.LEFT_SEPARATEUR_VERTICAL_MAQUETTE_HOROZONTALE * echelleHorizontale);
            right = (int) (Constants.RIGHT_SEPARATEUR_VERTICAL_MAQUETTE_HOROZONTALE * echelleHorizontale);

            paint.setColor(Constants.COULEUR_VIOLET);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(left, 0, right, canvas.getHeight(), paint);

            // Cartons
            top = (int) (Constants.TOP_PREMIER_CARTON_MAQUETTE_HOROZONTALE * echelleVerticale); // hauteurEcran / Constants.HAUTEUR_ECRAN_MAQUETTE_HORIZONTALE
            left = (int) (Constants.LEFT_PREMIER_CARTON_MAQUETTE_HOROZONTALE * echelleHorizontale); // largeurEcran / Constants.LARGEUR_ECRAN_MAQUETTE_HORIZONTALE;

            pagePaysage = 1;
            topCartons = top;
            topBoutonCartonsPrecedents = (int) ((Constants.TOP_BOUTON_CARTONS_PRECEDENTS_MAQUETTE_HORIZONTALE) * echelleVerticale);
            nbPagesPaysage = 1;

            // Affichage des cartons
            if(cartons != null) {
                for (int i = 0; i < cartons.size(); i++) {
                    cartons.get(i).setHeight((int)(Constants.NB_LIGNES_CARTON * Constants.TAILLE_CASE_CARTON * echelleHorizontale));

                    if(top + cartons.get(i).getTheHeight() > topBoutonCartonsPrecedents) {
                        top = topCartons;
                        pagePaysage++;
                        nbPagesPaysage++;
                    }

                    if(pagePaysage == pageCourantePaysage) {
                        // Si les données du carton n'ont pas déjà été initialisées, alors on les initialise.
                        cartons.get(i).initDonneesCarton(this.getContext(), left, top, pagePaysage, echelleHorizontale, joueur);

                        cartons.get(i).dessinerCarton(canvas, paint, rect);

                        ElementCliquable elemQuatriemeCarton = new ElementCliquable("carton" + cartons.get(i).getId(), left, top,
                                cartons.get(i).getTheWidth(),
                                cartons.get(i).getTheHeight());
                        listeElementsCliquables.add(elemQuatriemeCarton);
                    }

                    top = cartons.get(i).getPosY() + cartons.get(i).getTheHeight() + 20;
                }
            }

            // Bouton cartons précédents
            top = (int) ((Constants.TOP_BOUTON_CARTONS_PRECEDENTS_MAQUETTE_HORIZONTALE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_CARTONS_PRECEDENTS_MAQUETTE_HORIZONTALE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_CARTONS_PRECEDENTS_MAQUETTE_HORIZONTALE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_CARTONS_PRECEDENTS_MAQUETTE_HORIZONTALE) * echelleHorizontale);

            if(getPageCourantePaysage() > 1) {
                drawableBoutonCartonsPrecedents.setBounds(left, top, right, bottom);
                drawableBoutonCartonsPrecedents.draw(canvas);
            }
            else {
                drawableBoutonCartonsPrecedentsInactif.setBounds(left, top, right, bottom);
                drawableBoutonCartonsPrecedentsInactif.draw(canvas);
            }

            ElementCliquable elemBoutonCartonsPrecedents = new ElementCliquable("boutonCartonsPrecedents", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonCartonsPrecedents);

            // Bouton cartons suivants
            top = (int) ((Constants.TOP_BOUTON_CARTONS_SUIVANTS_MAQUETTE_HORIZONTALE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_CARTONS_SUIVANTS_MAQUETTE_HORIZONTALE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_CARTONS_SUIVANTS_MAQUETTE_HORIZONTALE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_CARTONS_SUIVANTS_MAQUETTE_HORIZONTALE) * echelleHorizontale);

            if(getPageCourantePaysage() < getNbPagesPaysage()) {
                drawableBoutonCartonsSuivants.setBounds(left, top, right, bottom);
                drawableBoutonCartonsSuivants.draw(canvas);
            }
            else {
                drawableBoutonCartonsSuivantsInactif.setBounds(left, top, right, bottom);
                drawableBoutonCartonsSuivantsInactif.draw(canvas);
            }

            ElementCliquable elemBoutonCartonsSuivants = new ElementCliquable("boutonCartonsSuivants", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonCartonsSuivants);

            // Bouton cartons quitter
            top = (int) ((Constants.TOP_BOUTON_QUITTER_MAQUETTE_HORIZONTALE) * echelleVerticale);
            left = (int) ((Constants.LEFT_BOUTON_QUITTER_MAQUETTE_HORIZONTALE) * echelleHorizontale);
            bottom = (int) ((Constants.BOTTOM_BOUTON_QUITTER_MAQUETTE_HORIZONTALE) * echelleVerticale);
            right = (int) ((Constants.RIGHT_BOUTON_QUITTER_MAQUETTE_HORIZONTALE) * echelleHorizontale);

            drawableBoutonQuitter.setBounds(left, top, right, bottom);
            drawableBoutonQuitter.draw(canvas);

            ElementCliquable elemBoutonQuitter = new ElementCliquable("boutonQuitter", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonQuitter);
        }
    }
}