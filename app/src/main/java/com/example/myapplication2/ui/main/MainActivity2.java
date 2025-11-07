package com.example.myapplication2.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication2.R;
import com.example.myapplication2.data.FirestoreRepository;
import com.example.myapplication2.ui.main.fragments.LevelOneFragment;
import com.example.myapplication2.ui.main.fragments.LevelTwoFragment;
import com.example.myapplication2.ui.main.fragments.LevelThreeFragment;
import com.example.myapplication2.ui.main.fragments.LevelFourFragment;

import com.google.firebase.firestore.DocumentSnapshot;

public class MainActivity2 extends AppCompatActivity {

    private Button buttonLevel1, buttonLevel2, buttonLevel3, buttonLevel4,
            buttonLevel5, buttonLevel6, buttonLevel7, buttonLevel8;
    private TableLayout tableLevels;
    private Button buttonBack;

    private FirestoreRepository firestoreRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.levelsactivity);

        tableLevels = findViewById(R.id.tableLevels);
        buttonBack = findViewById(R.id.buttonBack);

        buttonLevel1 = findViewById(R.id.buttonLevel1);
        buttonLevel2 = findViewById(R.id.buttonLevel2);
        buttonLevel3 = findViewById(R.id.buttonLevel3);
        buttonLevel4 = findViewById(R.id.buttonLevel4);
        buttonLevel5 = findViewById(R.id.buttonLevel5);
        buttonLevel6 = findViewById(R.id.buttonLevel6);
        buttonLevel7 = findViewById(R.id.buttonLevel7);
        buttonLevel8 = findViewById(R.id.buttonLevel8);

        firestoreRepo = new FirestoreRepository();

        loadUserProgressFromFirestore();

        buttonBack.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                tableLevels.setVisibility(TableLayout.VISIBLE);
            } else {
                finish();
            }
        });

        buttonLevel1.setOnClickListener(v -> abrirNivel(new LevelOneFragment()));
        buttonLevel2.setOnClickListener(v -> abrirNivel(new LevelTwoFragment()));
        buttonLevel3.setOnClickListener(v -> abrirNivel(new LevelThreeFragment()));
        buttonLevel4.setOnClickListener(v -> abrirNivel(new LevelFourFragment()));
    }

    private void loadUserProgressFromFirestore() {
        updateButtonStates(1);

        String uid = firestoreRepo.getCurrentUserId();
        if (uid == null) {
            Log.e("MainActivity2", "No hay usuario logueado. Mostrando solo Nivel 1.");
            return;
        }

        firestoreRepo.getUserData(uid, new FirestoreRepository.UserDataCallback() {
            @Override
            public void onDataLoaded(DocumentSnapshot document) {
                long highestLevel = 1;

                if (document.contains("highest_level_unlocked")) {
                    highestLevel = document.getLong("highest_level_unlocked");
                }

                Log.d("MainActivity2", "Nivel más alto desbloqueado: " + highestLevel);
                updateButtonStates(highestLevel);
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity2", "Error al cargar progreso de Firestore: " + e.getMessage());
                Toast.makeText(MainActivity2.this, "Error al cargar progreso", Toast.LENGTH_SHORT).show();
                updateButtonStates(1);
            }
        });
    }

    private void updateButtonStates(long highestLevel) {
        setupButton(buttonLevel1, true);
        setupButton(buttonLevel2, highestLevel >= 2);
        setupButton(buttonLevel3, highestLevel >= 3);
        setupButton(buttonLevel4, highestLevel >= 4);
        setupButton(buttonLevel5, highestLevel >= 5);
        setupButton(buttonLevel6, highestLevel >= 6);
        setupButton(buttonLevel7, highestLevel >= 7);
        setupButton(buttonLevel8, highestLevel >= 8);
    }

    private void setupButton(Button button, boolean habilitado) {
        button.setEnabled(habilitado);
        button.setAlpha(habilitado ? 1f : 0.5f);
    }

    public void abrirNivel(Fragment fragment) {
        tableLevels.setVisibility(TableLayout.GONE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

}