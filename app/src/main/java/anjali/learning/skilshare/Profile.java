package anjali.learning.skilshare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    TextView profileTitle, nameTV, emailTV, locationTV, skillsTV, skillOfferedTV, skillRequestedTV;
    DatabaseReference databaseReference;
    String username;
    Button learnrequestedskill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the username passed from the previous activity
        username = getIntent().getStringExtra("username");

        if (username == null || username.isEmpty()) {
            Toast.makeText(Profile.this, "Username not provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Link UI elements
        profileTitle = findViewById(R.id.profileTitle);
        nameTV = findViewById(R.id.textName);
        emailTV = findViewById(R.id.textEmail);
        locationTV = findViewById(R.id.textLocation);
        skillsTV = findViewById(R.id.textSkills);
        skillOfferedTV = findViewById(R.id.textSkillOffered);
        skillRequestedTV = findViewById(R.id.textSkillRequested);
        learnrequestedskill=findViewById(R.id.learnrequestedskill);
        learnrequestedskill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Profile.this, LearnRequestedSkill.class);
                i.putExtra("currentUsername", username);
                i.putExtra("skillrequested", skillRequestedTV.getText().toString());
                startActivity(i);

            }
        });
        // Reference to Firebase database node: users/username
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(username);

        // Fetch data from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String skills = snapshot.child("skills").getValue(String.class);
                    String skillOffered = snapshot.child("skilloffered").getValue(String.class);
                    String skillRequested = snapshot.child("skillrequested").getValue(String.class);

                    // Set values in UI
                    profileTitle.setText(name + "'s Profile");
                    nameTV.setText(name);
                    emailTV.setText(email);
                    locationTV.setText(location);
                    skillsTV.setText(skills);
                    skillOfferedTV.setText(skillOffered);
                    skillRequestedTV.setText(skillRequested);
                } else {
                    Toast.makeText(Profile.this, "User not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
