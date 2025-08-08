package anjali.learning.skilshare;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import anjali.learning.skilshare.Adapter.TutorAdapter;
import anjali.learning.skilshare.model.UserModel;

public class LearnRequestedSkill extends AppCompatActivity {

    RecyclerView recyclerView;
    TutorAdapter adapter;
    ArrayList<UserModel> tutorList;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_requested_skill);

        recyclerView = findViewById(R.id.recyclerTutors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tutorList = new ArrayList<>();

        // Get skill requested from Intent
        String skillRequestedCSV = getIntent().getStringExtra("skillrequested");
        if (skillRequestedCSV == null || skillRequestedCSV.trim().isEmpty()) {
            Toast.makeText(this, "No skill requested passed", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔁 Split comma-separated requested skills
        List<String> requestedSkills = splitCsv(skillRequestedCSV);

        // 🔐 Get current logged-in username
        String currentUsername = getIntent().getStringExtra("currentUsername");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not passed!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set adapter
        adapter = new TutorAdapter(this, tutorList, currentUsername);
        recyclerView.setAdapter(adapter);

        // Firebase DB reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tutorList.clear();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String skillOfferedCSV = userSnap.child("skilloffered").getValue(String.class);
                    List<String> offeredSkills = splitCsv(skillOfferedCSV);

                    boolean matched = false;

                    for (String requested : requestedSkills) {
                        for (String offered : offeredSkills) {
                            if (requested.equalsIgnoreCase(offered)) {
                                String name = userSnap.child("name").getValue(String.class);
                                String email = userSnap.child("email").getValue(String.class);
                                String username = userSnap.child("username").getValue(String.class);
                                tutorList.add(new UserModel(name, email, username, 0, 0, 0));
                                matched = true;
                                break;
                            }
                        }
                        if (matched) break;
                    }
                }

                adapter.notifyDataSetChanged();

                if (tutorList.isEmpty()) {
                    Toast.makeText(LearnRequestedSkill.this,
                            "No tutors found offering this skill", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LearnRequestedSkill.this,
                        "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Helper method to split comma-separated values (CSV)
    private List<String> splitCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) return new ArrayList<>();
        String[] arr = csv.split("\\s*,\\s*"); // split and trim
        return Arrays.asList(arr);
    }
}
