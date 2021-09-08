package com.projects.loto_fine.classes_utilitaires;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.projects.loto_fine.R;

public class ValidationDialogFragment extends DialogFragment {
    private String message;
    private boolean revenirAAccueil;

    public ValidationDialogFragment(String message, boolean revenirAAccueil) {
        this.message = message;
        this.revenirAAccueil = revenirAAccueil;
    }

    public interface ValidationDialogListener {
        void onFinishEditDialog(boolean revenirAAccueil);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
