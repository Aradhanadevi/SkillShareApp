package anjali.learning.skilshare;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    TextView profileTitle, nameTV, emailTV, locationTV, skillsTV, skillOfferedTV, skillRequestedTV;
    DatabaseReference databaseReference;
    String username;
    Button learnrequestedskill;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your layout
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get arguments passed to fragment
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }

        if (username == null || username.isEmpty()) {
            Toast.makeText(getContext(), "Username not provided!", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Link UI elements
        profileTitle = view.findViewById(R.id.profileTitle);
        nameTV = view.findViewById(R.id.textName);
        emailTV = view.findViewById(R.id.textEmail);
        locationTV = view.findViewById(R.id.textLocation);
        skillsTV = view.findViewById(R.id.textSkills);
        skillOfferedTV = view.findViewById(R.id.textSkillOffered);
        skillRequestedTV = view.findViewById(R.id.textSkillRequested);
        learnrequestedskill = view.findViewById(R.id.learnrequestedskill);

        learnrequestedskill.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), LearnRequestedSkill.class);
            i.putExtra("currentUsername", username);
            i.putExtra("skillrequested", skillRequestedTV.getText().toString());
            startActivity(i);
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
                    Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
