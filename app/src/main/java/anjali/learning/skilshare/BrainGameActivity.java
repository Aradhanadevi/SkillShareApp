package anjali.learning.skilshare; // replace with your actual package name

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import anjali.learning.skilshare.R;

public class BrainGameActivity extends AppCompatActivity {

    TextView xpTextView, streakTextView, statusTextView;
    Button completeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brain_game); // layout below

        xpTextView = findViewById(R.id.xpTextView);
        streakTextView = findViewById(R.id.streakTextView);
        statusTextView = findViewById(R.id.statusTextView);
        completeButton = findViewById(R.id.completeButton);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "defaultUser");

        loadUserStats(username);

        completeButton.setOnClickListener(v -> {
            updateUserXPAndStreak(username, 10); // +10 XP per brain game
        });
    }

    private void loadUserStats(String username) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long xp = snapshot.child("xp").getValue(Long.class) != null ? snapshot.child("xp").getValue(Long.class) : 0;
                long streak = snapshot.child("streak").getValue(Long.class) != null ? snapshot.child("streak").getValue(Long.class) : 0;

                xpTextView.setText("XP: " + xp);
                streakTextView.setText("Streak: " + streak + " 🔥");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updateUserXPAndStreak(String username, int xpEarned) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                long currentXp = snapshot.child("xp").getValue(Long.class) != null ? snapshot.child("xp").getValue(Long.class) : 0;
                long streak = snapshot.child("streak").getValue(Long.class) != null ? snapshot.child("streak").getValue(Long.class) : 0;
                String lastCompleted = snapshot.child("lastCompletedDate").getValue(String.class);

                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                boolean isNextDay = false;
                if (lastCompleted != null) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date lastDate = sdf.parse(lastCompleted);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(lastDate);
                        cal.add(Calendar.DATE, 1);
                        Date expectedDate = cal.getTime();

                        isNextDay = today.equals(sdf.format(expectedDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                long newXp = currentXp + xpEarned;
                long newStreak = (lastCompleted == null || isNextDay) ? streak + 1 : 1;

                HashMap<String, Object> updates = new HashMap<>();
                updates.put("xp", newXp);
                updates.put("streak", newStreak);
                updates.put("lastCompletedDate", today);

                userRef.updateChildren(updates).addOnSuccessListener(unused -> {
                    Toast.makeText(BrainGameActivity.this, "+10 XP earned!", Toast.LENGTH_SHORT).show();
                    statusTextView.setText("Task completed today ✅");
                    loadUserStats(username); // refresh values
                    completeButton.setEnabled(false);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
