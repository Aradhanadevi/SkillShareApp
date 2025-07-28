package anjali.learning.skilshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    Button Signup;
    TextView redirectToSignin;
    EditText username, name, password, confirmpassword, skils,skilloffered,skillrequested, location, email;
    CheckBox accepttandc,alsowanttutorrights;

    FirebaseAuth mAuth;
    DatabaseReference database;
    //comment
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
        skilloffered=findViewById(R.id.skillsOffered);
        skillrequested=findViewById(R.id.skillsRequested);
        alsowanttutorrights=findViewById(R.id.wanttutorrights);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");

        Signup.setOnClickListener(view -> {
            String Name = name.getText().toString().trim();
            String Username = username.getText().toString().trim();
            String Email = email.getText().toString().trim();
            String Password = password.getText().toString().trim();
            String ConfirmPassword = confirmpassword.getText().toString().trim();
            String Skils = skils.getText().toString().trim();
            String SkillOffered=skilloffered.getText().toString().trim();
            String SkillRequested=skillrequested.getText().toString().trim();
            String Location = location.getText().toString().trim();
            String emailPattern = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$";

            if (Name.isEmpty() || Username.isEmpty() || Password.isEmpty() || ConfirmPassword.isEmpty() || Skils.isEmpty() || Location.isEmpty() || Email.isEmpty()||SkillOffered.isEmpty()||SkillRequested.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
            } else if (!Email.matches(emailPattern)) {
                email.setError("Enter valid email");
            } else if (!ConfirmPassword.equals(Password)) {
                confirmpassword.setError("Password not matching");
            } else if (!accepttandc.isChecked()) {
                Toast.makeText(SignUpActivity.this, "Please accept T&C", Toast.LENGTH_SHORT).show();
            } else {
                // Create user in FirebaseAuth first
                mAuth.createUserWithEmailAndPassword(Email, Password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Then store user info in Realtime DB under chosen username
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("name", Name);
                                userMap.put("username", Username);
                                userMap.put("email", Email);
                                userMap.put("password", Password); // avoid storing plaintext in production
                                userMap.put("skills", Skils);
                                userMap.put("skilloffered",SkillOffered);
                                userMap.put("skillrequested",SkillRequested);
                                userMap.put("location", Location);
// to check whether user want and has tutor rights else if checkbox is not checked both will be stored as false.
                                if (alsowanttutorrights.isChecked()) {
                                    userMap.put("wanttutorrights", true);
                                    userMap.put("approvedtutor", false); // default false for admin approval
                                }else{
                                    userMap.put("wanttutorrights", false);
                                    userMap.put("approvedtutor", false);
                                }

                                database.child(Username).setValue(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SignUpActivity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e ->
                                        {
                                            Log.e("SignupError", "Signup failed", task.getException());
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
