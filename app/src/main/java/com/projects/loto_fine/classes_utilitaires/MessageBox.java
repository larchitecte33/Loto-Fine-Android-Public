package com.projects.loto_fine.classes_utilitaires;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.projects.loto_fine.activites.MainActivity;

public class MessageBox
{
    public void show(String title, String message, Context context)
    {
        dialog = new AlertDialog.Builder(context) // Pass a reference to your main activity here
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private AlertDialog dialog;
}