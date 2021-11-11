package com.projects.loto_fine.classes_utilitaires;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

// Classe permettant � l'utilisateur de s�lectionner un nombre.
public class NumberPickerDialogFragment extends DialogFragment {
    private String titre; // Titre de la fen�tre de s�lection.
    private String message; // Message indiquant � l'utilisateur ce qu'il doit faire.
    private NumberPicker.OnValueChangeListener valueChangeListener; // Listener d�clench� lorque la valeur du NumberPicker est modifi�e.

    // Constructeur
    public NumberPickerDialogFragment(String titre, String message) {
        this.titre = titre;
        this.message = message;
    }

    // Fonction ex�cut�e quand la bo�te de dialogue est cr��e.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // On cr�e un NumberPicker pour que l'utilisateur puisse s�lectionner un nombre.
        final NumberPicker numberPicker = new NumberPicker(getActivity());

        // D�finition des valeurs min et max.
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(50);

        // Cr�ation d'une AlertDialog pour afficher le NumberPicker ainsi qu'un bouton Valider et un bouton Annuler.
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
