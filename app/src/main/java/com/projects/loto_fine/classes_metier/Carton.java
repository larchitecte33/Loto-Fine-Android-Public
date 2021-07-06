package com.projects.loto_fine.classes_metier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcelable;
import android.os.Parcel;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.projects.loto_fine.Constants;

import java.util.Random;

/**
 * Classe qui représente une case d'un carton de quine
 * Cette classe fait partie du modèle.
 */
public class Carton implements Parcelable {
    private int id; // L'identifiant du carton
    private int posX; // La position en X du carton
    private int posY; // La position en y du carton
    private int width; // La largeur du carton
    private int height; // La hauteur du carton
    private float echelle; // L'échelle d'affichage du carton
    private int page; // La page d'affichage du carton
    private Personne joueur; // Le joueur à qui a été alloué le carton

    // caseCarton correspond aux cases du carton.
    // Chaque case est identifiée par son abscisse et son ordonnée sur le carton.
    // Par exemple, caseCarton[2][5] est la case qui se situe à la ligne 2 et à la colonne 5.
    private CaseCarton[][] casesCarton;

    // Constructeur
    public Carton(CaseCarton[][] casesCarton) {
        this.casesCarton = casesCarton;
    }

    // Constructeur utilisé pour recréer un carton à partir des données récupérées du savedInstanceState.
    public Carton(Parcel parcel) {
        this.id = parcel.readInt();
        this.posX = parcel.readInt();
        this.posY = parcel.readInt();
        this.width = parcel.readInt();
        this.height = parcel.readInt();
        this.echelle = parcel.readFloat();
    }

    // ***********************
    //       Accesseurs      *
    // ***********************
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getTheWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getTheHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Personne getJoueur() {
        return joueur;
    }

    public void setPersonne(Personne joueur) {
        this.joueur = joueur;
    }

    public CaseCarton getCaseCarton(int ligne, int colonne) {
        return this.casesCarton[ligne][colonne];
    }

    public void setCasesCarton(CaseCarton[][] casesCarton) {
        this.casesCarton = casesCarton;
    }

    // Implémentation des méthodes de Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.posX);
        dest.writeInt(this.posY);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeFloat(this.echelle);
    }

    /**
     * Initialisation des données du carton
     * @param context : contexte
     * @param posX : position en x du carton
     * @param posY : position en y du carton
     * @param page : page sur laquelle est affichée le carton
     * @param echelle : échelle d'affichage du carton
     */
    public void initDonneesCarton(Context context, int posX, int posY, int page, float echelle, Personne joueur) {
        // Déclarations
        int posXCase, posYCase;
        int id = 0; // Identifiant pour la case du carton
        Paint paintLigne = new Paint();

        this.posX = posX;
        this.posY = posY;
        this.echelle = echelle;
        this.width = (int)(Constants.NB_COLONNES_CARTON * Constants.TAILLE_CASE_CARTON * echelle);
        this.height = (int)(Constants.NB_LIGNES_CARTON * Constants.TAILLE_CASE_CARTON * echelle);
        this.page = page;
        this.joueur = joueur;

        paintLigne.setColor(Color.BLACK);

        // ************************************************************************************** //
        // **************************** Création des cases du carton **************************** //
        // ************************************************************************************** //

        // Pour chaque lignes du carton
        for(int i = 0 ; i < Constants.NB_LIGNES_CARTON ; i++) {
            // Pour chaque colonnes du carton
            for(int j = 0 ; j < Constants.NB_COLONNES_CARTON ; j++) {
                // On calcule la position en x et la position en y de la case.
                posXCase = (int)(this.posX + j * Constants.TAILLE_CASE_CARTON * echelle);
                posYCase = (int)(this.posY + i * Constants.TAILLE_CASE_CARTON * echelle);

                casesCarton[i][j].setPosX(posXCase);
                casesCarton[i][j].setPosY(posYCase);
                casesCarton[i][j].setId(id);

                id++;
            }
        }
    }

    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredWidth
     *            the desired width
     * @param text
     *            the text that should be that width
     */
    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    /**
     * Fonction permettant de dessiner un carton
     * @param canvas : le canvas sur lequel dessiner le carton
     * @param paint : le pinceau
     * @param rect : le rectangle à dessiner
     */
    public void dessinerCarton(Canvas canvas, Paint paint, Rect rect) {
        Paint paintFill = new Paint();
        paintFill.setColor(Constants.COULEUR_BLEU);
        paintFill.setStyle(Paint.Style.FILL);

        Paint paintFillText = new Paint();
        paintFillText.setColor(Constants.COULEUR_VIOLET);
        paintFillText.setStyle(Paint.Style.FILL);

        Paint paintFillCaseOccupee = new Paint();
        paintFillCaseOccupee.setColor(Constants.COULEUR_VERT);
        paintFillCaseOccupee.setStyle(Paint.Style.FILL);

        // Pour chaque lignes du carton
        for(int i = 0 ; i < Constants.NB_LIGNES_CARTON ; i++) {
            // Pour chaque colonnes du carton
            for (int j = 0; j < Constants.NB_COLONNES_CARTON; j++) {
                CaseCarton caseCarton = this.getCaseCarton(i, j);

                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);

                rect.left = caseCarton.getPosX();
                rect.top = caseCarton.getPosY();
                rect.right = (int)(rect.left + Constants.TAILLE_CASE_CARTON * echelle);
                rect.bottom = (int)(rect.top + Constants.TAILLE_CASE_CARTON * echelle);

                if(caseCarton.getValeur() == -1) {
                    canvas.drawRect(rect, paintFill);
                    canvas.drawRect(rect, paint);
                }
                else if(caseCarton.isOccupee()) {
                    canvas.drawRect(rect, paintFillCaseOccupee);
                    canvas.drawRect(rect, paint);
                    paintFillText.setTextSize((float)(Constants.TAILLE_CASE_CARTON * echelle * 0.75));
                    paintFillText.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(String.valueOf(caseCarton.getValeur()), rect.right - 3, rect.bottom - 20, paintFillText);

                }
                else {
                    canvas.drawRect(rect, paint);
                    paintFillText.setTextSize((float)(Constants.TAILLE_CASE_CARTON * echelle * 0.75));
                    paintFillText.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(String.valueOf(caseCarton.getValeur()), rect.right - 3, rect.bottom - 20, paintFillText);
                }
            }
        }
    }

    /**
     * Permet de cliquer sur une case d'un carton.
     * @param x : la position en x du clic.
     * @param y : la position en y du clic.
     */
    public void clicCase(int x, int y) {
        for(int i = 0 ; i < casesCarton.length ; i++) {
            for(int j = 0 ; j < casesCarton[i].length ; j++) {
                if ((x >= casesCarton[i][j].getPosX()) &&
                        (x < casesCarton[i][j].getPosX() + Constants.TAILLE_CASE_CARTON * echelle) &&
                        (y >= casesCarton[i][j].getPosY()) &&
                        (y < casesCarton[i][j].getPosY() + Constants.TAILLE_CASE_CARTON * echelle)) {
                    if ((casesCarton[i][j].getValeur() > -1)) {
                        if (casesCarton[i][j].isOccupee()) {
                            casesCarton[i][j].setOccupee(false);
                        } else {
                            casesCarton[i][j].setOccupee(true);
                        }
                    }
                }
            }
        }
    }

    // Fonction qui enlève tous les pions d'un carton
    public void enleverTousLesPions() {
        for(int i = 0 ; i < casesCarton.length ; i++) {
            for(int j = 0 ; j < casesCarton[i].length ; j++) {
                casesCarton[i][j].setOccupee(false);
            }
        }
    }
}
