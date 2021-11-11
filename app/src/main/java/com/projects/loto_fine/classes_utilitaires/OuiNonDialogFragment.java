package com.projects.loto_fine.classes_utilitaires;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.projects.loto_fine.R;

import java.util.HashMap;

// Classe permettant � l'utilisateur de s�lectionner Oui ou Non.
public class OuiNonDialogFragment extends DialogFragment {
    private String message; // Message permettant d'indiquer l'action � l'utilisateur.
    private String nomActionOui; // Nom de l'action qui doit �tre ex�cut�e si clic sur Oui.
    private String nomActionNon; // Nom de l'action qui doit �tre ex�cut�e si clic sur Non.
    private HashMap<String, String> args; // Arguments � passer � la fonction onFinishEditDialog.

    // Constructeur
    public OuiNonDialogFragment(String message, String nomActionOui, String nomActionNon, HashMap<String, String> args) {
        this.message = message;
        this.nomActionOui = nomActionOui;
        this.nomActionNon = nomActionNon;
        this.args = args;
    }

    // Interface impl�ment�e par les classes qui utilisent OuiNonDialogFragment.
    public interface OuiNonDialogListener {
        void onFinishEditDialog(String nomActionOui, String nomActionNon, boolean choix, HashMap<String, String> args);
    }

    // Fonction appel�e quand la boite de dialogue est cr��e.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Cr�ation d'un AlertDialog permettant � l'utilisateur de s�lectionner son choix.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Ajout du message � l'AlertDialog.
        builder.setMessage(this.message)
                .setPositiveButton(R.string.texte_oui, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { // Ajout du bouton Oui ainsi que de l'action sur ce bouton.
                        if(dialog != null) {
                            OuiNonDialogFragment.OuiNonDialogListener listener = (OuiNonDialogFragment.OuiNonDialogListener) getActivity();
                            listener.onFinishEditDialog(nomActionOui, nomActionNon, true, args);

                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.texte_non, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) { // Ajout du bouton Non ainsi que de l'action sur ce bouton.
                        if(dialog != null) {
                            OuiNonDialogFragment.OuiNonDialogListener listener = (OuiNonDialogFragment.OuiNonDialogListener) getActivity();
                            listener.onFinishEditDialog(nomActionOui, nomActionNon, false, args);

                            dialog.dismiss();
                        }
                    }
                });
        return builder.create();
    }
}
