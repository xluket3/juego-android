package com.example.myapplication2.ui.main;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.SeekBar;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication2.R;

public class OptionsActivity extends AppCompatActivity {

    private SeekBar seekVolume, seekBrightness;
    private AudioManager audioManager;
    private ContentResolver contentResolver;
    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        // Referencias de la UI
        seekVolume = findViewById(R.id.seekVolume);
        seekBrightness = findViewById(R.id.seekBrightness);
        btnVolver = findViewById(R.id.btnVolver);

        // Inicializa audio y brillo
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        contentResolver = getContentResolver();

        // --- Configurar volumen ---
        seekVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Ajusta el volumen en tiempo real
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // --- Configurar brillo (con permiso dinámico) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            Toast.makeText(this, "Se necesita permiso para cambiar el brillo", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else {
            try {
                int brilloActual = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
                seekBrightness.setMax(255);
                seekBrightness.setProgress(brilloActual);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Ajusta el brillo en tiempo real
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, progress);
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // --- Botón Volver ---
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(OptionsActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

