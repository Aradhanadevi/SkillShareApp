package anjali.learning.skilshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UploadSkillCourseFragment extends Fragment {

    EditText courseNameInput, categoryInput, descriptionInput, tutorInput, languageInput,
            locationInput, playlistLinkInput, imageUrlInput, priceInput, videoCountInput, skillsInput;
    Button uploadCourseBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_skill_course, container, false);

        courseNameInput = view.findViewById(R.id.courseNameInput);
        categoryInput = view.findViewById(R.id.categoryInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        tutorInput = view.findViewById(R.id.tutorInput);
        languageInput = view.findViewById(R.id.languageInput);
        locationInput = view.findViewById(R.id.locationInput);
        playlistLinkInput = view.findViewById(R.id.playlistLinkInput);
        imageUrlInput = view.findViewById(R.id.imageUrlInput);
        priceInput = view.findViewById(R.id.priceInput);
        videoCountInput = view.findViewById(R.id.videoCountInput);
        skillsInput = view.findViewById(R.id.skillsInput);
        uploadCourseBtn = view.findViewById(R.id.uploadCourseBtn);

        uploadCourseBtn.setOnClickListener(v -> uploadCourse());

        return view;
    }

    private void uploadCourse() {
        String name = courseNameInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String desc = descriptionInput.getText().toString().trim();
        String tutor = tutorInput.getText().toString().trim();
        String lang = languageInput.getText().toString().trim();
        String loc = locationInput.getText().toString().trim();
        String playlist = playlistLinkInput.getText().toString().trim();
        String imgUrl = imageUrlInput.getText().toString().trim();
        String skills = skillsInput.getText().toString().trim();

        int price = TextUtils.isEmpty(priceInput.getText()) ? 0 : Integer.parseInt(priceInput.getText().toString());
        int videos = TextUtils.isEmpty(videoCountInput.getText()) ? 0 : Integer.parseInt(videoCountInput.getText().toString());

        if (name.isEmpty() || desc.isEmpty() || playlist.isEmpty()) {
            Toast.makeText(getContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses").child(name);

        Map<String, Object> data = new HashMap<>();
        data.put("courseName", name);
        data.put("Category", category);
        data.put("Description", desc);
        data.put("Tutor", tutor);
        data.put("language", lang);
        data.put("location", loc);
        data.put("playlistlink", playlist);
        data.put("imageUrl", imgUrl);
        data.put("price", price);
        data.put("noofvideos", videos);
        data.put("skils", skills);

        courseRef.setValue(data).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Course uploaded!", Toast.LENGTH_SHORT).show();
            clearFields();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void clearFields() {
        courseNameInput.setText("");
        categoryInput.setText("");
        descriptionInput.setText("");
        tutorInput.setText("");
        languageInput.setText("");
        locationInput.setText("");
        playlistLinkInput.setText("");
        imageUrlInput.setText("");
        priceInput.setText("");
        videoCountInput.setText("");
        skillsInput.setText("");
    }
}
