package com.example.myapplication2.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication2.R;
import com.example.myapplication2.ui.login.LoginActivity;
import com.example.myapplication2.util.Music;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnJugar, btnOpciones, btnLogin, btnLogout;
    private TextView tvUbicacion, tvUsuario, tvTitulo;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Inicialización
        btnJugar = findViewById(R.id.btnJugar);
        btnOpciones = findViewById(R.id.btnOpciones);
        btnLogin = findViewById(R.id.btnLogin);
        tvUbicacion = findViewById(R.id.tvUbicacion);
        tvUsuario = findViewById(R.id.tvUsuario);
        tvTitulo = findViewById(R.id.tvTitulo);

        // Nuevo botón para cerrar sesión
        btnLogout = findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Music.startMusic(this);

        // Mostrar usuario actual
        actualizarUsuario();

        btnJugar.setOnClickListener(v -> startActivity(new Intent(this, MainActivity2.class)));
        btnOpciones.setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        // Iniciar sesión
        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            actualizarUsuario();
        });

        obtenerUbicacion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cada vez que vuelves al menú, se actualiza el usuario por si cambió
        actualizarUsuario();
    }

    private void actualizarUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            tvUsuario.setText("Usuario: " + user.getEmail());
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.5f);
            btnLogout.setEnabled(true);
            btnLogout.setAlpha(1f);
        } else {
            tvUsuario.setText("Usuario: invitado");
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1f);
            btnLogout.setEnabled(false);
            btnLogout.setAlpha(0.5f);
        }
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String ciudad = addresses.get(0).getLocality();
                        String pais = addresses.get(0).getCountryName();
                        tvUbicacion.setText("Ubicación: " + ciudad + ", " + pais);
                    } else {
                        tvUbicacion.setText("Ubicación no disponible");
                    }
                } catch (Exception e) {
                    tvUbicacion.setText("Error al obtener ubicación");
                }
            } else {
                tvUbicacion.setText("No se pudo obtener la ubicación");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            tvUbicacion.setText("Permiso de ubicación denegado");
        }
    }
}
