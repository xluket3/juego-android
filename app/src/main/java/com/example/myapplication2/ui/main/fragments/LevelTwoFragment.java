package com.example.myapplication2.ui.main.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication2.ui.main.MainActivity2;
import com.example.myapplication2.R;
import com.example.myapplication2.data.FirestoreRepository;
import com.example.myapplication2.ui.main.fragments.LevelThreeFragment; // Asegúrate de tener esta importación

public class LevelTwoFragment extends Fragment {

    private TextView textViewParrafo, textViewPregunta;
    private RadioGroup radioGroup;
    private RadioButton radioCorrecta, radioIncorrecta1, radioIncorrecta2;
    private Button buttonSiguiente, buttonNextLevel;
    private Button buttonAudio; // Declaración para el botón de audio
    private ProgressBar progressBar;

    private int currentQuestion = 0;
    private int score = 0;

    private FirestoreRepository firestoreRepo;

    private String[] parrafos = {
            "Bob Esponja vive en Fondo de Bikini, rodeado de corales y conchas. Su casa es muy especial.",
            "Calamardo trabaja en el Crustaceo Cascarudo, donde atiende y toca su clarinete.",
            "Calamardo toca un instrumento musical que se llama clarinete y practica todos los días.",
            "Arenita practica karate y le encanta entrenar para ser fuerte y ágil.",
            "Gary es un caracol y es la mascota de Bob Esponja, siempre lo acompaña en sus aventuras."
    };

    private String[] preguntas = {
            "¿Dónde vive realmente Bob Esponja?",
            "¿Dónde trabaja Calamardo?",
            "¿Qué instrumento toca Calamardo con frecuencia?",
            "¿Cuál es el pasatiempo favorito de Arenita?",
            "¿Cómo se llama la mascota de Bob Esponja?"
    };

    private String[][] opciones = {
            {"En una concha", "En una piña", "En una roca"},
            {"McDonals", "Pizza Planet", "Cangreburger"},
            {"Guitarra", "Clarinete", "Batería"},
            {"Karate", "Dormir", "Cocinar"},
            {"Gary", "Plankton", "Squilliam"}
    };

    private int[] respuestasCorrectas = {1, 2, 1, 0, 0};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_level_two, container, false);

        textViewParrafo = view.findViewById(R.id.textViewParrafo);
        textViewPregunta = view.findViewById(R.id.textView4);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioCorrecta = view.findViewById(R.id.radioButtonCorrecta);
        radioIncorrecta1 = view.findViewById(R.id.radioButtonIncorrecta1);
        radioIncorrecta2 = view.findViewById(R.id.radioButtonIncorrecta2);
        buttonSiguiente = view.findViewById(R.id.buttonSiguiente);
        progressBar = view.findViewById(R.id.progressBar);
        buttonNextLevel = view.findViewById(R.id.buttonNextLevel);

        // Enlazar el botón de audio.
        buttonAudio = view.findViewById(R.id.button_audio);

        firestoreRepo = new FirestoreRepository();

        progressBar.setMax(preguntas.length);

        mostrarPregunta();

        // --- Lógica para el botón de Audio (TTS) ---
        buttonAudio.setOnClickListener(v -> {
            String textoAPronunciar = parrafos[currentQuestion];

            // Llama al método speakText de la Activity centralizada
            if (getActivity() instanceof MainActivity2) {
                ((MainActivity2) getActivity()).speakText(textoAPronunciar);
            }
        });
        // ---------------------------------------------


        buttonSiguiente.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selected = view.findViewById(selectedId);
                int selectedIndex = radioGroup.indexOfChild(selected);

                if (selectedIndex == respuestasCorrectas[currentQuestion]) {
                    selected.setBackgroundColor(Color.GREEN);
                    score++;
                    progressBar.setProgress(score);

                    for (int i = 0; i < radioGroup.getChildCount(); i++)
                        radioGroup.getChildAt(i).setEnabled(false);

                    currentQuestion++;
                    if (currentQuestion < preguntas.length) {
                        // Solución: Usar getTts() en lugar de .tts.stop()
                        if (getActivity() instanceof MainActivity2) {
                            if (((MainActivity2) getActivity()).getTts() != null) {
                                ((MainActivity2) getActivity()).getTts().stop();
                            }
                        }
                        radioGroup.postDelayed(this::mostrarPregunta, 1000);
                    } else {
                        buttonSiguiente.setEnabled(false);
                        textViewPregunta.setText("¡Has completado el nivel 2!");
                        textViewParrafo.setText("");
                        buttonNextLevel.setVisibility(View.VISIBLE);

                        String uid = firestoreRepo.getCurrentUserId();
                        int levelNumber = 2;
                        firestoreRepo.saveLevelProgress(uid, levelNumber, score);
                    }

                } else {
                    selected.setBackgroundColor(Color.RED);
                    for (int i = 0; i < radioGroup.getChildCount(); i++)
                        radioGroup.getChildAt(i).setEnabled(true);
                }
            }
        });

        buttonNextLevel.setOnClickListener(v -> {
            // Solución: Usar getTts() en lugar de .tts.stop()
            if (getActivity() instanceof MainActivity2) {
                if (((MainActivity2) getActivity()).getTts() != null) {
                    ((MainActivity2) getActivity()).getTts().stop();
                }
                ((MainActivity2) getActivity()).abrirNivel(new LevelThreeFragment());
            }
        });

        return view;
    }

    private void mostrarPregunta() {
        RadioButton[] botones = {radioIncorrecta1, radioCorrecta, radioIncorrecta2};
        for (int i = 0; i < botones.length; i++) {
            botones[i].setText(opciones[currentQuestion][i]);
            botones[i].setEnabled(true);
            botones[i].setBackgroundColor(Color.TRANSPARENT);
        }
        textViewParrafo.setText(parrafos[currentQuestion]);
        textViewPregunta.setText(preguntas[currentQuestion]);
        radioGroup.clearCheck();
    }
}