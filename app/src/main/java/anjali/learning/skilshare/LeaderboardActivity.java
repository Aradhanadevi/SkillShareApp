package anjali.learning.skilshare;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import anjali.learning.skilshare.model.UserModel;

public class LeaderboardActivity extends AppCompatActivity {

    private ListView leaderboardListView;
    private ArrayAdapter<String> leaderboardAdapter;
    private ArrayList<String> leaderboardData;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardListView = findViewById(R.id.leaderboardListView);
        leaderboardData = new ArrayList<>();
        leaderboardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboardData);
        leaderboardListView.setAdapter(leaderboardAdapter);

        // Call the shared helper
        LeaderboardHelper.loadLeaderboard(leaderboardAdapter, leaderboardData);
    }


    private void loadLeaderboard() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<UserModel> userList = new ArrayList<>();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    UserModel user = userSnap.getValue(UserModel.class);
                    if (user != null) {
                        int calculatedLevel = GamificationHelper.calculateLevel(user.xp);
                        userList.add(user);
                        Log.d("LeaderboardDebug", "User: " + user.name + ", XP: " + user.xp + ", Level: " + calculatedLevel);

                    }
                }


                // Sort by XP descending
                Collections.sort(userList, new Comparator<UserModel>() {
                    @Override
                    public int compare(UserModel u1, UserModel u2) {
                        return Integer.compare(u2.xp, u1.xp);
                    }
                });

                leaderboardData.clear();
                for (UserModel user : userList) {
                    int calculatedLevel = GamificationHelper.calculateLevel(user.xp);
                    leaderboardData.add(user.name + " - Level " + calculatedLevel + " (" + user.xp + " XP)");
                }

                leaderboardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed
            }
        });
    }

}
