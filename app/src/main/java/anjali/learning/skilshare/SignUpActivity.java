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
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameEt, emailEt, passwordEt;
    private CheckBox tutorRightsCb;
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

        nameEt = findViewById(R.id.name);
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        tutorRightsCb = findViewById(R.id.wanttutorrights);
        uploadBtn = findViewById(R.id.btnUploadTutorDoc);
        signupBtn = findViewById(R.id.btnsignup);
        previewIv = findViewById(R.id.previewIv);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Hide upload stuff initially
        uploadBtn.setVisibility(View.GONE);
        previewIv.setVisibility(View.GONE);

        tutorRightsCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
        String username = nameEt.getText().toString().trim();
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tutorRightsCb.isChecked() && uploadedImageUrl == null) {
            Toast.makeText(this, "Please upload verification image", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // ✅ Step 1: Create in Firebase Auth
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        // ✅ Step 2: Build user profile for Realtime DB
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("username", username);
                        userMap.put("name", username);
                        userMap.put("email", email);

                        // 🔑 don't store password here for security
                        userMap.put("approvedTutor", false);
                        userMap.put("wanttutorrights", tutorRightsCb.isChecked());

                        if (tutorRightsCb.isChecked()) {
                            userMap.put("tutorVerificationUrl", uploadedImageUrl);
                        }

                        // default values
                        userMap.put("isAdmin", false);
                        userMap.put("isModerator", false);
                        userMap.put("isTutor", false);
                        userMap.put("location", "");
                        userMap.put("lastActiveDate", "");
                        userMap.put("lastLoginDate", "");
                        userMap.put("lastQuizDate", "");
                        userMap.put("level", 0);
                        userMap.put("xp", 0);
                        userMap.put("stars", 0);
                        userMap.put("streak", 0);
                        userMap.put("skills", "");
                        userMap.put("skilloffered", "");
                        userMap.put("skillrequested", "");
                        userMap.put("registeredCourses", new HashMap<>());
                        userMap.put("progress", new HashMap<>());
                        userMap.put("messages", new HashMap<>());
                        userMap.put("favourites", new HashMap<>());

                        usersRef.child(username).setValue(userMap).addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "DB Error: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
    }
}