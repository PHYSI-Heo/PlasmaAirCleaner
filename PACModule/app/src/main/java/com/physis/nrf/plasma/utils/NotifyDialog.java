package com.physis.nrf.plasma.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class NotifyDialog {

    private AlertDialog dialog = null;

    public void dismiss(){
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    public void show(Context context, String title, String message)
    {
        dismiss();

        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.ok, null)
                .setCancelable(false).create();
        dialog = dialogBuilder.show();
    }

    public void show(Context context, String title, String message, DialogInterface.OnClickListener onPositiveButtonClickListener)
    {
        dismiss();

        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false).create();
        dialog = dialogBuilder.show();
    }

    public void show(Context context, String title, String message, String btnText, DialogInterface.OnClickListener onPositiveButtonClickListener)
    {
        dismiss();

        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false).create();
        dialog = dialogBuilder.show();
    }

    public void show(Context context, String title, View view, DialogInterface.OnClickListener onPositiveButtonClickListener)
    {
        dismiss();

        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false).create();
        dialog = dialogBuilder.show();
    }

    public void show(Context context, String title, View view, String btnText, DialogInterface.OnClickListener onPositiveButtonClickListener)
    {
        dismiss();

        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title)
                .setView(view)
                .setPositiveButton(btnText, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false).create();
        dialog = dialogBuilder.show();
    }

    public void show(Context context, String title, View view, View.OnClickListener onClickListener)
    {
        dismiss();
        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title)
                .setView(view)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(android.R.string.cancel, null)
                .setCancelable(false);
        dialog = dialogBuilder.create();
        dialog.show();
        // no dismiss
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(onClickListener);
    }

}
