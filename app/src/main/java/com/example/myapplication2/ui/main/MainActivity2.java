package com.example.myapplication2.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication2.R;
import com.example.myapplication2.data.FirestoreRepository;
import com.example.myapplication2.ui.main.fragments.LevelOneFragment;
import com.example.myapplication2.ui.main.fragments.LevelTwoFragment;
import com.example.myapplication2.ui.main.fragments.LevelThreeFragment;
import com.example.myapplication2.ui.main.fragments.LevelFourFragment;
import com.example.myapplication2.util.Music; // ¡Importación clave!

import com.google.firebase.firestore.DocumentSnapshot;

// Se implementa la interfaz TextToSpeech.OnInitListener
public class MainActivity2 extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private Button buttonLevel1, buttonLevel2, buttonLevel3, buttonLevel4,
            buttonLevel5, buttonLevel6, buttonLevel7, buttonLevel8;
    private TableLayout tableLevels;
    private Button buttonBack;

    private FirestoreRepository firestoreRepo;

    // Declaración del objeto TextToSpeech (TTS)
    private TextToSpeech tts;

    // Eliminamos la declaración local de MediaPlayer (backgroundMusic)

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

        // Inicialización del objeto TextToSpeech (TTS)
        tts = new TextToSpeech(this, this);

        // ¡USAR CLASE MUSIC PARA INICIAR!
        // Pasamos el contexto de la aplicación, pero la lógica de inicio está en Music.
        Music.startMusic(this);


        buttonBack.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                // Detener TTS antes de volver al menú
                if (tts != null) tts.stop();

                // ¡Ahora usamos el método estático resumeMusic()!
                Music.resumeMusic();

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

    // Método requerido por TextToSpeech.OnInitListener
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("es", "ES"));

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "El idioma ES no es compatible o faltan datos.");
            }

            // Configurar el UtteranceProgressListener para saber cuándo el TTS termina
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) { }

                @Override
                public void onDone(String utteranceId) {
                    // La TTS terminó, reanudar la música en el hilo principal
                    runOnUiThread(() -> {
                        // ¡Ahora usamos el método estático resumeMusic()!
                        Music.resumeMusic();
                    });
                }

                @Override
                public void onError(String utteranceId) {
                    // Si hay un error, también reanudar la música en el hilo principal
                    runOnUiThread(() -> {
                        // ¡Ahora usamos el método estático resumeMusic()!
                        Music.resumeMusic();
                    });
                }
            });

        } else {
            Log.e("TTS", "Fallo en la inicialización de TTS.");
        }
    }

    /**
     * Permite que los Fragmentos accedan al objeto TTS.
     */
    public TextToSpeech getTts() {
        return tts;
    }

    /**
     * Método público para que los Fragmentos puedan reproducir texto.
     * Detiene la música de fondo antes de hablar.
     * @param text El texto a ser leído en voz alta.
     */
    public void speakText(String text) {
        // 1. Detener/Pausar la música de fondo
        // ¡Ahora usamos el método estático pauseMusic()!
        Music.pauseMusic();

        // 2. Reproducir el texto
        if (tts != null && tts.getLanguage() != null) {
            // Usamos un UtteranceId para que el Listener sepa cuándo terminó
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_SPEAKING_ID");
        } else {
            Log.e("TTS", "TTS no está inicializado o el idioma no está disponible.");

            // Si falla el TTS, reanudamos la música de inmediato
            Music.resumeMusic();
        }
    }

    // Liberación de recursos cuando la Activity se destruye
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        // ¡USAR CLASE MUSIC PARA LIBERAR RECURSOS!
        // Music.stopMusic(); // Si quieres apagar la música permanentemente al salir

        super.onDestroy();
    }

    // Agregamos onPause y onResume para manejar el audio cuando la app pasa a segundo plano
    @Override
    protected void onPause() {
        super.onPause();
        // ¡USAR CLASE MUSIC PARA PAUSAR!
        Music.pauseMusic();

        if (tts != null) {
            tts.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ¡USAR CLASE MUSIC PARA REANUDAR!
        Music.resumeMusic();
    }


    // --- Métodos de la lógica de la aplicación (sin cambios relevantes) ---

    private void loadUserProgressFromFirestore() {
        // ... (método loadUserProgressFromFirestore sin cambios) ...
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
        // ... (método updateButtonStates sin cambios) ...
        setupButton(buttonLevel1, true);
        setupButton(buttonLevel2, highestLevel >= 2);
        setupButton(buttonLevel3, highestLevel >= 3);
        setupButton(buttonLevel4, highestLevel >= 4);
        setupButton(buttonLevel5, highestLevel >= 5);
        setupButton(buttonLevel6, highestLevel >= 6);
        setupButton(buttonLevel7, highestLevel >= 7);
        buttonLevel8.setEnabled(highestLevel >= 8); // Simplificamos la llamada aquí
        setupButton(buttonLevel8, highestLevel >= 8);
    }

    private void setupButton(Button button, boolean habilitado) {
        // ... (método setupButton sin cambios) ...
        button.setEnabled(habilitado);
        button.setAlpha(habilitado ? 1f : 0.5f);
    }

    public void abrirNivel(Fragment fragment) {
        // Detener TTS al cambiar de fragmento
        if (tts != null) tts.stop();

        // ¡USAR CLASE MUSIC PARA REANUDAR!
        Music.resumeMusic();

        tableLevels.setVisibility(TableLayout.GONE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}