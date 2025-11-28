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

import com.example.myapplication2.data.FirestoreRepository;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication2.R;
import com.example.myapplication2.ui.main.MainActivity2;
import com.example.myapplication2.ui.main.fragments.LevelTwoFragment;

public class LevelOneFragment extends Fragment {

    private TextView textViewParrafo, textViewPregunta;
    private RadioGroup radioGroup;
    private RadioButton radioCorrecta, radioIncorrecta1, radioIncorrecta2;
    private Button buttonSiguiente;
    private Button buttonAudio; // Botón para activar el audio
    private ProgressBar progressBar;

    private int currentQuestion = 0;
    private int score = 0;

    private FirestoreRepository firestoreRepository;

    private String[] parrafos = {
            "Bob Esponja vive en Fondo de Bikini, un lugar debajo del mar lleno de corales y conchas. Su casa es una piña gigante que brilla con el sol.",
            "Bob Esponja siempre juega y comparte aventuras con su mejor amigo Patricio, una estrella de mar muy divertida y algo distraída.",
            "Nuestro amigo Bob trabaja en el Crustaceo Cascarudo, un restaurante famoso donde cocina hamburguesas muy sabrosas.",
            "Calamardo es el vecino de Bob. Siempre está gruñón y toca el clarinete, pero a veces también se une a las aventuras.",
            "Bob tiene una mascota llamada Gary. Gary es un caracol que hace sonidos de 'miau' y siempre acompaña a Bob en sus aventuras."
    };

    private String[] preguntas = {
            "¿En qué tipo de casa vive Bob Esponja?",
            "¿Quién es el mejor amigo de Bob Esponja?",
            "¿Dónde trabaja Bob Esponja?",
            "¿Cómo se llama el vecino gruñón de Bob?",
            "¿Cuál es el nombre de la mascota de Bob Esponja?"
    };

    private String[][] opciones = {
            {"Casa de árbol", "Piña", "Castillo de arena"},
            {"Arenita", "Calamardo", "Patricio"},
            {"Pizza Planeta", "Crustaceo Cascarudo", "Rough Riders"},
            {"Don Cangrejo", "Patricio", "Calamardo"},
            {"Perry el Ornitorrinco", "Caracolino", "Gary"}
    };

    private int[] respuestasCorrectas = {1, 0, 1, 0, 0};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main3, container, false);

        textViewParrafo = view.findViewById(R.id.textViewParrafo);
        textViewPregunta = view.findViewById(R.id.textView4);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioCorrecta = view.findViewById(R.id.radioButtonCorrecta);
        radioIncorrecta1 = view.findViewById(R.id.radioButtonIncorrecta1);
        radioIncorrecta2 = view.findViewById(R.id.radioButtonIncorrecta2);
        buttonSiguiente = view.findViewById(R.id.buttonSiguiente);
        progressBar = view.findViewById(R.id.progressBar);

        // Enlazar el botón de audio. (Asegúrate de que 'button_audio' exista en tu XML)
        buttonAudio = view.findViewById(R.id.button_audio);

        firestoreRepository = new FirestoreRepository();

        mostrarPregunta();

        // --- Lógica para el botón de Audio (TTS) ---
        buttonAudio.setOnClickListener(v -> {
            String textoAPronunciar = parrafos[currentQuestion];

            // Llamar al método speakText de la Activity
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
                        // Detener audio antes de cambiar de pregunta
                        if (getActivity() instanceof MainActivity2) {
                            if (((MainActivity2) getActivity()).getTts() != null) {
                                ((MainActivity2) getActivity()).getTts().stop();
                            }
                        }
                        radioGroup.postDelayed(this::mostrarPregunta, 1000);
                    } else {
                        textViewPregunta.setText("¡Has completado el nivel!");
                        textViewParrafo.setText("");
                        buttonSiguiente.setText("Siguiente Nivel");
                        buttonSiguiente.setBackgroundColor(Color.parseColor("#4CAF50"));

                        // Detener audio al terminar el nivel
                        if (getActivity() instanceof MainActivity2) {
                            if (((MainActivity2) getActivity()).getTts() != null) {
                                ((MainActivity2) getActivity()).getTts().stop();
                            }
                        }

                        String uid = firestoreRepository.getCurrentUserId();
                        int levelNumber = 1;

                        firestoreRepository.saveLevelProgress(uid, levelNumber, score);

                        buttonSiguiente.setOnClickListener(v2 -> {
                            LevelTwoFragment levelTwoFragment = new LevelTwoFragment();
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, levelTwoFragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                    }

                } else {
                    selected.setBackgroundColor(Color.RED);
                    // Dejar los botones habilitados para que el usuario pueda intentar de nuevo
                    for (int i = 0; i < radioGroup.getChildCount(); i++)
                        radioGroup.getChildAt(i).setEnabled(true);
                }
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

        // Detener el TTS si estaba hablando al cargar la nueva pregunta (CORREGIDO con getTts())
        if (getActivity() instanceof MainActivity2) {
            if (((MainActivity2) getActivity()).getTts() != null) {
                ((MainActivity2) getActivity()).getTts().stop();
            }
        }
    }
}