package com.suyashsrijan.lowbatterymonochrome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "LowBatteryMonochrome";
    boolean isSuAvailable = false;
    boolean isSecureSettingsPermGranted = false;
    boolean isMonochromeEnabled = false;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    MaterialDialog progressDialog;
    TextView textViewStatus;
    SwitchCompat toggleMonochromeSwitch;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0.0f);
        }
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isSuAvailable = settings.getBoolean("isSuAvailable", false);
        isSecureSettingsPermGranted = settings.getBoolean("isSecureSettingsPermGranted", false);
        toggleMonochromeSwitch = (SwitchCompat) findViewById(R.id.switch1);
        textViewStatus = (TextView) findViewById(R.id.textView2);
        isMonochromeEnabled = settings.getBoolean("isMonochromeEnabled", false);
        toggleMonochromeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor = settings.edit();
                    editor.putBoolean("isMonochromeEnabled", true);
                    editor.apply();
                    textViewStatus.setText("Monochrome is active");
                } else {
                    editor = settings.edit();
                    editor.putBoolean("isMonochromeEnabled", false);
                    editor.apply();
                    textViewStatus.setText("Monochrome is inactive");
                }
            }
        });

        if (isMonochromeEnabled) {
            toggleMonochromeSwitch.setChecked(true);
        } else {
            toggleMonochromeSwitch.setChecked(false);
        }

        if (!Utils.isSecureSettingsPermGranted(getApplicationContext())) {
            progressDialog = new MaterialDialog.Builder(this)
                    .title("Please wait")
                    .autoDismiss(false)
                    .cancelable(false)
                    .content("Requesting SU access...")
                    .progress(true, 0)
                    .show();
            Log.i(TAG, "Check if SU is available, and request SU permission if it is");

            Tasks.executeInBackground(MainActivity.this, new BackgroundWork<Boolean>() {
                @Override
                public Boolean doInBackground() throws Exception {
                    return Shell.SU.available();
                }
            }, new Completion<Boolean>() {
                @Override
                public void onSuccess(Context context, Boolean result) {
                    if (progressDialog != null) {
                        progressDialog.cancel();
                    }
                    isSuAvailable = result;
                    Log.i(TAG, "SU available: " + Boolean.toString(result));
                    Log.i(TAG, "WRITE_SECURE_SETTINGS granted: " + Boolean.toString(isSecureSettingsPermGranted));
                    if (isSuAvailable) {
                        Log.i(TAG, "Granting android.permission.WRITE_SECURE_SETTINGS to com.suyashsrijan.lowbatterymonochrome");
                        Utils.executeCommand("pm grant com.suyashsrijan.lowbatterymonochrome android.permission.WRITE_SECURE_SETTINGS", isSuAvailable);
                        editor = settings.edit();
                        editor.putBoolean("isSuAvailable", true);
                        isSecureSettingsPermGranted = true;
                        editor.putBoolean("isSecureSettingsPermGranted", true);
                        editor.apply();

                    } else {
                        Log.i(TAG, "Root not available");
                        toggleMonochromeSwitch.setChecked(false);
                        toggleMonochromeSwitch.setEnabled(false);
                        textViewStatus.setText("Monochrome is inactive");
                        Utils.showRootUnavailableDialog(MainActivity.this);
                    }
                }

                @Override
                public void onError(Context context, Exception e) {
                    Log.e(TAG, "Error querying SU: " + e.getMessage());
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        if (Utils.isSecureSettingsPermGranted(this)) {
            editor = settings.edit();
            isSecureSettingsPermGranted = true;
            editor.putBoolean("isSecureSettingsPermGranted", true);
            editor.apply();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_monochrome_more_info:
                Utils.showMoreInfoDialog(this);
                break;
            case R.id.action_donate_dev:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/suyashsrijan")));
                break;
            case R.id.action_reset_monochrome:
                Utils.resetMonochrome(getContentResolver());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
