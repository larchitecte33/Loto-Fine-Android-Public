package com.projects.loto_fine.classes_utilitaires;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

// Classe permettant à l'utilisateur de sélectionner un nombre.
public class NumberPickerDialogFragment extends DialogFragment {
    private String titre; // Titre de la fenêtre de sélection.
    private String message; // Message indiquant à l'utilisateur ce qu'il doit faire.
    private NumberPicker.OnValueChangeListener valueChangeListener; // Listener déclenché lorque la valeur du NumberPicker est modifiée.

    // Constructeur
    public NumberPickerDialogFragment(String titre, String message) {
        this.titre = titre;
        this.message = message;
    }

    // Fonction exécutée quand la boîte de dialogue est créée.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // On crée un NumberPicker pour que l'utilisateur puisse sélectionner un nombre.
        final NumberPicker numberPicker = new NumberPicker(getActivity());

        // Définition des valeurs min et max.
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(50);

        // Création d'une AlertDialog pour afficher le NumberPicker ainsi qu'un bouton Valider et un bouton Annuler.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(this.titre);
        builder.setMessage(this.message);

        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueChangeListener.onValueChange(numberPicker, numberPicker.getValue(), numberPicker.getValue());
            }
        });

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(numberPicker);
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    public void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }
}
