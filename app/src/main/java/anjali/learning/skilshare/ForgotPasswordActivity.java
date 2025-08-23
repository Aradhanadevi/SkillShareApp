package anjali.learning.skilshare;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText usernameInput;
    Button resetBtn;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        usernameInput = findViewById(R.id.usernameInput);
        resetBtn = findViewById(R.id.resetBtn);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();

        resetBtn.setOnClickListener(v -> {
            String Username = usernameInput.getText().toString().trim();
            if (Username.isEmpty()) {
                usernameInput.setError("Enter username");
            } else {
                fetchEmailAndSendReset(Username);
            }
        });
    }

    private void fetchEmailAndSendReset(String Username) {
        databaseReference.child(Username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = snapshot.child("email").getValue(String.class);
                    if (email != null) {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ForgotPasswordActivity.this,
                                                "Password reset link sent to " + email,
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(ForgotPasswordActivity.this,
                                                "Error: " + task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ForgotPasswordActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
