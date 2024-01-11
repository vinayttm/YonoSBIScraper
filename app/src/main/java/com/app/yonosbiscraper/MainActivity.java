package com.app.yonosbiscraper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.app.yonosbiscraper.api.ApiCaller;
import com.app.yonosbiscraper.localstorage.SharedPreferencesManager;
import com.app.yonosbiscraper.services.MyAccessibilityService;
import com.app.yonosbiscraper.utils.AccessibilityMethod;
import com.app.yonosbiscraper.utils.Config;
import com.app.yonosbiscraper.utils.MyDialog;


public class MainActivity extends AppCompatActivity {

    private EditText loginIdEditText, upiIdEditText, pinEditText;

    private SharedPreferencesManager sharedPreferencesManager;
    private ApiCaller apiCaller = new ApiCaller();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config.context = this;
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        setContentView(R.layout.activity_main);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        loginIdEditText = findViewById(R.id.loginId);
        upiIdEditText = findViewById(R.id.upiId);
        pinEditText = findViewById(R.id.pin);
        loginIdEditText.setText(sharedPreferencesManager.getStringValue("loginId"));
        upiIdEditText.setText(sharedPreferencesManager.getStringValue("upiId"));
        pinEditText.setText(sharedPreferencesManager.getStringValue("pinText"));
        boolean isServiceEnabled = AccessibilityMethod.isAccessibilityServiceEnabled(
                this, MyAccessibilityService.class);
        if (!isServiceEnabled) {
            MyDialog.showDialog(MainActivity.this, "Accessibility Permission Required.", "To use this app, you need to enable Accessibility Service. Go to Settings to enable it?",
                    new MyDialog.DialogClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            openAccessibilitySettings();
                        }

                        @Override
                        public void onNegativeButtonClick() {
                        }
                    });
        }
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    checkUpiStatus(upiIdEditText.getText().toString(), loginIdEditText.getText().toString(),
                            pinEditText.getText().toString());
                }
            }
        });
    }

    private void checkUpiStatus(String upiId, String loginId, String pinText) {
        if (apiCaller.getUpiStatus(Config.getUpiStatusUrl + upiId)) {
            saveToSharedPreferences(loginId, upiId, pinText);
        } else {
            Config.showToast("Upi Status in Active");
        }

    }

    private void saveToSharedPreferences(String loginId, String upiId, String pinText) {
        sharedPreferencesManager.saveStringValue("loginId", loginId);
        sharedPreferencesManager.saveStringValue("upiId", upiId);
        sharedPreferencesManager.saveStringValue("pinText", pinText);
        String savedLoginId = sharedPreferencesManager.getStringValue("loginId");
        String savedUpiId = sharedPreferencesManager.getStringValue("upiId");
        String savedPinText = sharedPreferencesManager.getStringValue("pinText");
        Log.d("savedLoginId", savedLoginId);
        Log.d("savedUpiId", savedUpiId);
        Log.d("savedPinText", savedPinText);
        Config.BankLoginId = savedLoginId;
        Config.upiId = savedUpiId;
        Config.loginPin = savedPinText;
        if (!Config.BankLoginId.isEmpty() || !Config.upiId.isEmpty() || !Config.loginPin.isEmpty()) {
            openApp();
        } else {
            Config.showToast("It seems like your fields is incomplete.");
        }
    }

    private void openApp() {
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(Config.packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            System.out.println("App with package name " + Config.packageName + " not found.");
        }
    }

    private void openAccessibilitySettings() {
        Intent accessibilityIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(accessibilityIntent);
    }

    private boolean validateForm() {
        String loginId = loginIdEditText.getText().toString().trim();
        String upiId = upiIdEditText.getText().toString().trim();
        String pin = pinEditText.getText().toString().trim();
        if (loginId.isEmpty()) {
            loginIdEditText.setError("Please enter Login ID");
            return false;
        }

        if (upiId.isEmpty()) {
            upiIdEditText.setError("Please enter UPI");
            return false;
        }

        if (pin.isEmpty()) {
            pinEditText.setError("Please enter PIN");
            return false;
        }
        return true;
    }
}