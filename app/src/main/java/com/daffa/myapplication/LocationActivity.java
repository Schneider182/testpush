package com.daffa.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationActivity extends AppCompatActivity {

    private Button btnCurrentLocation;
    private EditText inputKota;
    private Button btnCariKota;
    private TextView txtKoordinat;

    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean granted =
                                Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                        || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                        if (granted) {
                            ambilLokasi();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Izin lokasi diperlukan untuk menggunakan fitur ini.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

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

        setContentView(R.layout.activity_location);

        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        inputKota = findViewById(R.id.inputKota);
        btnCariKota = findViewById(R.id.btnCariKota);
        txtKoordinat = findViewById(R.id.txtKoordinat);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        btnCurrentLocation.setOnClickListener(v -> cekIzinLokasi());

        btnCariKota.setOnClickListener(v -> {
            String kota = inputKota.getText().toString().trim();

            if (kota.isEmpty()) {
                inputKota.setError("Masukkan nama kota");
                return;
            }

            Intent intent = new Intent(this, WeatherActivity.class);
            intent.putExtra("nama_kota", kota);
            startActivity(intent);
        });
    }

    private void cekIzinLokasi() {
        boolean fineGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            ambilLokasi();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void ambilLokasi() {
        boolean fineGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            return;
        }

        txtKoordinat.setText("Mengambil lokasi...");

        CurrentLocationRequest request =
                new CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setDurationMillis(30000)
                        .build();

        fusedLocationClient.getCurrentLocation(request, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        txtKoordinat.setText("Lokasi tidak ditemukan");
                        Toast.makeText(
                                this,
                                "Aktifkan GPS dan coba lagi.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    txtKoordinat.setText(
                            "Latitude: " + latitude +
                                    "\nLongitude: " + longitude
                    );

                    Intent intent = new Intent(
                            LocationActivity.this,
                            WeatherActivity.class
                    );

                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("gunakan_lokasi", true);

                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    txtKoordinat.setText("Gagal mengambil lokasi");

                    Toast.makeText(
                            this,
                            "Gagal mendapatkan lokasi perangkat.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}