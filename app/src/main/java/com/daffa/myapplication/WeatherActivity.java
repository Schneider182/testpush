package com.daffa.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherActivity extends AppCompatActivity {

    private TextView txtKota, txtSuhu, txtDeskripsi;
    private TextView txtTerasa, txtKelembapan, txtAngin;
    private View weatherRoot;
    private Button btnPilihLokasi, btnLogout;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final String API_KEY = BuildConfig.OPENWEATHER_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WindowCompat.setDecorFitsSystemWindows(window, false);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        window, window.getDecorView()
                );

        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_weather);

        txtKota = findViewById(R.id.txtKota);
        txtSuhu = findViewById(R.id.txtSuhu);
        txtDeskripsi = findViewById(R.id.txtDeskripsi);
        txtTerasa = findViewById(R.id.txtTerasa);
        txtKelembapan = findViewById(R.id.txtKelembapan);
        txtAngin = findViewById(R.id.txtAngin);
        weatherRoot = findViewById(R.id.weatherRoot);

        btnPilihLokasi = findViewById(R.id.btnPilihLokasi);
        btnLogout = findViewById(R.id.btnLogout);

        ViewCompat.setOnApplyWindowInsetsListener(
                weatherRoot, (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        btnPilihLokasi.setOnClickListener(v -> {
            startActivity(new Intent(
                    WeatherActivity.this,
                    LocationActivity.class
            ));
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(WeatherActivity.this)
                    .setTitle("Logout")
                    .setMessage("Apakah kamu yakin ingin keluar?")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Logout", (dialog, which) -> {
                        getSharedPreferences(
                                "WMWeatherPrefs",
                                MODE_PRIVATE
                        ).edit()
                                .remove("isLoggedIn")
                                .apply();

                        Intent intent = new Intent(
                                WeatherActivity.this,
                                LoginActivity.class
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();
                    })
                    .show();
        });

        Intent intent = getIntent();

        if (intent.getBooleanExtra("gunakan_lokasi", false)) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);

            ambilCuacaKoordinat(latitude, longitude);
        } else {
            String kota = intent.getStringExtra("nama_kota");

            if (kota == null || kota.trim().isEmpty()) {
                Toast.makeText(
                        this,
                        "Silakan pilih lokasi atau cari nama kota.",
                        Toast.LENGTH_LONG
                ).show();

                finish();
                return;
            }

            ambilCuaca(kota);
        }
    }

    private void ambilCuaca(String kota) {
        String encodedCity;

        try {
            encodedCity = URLEncoder.encode(kota, "UTF-8");
        } catch (Exception e) {
            txtDeskripsi.setText("Nama kota tidak valid.");
            return;
        }

        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?q=" + encodedCity
                + "&appid=" + API_KEY
                + "&units=metric&lang=id";

        requestCuaca(url);
    }

    private void ambilCuacaKoordinat(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + latitude
                + "&lon=" + longitude
                + "&appid=" + API_KEY
                + "&units=metric&lang=id";

        requestCuaca(url);
    }

    private void requestCuaca(String urlString) {
        txtDeskripsi.setText("Memuat cuaca...");

        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            txtDeskripsi.setText("API key belum dikonfigurasi.");

            Toast.makeText(
                    this,
                    "Periksa file .env dan konfigurasi Gradle.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int status = connection.getResponseCode();

                if (status != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP " + status);
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JsonObject data = JsonParser.parseString(
                        response.toString()
                ).getAsJsonObject();

                String namaKota = data.get("name").getAsString();

                JsonObject main = data.getAsJsonObject("main");
                JsonObject wind = data.getAsJsonObject("wind");
                JsonObject weather = data.getAsJsonArray("weather")
                        .get(0).getAsJsonObject();

                double suhu = main.get("temp").getAsDouble();
                double terasa = main.get("feels_like").getAsDouble();
                int kelembapan = main.get("humidity").getAsInt();
                double angin = wind.get("speed").getAsDouble();

                String kondisi = weather.get("main").getAsString();
                String deskripsi = weather.get("description").getAsString();
                long timezone = data.get("timezone").getAsLong();

                runOnUiThread(() -> {
                    txtKota.setText(namaKota);

                    txtSuhu.setText(String.format(
                            Locale.getDefault(),
                            "%.1f°C",
                            suhu
                    ));

                    txtDeskripsi.setText(deskripsi);

                    txtTerasa.setText(String.format(
                            Locale.getDefault(),
                            "Terasa seperti %.1f°C",
                            terasa
                    ));

                    txtKelembapan.setText(
                            "Kelembapan: " + kelembapan + "%"
                    );

                    txtAngin.setText(String.format(
                            Locale.getDefault(),
                            "Kecepatan angin: %.1f m/s",
                            angin
                    ));

                    aturBackground(kondisi, timezone);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtDeskripsi.setText("Gagal mengambil data cuaca.");

                    Toast.makeText(
                            WeatherActivity.this,
                            "Gagal mengambil cuaca. Periksa koneksi dan API key.",
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void aturBackground(String kondisi, long timezone) {
        String weather = kondisi.toLowerCase(Locale.ROOT);

        if (weather.contains("rain")
                || weather.contains("drizzle")
                || weather.contains("thunderstorm")) {
            weatherRoot.setBackgroundResource(R.drawable.bg_rain);
            return;
        }

        long waktuLokal = System.currentTimeMillis()
                + timezone * 1000L;

        Calendar calendar = Calendar.getInstance(
                TimeZone.getTimeZone("UTC")
        );

        calendar.setTimeInMillis(waktuLokal);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 11) {
            weatherRoot.setBackgroundResource(R.drawable.bg_morning);
        } else if (hour >= 11 && hour < 17) {
            weatherRoot.setBackgroundResource(R.drawable.bg_day);
        } else {
            weatherRoot.setBackgroundResource(R.drawable.bg_night);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}