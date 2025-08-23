package anjali.learning.skilshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class SignInActivity extends AppCompatActivity {

    Button Signin;
    TextView redirectToSignup;
    EditText username, password;
    TextView forgotPassword;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Signin = findViewById(R.id.btnlogin);
        redirectToSignup = findViewById(R.id.signupredirecttxt);
        username = findViewById(R.id.lusername);
        password = findViewById(R.id.lpassword);
        forgotPassword = findViewById(R.id.forgotPassword);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        Signin.setOnClickListener(view -> {
            String Username = username.getText().toString().trim();
            String Password = password.getText().toString().trim();

            if (Username.isEmpty()) {
                username.setError("Please enter username");
            } else if (Password.isEmpty()) {
                password.setError("Please enter password");
            } else {
                Signin.setEnabled(false);
                loginUser(Username, Password);
            }
        });

        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        redirectToSignup.setOnClickListener(view -> {
            Intent redirect = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(redirect);
        });
    }

    private void loginUser(String Username, String Password) {
        // Fetch user email from Realtime DB
        databaseReference.child(Username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = snapshot.child("email").getValue(String.class);

                    if (email != null) {
                        // ✅ Only check with FirebaseAuth
                        mAuth.signInWithEmailAndPassword(email, Password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {

                                        // ✅ XP & Stars Setup
                                        checkAndSetXPandStars(Username);

                                        // Save username in SharedPreferences
                                        getSharedPreferences("SkillSharePrefs", MODE_PRIVATE)
                                                .edit()
                                                .putString("currentUsername", Username)
                                                .apply();

                                        Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        intent.putExtra("username", Username);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(SignInActivity.this, "Auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        Signin.setEnabled(true);
                                    }
                                });
                    } else {
                        username.setError("Email not found for this username");
                        Signin.setEnabled(true);
                    }
                } else {
                    username.setError("User not found");
                    Signin.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SignInActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Signin.setEnabled(true);
            }
        });
    }

    // ✅ New method to check and set XP & Stars
    private void checkAndSetXPandStars(String Username) {
        DatabaseReference userRef = databaseReference.child(Username);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!snapshot.hasChild("xp")) {
                        userRef.child("xp").setValue(0);
                    }
                    if (!snapshot.hasChild("stars")) {
                        userRef.child("stars").setValue(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Signin.setEnabled(true);
            }
        });
    }
}
