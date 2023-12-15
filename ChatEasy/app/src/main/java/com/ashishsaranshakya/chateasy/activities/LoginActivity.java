package com.ashishsaranshakya.chateasy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ashishsaranshakya.chateasy.R;
import com.ashishsaranshakya.chateasy.Util;
import com.ashishsaranshakya.chateasy.models.http.AuthRequest;
import com.ashishsaranshakya.chateasy.models.http.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    Button register;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        username = findViewById(R.id.editUsername);
        password = findViewById(R.id.editPassword);
        register = findViewById(R.id.btnRegister);
        login = findViewById(R.id.btnLogin);

        login.setOnClickListener(v -> {
            String username = this.username.getText().toString();
            String password = this.password.getText().toString();

            AuthRequest authRequest = new AuthRequest(username, password); // Replace with your login credentials

            Call<LoginResponse> call = Util.getHttpService().login(authRequest);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful()) {
                        LoginResponse loginResponse = response.body();
                        if (loginResponse != null && loginResponse.getSuccess()) {
                            LoginResponse.User user = loginResponse.getUser();
                            Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            SharedPreferences sharedPreferences = Util.getEncryptedSharedPreferences(LoginActivity.this);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("session", user.getToken());
                            editor.putString("username", user.getUsername());
                            editor.putString("userId", user.getUserId());
                            editor.apply();
                            Intent intent = new Intent(LoginActivity.this, ChatsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        register.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

    }
}