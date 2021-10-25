package me.mclellan.lab6;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public boolean authenticateUser(String user, String password) {
        AccountManager am = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = am.getAccountsByType("me.mclellan.lab6user");
        for (Account a : accounts) {
            if (a.name.equals(user)) {
                Toast.makeText(getApplicationContext(), "Authentication failed: incorrect password", Toast.LENGTH_LONG).show();
                return am.getPassword(a).equals(password);
            }
        }
        Toast.makeText(getApplicationContext(), "Authentication failed: user not found", Toast.LENGTH_LONG).show();
        return false;
    }

    public void authenticateUser(View v){
        EditText user = (EditText) findViewById(R.id.username);
        EditText pw = (EditText) findViewById(R.id.password);
        String usernameString = user.getEditableText().toString();
        String passwordString = pw.getEditableText().toString();
        if(authenticateUser(usernameString, passwordString)) {
            Intent intent = new Intent(this, Activity3.class);
            startActivity(intent);
        } else {
            //Toast.makeText(getApplicationContext(), "Authentication failure", Toast.LENGTH_SHORT).show();
        }
    }

    public void launchActivity2(View v) {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }
}