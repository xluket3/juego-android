package com.example.myapplication2.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.Map;
import java.util.HashMap;

public class FirestoreRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public void saveLevelProgress(String uid, int levelNumber, int score) {
        if (uid == null) {
            System.out.println("Error: No se puede guardar progreso, usuario no logueado.");
            return;
        }

        Map<String, Object> levelData = new HashMap<>();
        levelData.put("score", score);
        levelData.put("completed", true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("level_progress.level" + levelNumber, levelData);

        updates.put("highest_level_unlocked", levelNumber + 1);

        db.collection("users").document(uid)
                .set(updates, SetOptions.merge()) //
                .addOnSuccessListener(aVoid -> {
                    System.out.println("¡Progreso del Nivel " + levelNumber + " guardado en Firestore!");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error al guardar progreso: " + e.getMessage());
                });
    }
    public void createNewUserDocument(String uid, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("highest_level_unlocked", 1);
        userData.put("level_progress", new HashMap<>());

        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("¡Documento de usuario creado!");
                });
    }

    public interface UserDataCallback {
        void onDataLoaded(DocumentSnapshot document);

        void onError(Exception e);
    }


    public void getUserData(String uid, final UserDataCallback callback) {
        if (uid == null) {
            callback.onError(new Exception("El UID del usuario es nulo."));
            return;
        }

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onDataLoaded(documentSnapshot);
                    } else {
                        callback.onError(new Exception("El documento del usuario no existe."));
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }
}
