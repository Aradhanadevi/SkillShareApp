package anjali.learning.skilshare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class CourseDetailFragment extends Fragment {

    TextView nameText, tutorText, descriptionText, languageText, locationText, videosText, categoryText, priceText, skillsText;
    ImageView imageView, heartIcon;
    Button registerBtn, goToCourseBtn;

    String name, tutor, description, language, location, imageUrl, category, skils;
    int videos, price;

    boolean isFavorite = false;
    String username = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_detail, container, false);

        // Initialize views
        nameText = view.findViewById(R.id.courseNameDetail);
        tutorText = view.findViewById(R.id.tutorDetail);
        descriptionText = view.findViewById(R.id.descriptionDetail);
        languageText = view.findViewById(R.id.languageDetail);
        locationText = view.findViewById(R.id.locationDetail);
        videosText = view.findViewById(R.id.videosDetail);
        imageView = view.findViewById(R.id.imageDetail);
        categoryText = view.findViewById(R.id.categoryDetail);
        priceText = view.findViewById(R.id.priceDetail);
        skillsText = view.findViewById(R.id.skillsDetail);
        registerBtn = view.findViewById(R.id.registerNowBtn);
        heartIcon = view.findViewById(R.id.heartIcon);
        goToCourseBtn = view.findViewById(R.id.goToCourseBtn);

        // Get arguments
        if (getArguments() != null) {
            name = getArguments().getString("name");
            tutor = getArguments().getString("tutor");
            description = getArguments().getString("description");
            language = getArguments().getString("language");
            location = getArguments().getString("location");
            videos = getArguments().getInt("videos");
            imageUrl = getArguments().getString("imageUrl");
            category = getArguments().getString("category");
            skils = getArguments().getString("skils");
            price = getArguments().getInt("price");

            nameText.setText("Course: " + name);
            tutorText.setText("Tutor: " + tutor);
            descriptionText.setText(description);
            languageText.setText("Language: " + language);
            locationText.setText("Location: " + location);
            videosText.setText("No. of videos: " + videos);
            categoryText.setText("Category: " + category);
            skillsText.setText("Skills: " + skils);
            priceText.setText(price == 0 ? "Price: Free" : "Price: ₹" + price);

            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageView);
        }

        // Firebase auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String email = userSnap.child("email").getValue(String.class);
                        if (email != null && email.equals(userEmail)) {
                            username = userSnap.getKey();

                            // 🔸 Check registration
                            DatabaseReference regRef = FirebaseDatabase.getInstance()
                                    .getReference("registeredcourse").child(name).child(username);

                            regRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        registerBtn.setVisibility(View.GONE);
                                        goToCourseBtn.setVisibility(View.VISIBLE);
                                    } else {
                                        registerBtn.setVisibility(View.VISIBLE);
                                        goToCourseBtn.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), "Error checking registration", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // 🔸 Favourite logic
                            DatabaseReference favRef = usersRef.child(username).child("favourites").child(name);
                            favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    isFavorite = snapshot.exists();
                                    updateHeartIcon();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), "Failed to fetch favourite", Toast.LENGTH_SHORT).show();
                                }
                            });

                            heartIcon.setOnClickListener(v -> {
                                DatabaseReference favClickRef = usersRef.child(username).child("favourites").child(name);
                                if (isFavorite) {
                                    favClickRef.removeValue();
                                    isFavorite = false;
                                    Toast.makeText(getContext(), "Removed from favourites", Toast.LENGTH_SHORT).show();
                                } else {
                                    favClickRef.setValue(true);
                                    isFavorite = true;
                                    Toast.makeText(getContext(), "Added to favourites", Toast.LENGTH_SHORT).show();
                                }
                                updateHeartIcon();
                            });

                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "User fetch failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ✅ Register Now
        registerBtn.setOnClickListener(v -> {
            FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
            if (user1 != null) {
                String userEmail = user1.getEmail();
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String email = userSnap.child("email").getValue(String.class);
                            if (email != null && email.equals(userEmail)) {
                                String username = userSnap.getKey();
                                registerCourse(username);
                                return;
                            }
                        }
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Go to Course
        goToCourseBtn.setOnClickListener(v -> {
            DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses").child(name);
            courseRef.child("playlistlink").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String playlistLink = snapshot.getValue(String.class);

                        PlaylistFragment playlistFragment = new PlaylistFragment();
                        Bundle args = new Bundle();
                        args.putString("courseName", name);
                        args.putString("playlistLink", playlistLink);
                        playlistFragment.setArguments(args);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, playlistFragment)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(getContext(), "No playlist found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    private void updateHeartIcon() {
        if (isFavorite) {
            heartIcon.setImageResource(R.drawable.ic_heart_filled);
        } else {
            heartIcon.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    private void registerCourse(String username) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("registeredcourse").child(name).child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(getContext(), "Already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            rootRef.child("registeredcourse").child(name).child(username).setValue(true);
                            rootRef.child("users").child(username).child("registeredCourses").child(name).setValue(true);
                            Toast.makeText(getContext(), "Registered successfully", Toast.LENGTH_SHORT).show();

                            registerBtn.setVisibility(View.GONE);
                            goToCourseBtn.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
