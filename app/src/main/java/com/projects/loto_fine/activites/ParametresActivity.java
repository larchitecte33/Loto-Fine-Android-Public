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

public class ParametresActivity extends AppCompatActivity {

    private ValidationDialogFragment vdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        EditText editAdresseServeur = findViewById(R.id.edit_adresse_serveur);
        EditText editNbSecondesAvantMAJInfosTablette = findViewById(R.id.edit_nb_secondes_avant_maj_infos_tablette);
        Button boutonValider = findViewById(R.id.bouton_valider_parametres);
        Button boutonAnnuler = findViewById(R.id.bouton_annuler_parametres);

        // Assignation couleur boutons (utile pour les anciennes versions d'Android)
        boutonValider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonValider.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));
        boutonAnnuler.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.violet));
        boutonAnnuler.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.vert));

        SharedPreferences sharedPref = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        editAdresseServeur.setText(sharedPref.getString("AdresseServeur", ""));

        try {
            int nbSecondes = sharedPref.getInt("NbSecondesAvantMAJInfosTablette", 0);
            editNbSecondesAvantMAJInfosTablette.setText(String.valueOf(nbSecondes));
        }
        catch(Exception e) {
            AccueilActivity.afficherMessage(e.getMessage(), false, getSupportFragmentManager());
        }

        boutonValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nbSecondes = 0;

                try {
                    nbSecondes = Integer.valueOf(editNbSecondesAvantMAJInfosTablette.getText().toString());
                }
                catch(NumberFormatException e) {
                    nbSecondes = 0;
                }

                SharedPreferences.Editor edit = sharedPref.edit();
                edit.clear();

                String adresseServeur = sharedPref.getString("AdresseServeur", "http://localhost:8081");
                edit.putString("AdresseServeur", editAdresseServeur.getText().toString());
                edit.putInt("NbSecondesAvantMAJInfosTablette", nbSecondes);
                edit.commit();

                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });

        boutonAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccueilActivity.class);
                startActivity(intent);
            }
        });
    }
}