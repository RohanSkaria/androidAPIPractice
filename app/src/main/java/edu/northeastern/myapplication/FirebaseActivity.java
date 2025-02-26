package edu.northeastern.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class FirebaseActivity extends AppCompatActivity {
    private static final String TAG = "FrebaeActivity";

    // UI Elements
    private EditText etUsername, etFriendUsername;
    private Button btnLogin, btnSendSticker, btnAbout, btnBackFromAbout;
    private LinearLayout loginPanel, mainPanel, aboutPanel;
    private Spinner spinnerStickers;
    private TextView tvStickerCounts, tvHistory, tvGroupName;

    // Firebase references
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;

    // Local user
    private String currentUser = null;

    // Notification channel
    private static final String CHANNEL_ID = "stickers_channel_id";

    // Some example sticker IDs (could map to resource drawables in your code)
    // In a real app, you'd store these images in res/drawable and reference them by ID.
    // Here we’re just giving them symbolic names.
    private static final String[] STICKER_IDS = {"sticker_heart", "sticker_smile", "sticker_thumbs_up", "unknown_sticker"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        // 1) Initialize Firebase
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();

        // 2) Setup Notification Channel (for Android 8.0+)
        createNotificationChannel();

        // 3) Find Views
        loginPanel       = findViewById(R.id.loginPanel);
        mainPanel        = findViewById(R.id.mainPanel);
        aboutPanel       = findViewById(R.id.aboutPanel);
        etUsername       = findViewById(R.id.etUsername);
        etFriendUsername = findViewById(R.id.etFriendUsername);
        btnLogin         = findViewById(R.id.btnLogin);
        btnSendSticker   = findViewById(R.id.btnSendSticker);
        btnAbout         = findViewById(R.id.btnAbout);
        btnBackFromAbout = findViewById(R.id.btnBackFromAbout);
        spinnerStickers  = findViewById(R.id.spinnerStickers);
        tvStickerCounts  = findViewById(R.id.tvStickerCounts);
        tvHistory        = findViewById(R.id.tvHistory);
        tvGroupName      = findViewById(R.id.tvGroupName);

        // 4) Populate sticker spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                STICKER_IDS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStickers.setAdapter(adapter);

        // 5) Set up click listeners
        btnLogin.setOnClickListener(v -> doLogin());
        btnSendSticker.setOnClickListener(v -> sendSticker());
        btnAbout.setOnClickListener(v -> showAboutPanel(true));
        btnBackFromAbout.setOnClickListener(v -> showAboutPanel(false));

        // 6) Show group name on main screen
        // (You can hardcode or load from strings.xml)
        tvGroupName.setText("Group: MyAwesomeGroup");

        // If you want to auto-login a previously used username, you could read from SharedPreferences here.
        // For the assignment, a simple approach is to have the user type in a name each time.
    }

    // ------------------------------------------------------------------------------
    // LOGIN LOGIC (No Password)
    // ------------------------------------------------------------------------------

    private void doLogin() {
        String enteredUser = etUsername.getText().toString().trim();
        if (enteredUser.isEmpty()) {
            Toast.makeText(this, "Please enter a username!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Accept this username with no password check
        currentUser = enteredUser;

        // Optionally store in SharedPreferences if you want to persist next time
        // SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        // prefs.edit().putString("username", currentUser).apply();

        Toast.makeText(this, "Logged in as " + currentUser, Toast.LENGTH_SHORT).show();
        showLoginPanel(false);
        setupReceiveStickersListener();
        loadStickerCounts();
        loadReceivedHistory();
    }

    private void showLoginPanel(boolean show) {
        loginPanel.setVisibility(show ? View.VISIBLE : View.GONE);
        mainPanel.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ------------------------------------------------------------------------------
    // SENDING STICKERS
    // ------------------------------------------------------------------------------

    private void sendSticker() {
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String friendUser = etFriendUsername.getText().toString().trim();
        if (friendUser.isEmpty()) {
            Toast.makeText(this, "Enter your friend's username!", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedSticker = (String) spinnerStickers.getSelectedItem();
        if (selectedSticker == null) {
            Toast.makeText(this, "No sticker selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Write to /stickersReceived/<friendUser>/someAutoKey with {from, stickerId, timestamp}
        DatabaseReference friendInboxRef = rootRef.child("stickersReceived").child(friendUser);
        String key = friendInboxRef.push().getKey();
        if (key == null) {
            Log.e(TAG, "Failed to get push key for friendInboxRef!");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("from", currentUser);
        data.put("stickerId", selectedSticker);
        data.put("timestamp", System.currentTimeMillis());
        friendInboxRef.child(key).setValue(data)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Sent sticker " + selectedSticker + " to " + friendUser))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to send sticker: " + e.getMessage()));

        // 2) Update the “count” of how many times currentUser has sent that sticker
        DatabaseReference senderCountRef = rootRef
                .child("users")
                .child(currentUser)
                .child("sentCount")
                .child(selectedSticker);

        senderCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long currentValue = currentData.getValue(Long.class);
                if (currentValue == null) {
                    currentData.setValue(1L);
                } else {
                    currentData.setValue(currentValue + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed: " + error.getMessage());
                } else {
                    Log.d(TAG, "Sticker count updated successfully.");
                }
            }
        });

        Toast.makeText(this, "Sticker sent to " + friendUser + "!", Toast.LENGTH_SHORT).show();
    }

    // ------------------------------------------------------------------------------
    // RECEIVE STICKERS (LISTENER + NOTIFICATION)
    // ------------------------------------------------------------------------------

    private void setupReceiveStickersListener() {
        // Listen for new children in /stickersReceived/<currentUser>
        DatabaseReference myInboxRef = rootRef.child("stickersReceived").child(currentUser);
        myInboxRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                // A new sticker was received
                Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                if (data == null) return;

                String from = (String) data.get("from");
                String stickerId = (String) data.get("stickerId");

                // Show a notification with more than just text
                showStickerNotification(from, stickerId);

                // Reload history to show the newly received sticker
                loadReceivedHistory();
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showStickerNotification(String fromUser, String stickerId) {
        // For the assignment, we want to show something more than plain text.
        // Example: set a small icon, big text, or large icon style, etc.
        String contentTitle = "New Sticker from " + fromUser;
        String contentText = "Sticker: " + stickerId;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.star_big_on) // Just an example icon
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        // Example of adding "big text style" to be more than plain text:
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("You received sticker: " + stickerId + "\nTap to view."))
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        // Notification channels are required on Android 8.0+ for posting notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Stickers Channel";
            String description = "Channel for sticker notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // ------------------------------------------------------------------------------
    // DISPLAY COUNTS OF STICKERS SENT
    // ------------------------------------------------------------------------------

    private void loadStickerCounts() {
        if (currentUser == null) return;
        DatabaseReference countRef = rootRef.child("users").child(currentUser).child("sentCount");
        countRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Build a display string
                StringBuilder sb = new StringBuilder("Counts of Stickers Sent:\n");
                for (DataSnapshot child : snapshot.getChildren()) {
                    String stickerId = child.getKey();
                    Long count = child.getValue(Long.class);
                    sb.append(stickerId).append(": ").append(count).append("\n");
                }
                tvStickerCounts.setText(sb.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadStickerCounts cancelled: " + error.getMessage());
            }
        });
    }

    // ------------------------------------------------------------------------------
    // SHOW HISTORY OF STICKERS RECEIVED
    // ------------------------------------------------------------------------------

    private void loadReceivedHistory() {
        if (currentUser == null) return;
        DatabaseReference inboxRef = rootRef.child("stickersReceived").child(currentUser);
        inboxRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder sb = new StringBuilder("History of Stickers Received:\n");
                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> data = (Map<String, Object>) child.getValue();
                    if (data == null) continue;

                    String from = (String) data.get("from");
                    String stickerId = (String) data.get("stickerId");
                    Long timestamp = (Long) data.get("timestamp");

                    // Handle unknown sticker ID gracefully
                    String displaySticker = stickerId;
                    if (!isKnownStickerId(stickerId)) {
                        displaySticker = "Unknown Sticker";
                    }

                    sb.append("From: ").append(from)
                            .append(" | Sticker: ").append(displaySticker)
                            .append(" | When: ").append(timestampToString(timestamp))
                            .append("\n");
                }
                tvHistory.setText(sb.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadReceivedHistory cancelled: " + error.getMessage());
            }
        });
    }

    private boolean isKnownStickerId(String stickerId) {
        for (String s : STICKER_IDS) {
            if (s.equals(stickerId)) return true;
        }
        return false;
    }

    private String timestampToString(Long timestamp) {
        if (timestamp == null) return "Unknown time";
        // Could convert to a Date format, but for simplicity:
        return String.valueOf(timestamp);
    }

    // ------------------------------------------------------------------------------
    // ABOUT SCREEN
    // ------------------------------------------------------------------------------

    private void showAboutPanel(boolean show) {
        aboutPanel.setVisibility(show ? View.VISIBLE : View.GONE);
        mainPanel.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
