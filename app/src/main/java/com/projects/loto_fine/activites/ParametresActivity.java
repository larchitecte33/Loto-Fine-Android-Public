package com.projects.loto_fine.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_utilitaires.ValidationDialogFragment;

/**
 * Cette activité est affichée lors du clic sur le bouton PARAMETRES de l'activité AccueilActivity.
 * Elle permet à l'utilisateur de consulter et modifier les paramètres de l'application.
 */
public class ParametresActivity extends AppCompatActivity {

    private ValidationDialogFragment vdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        // Récupération des composants.
        EditText editAdresseServeur = findViewById(R.id.edit_adresse_serveur);
        Button boutonValider = findViewById(R.id.bouton_valider_parametres);
        Button boutonAnnuler = findViewById(R.id.bouton_annuler_parametres);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        boutonValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        boutonAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        editAdresseServeur.setText(sharedPref.getString("AdresseServeur", ""));

        // Clic sur le bouton Valider.
        boutonValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On édite les SharedPreferences.
                SharedPreferences.Editor edit = sharedPref.edit();

                // On modifie l'adresse du serveur dans les SharedPreferences.
                edit.putString("AdresseServeur", editAdresseServeur.getText().toString());
                // On enregistre les modifications des SharedPreferences.
                edit.commit();

                // On retourne sur l'accueil.
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        // Clic sur le bouton Annuler
        boutonAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On retourne sur l'accueil.
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });
    }
}