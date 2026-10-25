
package com.daffa.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView txtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.view.Window window = getWindow();
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(
                window, false
        );

        androidx.core.view.WindowInsetsControllerCompat controller =
                androidx.core.view.WindowCompat.getInsetsController(
                        window, window.getDecorView()
                );

        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_login);

        android.content.SharedPreferences preferences =
                getSharedPreferences("WMWeatherPrefs", MODE_PRIVATE);

        if (preferences.getBoolean("isLoggedIn", false)) {
            Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtMessage = findViewById(R.id.txtMessage);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                new android.app.AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Login Gagal")
                        .setMessage("Username dan password wajib diisi.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            if (username.equals("tes") && password.equals("1234")) {
                preferences.edit()
                        .putBoolean("isLoggedIn", true)
                        .apply();

                Intent intent = new Intent(
                        LoginActivity.this,
                        LocationActivity.class
                );

                startActivity(intent);
                finish();
            } else {
                new android.app.AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Login Gagal")
                        .setMessage("Username atau password salah.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
}
