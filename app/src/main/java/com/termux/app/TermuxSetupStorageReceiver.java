package com.termux.app;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.termux.shared.data.IntentUtils;
import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxConstants.TERMUX_APP.TERMUX_ACTIVITY;

/**
 * A broadcast receiver to handle storage permission requests from termux-setup-storage script.
 * This receiver is exported to allow broadcasts from shell commands via 'am broadcast'.
 */
public class TermuxSetupStorageReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "TermuxSetupStorageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Logger.logError(LOG_TAG, "Received null intent");
            return;
        }

        Logger.logDebug(LOG_TAG, "Intent Received:\n" + IntentUtils.getIntentString(intent));

        String action = intent.getAction();
        if (action == null) {
            Logger.logError(LOG_TAG, "Received intent with null action");
            return;
        }

        // Check if this is the reload_style action with storage extra
        if (TERMUX_ACTIVITY.ACTION_RELOAD_STYLE.equals(action)) {
            String extraReloadStyle = intent.getStringExtra(TERMUX_ACTIVITY.EXTRA_RELOAD_STYLE);
            
            if (TERMUX_ACTIVITY.EXTRA_VALUE_RELOAD_STYLE_STORAGE.equals(extraReloadStyle)) {
                Logger.logDebug(LOG_TAG, "Received storage permission request, starting TermuxActivity");
                
                // Start TermuxActivity with ACTION_REQUEST_PERMISSIONS
                Intent activityIntent = new Intent(context, TermuxActivity.class);
                activityIntent.setAction(TERMUX_ACTIVITY.ACTION_REQUEST_PERMISSIONS);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                
                try {
                    context.startActivity(activityIntent);
                } catch (ActivityNotFoundException e) {
                    Logger.logError(LOG_TAG, "Failed to start TermuxActivity - activity not found: " + e.getMessage());
                } catch (SecurityException e) {
                    Logger.logError(LOG_TAG, "Failed to start TermuxActivity - security exception: " + e.getMessage());
                }
            } else if (extraReloadStyle != null) {
                // Only warn for non-null unexpected values
                Logger.logWarn(LOG_TAG, "Received reload_style action with non-storage extra: " + extraReloadStyle);
            } else {
                // Log at debug level when extra is null (deprecated action usage)
                Logger.logDebug(LOG_TAG, "Received reload_style action with null extra (deprecated action)");
            }
        } else {
            Logger.logWarn(LOG_TAG, "Received unexpected action: " + action);
        }
    }
}
