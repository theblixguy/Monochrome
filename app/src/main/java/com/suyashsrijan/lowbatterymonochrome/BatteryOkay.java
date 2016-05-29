package com.suyashsrijan.lowbatterymonochrome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BatteryOkay extends BroadcastReceiver {

    public static final String TAG = "LowBatteryMonochrome";
    boolean isSecureSettingsPermGranted = false;
    boolean isMonochromeEnabled = false;
    SharedPreferences settings;

    public BatteryOkay() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OKAY battery broadcast received, disabling monochrome mode");
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        isSecureSettingsPermGranted = settings.getBoolean("isSecureSettingsPermGranted", false);
        isMonochromeEnabled = settings.getBoolean("isMonochromeEnabled", false);
        if (isSecureSettingsPermGranted) {
            if (isMonochromeEnabled) {
                Utils.toggleMonochrome(0, context.getContentResolver());
            } else {
                Log.i(TAG, "OKAY battery broadcast received, but Monochrome is not enabled, so skipping");
            }
        } else {
            Log.i(TAG, "OKAY battery broadcast received, but WRITE_SECURE_SETTINGS permission not granted, so skipping");
        }
    }
}
