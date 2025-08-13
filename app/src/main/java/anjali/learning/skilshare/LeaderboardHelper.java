package anjali.learning.skilshare;

import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import anjali.learning.skilshare.model.UserModel;

public class LeaderboardHelper {

    public static void loadLeaderboard(ArrayAdapter<String> adapter, ArrayList<String> dataList) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<UserModel> userList = new ArrayList<>();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    UserModel user = userSnap.getValue(UserModel.class);
                    if (user != null) {
                        int calculatedLevel = GamificationHelper.calculateLevel(user.xp);
                        Log.d("LeaderboardDebug", "User: " + user.name + ", XP: " + user.xp + ", Level: " + calculatedLevel);
                        userList.add(user);
                    }
                }

                Collections.sort(userList, new Comparator<UserModel>() {
                    @Override
                    public int compare(UserModel u1, UserModel u2) {
                        return Integer.compare(u2.xp, u1.xp);
                    }
                });

                dataList.clear();
                for (UserModel user : userList) {
                    int calculatedLevel = GamificationHelper.calculateLevel(user.xp);
                    dataList.add(user.name + " - Level " + calculatedLevel + " (" + user.xp + " XP)");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
