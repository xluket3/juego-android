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

public class LevelFourFragment extends Fragment {

    private TextView textViewParrafo, textViewPregunta;
    private RadioGroup radioGroup;
    private RadioButton radioCorrecta, radioIncorrecta1, radioIncorrecta2;
    private Button buttonSiguiente, buttonNextLevel;
    private ProgressBar progressBar;

    private int currentQuestion = 0;
    private int score = 0;

    private FirestoreRepository firestoreRepo;

    private String[] parrafos = {
            "El Crustáceo Cascarudo (Krusty Krab) es el restaurante de comida rápida más popular de la ciudad. Su éxito se debe a la deliciosa y secreta receta de la Cangreburger.",
            "La casa de Bob Esponja es inconfundible. Es una piña de dos pisos con una biblioteca completa en su interior. Justo al lado, vive Calamardo en una cabeza de piedra Tiki.",
            "El Balde de Carnada (Chum Bucket) es el restaurante rival, propiedad de Plankton. A diferencia del Crustáceo Cascarudo, este lugar casi siempre está vacío porque su comida es horrible.",
            "Campos de Medusas es el lugar favorito de Bob Esponja y Patricio para pasar el rato. Pasan horas allí con sus redes, intentando atrapar medusas y recolectar su jalea.",
            "Laguna Pegajosa (Goo Lagoon) es la playa principal de Fondo de Bikini. Es un lugar popular para tomar el sol, levantar pesas (como lo hace Larry la Langosta) y surfear."
    };

    private String[] preguntas = {
            "¿A qué se debe el éxito del Crustáceo Cascarudo?",
            "¿Qué forma tiene la casa de Calamardo?",
            "¿Por qué el Balde de Carnada casi siempre está vacío?",
            "¿Qué actividad realizan Bob Esponja y Patricio en los Campos de Medusas?",
            "¿Quién es el personaje que suele levantar pesas en Laguna Pegajosa?"
    };

    private String[][] opciones = {
            {"A sus bajos precios", "A la Cangreburger", "A sus postres"},
            {"Un ancla", "Una cabeza Tiki", "Una bota"},
            {"Es muy caro", "Cierra muy temprano", "Su comida es horrible"},
            {"Nadar", "Tomar sol", "Atrapar medusas"},
            {"Larry la Langosta", "Don Cangrejo", "Arenita"}
    };

    private int[] respuestasCorrectas = {
            1,
            1,
            2,
            2,
            0
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_level_four, container, false);

        textViewParrafo = view.findViewById(R.id.textViewParrafo);
        textViewPregunta = view.findViewById(R.id.textView4);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioCorrecta = view.findViewById(R.id.radioButtonCorrecta);
        radioIncorrecta1 = view.findViewById(R.id.radioButtonIncorrecta1);
        radioIncorrecta2 = view.findViewById(R.id.radioButtonIncorrecta2);
        buttonSiguiente = view.findViewById(R.id.buttonSiguiente);
        progressBar = view.findViewById(R.id.progressBar);
        buttonNextLevel = view.findViewById(R.id.buttonNextLevel);

        firestoreRepo = new FirestoreRepository();

        progressBar.setMax(preguntas.length);

        mostrarPregunta();

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
                        radioGroup.postDelayed(this::mostrarPregunta, 1000);
                    } else {
                        buttonSiguiente.setEnabled(false);
                        textViewPregunta.setText("¡Has completado el nivel 4!");
                        textViewParrafo.setText("");
                        buttonNextLevel.setVisibility(View.VISIBLE);

                        String uid = firestoreRepo.getCurrentUserId();
                        int levelNumber = 4;
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
            if (getActivity() instanceof MainActivity2) {
                // ((MainActivity2) getActivity()).abrirNivel(new LevelFiveFragment());
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