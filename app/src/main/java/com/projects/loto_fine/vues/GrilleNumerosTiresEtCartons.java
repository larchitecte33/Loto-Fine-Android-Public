package com.projects.loto_fine.vues;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.projects.loto_fine.constantes.Constants;
import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Carton;
import com.projects.loto_fine.classes_utilitaires.ElementCliquable;

import java.util.ArrayList;

public class GrilleNumerosTiresEtCartons extends View {
    private Carton carton;
    private ArrayList<ElementCliquable> listeElementsCliquables;
    private boolean isRecupQuinesVisible, lotCartonPlein = false;
    private String lotEnCours = "";
    private int numeroEnCours;
    private ArrayList<Integer> numerosTires = new ArrayList<>();
    private boolean isBoutonCartonSuivantActif = false;

    public GrilleNumerosTiresEtCartons(Context context) {
        super(context);
        listeElementsCliquables = new ArrayList<>();
    }

    public ArrayList<ElementCliquable> getListeElementsCliquables() {
        return this.listeElementsCliquables;
    }

    public void setRecupQuinesVisible(boolean isRecupQuinesVisible) {
        this.isRecupQuinesVisible = isRecupQuinesVisible;
    }

    public boolean getRecupQuinesVisible() {
        return this.isRecupQuinesVisible;
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

    public void setCarton(Carton carton) {
        this.carton = carton;
    }

    public void setNumerosTires(ArrayList<Integer> numerosTires) {
        this.numerosTires = numerosTires;
    }

    public void setBoutonCartonSuivantActif(boolean isBoutonCartonSuivantActif) {
        this.isBoutonCartonSuivantActif = isBoutonCartonSuivantActif;
    }

    public void drawCaseGrille(Canvas canvas, int numeroCase, float echelleHorizontale,
                               float echelleVerticale, Paint paint) {
        int left = (int)(((Constants.LEFT_GRILLE_NUMEROS_TIRES_MAQUETTE +
                (((numeroCase - 1) % 10) - 1) * Constants.TAILLE_CASE_GRILLE_MAQUETTE) +
                Constants.TAILLE_CASE_GRILLE_MAQUETTE) *
                echelleHorizontale);
        int top = (int)(((Constants.TOP_GRILLE_NUMEROS_TIRES_MAQUETTE +
                ((int)((numeroCase - 1) / 10) - 1) * Constants.TAILLE_CASE_GRILLE_MAQUETTE) +
                Constants.TAILLE_CASE_GRILLE_MAQUETTE) *
                echelleVerticale);
        int rigth = (int)(((Constants.LEFT_GRILLE_NUMEROS_TIRES_MAQUETTE +
                (((numeroCase - 1) % 10)) * Constants.TAILLE_CASE_GRILLE_MAQUETTE) +
                Constants.TAILLE_CASE_GRILLE_MAQUETTE) *
                echelleHorizontale);
        int bottom = (int)(((Constants.TOP_GRILLE_NUMEROS_TIRES_MAQUETTE +
                ((int)((numeroCase - 1) / 10)) * Constants.TAILLE_CASE_GRILLE_MAQUETTE) +
                Constants.TAILLE_CASE_GRILLE_MAQUETTE) *
                echelleVerticale);

        if(numerosTires.indexOf(numeroCase) > -1) {
            Paint paintFillCaseOccupee = new Paint();
            paintFillCaseOccupee.setColor(Constants.COULEUR_VERT);
            paintFillCaseOccupee.setStyle(Paint.Style.FILL);
            canvas.drawRect(left, top, rigth, bottom, paintFillCaseOccupee);
        }

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(left, top, rigth, bottom, paint);

        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(String.valueOf(numeroCase), (rigth + left) / 2, (4 * bottom + top) / 5,
                paint);
    }

    @Override
    protected void onDraw(Canvas canvas){
        int top, bottom, left, right;
        int largeurEcran, hauteurEcran;
        Paint paint = new Paint();
        Rect rect = new Rect();

        int height = this.getHeight();
        int numeroTire = numeroEnCours;

        Drawable drawableLogoSecondaire = ResourcesCompat.getDrawable(getResources(), R.drawable.logo_secondaire, null);
        Drawable drawableBoutonTirerNumero = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_tirer_numero, null);
        Drawable drawableBoutonValiderQuine = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_valider_quine, null);
        Drawable drawableBoutonInvaliderQuine = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_invalider_quine, null);
        Drawable drawableBoutonCartonSuivant = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_carton_suivant, null);
        Drawable drawableBoutonValiderQuineInactif = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_valider_quine_inactif, null);
        Drawable drawableBoutonInvaliderQuineInactif = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_invalider_quine_inactif, null);
        Drawable drawableBoutonCartonSuivantInactif = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_carton_suivant_inactif, null);
        Drawable drawableBoutonQuitter = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_quitter, null);
        Drawable drawableBoutonRecupQuines = ResourcesCompat.getDrawable(getResources(), R.drawable.bouton_recup_quines, null);

        listeElementsCliquables.clear();

        largeurEcran = canvas.getWidth();
        hauteurEcran = canvas.getHeight();

        float echelleHorizontale = (float) largeurEcran / (float) Constants.LARGEUR_ECRAN_MAQUETTE;
        float echelleVerticale = (float) hauteurEcran / (float) Constants.HAUTEUR_ECRAN_MAQUETTE;

        top = (int)(Constants.TOP_LOGO_SECONDAIRE_MAQUETTE * echelleVerticale);
        bottom = (int)(Constants.BOTTOM_LOGO_SECONDAIRE_MAQUETTE * echelleVerticale);
        left = (largeurEcran / 2) - (int) (Constants.LOGO_SECONDAIRE_LARGEUR * echelleHorizontale / 2);
        right = (largeurEcran / 2) + (int) (Constants.LOGO_SECONDAIRE_LARGEUR * echelleHorizontale / 2);

        drawableLogoSecondaire.setBounds(left, top, right, bottom);
        drawableLogoSecondaire.draw(canvas);

        top = (int)(Constants.TOP_BOUTON_TIRER_NUMERO_MAQUETTE * echelleVerticale);
        bottom = (int)(Constants.BOTTOM_BOUTON_TIRER_NUMERO_MAQUETTE * echelleVerticale);
        left = (int)(Constants.LEFT_BOUTON_TIRER_NUMERO_MAQUETTE * echelleHorizontale);
        right = (int)(Constants.RIGHT_BOUTON_TIRER_NUMERO_MAQUETTE * echelleHorizontale);

        if(this.isRecupQuinesVisible) {
            drawableBoutonRecupQuines.setBounds(left, top, right, bottom);
            drawableBoutonRecupQuines.draw(canvas);

            ElementCliquable elemBoutonRecupQuines = new ElementCliquable("boutonRecupQuines", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonRecupQuines);
        }
        else {
            drawableBoutonTirerNumero.setBounds(left, top, right, bottom);
            drawableBoutonTirerNumero.draw(canvas);

            ElementCliquable elemBoutonTirerNumero = new ElementCliquable("boutonTirerNumero", left, top, right - left, bottom - top);
            listeElementsCliquables.add(elemBoutonTirerNumero);
        }

        left = (int) ((Constants.LEFT_TEXTE_NUMERO_EN_COURS_GESTION_PARTIE_MAQUETTE) * echelleHorizontale);
        top = (int) ((Constants.TOP_TEXTE_NUMERO_EN_COURS_GESTION_PARTIE_MAQUETTE) * echelleVerticale);
        right = (int) ((Constants.RIGHT_TEXTE_NUMERO_EN_COURS_GESTION_PARTIE_MAQUETTE) * echelleHorizontale);

        top = top + 25;
        int leftTemp;

        paint.setColor(Color.BLACK);
        paint.setTextSize((float)((Constants.TAILLE_CASE_CARTON * echelleHorizontale * 0.75) / 2));
        String texte = "Numéro";
        leftTemp = left + (int)(right - left) / 2 - (int) paint.measureText(texte) / 2;
        canvas.drawText(texte, leftTemp, top, paint);
        top = top + (int)paint.getTextSize();
        texte = "en-cours :";
        leftTemp = left + (int)(right - left) / 2 - (int) paint.measureText(texte) / 2;
        canvas.drawText(texte, leftTemp, top, paint);

        if(numeroTire > 0) {
            texte = String.valueOf(numeroTire);
            leftTemp = left + (int) (right - left) / 2 - (int) paint.measureText(texte) / 2;
            paint.setTextSize(paint.getTextSize() * 2);
            top = top + (int)paint.getTextSize();

            paint.setColor(Color.BLACK);
            canvas.drawText(String.valueOf(numeroTire), leftTemp, top, paint);
        }

        left = (int) ((Constants.LEFT_TEXTE_LOT_GESTION_PARTIE_MAQUETTE) * echelleHorizontale);
        top = (int) ((Constants.TOP_TEXTE_LOT_GESTION_PARTIE_MAQUETTE) * echelleVerticale);

        paint.setColor(Color.BLACK);
        paint.setTextSize((float)((Constants.TAILLE_CASE_GRILLE_MAQUETTE * echelleHorizontale * 0.75) / 2));

        if (this.lotCartonPlein) {
            canvas.drawText("Au carton plein :", left, top, paint);
        } else {
            canvas.drawText("A la ligne :", left, top, paint);
        }

        top = top + (int)paint.getTextSize();
        canvas.drawText(this.lotEnCours, left, top, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize((float)(Constants.TAILLE_CASE_GRILLE_MAQUETTE * echelleHorizontale * 0.75));

        // Dessin de la grille des numéros tirés
        for(int i = 0 ; i < 9 ; i++) {
            for(int j = 0 ; j < 10 ; j++) {
                drawCaseGrille(canvas, (i * 10) + j + 1, echelleHorizontale, echelleVerticale, paint);
            }
        }

        top = (int)(Constants.TOP_BOUTON_VALIDER_QUINE_MAQUETTE * echelleVerticale);
        bottom = (int)(Constants.BOTTOM_BOUTON_VALIDER_QUINE_MAQUETTE * echelleVerticale);
        left = (int)(Constants.LEFT_BOUTON_VALIDER_QUINE_MAQUETTE * echelleHorizontale);
        right = (int)(Constants.RIGHT_BOUTON_VALIDER_QUINE_MAQUETTE * echelleHorizontale);

        if(carton != null) {
            drawableBoutonValiderQuine.setBounds(left, top, right, bottom);
            drawableBoutonValiderQuine.draw(canvas);
        }
        else {
            drawableBoutonValiderQuineInactif.setBounds(left, top, right, bottom);
            drawableBoutonValiderQuineInactif.draw(canvas);
        }

        ElementCliquable elemBoutonValiderQuine = new ElementCliquable("boutonValiderQuine", left, top, right - left, bottom - top);
        listeElementsCliquables.add(elemBoutonValiderQuine);

        top = (int)(Constants.TOP_BOUTON_INVALIDER_QUINE_MAQUETTE * echelleVerticale);
        bottom = (int)(Constants.BOTTOM_BOUTON_INVALIDER_QUINE_MAQUETTE * echelleVerticale);
        left = (int)(Constants.LEFT_BOUTON_INVALIDER_QUINE_MAQUETTE * echelleHorizontale);
        right = (int)(Constants.RIGHT_BOUTON_INVALIDER_QUINE_MAQUETTE * echelleHorizontale);

        if(carton != null) {
            drawableBoutonInvaliderQuine.setBounds(left, top, right, bottom);
            drawableBoutonInvaliderQuine.draw(canvas);
        }
        else {
            drawableBoutonInvaliderQuineInactif.setBounds(left, top, right, bottom);
            drawableBoutonInvaliderQuineInactif.draw(canvas);
        }

        ElementCliquable elemBoutonInvaliderQuine = new ElementCliquable("boutonInvaliderQuine", left, top, right - left, bottom - top);
        listeElementsCliquables.add(elemBoutonInvaliderQuine);

        top = (int)(Constants.TOP_BOUTON_CARTON_SUIVANT_MAQUETTE * echelleVerticale);
        bottom = (int)(Constants.BOTTOM_BOUTON_CARTON_SUIVANT_MAQUETTE * echelleVerticale);
        left = (int)(Constants.LEFT_BOUTON_CARTON_SUIVANT_MAQUETTE * echelleHorizontale);
        right = (int)(Constants.RIGHT_BOUTON_CARTON_SUIVANT_MAQUETTE * echelleHorizontale);

        if(isBoutonCartonSuivantActif) {
            drawableBoutonCartonSuivant.setBounds(left, top, right, bottom);
            drawableBoutonCartonSuivant.draw(canvas);
        }
        else {
            drawableBoutonCartonSuivantInactif.setBounds(left, top, right, bottom);
            drawableBoutonCartonSuivantInactif.draw(canvas);
        }

        ElementCliquable elemBoutonCartonSuivant = new ElementCliquable("boutonCartonSuivant", left, top, right - left, bottom - top);
        listeElementsCliquables.add(elemBoutonCartonSuivant);

        top = (int)(Constants.TOP_BOUTON_QUITTER_GESTION_PARTIE_MAQUETTE * echelleVerticale);
        bottom = (int)(Constants.BOTTOM_BOUTON_QUITTER_GESTION_PARTIE_MAQUETTE * echelleVerticale);
        left = (int)(Constants.LEFT_BOUTON_QUITTER_GESTION_PARTIE_MAQUETTE * echelleHorizontale);
        right = (int)(Constants.RIGHT_BOUTON_QUITTER_GESTION_PARTIE_MAQUETTE * echelleHorizontale);

        drawableBoutonQuitter.setBounds(left, top, right, bottom);
        drawableBoutonQuitter.draw(canvas);

        ElementCliquable elemBoutonQuitter = new ElementCliquable("boutonQuitter", left, top, right - left, bottom - top);
        listeElementsCliquables.add(elemBoutonQuitter);

        top = (int)(Constants.TOP_CARTON_GESTION_PARTIE_MAQUETTE * echelleVerticale);
        left = (int)(Constants.LEFT_CARTON_GESTION_PARTIE_MAQUETTE * echelleHorizontale);

        if(carton != null) {
            float tailleCarton = Constants.TAILLE_CASE_CARTON * Constants.NB_COLONNES_CARTON
                    * echelleVerticale;
            left = (int)((largeurEcran / 2) - (tailleCarton / 2));

            carton.initDonneesCarton(this.getContext(), left, top, 1, echelleVerticale, carton.getJoueur());
            carton.dessinerCarton(canvas, paint, rect);
        }
    }
}
