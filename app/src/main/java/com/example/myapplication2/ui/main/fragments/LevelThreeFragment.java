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

public class LevelThreeFragment extends Fragment {

    private TextView textViewParrafo, textViewPregunta;
    private RadioGroup radioGroup;
    private RadioButton radioCorrecta, radioIncorrecta1, radioIncorrecta2;
    private Button buttonSiguiente, buttonNextLevel;
    private ProgressBar progressBar;

    private int currentQuestion = 0;
    private int score = 0;

    private FirestoreRepository firestoreRepo;

    private String[] parrafos = {
            "Don Cangrejo es el propietario del Crustáceo Cascarudo. Es un cangrejo extremadamente tacaño y su único amor verdadero, además de su hija Perlita, es el dinero.",
            "Patricio Estrella es el mejor amigo de Bob Esponja. Es una estrella de mar de color rosa que vive debajo de una gran roca, justo al lado de la casa de Calamardo.",
            "Plankton es el diminuto archienemigo de Don Cangrejo. Su objetivo en la vida es robar la fórmula secreta de la Cangreburger para su propio restaurante, el Balde de Carnada.",
            "La Señora Puff dirige la Escuela de Botes de Fondo de Bikini. Ha intentado incansablemente enseñar a Bob Esponja a conducir, pero él siempre falla el examen de manejo.",
            "Perlita es la hija adolescente de Don Cangrejo. A pesar de que su padre es un cangrejo, ella es una enorme ballena cachalote a la que le encanta ir de compras."
    };

    private String[] preguntas = {
            "¿Qué es lo que más ama Don Cangrejo?",
            "¿Dónde vive Patricio Estrella?",
            "¿Cómo se llama el restaurante de Plankton?",
            "¿Qué intenta enseñarle la Señora Puff a Bob Esponja?",
            "¿Qué tipo de animal es Perlita?"
    };

    private String[][] opciones = {
            {"Las Cangreburgers", "El dinero", "Calamardo"},
            {"Debajo de una roca", "En un ancla", "En una piña"},
            {"El Crustáceo Cascarudo", "El Balde de Carnada", "La Carnada Sabrosa"},
            {"A cocinar", "A pescar medusas", "A conducir un bote"},
            {"Un tiburón", "Una ballena", "Un cangrejo"}
    };


    private int[] respuestasCorrectas = {1, 0, 1, 2, 1};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_level_three, container, false);

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
                        textViewPregunta.setText("¡Has completado el nivel 3!");
                        textViewParrafo.setText("");
                        buttonNextLevel.setVisibility(View.VISIBLE);

                        String uid = firestoreRepo.getCurrentUserId();
                        int levelNumber = 3;
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
                ((MainActivity2) getActivity()).abrirNivel(new LevelFourFragment());
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