package com.ashishsaranshakya.chateasy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ashishsaranshakya.chateasy.activities.ChatsActivity;
import com.ashishsaranshakya.chateasy.activities.LoginActivity;
import com.ashishsaranshakya.chateasy.activities.RegisterActivity;

public class MainActivity extends AppCompatActivity {
    Button register;
    Button login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = Util.getEncryptedSharedPreferences(this);
        String token = sharedPreferences.getString("session", "");
        if (!token.equals("")) {
            Intent intent = new Intent(MainActivity.this, ChatsActivity.class);
            startActivity(intent);
            finish();
        }

        register = findViewById(R.id.btnRegister);
        login = findViewById(R.id.btnLogin);

        login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        register.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }
}