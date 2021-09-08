package com.projects.loto_fine.classes_utilitaires;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.projects.loto_fine.R;

import java.util.HashMap;

public class OuiNonDialogFragment extends DialogFragment {
    private String message, nomActionOui, nomActionNon;
    private boolean choix;
    private HashMap<String, String> args;

    public OuiNonDialogFragment(String message, String nomActionOui, String nomActionNon, HashMap<String, String> args) {
        this.message = message;
        this.nomActionOui = nomActionOui;
        this.nomActionNon = nomActionNon;
        this.args = args;
    }

    public interface OuiNonDialogListener {
        void onFinishEditDialog(String nomActionOui, String nomActionNon, boolean choix, HashMap<String, String> args);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(this.message)
                .setPositiveButton(R.string.texte_oui, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(dialog != null) {
                            OuiNonDialogFragment.OuiNonDialogListener listener = (OuiNonDialogFragment.OuiNonDialogListener) getActivity();
                            listener.onFinishEditDialog(nomActionOui, nomActionNon, true, args);

                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.texte_non, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
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
