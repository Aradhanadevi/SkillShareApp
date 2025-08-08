package anjali.learning.skilshare;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class GamificationHelper {

    private static final int XP_PER_LEVEL = 100; // change if you want faster/slower leveling
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static int calculateLevel(int xp) {
        return (xp / 100) + 1; // Example: 0–99 XP = Level 1, 100–199 XP = Level 2
    }

    public static int xpForNextLevel(int xp) {
        int currentLevel = calculateLevel(xp);
        return (currentLevel * 100) - xp;
    }
    public static void addXP(String uid, int earnedXP) {
        DatabaseReference userRef = database.getReference("users").child(uid);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                long currentXP = snapshot.child("xp").getValue(Long.class) != null
                        ? snapshot.child("xp").getValue(Long.class) : 0;

                long newXP = currentXP + earnedXP;
                long newLevel = newXP / XP_PER_LEVEL;

                Map<String, Object> updates = new HashMap<>();
                updates.put("xp", newXP);
                updates.put("level", newLevel);

                userRef.updateChildren(updates);
            }
        });
    }

    public static void updateStreak(String uid) {
        DatabaseReference userRef = database.getReference("users").child(uid);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                long lastPlayed = snapshot.child("lastPlayed").getValue(Long.class) != null
                        ? snapshot.child("lastPlayed").getValue(Long.class) : 0;

                long currentStreak = snapshot.child("streak").getValue(Long.class) != null
                        ? snapshot.child("streak").getValue(Long.class) : 0;

                long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24); // days since epoch
                long lastPlayedDay = lastPlayed / (1000 * 60 * 60 * 24);

                if (today == lastPlayedDay + 1) {
                    currentStreak++;
                } else if (today != lastPlayedDay) {
                    currentStreak = 1;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("streak", currentStreak);
                updates.put("lastPlayed", System.currentTimeMillis());

                userRef.updateChildren(updates);
            }
        });
    }

    public static void rewardQuizCompletion(int earnedXP) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        addXP(uid, earnedXP);
        updateStreak(uid);
    }
}
