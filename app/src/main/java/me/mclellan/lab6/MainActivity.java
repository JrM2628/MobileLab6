package me.mclellan.lab6;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/*
Jake McLellan
Main landing page for the app. Allows users to enter credentials to log in, or create an account
*/
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "username";
    public static final String KEY_ALIAS = "alias";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /*
        Returns true if the user and password match an existing user of type "me.mclellan.lab6user"
        Returns false otherwise
     */
    public boolean authenticateUser(String user, String password) {
        AccountManager am = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = am.getAccountsByType("me.mclellan.lab6user");
        for (Account a : accounts) {
            if (a.name.equals(user)) {
                return am.getPassword(a).equals(password);
            }
        }
        Toast.makeText(getApplicationContext(), "Authentication failed: user not found", Toast.LENGTH_LONG).show();
        return false;
    }


    /*
        Handles the authentication button click
     */
    public void authenticateUser(View v) {
        EditText user = (EditText) findViewById(R.id.username);
        EditText pw = (EditText) findViewById(R.id.password);
        String usernameString = user.getEditableText().toString();
        String passwordString = pw.getEditableText().toString();
        if(authenticateUser(usernameString, passwordString)) {
            Intent intent = new Intent(this, Activity3.class);
            intent.putExtra(EXTRA_MESSAGE, usernameString);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Authentication failure", Toast.LENGTH_SHORT).show();
        }
    }


    /*
        Launches activity 2
     */
    public void launchActivity2(View v) {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }
}