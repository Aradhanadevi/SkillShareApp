package anjali.learning.skilshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {
    Button Signup;
    TextView redirectToSignin;
    EditText username, name, password, confirmpassword, skils, skilloffered, skillrequested, location, email;
    private CheckBox alsowanttutorrights,accepttandc;
    private Button signupBtn, uploadBtn;
    private ImageView previewIv;

    private Uri selectedImageUri;
    private String uploadedImageUrl = null;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String CLOUD_NAME = "dbfdxuovq"; // change this
    private static final String UPLOAD_PRESET = "Tutor Verification"; // your preset


    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);




        signupBtn = findViewById(R.id.btnsignup);
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
        uploadBtn = findViewById(R.id.btnUploadTutorDoc);
        previewIv = findViewById(R.id.previewIv);


        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Hide upload stuff initially

        redirectToSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent redirectToSignin=new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(redirectToSignin);
            }
        });
        uploadBtn.setVisibility(View.GONE);
        previewIv.setVisibility(View.GONE);

        alsowanttutorrights.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                uploadBtn.setVisibility(View.VISIBLE);
                previewIv.setVisibility(View.VISIBLE);
            } else {
                uploadBtn.setVisibility(View.GONE);
                previewIv.setVisibility(View.GONE);
                uploadedImageUrl = null;
            }
        });

        uploadBtn.setOnClickListener(v -> openGallery());

        signupBtn.setOnClickListener(v -> registerUser());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            previewIv.setImageURI(selectedImageUri);

            // Upload to Cloudinary
            uploadImageToCloudinary(selectedImageUri);
        }
    }

    private void uploadImageToCloudinary(Uri fileUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                String uploadUrl = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                byte[] fileBytes = bos.toByteArray();
                inputStream.close();


                String boundary = "Boundary-" + System.currentTimeMillis();
                URL url = new URL(uploadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream request = new DataOutputStream(conn.getOutputStream());
                request.writeBytes("--" + boundary + "\r\n");
                request.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
                request.writeBytes(UPLOAD_PRESET + "\r\n");

                request.writeBytes("--" + boundary + "\r\n");
                request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n");
                request.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                request.write(fileBytes);
                request.writeBytes("\r\n--" + boundary + "--\r\n");
                request.flush();
                request.close();

                InputStream responseStream = conn.getInputStream();
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = responseStream.read()) != -1) {
                    sb.append((char) ch);
                }
                responseStream.close();

                JSONObject json = new JSONObject(sb.toString());
                uploadedImageUrl = json.getString("secure_url");

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    private void registerUser() {
        String Name = name.getText().toString().trim();
        String Username = username.getText().toString().trim();
        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();
        String ConfirmPassword = confirmpassword.getText().toString().trim();
        String Skils = skils.getText().toString().trim();
        String SkillOffered = skilloffered.getText().toString().trim();
        String SkillRequested = skillrequested.getText().toString().trim();
        String Location = location.getText().toString().trim();

        // Firebase key-safe username (replace spaces/dots etc.)
        String safeUsername = Username.replaceAll("[.#$\\[\\]]", "_");

        // Basic validation
        if (Name.isEmpty() || Username.isEmpty() || Password.isEmpty() || ConfirmPassword.isEmpty()
                || Skils.isEmpty() || Location.isEmpty() || Email.isEmpty()
                || SkillOffered.isEmpty() || SkillRequested.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }
        if (alsowanttutorrights.isChecked() && uploadedImageUrl == null) {
            Toast.makeText(this, "Please upload verification image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Email.matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")) {
            email.setError("Enter valid email");
            return;
        }
        if (!ConfirmPassword.equals(Password)) {
            confirmpassword.setError("Password not matching");
            return;
        }
        if (!accepttandc.isChecked()) {
            Toast.makeText(this, "Please accept T&C", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("username", Username);
                        userMap.put("name", Name);
                        userMap.put("email", Email);
                        userMap.put("approvedTutor", false);
                        userMap.put("wanttutorrights", alsowanttutorrights.isChecked());

                        if (alsowanttutorrights.isChecked()) {
                            userMap.put("tutorVerificationUrl", uploadedImageUrl);
                        }

                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        userMap.put("isAdmin", false);
                        userMap.put("isModerator", false);
                        userMap.put("isTutor", false);
                        userMap.put("location", Location);
                        userMap.put("lastActiveDate", "");
                        userMap.put("lastLoginDate", today);
                        userMap.put("lastQuizDate", "");
                        userMap.put("level", 0);
                        userMap.put("xp", 0);
                        userMap.put("stars", 0);
                        userMap.put("streak", 0);
                        userMap.put("skills", Skils);
                        userMap.put("skilloffered", SkillOffered);
                        userMap.put("skillrequested", SkillRequested);
                        userMap.put("registeredCourses", new HashMap<>());
                        userMap.put("progress", new HashMap<>());
                        userMap.put("messages", new HashMap<>());
                        userMap.put("favourites", new HashMap<>());

                        // ✅ Debug log
                        android.util.Log.d("FIREBASE", "Saving user: " + userMap);

                        usersRef.child(safeUsername).setValue(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    progressDialog.dismiss();
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        android.util.Log.e("FIREBASE", "DB Error", dbTask.getException());
                                        Toast.makeText(this, "DB Error: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


}