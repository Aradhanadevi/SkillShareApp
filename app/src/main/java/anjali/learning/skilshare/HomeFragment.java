package anjali.learning.skilshare;

import static android.content.Context.MODE_PRIVATE;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.*;

import anjali.learning.skilshare.Adapter.CourseAdapter;
import anjali.learning.skilshare.Adapter.FeaturedCourseAdapter;
import anjali.learning.skilshare.model.Course;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerCourses;
    private ArrayList<Course> courseList;
    private CourseAdapter adapter;

    private ProgressBar xpProgress;
    private TextView xpStatus;

    private int currentXP = 0;
    private int xpGoal = 100;

    private ViewPager2 viewPagerFeatured;
    private ArrayList<Course> featuredList;
    private FeaturedCourseAdapter featuredAdapter;

    private TextView featuredTitle, featuredDescription, featuredDuration;
    private TextView streakTextView;

    private Handler carouselHandler = new Handler(Looper.getMainLooper());
    private int carouselIndex = 0;

    private AutoCompleteTextView etSearchBar;
    private ImageView ivSearchIcon;
    private FloatingActionButton fabBot, fabMessage;

    private View cardDailyQuiz;

    private ActivityResultLauncher<Intent> quizLauncher;

    private String currentUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        streakTextView = view.findViewById(R.id.streakTextView);

        // Initialize views
        xpProgress = view.findViewById(R.id.xpProgress);
        xpStatus = view.findViewById(R.id.xpStatus);
        featuredTitle = view.findViewById(R.id.featuredCourseTitle);
        featuredDescription = view.findViewById(R.id.featuredCourseDescription);
        featuredDuration = view.findViewById(R.id.featuredCourseDuration);
        viewPagerFeatured = view.findViewById(R.id.viewPagerFeatured);
        etSearchBar = view.findViewById(R.id.etSearchBar);
        ivSearchIcon = view.findViewById(R.id.ivSearchIcon);
        fabBot = view.findViewById(R.id.fabBot);
        fabMessage = view.findViewById(R.id.fabMessages);
        cardDailyQuiz = view.findViewById(R.id.cardDailyQuiz);

        // Username
        currentUsername = requireContext()
                .getSharedPreferences("SkillSharePrefs", MODE_PRIVATE)
                .getString("currentUsername", null);

        // Setup RecyclerView
        recyclerCourses = view.findViewById(R.id.recyclerCourses);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        courseList = new ArrayList<>();
        adapter = new CourseAdapter(courseList);
        recyclerCourses.setAdapter(adapter);

        // Featured courses
        featuredList = new ArrayList<>();
        featuredAdapter = new FeaturedCourseAdapter(featuredList, getContext(), this::updateFeaturedDetails);
        viewPagerFeatured.setAdapter(featuredAdapter);

        // Launch quiz and refresh XP when done
        quizLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("quizCompleted", false)) {
                            fetchAndShowXP();

                            // 👇 Add today's date as lastActiveDate
                            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(Calendar.getInstance().getTime());

                            FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(currentUsername)
                                    .child("lastActiveDate")
                                    .setValue(today);
                        }
                    }
                });


        cardDailyQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DailyQuizActivity.class);
            intent.putExtra("username", currentUsername);
            quizLauncher.launch(intent);
        });

        fabMessage.setOnClickListener(v -> {
            Intent msgIntent = new Intent(requireContext(), MessageRequestActivity.class);
            msgIntent.putExtra("currentUsername", currentUsername);
            startActivity(msgIntent);
        });

        fabBot.setOnClickListener(v -> openChatBot());

        // Load everything
        setupSearchBot();
        loadFeaturedCourses();
        loadUserSkillsAndFilterCourses();
        fetchAndShowXP();

        return view;
    }

    private void fetchAndShowXP() {
        if (currentUsername == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUsername);


        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer xp = snapshot.child("xp").getValue(Integer.class);
                currentXP = (xp != null) ? xp : 0;

                Long streak = snapshot.child("streak").getValue(Long.class);
                if (streak == null) streak = 0L;
                streakTextView.setText("🔥 Streak: " + streak + " days");

                String lastActive = snapshot.child("lastActiveDate").getValue(String.class);
                checkStreakReset(userRef, lastActive, streak);

                updateXPUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load XP", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateXPUI() {
        xpProgress.setMax(xpGoal);
        xpProgress.setProgress(currentXP);
        xpStatus.setText("XP: " + currentXP + " / " + xpGoal);
    }

    private void openChatBot() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String email = userSnap.child("email").getValue(String.class);
                        if (email != null && email.equals(user.getEmail())) {
                            String name = userSnap.child("name").getValue(String.class);
                            String skills = userSnap.child("skills").getValue(String.class);

                            Bundle bundle = new Bundle();
                            bundle.putString("name", name);
                            bundle.putString("skills", skills);

                            ChatBottomSheet bottomSheet = new ChatBottomSheet();
                            bottomSheet.setArguments(bundle);
                            bottomSheet.show(getParentFragmentManager(), "chat");
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupSearchBot() {
        ArrayList<String> courseNames = new ArrayList<>();
        ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_dropdown,
                R.id.dropdownItem,
                courseNames
        );

        etSearchBar.setAdapter(autoAdapter);
        etSearchBar.setThreshold(1);

        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses");
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseNames.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Course course = snap.getValue(Course.class);
                    if (course != null && course.getCourseName() != null) {
                        courseNames.add(course.getCourseName());
                    }
                }
                autoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load suggestions", Toast.LENGTH_SHORT).show();
            }
        });

        etSearchBar.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            searchCourses(selected);
        });

        ivSearchIcon.setOnClickListener(v -> {
            String query = etSearchBar.getText().toString().trim();
            if (query.isEmpty()) {
                etSearchBar.setError("Type something");
                return;
            }
            searchCourses(query);
        });
    }

    private void searchCourses(String query) {
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses");

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Course> resultList = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Course course = snap.getValue(Course.class);
                    if (course != null && course.getCourseName() != null &&
                            course.getCourseName().toLowerCase().contains(query.toLowerCase())) {
                        resultList.add(course);
                    }
                }

                if (resultList.isEmpty()) {
                    Toast.makeText(getContext(), "No course found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("courses", resultList);

                SearchFragment searchFragment = new SearchFragment();
                searchFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, searchFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFeaturedDetails(Course course) {
        featuredTitle.setText(course.getCourseName());
        featuredDescription.setText(course.getDescription());
        featuredDuration.setText(course.getNoofvideos() + " videos");
    }

    private void loadUserSkillsAndFilterCourses() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String currentUserEmail = user.getEmail();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String email = userSnap.child("email").getValue(String.class);
                    if (email != null && email.equals(currentUserEmail)) {
                        String userSkills = userSnap.child("skills").getValue(String.class);
                        if (userSkills != null && !userSkills.isEmpty()) {
                            fetchCoursesMatchingSkills(userSkills);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchCoursesMatchingSkills(String userSkills) {
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses");

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Course course = snap.getValue(Course.class);
                    if (course != null && course.getSkills() != null) {
                        for (String userSkill : userSkills.split(",")) {
                            if (course.getSkills().toLowerCase().contains(userSkill.trim().toLowerCase())) {
                                courseList.add(course);
                                break;
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadFeaturedCourses() {
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses");

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                featuredList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Course course = snap.getValue(Course.class);
                    if (course != null && course.getNoofvideos() > 30) {
                        featuredList.add(course);
                    }
                }
                featuredAdapter.notifyDataSetChanged();
                if (!featuredList.isEmpty()) {
                    updateFeaturedDetails(featuredList.get(0));
                    startCarousel();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startCarousel() {
        carouselHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!featuredList.isEmpty()) {
                    carouselIndex = (carouselIndex + 1) % featuredList.size();
                    viewPagerFeatured.setCurrentItem(carouselIndex, true);
                    updateFeaturedDetails(featuredList.get(carouselIndex));
                }
                carouselHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        carouselHandler.removeCallbacksAndMessages(null);
    }

    private void checkStreakReset(DatabaseReference userRef, String lastActive, Long currentStreak) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        if (lastActive == null || lastActive.isEmpty()) return;

        Calendar lastCal = Calendar.getInstance();
        Calendar todayCal = Calendar.getInstance();
        try {
            lastCal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastActive));
        } catch (Exception e) {
            return;
        }

        lastCal.add(Calendar.DATE, 1); // Expected next date
        String expectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(lastCal.getTime());

        if (!expectedDate.equals(today)) {
            userRef.child("streak").setValue(0);
        }
    }
}
