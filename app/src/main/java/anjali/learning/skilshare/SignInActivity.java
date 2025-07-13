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
                loginUser(Username, Password);
            }
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
                    String storedPassword = snapshot.child("password").getValue(String.class);

                    if (email != null && storedPassword != null && storedPassword.equals(Password)) {
                        // FirebaseAuth login
                        mAuth.signInWithEmailAndPassword(email, Password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        getSharedPreferences("SkillSharePrefs", MODE_PRIVATE)
                                                .edit()
                                                .putString("currentUsername", Username)
                                                .apply();
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        intent.putExtra("username", Username); // still pass username
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SignInActivity.this, "Auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        password.setError("Incorrect password");
                    }
                } else {
                    username.setError("User not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SignInActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
