package anjali.learning.skilshare;

import android.os.Bundle;
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

    TextView profileTitle, nameTV, emailTV, locationTV, skillsTV;
    DatabaseReference databaseReference;

    String username; // dynamic username

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

        // Getting username from Intent
        username = getIntent().getStringExtra("username");

        if (username == null || username.isEmpty()) {
            Toast.makeText(Profile.this, "Username not provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Linking UI elements
        profileTitle = findViewById(R.id.profileTitle);
        nameTV = findViewById(R.id.textName);
        emailTV = findViewById(R.id.textEmail);
        locationTV = findViewById(R.id.textLocation);
        skillsTV = findViewById(R.id.textSkills);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(username);

        // Fetching user data
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String skills = snapshot.child("skills").getValue(String.class);

                    profileTitle.setText(name + "'s Profile");
                    nameTV.setText("Name: " + name);
                    emailTV.setText("Email: " + email);
                    locationTV.setText("Location: " + location);
                    skillsTV.setText("Skills: " + skills);
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
