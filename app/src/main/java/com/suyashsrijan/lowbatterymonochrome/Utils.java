package com.suyashsrijan.lowbatterymonochrome;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class Utils {

    public static final String TAG = "LowBatteryMonochrome";

    public static boolean isSecureSettingsPermGranted(Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED)
            return true;
        else return false;
    }

    public static int getLowBatteryLevel() {
        int level;
        try {
            level = Resources.getSystem().getInteger(Resources.getSystem().getIdentifier("config_lowBatteryWarningLevel", "int", "android"));
        } catch (NotFoundException e) {
            level = 15;
        }
        return (level >= 15 ? level : 0);
    }

    public static int getBatteryLevel(Context context) {

        final Intent batteryIntent = context
                .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryIntent == null) {
            return Math.round(50.0f);
        }

        final int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        final int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level == -1 || scale == -1) {
            return Math.round(50.0f);
        }

        float battery_level = ((float) level / (float) scale) * 100.0f;
        return Math.round(battery_level);

    }

    public static void executeCommand(final String command, boolean isSuAvailable) {
        if (isSuAvailable) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<String> output = Shell.SU.run(command);
                    if (output != null) {
                        printShellOutput(output);
                    } else {
                        Log.i(TAG, "Error occurred while executing command (" + command + ")");
                    }
                }
            });
        } else {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<String> output = Shell.SH.run(command);
                    if (output != null) {
                        printShellOutput(output);
                    } else {
                        Log.i(TAG, "Error occurred while executing command (" + command + ")");
                    }
                }
            });
        }
    }

    public static void printShellOutput(List<String> output) {
        if (!output.isEmpty()) {
            for (String s : output) {
                Log.i(TAG, s);
            }
        }
    }

    public static void toggleMonochrome(int value, ContentResolver contentResolver) {
        Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", value);
        if (value == 0) {
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", -1);
        } else if (value == 1) {
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", 0);
        }
    }

    public static void resetMonochrome(ContentResolver contentResolver) {
        Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", 0);
        Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", -1);
    }

    public static void showRootWorkaroundInstructions(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("No-root workaround");
        builder.setMessage("If your device isn't rooted, you can manually grant the permission 'android.permission.WRITE_SECURE_SETTINGS' " +
                "to this app by executing the following ADB command from your PC (the command is one-line, not separated):\n\n" +
                "\"adb -d shell pm grant com.suyashsrijan.lowbatterymonochrome android.permission.WRITE_SECURE_SETTINGS\"\n\n" +
                "Once you have done, please close this app and start again and you will then be able to access the app properly.");
        builder.setPositiveButton("Okay", null);
        builder.setNegativeButton("Share command", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "adb -d shell pm grant com.suyashsrijan.lowbatterymonochrome android.permission.WRITE_SECURE_SETTINGS");
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);

            }
        });
        builder.show();
    }

    public static void showPermNotGrantedDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Error");
        builder.setMessage("android.permission.WRITE_SECURE_SETTINGS not granted");
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    public static void showRootUnavailableDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Error");
        builder.setMessage("SU permission denied or not available! If you don't have root, " +
                "press 'Root workaround' to get instructions on how to use this app without root");
        builder.setPositiveButton("Close", null);
        builder.setNegativeButton("Root workaround", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                showRootWorkaroundInstructions(context);
            }
        });
        builder.show();
    }

    public static void showMonochromeActiveDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("That's it");
        builder.setMessage("Monochrome is now enabled and will automatically activate when your battery becomes low");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    public static void showMoreInfoDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("More info");
        builder.setMessage("Monochrome turns on monochrome (or black & white) mode when your battery is low, to reduce display power consumption. " +
                "\n\nMonochrome is triggered when your device's battery level hits the LOW level as defined by the OEM (usually 15%) and Monochrome is triggered again when " +
                "your battery level hits the OKAY level (usually 30%) as defined by the OEM. When your battery level reaches the LOW state, Monochrome is turned on and " +
                "when your battery level reaches the OKAY state, Monochrome is turned off. This is all done automatically so you don't have to manage anything. " +
                "\n\nMonochrome also works on the GPU level, so Monochrome does not implement a hidden/invisible view on top of apps to enforce black and white colors. " +
                "This means Monochrome does not run in the background and/or constantly monitor your battery level, it's only activated when your battery reaches LOW " +
                "and OKAY states and after enabling/disabling B/W mode on the GPU level, Monochrome exits.\n\nIf the display remains stuck in monochrome even after " +
                "the battery level reached OKAY state, you can use the Reset option to get back to colour."
        );
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

}
