package com.nabinbhandari.android.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <pre>
 * Helper class for handling runtime permissions.
 * Created on 6/11/2017 on 9:32 PM
 * </pre>
 *
 * @author Nabin Bhandari
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Permissions {

    static boolean loggingEnabled = true;

    /**
     * Disable logs.
     */
    public static void disableLogging() {
        loggingEnabled = false;
    }

    static void log(String message) {
        if (loggingEnabled) Log.d("Permissions", message);
    }

    /**
     * Check/Request a permission and call the callback methods of permission handler accordingly.
     *
     * @param context    the android context.
     * @param permission the permission to be requested.
     * @param rationale  Explanation to be shown to user if s/he has denied permission earlier.
     *                   If this parameter is null, permissions will be requested without showing
     *                   the rationale dialog.
     * @param handler    The permission handler object for handling callbacks of various user
     *                   actions such as permission granted, permission denied, etc.
     */
    public static void check(Context context, String permission, String rationale,
                             PermissionHandler handler,Activity activity) {
        check(context, new String[]{permission}, rationale, null, handler, activity);
    }

    /**
     * Check/Request a permission and call the callback methods of permission handler accordingly.
     *
     * @param context     the android context.
     * @param permission  the permission to be requested.
     * @param rationaleId The string resource id of the explanation to be shown to user if s/he has
     *                    denied permission earlier. If resource is not found, permissions will be
     *                    requested without showing the rationale dialog.
     * @param handler     The permission handler object for handling callbacks of various user
     *                    actions such as permission granted, permission denied, etc.
     */
    public static void check(Context context, String permission, int rationaleId,
                             PermissionHandler handler,Activity activity) {
        String rationale = null;
        try {
            rationale = context.getString(rationaleId);
        } catch (Exception ignored) {
        }
        check(context, new String[]{permission}, rationale, null, handler, activity);
    }

    /**
     * Check/Request permissions and call the callback methods of permission handler accordingly.
     *  @param context     Android context.
     * @param permissions The array of one or more permission(s) to request.
     * @param rationale   Explanation to be shown to user if s/he has denied permission earlier.
 *                    If this parameter is null, permissions will be requested without showing
 *                    the rationale dialog.
     * @param options     The options for handling permissions.
     * @param handler     The permission handler object for handling callbacks of various user
     * @param mainActivity
     */
    public static void check(final Context context, String[] permissions, String rationale,
                             Options options, final PermissionHandler handler, Activity mainActivity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            handler.onGranted();
            log("Android version < 23");
        } else {
            Set<String> permissionsSet = new LinkedHashSet<>();
            Collections.addAll(permissionsSet, permissions);
            boolean allPermissionProvided = true;
            for (String aPermission : permissionsSet) {
                if (context.checkSelfPermission(aPermission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionProvided = false;
                    break;
                }
            }

            if (allPermissionProvided) {
                handler.onGranted();
                log("Permission(s) " + (PermissionsActivity.permissionHandler == null ?
                        "already granted." : "just granted from settings."));
                PermissionsActivity.permissionHandler = null;

            } else {
                PermissionsActivity.permissionHandler = handler;
                ArrayList<String> permissionsList = new ArrayList<>(permissionsSet);

                Intent intent = new Intent(context, PermissionsActivity.class)
                        .putExtra(PermissionsActivity.EXTRA_PERMISSIONS, permissionsList)
                        .putExtra(PermissionsActivity.EXTRA_RATIONALE, rationale)
                        .putExtra(PermissionsActivity.EXTRA_OPTIONS, options)
                        .putExtra(PermissionsActivity.EXTRA_MAIN_ACTIVITY, (Parcelable) mainActivity);

                if (options != null && options.createNewTask) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            }
        }
    }

    /**
     * Check/Request permissions and call the callback methods of permission handler accordingly.
     *
     * @param context     Android context.
     * @param permissions The array of one or more permission(s) to request.
     * @param rationaleId The string resource id of the explanation to be shown to user if s/he has
     *                    denied permission earlier. If resource is not found, permissions will be
     *                    requested without showing the rationale dialog.
     * @param options     The options for handling permissions.
     * @param handler     The permission handler object for handling callbacks of various user
     *                    actions such as permission granted, permission denied, etc.
     */
    public static void check(final Context context, String[] permissions, int rationaleId,
                             Options options, final PermissionHandler handler,Activity activity) {
        String rationale = null;
        try {
            rationale = context.getString(rationaleId);
        } catch (Exception ignored) {
        }
        check(context, permissions, rationale, options, handler,activity );
    }

    /**
     * Options to customize while requesting permissions.
     */
    public static class Options implements Serializable {

        String settingsText = "Settings";


        boolean sendBlockedToSettings = true;
        boolean createNewTask = false;

        String rationalePositiveBtn = "R +";
        String rationaleNegativeBtn = "R -";
        String rationaleDialogTitle = "Permissions Required";
        String rationaleDialogMessage = "Permissions Required";


        String dialogNegativeBtn = "D -";
        String dialogPositiveBtn = "D +";
        String settingsDialogTitle = "Permissions Required";
        String settingsDialogMessage = "Required permission(s) have been set" +
                " not to ask again! Please provide them from settings.";

        public Options setRationalePositiveBtn(String rationalePositiveBtn) {
            this.rationalePositiveBtn = rationalePositiveBtn;
            return this;
        }
        public Options setRationaleDialogMessage(String rationaleDialogMessage) {
            this.rationaleDialogMessage = rationaleDialogMessage;
            return this;
        }
        public Options setRationaleNegativeBtn(String rationaleNegativeBtn) {
            this.rationaleNegativeBtn = rationaleNegativeBtn;
            return this;
        }
        public Options setDialogPositiveBtn(String dialogPositiveBtn) {
            this.dialogPositiveBtn = dialogPositiveBtn;
            return this;
        }
        public Options setDialogNegativeBtn(String dialogNegativeBtn) {
            this.dialogNegativeBtn = dialogNegativeBtn;
            return this;
        }

        /**
         * Sets the button text for "settings" while asking user to go to settings.
         *
         * @param settingsText The text for "settings".
         * @return same instance.
         */
        public Options setSettingsText(String settingsText) {
            this.settingsText = settingsText;
            return this;
        }

        /**
         * Sets the "Create new Task" flag in Intent, for when we're
         * calling this library from within a Service or other
         * non-activity context.
         *
         * @param createNewTask true if we need the Intent.FLAG_ACTIVITY_NEW_TASK
         * @return same instance.
         */
        public Options setCreateNewTask(boolean createNewTask) {
            this.createNewTask = createNewTask;
            return this;
        }

        /**
         * Sets the title text for permission rationale dialog.
         *
         * @param rationaleDialogTitle the title text.
         * @return same instance.
         */
        public Options setRationaleDialogTitle(String rationaleDialogTitle) {
            this.rationaleDialogTitle = rationaleDialogTitle;
            return this;
        }

        /**
         * Sets the title text of the dialog which asks user to go to settings, in the case when
         * permission(s) have been set not to ask again.
         *
         * @param settingsDialogTitle the title text.
         * @return same instance.
         */
        public Options setSettingsDialogTitle(String settingsDialogTitle) {
            this.settingsDialogTitle = settingsDialogTitle;
            return this;
        }

        /**
         * Sets the message of the dialog which asks user to go to settings, in the case when
         * permission(s) have been set not to ask again.
         *
         * @param settingsDialogMessage the dialog message.
         * @return same instance.
         */
        public Options setSettingsDialogMessage(String settingsDialogMessage) {
            this.settingsDialogMessage = settingsDialogMessage;
            return this;
        }

        /**
         * In the case the user has previously set some permissions not to ask again, if this flag
         * is true the user will be prompted to go to settings and provide the permissions otherwise
         * the method {@link PermissionHandler#onDenied(Context, ArrayList)} will be invoked
         * directly. The default state is true.
         *
         * @param send whether to ask user to go to settings or not.
         * @return same instance.
         */
        public Options sendDontAskAgainToSettings(boolean send) {
            sendBlockedToSettings = send;
            return this;
        }
    }

}
