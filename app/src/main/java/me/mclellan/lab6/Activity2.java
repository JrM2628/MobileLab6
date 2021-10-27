package me.mclellan.lab6;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;

/*
Jake McLellan
Account creation activity. Creates an account and generates a corresponding key.
*/
public class Activity2 extends AppCompatActivity {

    private AccountManager m;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        this.m = AccountManager.get(this);
    }


    /*
        Creates a key and adds to the AndroidKeyStore using the paramater keyAlias as the alias
        Returns true if the key generates successfully, false otherwise
     */
    public boolean addKey(String keyAlias) {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if(!ks.containsAlias(keyAlias)) {
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
                return true;
            } else {
                // Keystore already contains alias
                return false;
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException |
                InvalidAlgorithmParameterException | NoSuchProviderException |
                KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
    }


    /*
        Creates an account of type me.mclellan.lab6user using the given username and password
        Returns the account if successful, null otherwise
     */
    public Account addUser(String user, String password) {
        Account account = new Account(user, "me.mclellan.lab6user");
        if(this.m.addAccountExplicitly(account, password, null))
            return account;
        return null;
    }


    /*
        Handles the user creation button click
        Gives an error message if the account/key creation was unsuccessful
        Launches activity 3 if successful
    */
    public void createUser(View v) {
        EditText user = (EditText) findViewById(R.id.userAct2);
        EditText pw = (EditText) findViewById(R.id.passwordAct2);
        String userString = user.getEditableText().toString();
        String pwString = pw.getEditableText().toString();
        Account account = addUser(userString, pwString);
        if(account != null) {
            Toast.makeText(getApplicationContext(), "Added user", Toast.LENGTH_SHORT).show();
            if(addKey(userString)) {
                Toast.makeText(getApplicationContext(), "Key added successfully", Toast.LENGTH_SHORT).show();
                this.m.setUserData(account, MainActivity.KEY_ALIAS, userString);
                Intent intent = new Intent(this, Activity3.class);
                intent.putExtra(MainActivity.EXTRA_MESSAGE, user.getEditableText().toString());
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Key failed to add", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to add user", Toast.LENGTH_SHORT).show();
        }
    }
}