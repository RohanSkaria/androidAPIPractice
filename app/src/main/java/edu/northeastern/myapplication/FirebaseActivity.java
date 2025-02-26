package edu.northeastern.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FirebaseActivity extends AppCompatActivity {

    // Constants
    private static final String PREFS_NAME = "StickerAppPrefs";
    private static final String KEY_USERNAME = "username";

    // UI Components
    private EditText editTextUsername;
    private Button btnLogin;
    private View loginLayout;
    private View mainLayout;
    private TabLayout tabLayout;
    private RecyclerView recyclerViewContent;
    private TextView noDataTextView;

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference messagesRef;

    // Adapters
    private StickerAdapter stickerAdapter;
    private FriendAdapter friendAdapter;

    // Data
    private String currentUsername;
    private List<Sticker> availableStickers;
    private List<String> availableFriends;

    // Currently selected items
    private Sticker selectedSticker;

    private List<StickerMessage> receivedMessages;
    private HistoryAdapter historyAdapter;
    private DatabaseReference stickersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);
//        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        messagesRef = database.getReference("messages");

        // Initialize UI components
        editTextUsername = findViewById(R.id.editTextUsername);
        btnLogin = findViewById(R.id.btnLogin);
        loginLayout = findViewById(R.id.loginLayout);
        mainLayout = findViewById(R.id.mainLayout);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewContent = findViewById(R.id.recyclerViewContent);
        noDataTextView = findViewById(R.id.noDataTextView);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
            currentUsername = null;
            loginLayout.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.GONE);
        });

        // Initialize sticker data
        initializeStickers();

        // Setup login button
        btnLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save username
            currentUsername = username;
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(KEY_USERNAME, currentUsername);
            editor.apply();

            // Register user in Firebase
            registerUserInFirebase(username);

            // Switch to main layout
            loginLayout.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);

            // Setup tabs
            setupTabLayout();

            // Show stickers by default
            setupStickerSelection();
            createNotificationChannel();
            requestNotificationPermission();
            listenForNewMessages();
        });

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, null);
        if (savedUsername != null) {
            currentUsername = savedUsername;
            loginLayout.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);

            // Setup tabs
            setupTabLayout();

            // Show stickers by default
            setupStickerSelection();
            createNotificationChannel();
            requestNotificationPermission();
            listenForNewMessages();
        } else {
            loginLayout.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.GONE);
        }
    }

    private void initializeStickers() {
        availableStickers = new ArrayList<>();

        // Add your stickers here (you should add drawables to your project)
        availableStickers.add(new Sticker("sticker_1", "Happy Face", R.drawable.sticker_happy));
        availableStickers.add(new Sticker("sticker_2", "Sad Face", R.drawable.sticker_sad));
        availableStickers.add(new Sticker("sticker_3", "Thumbs Up", R.drawable.sticker_thumbs_up));
        availableStickers.add(new Sticker("sticker_4", "Heart", R.drawable.sticker_heart));
        availableStickers.add(new Sticker("sticker_5", "Party", R.drawable.sticker_party));
    }

    private void registerUserInFirebase(String username) {
        usersRef.child(username).setValue(true);
    }

    private void setupTabLayout() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Send Sticker"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));
        tabLayout.addTab(tabLayout.newTab().setText("My Stats"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    setupStickerSelection();
                } else if (position == 1) {
                    setupHistoryView();
                } else if (position == 2) {
                    // We'll implement stats view later
                    noDataTextView.setText("Stats view not implemented yet");
                    noDataTextView.setVisibility(View.VISIBLE);
                    recyclerViewContent.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void setupStickerSelection() {
        // Reset selection
        selectedSticker = null;

        // Show stickers grid
        stickerAdapter = new StickerAdapter(availableStickers, this::onStickerSelected);
        recyclerViewContent.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewContent.setAdapter(stickerAdapter);

        noDataTextView.setVisibility(View.GONE);
        recyclerViewContent.setVisibility(View.VISIBLE);
    }

    private void onStickerSelected(Sticker sticker) {
        // Save the selected sticker
        selectedSticker = sticker;

        // Now show friends for selection
        loadFriends();
    }

    private void loadFriends() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                availableFriends = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    if (username != null && !username.equals(currentUsername)) {
                        availableFriends.add(username);
                    }
                }

                if (availableFriends.isEmpty()) {
                    noDataTextView.setText("No friends available. You need other users to send stickers.");
                    noDataTextView.setVisibility(View.VISIBLE);
                    recyclerViewContent.setVisibility(View.GONE);
                } else {
                    noDataTextView.setVisibility(View.GONE);
                    recyclerViewContent.setVisibility(View.VISIBLE);

                    // Show friends list
                    friendAdapter = new FriendAdapter(availableFriends, FirebaseActivity.this::onFriendSelected);
                    recyclerViewContent.setLayoutManager(new LinearLayoutManager(FirebaseActivity.this));
                    recyclerViewContent.setAdapter(friendAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FirebaseActivity.this, "Failed to load friends: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onFriendSelected(String friend) {
        // Send the sticker
        if (selectedSticker != null) {
            sendSticker(selectedSticker, friend);
        }
    }

    private void sendSticker(Sticker sticker, String recipient) {
        // Create message object
        StickerMessage message = new StickerMessage(
                currentUsername,
                recipient,
                sticker.getId(),
                System.currentTimeMillis()
        );

        // Push to database
        String messageId = messagesRef.push().getKey();
        if (messageId != null) {
            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(FirebaseActivity.this, "Sticker sent to " + recipient, Toast.LENGTH_SHORT).show();

                        // Also update sent count (we'll implement this later)
                        updateSentStickerCount(sticker.getId());

                        // Go back to sticker selection
                        setupStickerSelection();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FirebaseActivity.this, "Failed to send sticker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateSentStickerCount(String stickerId) {
        // We'll implement this in the next part
        // This will track how many of each sticker a user has sent
    }

    private void setupHistoryView() {
        loadReceivedMessages();
    }

    private void loadReceivedMessages() {
        messagesRef.orderByChild("recipient").equalTo(currentUsername)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        receivedMessages = new ArrayList<>();

                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            StickerMessage message = messageSnapshot.getValue(StickerMessage.class);
                            if (message != null) {
                                receivedMessages.add(message);
                            }
                        }

                        //timestamping
                        Collections.sort(receivedMessages, (m1, m2) ->
                                Long.compare(m2.getTimestamp(), m1.getTimestamp()));

                        if (receivedMessages.isEmpty()) {
                            noDataTextView.setText("No stickers received yet");
                            noDataTextView.setVisibility(View.VISIBLE);
                            recyclerViewContent.setVisibility(View.GONE);
                        } else {
                            noDataTextView.setVisibility(View.GONE);
                            recyclerViewContent.setVisibility(View.VISIBLE);


                            Map<String, Integer> stickerResources = new HashMap<>();
                            for (Sticker sticker : availableStickers) {
                                stickerResources.put(sticker.getId(), sticker.getResourceId());
                            }


                            historyAdapter = new HistoryAdapter(receivedMessages, stickerResources, FirebaseActivity.this);
                            recyclerViewContent.setLayoutManager(new LinearLayoutManager(FirebaseActivity.this));
                            recyclerViewContent.setAdapter(historyAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(FirebaseActivity.this, "Failed to load history: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "StickerChannel";
            String description = "Channel for sticker notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("STICKER_CHANNEL", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void showNotification(String sender, String stickerId) {
        // check authroity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(this, FirebaseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "STICKER_CHANNEL")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("New Sticker Received!")
                .setContentText(sender + " sent you a sticker!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void listenForNewMessages() {
        messagesRef
                .orderByChild("recipient")
                .equalTo(currentUsername)
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        StickerMessage newMessage = snapshot.getValue(StickerMessage.class);
                        if (newMessage != null) {
                            String sender = newMessage.getSender();
                            String stickerId = newMessage.getStickerId();
//                            Log.d("FirebaseActivity", "New Sticker from " + sender + "，stickerId=" + stickerId);
                            showNotification(sender, stickerId);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseActivity", "error message: " + error.getMessage());
                    }
                });
    }
}

