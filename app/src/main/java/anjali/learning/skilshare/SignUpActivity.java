package anjali.learning.skilshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class SignUpActivity extends AppCompatActivity {

    Button Signup;
    TextView redirectToSignin;
    EditText username, name, password, confirmpassword, skils, skilloffered, skillrequested, location, email;
    CheckBox accepttandc, alsowanttutorrights;

    FirebaseAuth mAuth;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Signup = findViewById(R.id.btnsignup);
        redirectToSignin = findViewById(R.id.loginredirecttxt);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmpassword = findViewById(R.id.confirmpassword);
        skils = findViewById(R.id.skils);
        location = findViewById(R.id.location);
        email = findViewById(R.id.email);
        accepttandc = findViewById(R.id.accepttandc);
        name = findViewById(R.id.name);
        skilloffered = findViewById(R.id.skillsOffered);
        skillrequested = findViewById(R.id.skillsRequested);
        alsowanttutorrights = findViewById(R.id.wanttutorrights);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");

        Signup.setOnClickListener(view -> {
            String Name = name.getText().toString().trim();
            String Username = username.getText().toString().trim();
            String Email = email.getText().toString().trim();
            String Password = password.getText().toString().trim();
            String ConfirmPassword = confirmpassword.getText().toString().trim();
            String Skils = skils.getText().toString().trim();
            String SkillOffered = skilloffered.getText().toString().trim();
            String SkillRequested = skillrequested.getText().toString().trim();
            String Location = location.getText().toString().trim();
            String emailPattern = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$";

            if (Name.isEmpty() || Username.isEmpty() || Password.isEmpty() || ConfirmPassword.isEmpty() || Skils.isEmpty() || Location.isEmpty() || Email.isEmpty() || SkillOffered.isEmpty() || SkillRequested.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
            } else if (!Email.matches(emailPattern)) {
                email.setError("Enter valid email");
            } else if (!ConfirmPassword.equals(Password)) {
                confirmpassword.setError("Password not matching");
            } else if (!accepttandc.isChecked()) {
                Toast.makeText(SignUpActivity.this, "Please accept T&C", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(Email, Password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // 🟢 Gamification Fields
                                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("name", Name);
                                userMap.put("username", Username);
                                userMap.put("email", Email);
                                userMap.put("password", Password);
                                userMap.put("skills", Skils);
                                userMap.put("skilloffered", SkillOffered);
                                userMap.put("skillrequested", SkillRequested);
                                userMap.put("location", Location);
                                userMap.put("xp", 0); // Starting XP
                                userMap.put("streak", 1); // Start at 1
                                userMap.put("lastLoginDate", today); // Today’s date
                                userMap.put("isAdmin", false);
                                userMap.put("isModerator", false);
                                userMap.put("isTutor", false);

                                if (alsowanttutorrights.isChecked()) {
                                    userMap.put("wanttutorrights", true);
                                    userMap.put("approvedtutor", false);
                                } else {
                                    userMap.put("wanttutorrights", false);
                                    userMap.put("approvedtutor", false);
                                }

                                database.child(Username).setValue(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SignUpActivity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("SignupError", "Signup failed", e);
                                            Toast.makeText(SignUpActivity.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.e("SignupError", "Signup failed", task.getException());
                                Toast.makeText(SignUpActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        redirectToSignin.setOnClickListener(view -> {
            Intent redirect = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(redirect);
        });
    }
}
