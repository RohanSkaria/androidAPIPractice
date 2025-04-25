package edu.northeastern.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        TextView groupNameTextView = findViewById(R.id.group_name);
        groupNameTextView.setText("Group Name: Group 10");


        TextView membersTextView = findViewById(R.id.team_members);
        String members = "Team Members:\n" +
                "Madisen Patrick\n" +
                "Wilem Santry\n" +
                "Parwaz Sarao\n" +
                "Yunmu Shu\n" +
                "Rohan Skaria";
        membersTextView.setText(members);
    }
}
