package me.mclellan.lab6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Activity3 extends AppCompatActivity {

    private String userName;
    private AccountManager m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        Intent intent = getIntent();
        this.userName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        this.m = AccountManager.get(this);
    }

    private Account getAccount(String userName){
        Account[] accounts = this.m.getAccountsByType("me.mclellan.lab6user");
        for (Account a : accounts) {
            if (a.name.equals(userName)) {
                return a;
            }
        }
        return null;
    }

    public BiometricPrompt.PromptInfo createBiometricPrompt(){
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Lab 6")
                .setSubtitle("Please provide your pin, password, etc.")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .setConfirmationRequired(false)
                .build();
    }

    public BiometricPrompt invokeBiometricPrompt(Executor ex){
        return new BiometricPrompt(this, ex, new BiometricPrompt.AuthenticationCallback() {
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
    }

    public void authenticate(View v) {
        Executor ex = ContextCompat.getMainExecutor(this);
        BiometricPrompt prompt = invokeBiometricPrompt(ex);
        BiometricPrompt.PromptInfo details = createBiometricPrompt();
        prompt.authenticate(details);
    }


    public void generateNewKey(View v){
        Account account = getAccount(this.userName);
        String keyAlias = this.m.getUserData(account, MainActivity.KEY_ALIAS);

        KeyStore ks;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationParameters(120, KeyProperties.AUTH_DEVICE_CREDENTIAL)
                    .setKeySize(128)
                    .build();
            KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            kg.init(keySpec);
            kg.generateKey();
            Toast.makeText(getApplicationContext(), "New key added successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException | CertificateException | NoSuchAlgorithmException |
                InvalidAlgorithmParameterException | NoSuchProviderException | KeyStoreException e){
            e.printStackTrace();
        }
    }

    public void encrypt(View v) {
        try{
            String path = getFilesDir().toString();
            EditText edtFileName = (EditText)findViewById(R.id.fileName);
            EditText edtData = (EditText)findViewById(R.id.data);

            String fileName = edtFileName.getText().toString();
            String plainText = edtData.getText().toString();
            Account account = getAccount(this.userName);
            String keyAlias = this.m.getUserData(account, MainActivity.KEY_ALIAS);

            byte[] iv;
            byte[] cipherText;

            //fetch keystore service, fetch the key, do the encryption, save IV to a file
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            if(!ks.containsAlias(keyAlias)){
                Toast.makeText(getApplicationContext(), "Key alias not found", Toast.LENGTH_LONG).show();
                return;
            }

            SecretKey k = ((KeyStore.SecretKeyEntry)ks.getEntry(keyAlias, null)).getSecretKey();
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, k);
            iv = c.getIV();
            cipherText = c.doFinal(plainText.getBytes());
            FileOutputStream ctOut = new FileOutputStream(path + File.separator + fileName);
            for(Byte b: cipherText){
                ctOut.write(b);
            }
            ctOut.close();

            FileOutputStream ivOut = new FileOutputStream(path + File.separator + fileName + "_iv");
            for(Byte b: iv){
                ivOut.write(b);
            }
            ivOut.close();
            edtData.setText("");
        } catch(UserNotAuthenticatedException e){
            Executor ex = ContextCompat.getMainExecutor(this);
            BiometricPrompt prompt = invokeBiometricPrompt(ex);
            BiometricPrompt.PromptInfo details = createBiometricPrompt();
            prompt.authenticate(details);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException |
                UnrecoverableEntryException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }


    public void decrypt(View v){
        String path = getFilesDir().toString();
        try{
            /*
            check the alias
            if the alias doesn't exist throw an error and end
                grab key
                grab IV
                decrypt
             */
            EditText edtFileName = (EditText)findViewById(R.id.fileName);
            EditText edtData = (EditText)findViewById(R.id.data);
            String fileName = edtFileName.getText().toString();
            Account account = getAccount(this.userName);
            String keyAlias = this.m.getUserData(account, MainActivity.KEY_ALIAS);

            byte[] iv;
            byte[] cipherText;
            byte[] plainText;
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if(!ks.containsAlias(keyAlias)){
                Toast.makeText(getApplicationContext(), "Key alias not found", Toast.LENGTH_LONG).show();
                return;
            }

            SecretKey k = ((KeyStore.SecretKeyEntry)ks.getEntry(keyAlias, null)).getSecretKey();
            File ivFile = new File(path + File.separator + fileName + "_iv");
            if(ivFile.exists()){
                Path p = Paths.get(path + File.separator + fileName + "_iv");
                iv = Files.readAllBytes(p);
            } else {
                Toast.makeText(getApplicationContext(), "IV file missing cannot decrypt", Toast.LENGTH_LONG).show();
                return;
            }
            GCMParameterSpec params = new GCMParameterSpec(128, iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, k, params);

            Path p = Paths.get(path + File.separator + fileName);
            cipherText = Files.readAllBytes(p);

            try {
                plainText = c.doFinal(cipherText);
                String readablePlaintext = new String(plainText, StandardCharsets.UTF_8);
                edtData.setText(readablePlaintext);
            } catch (AEADBadTagException e){
                Toast.makeText(getApplicationContext(), "Error decrypting file. Did you generate a new key?", Toast.LENGTH_LONG).show();
            }
        } catch(UserNotAuthenticatedException e){
            Executor ex = ContextCompat.getMainExecutor(this);
            BiometricPrompt prompt = invokeBiometricPrompt(ex);
            BiometricPrompt.PromptInfo details = createBiometricPrompt();
            prompt.authenticate(details);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException |
                NoSuchPaddingException | UnrecoverableEntryException | InvalidKeyException |
                InvalidAlgorithmParameterException | BadPaddingException |
                IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

}