package anjali.learning.skilshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RequestSkilCourseFragment extends Fragment {

    private EditText etUsername, etEmail, etCourseRequest, etTutorName, etCourseDescription;
    private Button btnSubmitRequest;

    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_skil_course, container, false);

        // Initialize views
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etCourseRequest = view.findViewById(R.id.etCourseRequest);
        etTutorName = view.findViewById(R.id.etTutorName);
        etCourseDescription = view.findViewById(R.id.etCourseDescription);
        btnSubmitRequest = view.findViewById(R.id.btnSubmitRequest);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("requestedskillcourses");

        btnSubmitRequest.setOnClickListener(v -> submitRequest());

        return view;
    }

    private void submitRequest() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String courseName = etCourseRequest.getText().toString().trim();
        String tutorName = etTutorName.getText().toString().trim();
        String description = etCourseDescription.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(courseName) || TextUtils.isEmpty(tutorName)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("email", email);
        requestData.put("tutor", tutorName);
        requestData.put("description", description);

        // Save to Firebase
        databaseReference.child(username).child(courseName).setValue(requestData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to submit request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        etUsername.setText("");
        etEmail.setText("");
        etCourseRequest.setText("");
        etTutorName.setText("");
        etCourseDescription.setText("");
    }
}
