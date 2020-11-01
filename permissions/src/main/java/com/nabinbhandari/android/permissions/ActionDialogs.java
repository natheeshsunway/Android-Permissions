package com.nabinbhandari.android.permissions;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ActionDialogs {
private String TAG="Permission";
    private TextView permission_title;
    private TextView permission_subject;
    private String positiveButtonMessage;
    private String negativeButtonMessage;
    Button permission_yesButton, permission_noButton;
    DialogClickListeners dialogClickListeners;

    private Activity activity;
    private Dialog dialog;


    public ActionDialogs(String permission_title, String permissionSubject, String positiveButtonMessage,
                         String negativeButtonMessage, Activity activity, DialogClickListeners dialogClickListeners) {
        this.activity = activity;
        this.dialogClickListeners = dialogClickListeners;
        this.positiveButtonMessage = positiveButtonMessage;
        this.negativeButtonMessage = negativeButtonMessage;
        setDialog();
        findViews();
        setData(permission_title, permissionSubject);
    }


    public void showDialog() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }


    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.permission_dialog_box);
    }

    private void findViews() {
        permission_title = dialog.findViewById(R.id.permission_title);
        permission_subject = dialog.findViewById(R.id.permission_subjects);
        permission_yesButton = dialog.findViewById(R.id.permission_positive_action);
        permission_noButton = dialog.findViewById(R.id.permission_negative_action);
    }

    private void setData(String title, String subtitle) {
        permission_title.setText(title);
        permission_subject.setText(subtitle);
        permission_yesButton.setText(positiveButtonMessage);
        permission_yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickListeners.positiveClick();
                Log.i(TAG, "Yesss");
            }
        });

        permission_noButton.setText(negativeButtonMessage);
        permission_noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickListeners.negativeClick();
                Log.i(TAG, "Noo");
            }
        });
    }


}
