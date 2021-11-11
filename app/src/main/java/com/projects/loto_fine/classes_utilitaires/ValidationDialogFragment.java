package com.projects.loto_fine.classes_utilitaires;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.projects.loto_fine.R;

// Classe permettant d'afficher un message � l'utilisateur
public class ValidationDialogFragment extends DialogFragment {
    private String message; // Message � afficher
    private boolean revenirAAccueil; // Doit-on revenir � l'activit� qui a ouvert l'activit� actuelle ?

    // Constructeur
    public ValidationDialogFragment(String message, boolean revenirAAccueil) {
        this.message = message;
        this.revenirAAccueil = revenirAAccueil;
    }

    // Interface impl�ment�e par les classes qui utilisent ValidationDialogFragment
    public interface ValidationDialogListener {
        void onFinishEditDialog(boolean revenirAAccueil);
    }

    // Fonction appel�e quand la boite de dialogue est cr��e.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Cr�ation d'un AlertDialog permettant � l'utilisateur de visualiser le message.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Ajout du message � l'AlertDialog.
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
        // Cr�e l'objet AlertDialog et le retrourne.
        return builder.create();
    }
}
