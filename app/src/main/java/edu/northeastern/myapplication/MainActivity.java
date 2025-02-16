package edu.northeastern.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnApi = findViewById(R.id.buttonApi);
        Button btnFirebase = findViewById(R.id.buttonFirebase);


        btnApi.setOnClickListener(view -> {
            Intent intent = new Intent(this, ApiActivity.class);
            startActivity(intent);
        });

        btnFirebase.setOnClickListener(view -> {
            Intent intent = new Intent(this, FirebaseActivity.class);
            startActivity(intent);
        });
    }
}