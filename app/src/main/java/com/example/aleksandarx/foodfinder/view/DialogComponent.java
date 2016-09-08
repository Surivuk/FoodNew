package com.example.aleksandarx.foodfinder.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by EuroPATC on 9/7/2016.
 */
public class DialogComponent {

    public static Dialog permissionDialog(Context context, String title, String message, String positiveLabel, String negativeLabel,
                                          final Runnable positiveAction, final Runnable negativeAction){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if(positiveAction != null)
                            positiveAction.run();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        if(negativeAction != null)
                            negativeAction.run();
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(positiveLabel, dialogClickListener)
                .setCancelable(false)
                .setNegativeButton(negativeLabel, dialogClickListener);
        return builder.create();
    }
}
