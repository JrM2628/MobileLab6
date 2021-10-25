package me.mclellan.lab6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class Activity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
    }

    public void authenticate(View v) {
        Executor ex = ContextCompat.getMainExecutor(this);
        BiometricPrompt.PromptInfo details = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Lab 6")
                .setSubtitle("Please provide your pin, password, etc.")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .setConfirmationRequired(false)
                .build();
        BiometricPrompt prompt = new BiometricPrompt(this, ex, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Auth Error", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Auth Success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Auth Failure", Toast.LENGTH_LONG).show();
            }
        });
        prompt.authenticate(details);
    }

}