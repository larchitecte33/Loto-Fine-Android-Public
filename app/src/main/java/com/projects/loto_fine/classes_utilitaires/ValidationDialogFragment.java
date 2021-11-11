package com.projects.loto_fine.classes_utilitaires;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.projects.loto_fine.R;

// Classe permettant d'afficher un message à l'utilisateur
public class ValidationDialogFragment extends DialogFragment {
    private String message; // Message à afficher
    private boolean revenirAAccueil; // Doit-on revenir à l'activité qui a ouvert l'activité actuelle ?

    // Constructeur
    public ValidationDialogFragment(String message, boolean revenirAAccueil) {
        this.message = message;
        this.revenirAAccueil = revenirAAccueil;
    }

    // Interface implémentée par les classes qui utilisent ValidationDialogFragment
    public interface ValidationDialogListener {
        void onFinishEditDialog(boolean revenirAAccueil);
    }

    // Fonction appelée quand la boite de dialogue est créée.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Création d'un AlertDialog permettant à l'utilisateur de visualiser le message.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Ajout du message à l'AlertDialog.
        builder.setMessage(this.message)
                .setPositiveButton(R.string.texte_valider, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(dialog != null) {
                            ValidationDialogListener listener = (ValidationDialogListener) getActivity();
                            listener.onFinishEditDialog(revenirAAccueil);

                            dialog.dismiss();
                        }
                    }
                });
        // Crée l'objet AlertDialog et le retrourne.
        return builder.create();
    }
}
