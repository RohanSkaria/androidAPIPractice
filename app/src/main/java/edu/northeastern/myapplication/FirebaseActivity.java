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
    private static final String TAG = "FirebaseActivity";

    // UI
    private LinearLayout loginPanel, mainPanel, aboutPanel;
    private EditText etUsername, etPassword, etFriendUsername;
    private Button btnLogin, btnSendSticker, btnAbout, btnBackFromAbout;
    private Spinner spinnerStickers;
    private TextView tvGroupName, tvStickerCounts, tvHistory;

    // Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;

    // The user currently logged in
    private String currentUser = null;

    // For notifications
    private static final String CHANNEL_ID = "stickers_channel_id";

    // Example sticker set
    private static final String[] STICKER_IDS = {
            "sticker_heart", "sticker_smile", "sticker_thumbs_up"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();

        // Create notification channel (Android 8.0+)
        createNotificationChannel();

        // Find views
        loginPanel       = findViewById(R.id.loginPanel);
        mainPanel        = findViewById(R.id.mainPanel);
        aboutPanel       = findViewById(R.id.aboutPanel);
        etUsername       = findViewById(R.id.etUsername);
        etPassword       = findViewById(R.id.etPassword);
        etFriendUsername = findViewById(R.id.etFriendUsername);
        btnLogin         = findViewById(R.id.btnLogin);
        btnSendSticker   = findViewById(R.id.btnSendSticker);
        btnAbout         = findViewById(R.id.btnAbout);
        btnBackFromAbout = findViewById(R.id.btnBackFromAbout);
        spinnerStickers  = findViewById(R.id.spinnerStickers);
        tvGroupName      = findViewById(R.id.tvGroupName);
        tvStickerCounts  = findViewById(R.id.tvStickerCounts);
        tvHistory        = findViewById(R.id.tvHistory);

        // Spinner with sticker IDs
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                STICKER_IDS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStickers.setAdapter(adapter);

        // Click listeners
        btnLogin.setOnClickListener(v -> doLogin());
        btnSendSticker.setOnClickListener(v -> sendSticker());
        btnAbout.setOnClickListener(v -> showAboutPanel(true));
        btnBackFromAbout.setOnClickListener(v -> showAboutPanel(false));
    }

    private void doLogin() {
        String enteredUser = etUsername.getText().toString().trim();
        String enteredPass = etPassword.getText().toString().trim();

        if (enteredUser.isEmpty()) {
            Toast.makeText(this, "Please enter a username!", Toast.LENGTH_SHORT).show();
            return;
        }
        // ANY password is accepted per assignment instructions
        // We do NOT validate 'enteredPass' at all.

        currentUser = enteredUser;

        // (Optional) Create/Update user record in Firebase
        DatabaseReference userRef = rootRef.child("users").child(currentUser);
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", currentUser);
        userData.put("password", enteredPass); // storing it, but not validating
        userData.put("lastLogin", System.currentTimeMillis());
        userRef.updateChildren(userData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User record created/updated in Firebase"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update user record: " + e.getMessage()));

        // Show message
        Toast.makeText(this, "Logged in as " + currentUser
                + " (password ignored)", Toast.LENGTH_LONG).show();

        // Switch panels
        loginPanel.setVisibility(View.GONE);
        mainPanel.setVisibility(View.VISIBLE);

        // Start listening for incoming stickers
        setupReceiveStickersListener();

        // Load counts and history from Firebase
        loadStickerCounts();
        loadReceivedHistory();
    }

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

        // Write a new "received" record for friendUser
        DatabaseReference friendInboxRef = rootRef.child("stickersReceived").child(friendUser);
        String key = friendInboxRef.push().getKey();
        if (key == null) {
            Log.e(TAG, "Failed to get push key!");
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

        // Update the count for the currentUser
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

    private void setupReceiveStickersListener() {
        DatabaseReference inboxRef = rootRef.child("stickersReceived").child(currentUser);
        inboxRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                // A new sticker was received
                Map<String, Object> stickerData = (Map<String, Object>) snapshot.getValue();
                if (stickerData != null) {
                    String from = (String) stickerData.get("from");
                    String stickerId = (String) stickerData.get("stickerId");

                    // Show notification
                    showStickerNotification(from, stickerId);

                    // Reload history
                    loadReceivedHistory();
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showStickerNotification(String fromUser, String stickerId) {
        String title = "New Sticker from " + fromUser;
        String text = "Sticker: " + stickerId;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.star_big_on) // example icon
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("You received " + stickerId + " from " + fromUser + "!"))
                        .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
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
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sticker Channel";
            String desc = "Channel for sticker notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(desc);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void loadStickerCounts() {
        if (currentUser == null) return;
        DatabaseReference countRef = rootRef.child("users").child(currentUser).child("sentCount");
        countRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder sb = new StringBuilder("Counts of Stickers Sent:\n");
                for (DataSnapshot child : snapshot.getChildren()) {
                    String stickerId = child.getKey();
                    Long count = child.getValue(Long.class);
                    sb.append(stickerId).append(" : ").append(count).append("\n");
                }
                tvStickerCounts.setText(sb.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadStickerCounts cancelled: " + error.getMessage());
            }
        });
    }

    private void loadReceivedHistory() {
        if (currentUser == null) return;
        DatabaseReference inboxRef = rootRef.child("stickersReceived").child(currentUser);
        inboxRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder sb = new StringBuilder("History of Received Stickers:\n");
                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> data = (Map<String, Object>) child.getValue();
                    if (data == null) continue;

                    String from = (String) data.get("from");
                    String stickerId = (String) data.get("stickerId");
                    Long timestamp = (Long) data.get("timestamp");

                    sb.append("From: ").append(from)
                            .append(" | Sticker: ").append(stickerId)
                            .append(" | When: ").append(timestamp)
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

    private void showAboutPanel(boolean show) {
        aboutPanel.setVisibility(show ? View.VISIBLE : View.GONE);
        mainPanel.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
