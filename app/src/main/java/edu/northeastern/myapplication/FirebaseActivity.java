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
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
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
// NOTE: For counting, we use ValueEventListener below
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "StickerAppPrefs";
    private static final String KEY_USERNAME = "username";

    private static final String CHANNEL_ID = "sticker_notifications";
    private static final int NOTIFICATION_ID = 1;
    private static final int PERMISSION_REQUEST_CODE = 123;

    private EditText editTextUsername;
    private Button btnLogin;
    private View loginLayout;
    private View mainLayout;
    private TabLayout tabLayout;
    private RecyclerView recyclerViewContent;
    private TextView noDataTextView;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference messagesRef;

    private StickerAdapter stickerAdapter;
    private FriendAdapter friendAdapter;

    private String currentUsername;
    private List<Sticker> availableStickers;
    private List<String> availableFriends;

    private Sticker selectedSticker;

    private List<StickerMessage> receivedMessages;
    private HistoryAdapter historyAdapter;
    private DatabaseReference stickersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        messagesRef = database.getReference("messages");

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

        Button btnAbout = findViewById(R.id.btnAbout);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                Intent intent = new Intent(FirebaseActivity.this, AboutActivity.class);
                startActivity(intent);
            });
        }

        initializeStickers();

        btnLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUsername = username;
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(KEY_USERNAME, currentUsername);
            editor.apply();

            registerUserInFirebase(username);

            loginLayout.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);

            setupTabLayout();

            setupStickerSelection();
            createNotificationChannel();
            requestNotificationPermission();
            listenForNewMessages();
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, null);
        if (savedUsername != null) {
            currentUsername = savedUsername;
            loginLayout.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);

            setupTabLayout();

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
                    // REPLACED: Instead of "Stats view not implemented yet",
                    // we call a method that loads user stats from Firebase.
                    setupStatsView();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (recyclerViewContent != null && recyclerViewContent.getLayoutManager() instanceof GridLayoutManager) {
                ((GridLayoutManager) recyclerViewContent.getLayoutManager()).setSpanCount(5);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (recyclerViewContent != null && recyclerViewContent.getLayoutManager() instanceof GridLayoutManager) {
                ((GridLayoutManager) recyclerViewContent.getLayoutManager()).setSpanCount(3);
            }
        }
    }

    private void setupStickerSelection() {
        selectedSticker = null;

        int spanCount = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE ? 5 : 3;

        stickerAdapter = new StickerAdapter(availableStickers, this::onStickerSelected);
        recyclerViewContent.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerViewContent.setAdapter(stickerAdapter);

        noDataTextView.setVisibility(View.GONE);
        recyclerViewContent.setVisibility(View.VISIBLE);
    }

    private void onStickerSelected(Sticker sticker) {
        selectedSticker = sticker;
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

                    friendAdapter = new FriendAdapter(availableFriends, FirebaseActivity.this::onFriendSelected);
                    recyclerViewContent.setLayoutManager(new LinearLayoutManager(FirebaseActivity.this));
                    recyclerViewContent.setAdapter(friendAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FirebaseActivity.this, "Failed to load friends: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onFriendSelected(String friend) {
        if (selectedSticker != null) {
            sendSticker(selectedSticker, friend);
        }
    }

    private void sendSticker(Sticker sticker, String recipient) {
        StickerMessage message = new StickerMessage(
                currentUsername,
                recipient,
                sticker.getId(),
                System.currentTimeMillis()
        );

        String messageId = messagesRef.push().getKey();
        if (messageId != null) {
            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(FirebaseActivity.this, "Sticker sent to " + recipient,
                                Toast.LENGTH_SHORT).show();

                        // Call the method to increment the "sent sticker" count
                        updateSentStickerCount(sticker.getId());

                        setupStickerSelection();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FirebaseActivity.this, "Failed to send sticker: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // ADDED: This method increments the count of how many times the user has sent a particular sticker.
    private void updateSentStickerCount(String stickerId) {
        // We'll store counts under "users/{username}/stickerCounts/{stickerId}"
        DatabaseReference stickerCountRef = usersRef
                .child(currentUsername)
                .child("stickerCounts")
                .child(stickerId);

        stickerCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentCount = 0;
                if (snapshot.exists()) {
                    Long val = snapshot.getValue(Long.class);
                    if (val != null) {
                        currentCount = val;
                    }
                }
                // Increment and save
                stickerCountRef.setValue(currentCount + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
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

                        // Sort by timestamp descending
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
                        Toast.makeText(FirebaseActivity.this, "Failed to load history: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ADDED: Loads the user's sticker-sent counts and displays in a RecyclerView
    private void setupStatsView() {
        DatabaseReference stickerCountsRef = usersRef
                .child(currentUsername)
                .child("stickerCounts");

        stickerCountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Long> statsMap = new HashMap<>();
                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    String stickerId = childSnap.getKey();
                    Long count = childSnap.getValue(Long.class);
                    if (stickerId != null && count != null) {
                        statsMap.put(stickerId, count);
                    }
                }

                if (statsMap.isEmpty()) {
                    noDataTextView.setText("No stats yet. Send a sticker first!");
                    noDataTextView.setVisibility(View.VISIBLE);
                    recyclerViewContent.setVisibility(View.GONE);
                } else {
                    noDataTextView.setVisibility(View.GONE);
                    recyclerViewContent.setVisibility(View.VISIBLE);

                    // Use the StatsAdapter (defined in a separate file)
                    StatsAdapter statsAdapter = new StatsAdapter(statsMap, availableStickers);
                    recyclerViewContent.setLayoutManager(new LinearLayoutManager(FirebaseActivity.this));
                    recyclerViewContent.setAdapter(statsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FirebaseActivity.this, "Failed to load stats: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sticker Notifications";
            String description = "Channel for sticker app notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications disabled. You won't be notified of new stickers.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showNotification(String sender, String stickerId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
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

    private void showStickerNotification(StickerMessage message) {
        int stickerResId = R.drawable.sticker_unknown;
        for (Sticker sticker : availableStickers) {
            if (sticker.getId().equals(message.getStickerId())) {
                stickerResId = sticker.getResourceId();
                break;
            }
        }

        Intent intent = new Intent(this, FirebaseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), stickerResId))
                .setContentTitle("New Sticker Received!")
                .setContentText("You received a sticker from " + message.getSender())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void listenForNewMessages() {
        messagesRef.orderByChild("recipient").equalTo(currentUsername)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        StickerMessage message = snapshot.getValue(StickerMessage.class);
                        if (message != null) {
                            long currentTime = System.currentTimeMillis();
                            // Only notify if the message is recent (e.g., within the last 60s)
                            if (currentTime - message.getTimestamp() < 60000) {
                                showStickerNotification(message);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
}
