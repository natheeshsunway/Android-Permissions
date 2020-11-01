package com.nabinbhandari.android.permissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Nabin Bhandari on 7/21/2017 on 11:19 PM
 */

@SuppressWarnings("unchecked")
@TargetApi(Build.VERSION_CODES.M)
public class PermissionsActivity extends Activity {

    private static final int RC_SETTINGS = 6739;
    private static final int RC_PERMISSION = 6937;

    static final String EXTRA_PERMISSIONS = "permissions";
    static final String EXTRA_RATIONALE = "rationale";
    static final String EXTRA_OPTIONS = "options";

    static PermissionHandler permissionHandler;

    private ArrayList<String> allPermissions, deniedPermissions, noRationaleList;
    private Permissions.Options options;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(EXTRA_PERMISSIONS)) {
            finish();
            return;
        }

        getWindow().setStatusBarColor(0);
        allPermissions = (ArrayList<String>) intent.getSerializableExtra(EXTRA_PERMISSIONS);
        options = (Permissions.Options) intent.getSerializableExtra(EXTRA_OPTIONS);
        if (options == null) {
            options = new Permissions.Options();
        }
        deniedPermissions = new ArrayList<>();
        noRationaleList = new ArrayList<>();

        boolean noRationale = true;
        for (String permission : allPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
                if (shouldShowRequestPermissionRationale(permission)) {
                    noRationale = false;
                } else {
                    noRationaleList.add(permission);
                }
            }
        }

        if (deniedPermissions.isEmpty()) {
            grant();
            return;
        }

        String rationale = intent.getStringExtra(EXTRA_RATIONALE);
        if (noRationale || TextUtils.isEmpty(rationale)) {
            Permissions.log("No rationale.");
            requestPermissions(toArray(deniedPermissions), RC_PERMISSION);
        } else {
            Permissions.log("Show rationale.");
            showRationale(rationale);
        }
    }

    private void showRationale(String rationale) {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(
                        R.layout.permission_dialog_box,
                        null);
        builder.setView(customLayout);
        TextView title=(TextView)customLayout.findViewById(R.id.permission_title);
        title.setText(options.rationaleDialogTitle);
        TextView subject=(TextView)customLayout.findViewById(R.id.permission_subjects);
        subject.setText(options.rationaleDialogMessage);
        Button positiveButton = (Button) customLayout.findViewById(R.id.permission_positive_action);
        positiveButton.setText(options.rationalePositiveBtn);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                        Uri.fromParts("package", getPackageName(), null));
//                startActivityForResult(intent, RC_SETTINGS);
                requestPermissions(toArray(deniedPermissions), RC_PERMISSION);
            }
        });
        Button negativeButton = (Button) customLayout.findViewById(R.id.permission_negative_action);
        negativeButton.setText(options.rationaleNegativeBtn);
       negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deny();
            }
        });


        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                deny();
            }
        });

        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();

    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length == 0) {
            deny();
        } else {
            deniedPermissions.clear();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (deniedPermissions.size() == 0) {
                Permissions.log("Just allowed.");
                grant();
            } else {
                ArrayList<String> blockedList = new ArrayList<>(); //set not to ask again.
                ArrayList<String> justBlockedList = new ArrayList<>(); //just set not to ask again.
                ArrayList<String> justDeniedList = new ArrayList<>();
                for (String permission : deniedPermissions) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        justDeniedList.add(permission);
                    } else {
                        blockedList.add(permission);
                        if (!noRationaleList.contains(permission)) {
                            justBlockedList.add(permission);
                        }
                    }
                }

                if (justBlockedList.size() > 0) { //checked don't ask again for at least one.
                    PermissionHandler permissionHandler = PermissionsActivity.permissionHandler;
                    finish();
                    if (permissionHandler != null) {
                        permissionHandler.onJustBlocked(getApplicationContext(), justBlockedList,
                                deniedPermissions);
                    }

                } else if (justDeniedList.size() > 0) { //clicked deny for at least one.
                    deny();

                } else { //unavailable permissions were already set not to ask again.
                    if (permissionHandler != null &&
                            !permissionHandler.onBlocked(getApplicationContext(), blockedList)) {
                        sendToSettings();

                    } else finish();
                }
            }
        }
    }

    private void sendToSettings() {
        if (!options.sendBlockedToSettings) {
            deny();
            return;
        }
        Permissions.log("Ask to go to settings.");


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(
                R.layout.permission_dialog_box,
                null);
        builder.setView(customLayout);
        TextView title=(TextView)customLayout.findViewById(R.id.permission_title);
        title.setText(options.settingsDialogTitle);
        TextView subject=(TextView)customLayout.findViewById(R.id.permission_subjects);
        subject.setText(options.settingsDialogMessage);
        Button positiveButton = (Button) customLayout.findViewById(R.id.permission_positive_action);
        positiveButton.setText(options.dialogPositiveBtn);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                startActivityForResult(intent, RC_SETTINGS);
//                requestPermissions(toArray(deniedPermissions), RC_PERMISSION);
            }
        });
        Button negativeButton = (Button) customLayout.findViewById(R.id.permission_negative_action);
        negativeButton.setText(options.dialogNegativeBtn);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deny();
            }
        });


        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                deny();
            }
        });

        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SETTINGS && permissionHandler != null) {
            Permissions.check(this, toArray(allPermissions), null, options,
                    permissionHandler);
        }
        // super, because overridden method will make the handler null, and we don't want that.
        super.finish();
    }

    private String[] toArray(ArrayList<String> arrayList) {
        int size = arrayList.size();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = arrayList.get(i);
        }
        return array;
    }

    @Override
    public void finish() {
        permissionHandler = null;
        super.finish();
    }

    private void deny() {
        PermissionHandler permissionHandler = PermissionsActivity.permissionHandler;
        finish();
        if (permissionHandler != null) {
            permissionHandler.onDenied(getApplicationContext(), deniedPermissions);
        }
    }

    private void grant() {
        PermissionHandler permissionHandler = PermissionsActivity.permissionHandler;
        finish();
        if (permissionHandler != null) {
            permissionHandler.onGranted();
        }
    }

}
