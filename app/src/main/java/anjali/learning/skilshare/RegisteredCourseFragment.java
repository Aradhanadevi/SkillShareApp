package anjali.learning.skilshare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import anjali.learning.skilshare.Adapter.RegisteredCourseAdapter;
import anjali.learning.skilshare.model.Course;

public class RegisteredCourseFragment extends Fragment {

    private RecyclerView recyclerView;
    private RegisteredCourseAdapter adapter;
    private List<Course> registeredCourses = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registered_course, container, false);

        recyclerView = view.findViewById(R.id.registeredCoursesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RegisteredCourseAdapter(registeredCourses, new RegisteredCourseAdapter.CourseClickListener() {
            @Override
            public void onCourseClick(Course course) {
                openCourseDetail(course);
            }

            @Override
            public void onCancelClick(Course course) {
                cancelRegistration(course);
            }
        });

        recyclerView.setAdapter(adapter);
        loadRegisteredCourses();

        return view;
    }

    private void loadRegisteredCourses() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String email = userSnap.child("email").getValue(String.class);
                    if (email != null && email.equals(userEmail)) {
                        String username = userSnap.getKey();

                        DatabaseReference regCoursesRef = usersRef.child(username).child("registeredCourses");
                        regCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Set<String> courseNames = new HashSet<>();
                                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                                    courseNames.add(courseSnap.getKey());
                                }

                                fetchCourseDetails(courseNames,username);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), "Error loading courses", Toast.LENGTH_SHORT).show();
                            }
                        });

                        return;
                    }
                }

                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCourseDetails(Set<String> courseNames, String username) {
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");
        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference("users").child(username).child("progress");

        registeredCourses.clear();

        for (String courseName : courseNames) {
            coursesRef.child(courseName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        // Fetch progress separately
                        progressRef.child(courseName).child("progress").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot progressSnap) {
                                Integer progress = progressSnap.getValue(Integer.class);
                                if (progress != null) {
                                    course.setProgress(progress);
                                } else {
                                    course.setProgress(0); // Default if no progress yet
                                }

                                registeredCourses.add(course);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                course.setProgress(0);
                                registeredCourses.add(course);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error loading course data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openCourseDetail(Course course) {
        CourseDetailFragment fragment = new CourseDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", course.getCourseName());
        bundle.putString("description", course.getDescription());
        bundle.putString("tutor", course.getTutor());
        bundle.putString("language", course.getLanguage());
        bundle.putString("location", course.getLocation());
        bundle.putInt("videos", course.getNoofvideos());
        bundle.putString("imageUrl", course.getImageUrl());
        bundle.putString("category", course.getCategory());
        bundle.putString("skils", course.getSkills());
        bundle.putInt("price", course.getPrice());
        bundle.putInt("progress",course.getProgress());

        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void cancelRegistration(Course course) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userEmail = user.getEmail();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String email = userSnap.child("email").getValue(String.class);
                    if (email != null && email.equals(userEmail)) {
                        String username = userSnap.getKey();

                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                        rootRef.child("registeredcourse").child(course.getCourseName()).child(username).removeValue();
                        rootRef.child("users").child(username).child("registeredCourses").child(course.getCourseName()).removeValue();

                        Toast.makeText(getContext(), "Unregistered from course", Toast.LENGTH_SHORT).show();

                        registeredCourses.remove(course);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
