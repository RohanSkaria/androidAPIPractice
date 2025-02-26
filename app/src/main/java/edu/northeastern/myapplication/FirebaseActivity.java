package edu.northeastern.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseActivity extends AppCompatActivity {

    private static final String TAG = "FirebaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        // Initialize Firebase (optional if using the google-services plugin; safe to call anyway)
        FirebaseApp.initializeApp(this);

        // Get a reference to the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("testNode");

        // Write a simple value to "testNode"
        dbRef.setValue("Hello from FirebaseActivity!")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Write succeeded!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Write failed: " + e.getMessage());
                });
    }
}
