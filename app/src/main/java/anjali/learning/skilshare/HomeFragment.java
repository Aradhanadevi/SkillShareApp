package anjali.learning.skilshare;

import static android.content.Context.MODE_PRIVATE;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

import anjali.learning.skilshare.Adapter.CourseAdapter;
import anjali.learning.skilshare.Adapter.FeaturedCourseAdapter;
import anjali.learning.skilshare.model.Course;
import anjali.learning.skilshare.model.UserModel;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerCourses;
    private ArrayList<Course> courseList = new ArrayList<>();
    private CourseAdapter adapter;
    private ProgressBar xpProgress;
    private TextView xpStatus, streakTextView;
    private int currentXP = 0, xpGoal = 200;
    private ViewPager2 viewPagerFeatured;
    private ArrayList<Course> featuredList = new ArrayList<>();
    private FeaturedCourseAdapter featuredAdapter;
    private TextView featuredTitle, featuredDescription, featuredDuration;
    private Handler carouselHandler = new Handler(Looper.getMainLooper());
    private int carouselIndex = 0;
    private AutoCompleteTextView etSearchBar;
    private ImageView ivSearchIcon;
    private FloatingActionButton fabBot, fabMessage;
    private View cardDailyQuiz;
    private ActivityResultLauncher<Intent> quizLauncher;
    private String currentUsername;
    // Removed spinner declarations
    private final String[] languages = {"All", "Gujarati", "Hindi", "English", "Marathi", "Punjabi", "Bengali", "Others"};
    private final String[] categoryList = {"All","Web Development", "Android", "Data Science","flutter","dsa","dance","design","python","App Development","communication","cooking","coding","soft skills"};
    private String selectedLanguage = "all";
    private String selectedCategory = "all";

    private TextView levelText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        levelText = view.findViewById(R.id.levelText);

        // Initialize views
        streakTextView = view.findViewById(R.id.streakTextView);
        xpProgress = view.findViewById(R.id.xpProgress);
        xpStatus = view.findViewById(R.id.xpStatus);
        featuredTitle = view.findViewById(R.id.featuredCourseTitle);
        featuredDescription = view.findViewById(R.id.featuredCourseDescription);
        featuredDuration = view.findViewById(R.id.featuredCourseDuration);
        viewPagerFeatured = view.findViewById(R.id.viewPagerFeatured);
        etSearchBar = view.findViewById(R.id.etSearchBar);
        ivSearchIcon = view.findViewById(R.id.ivSearchIcon);
        LinearLayout layoutLanguageFilters = view.findViewById(R.id.layoutLanguageFilters);
        LinearLayout layoutCategoryFilters = view.findViewById(R.id.layoutCategoryFilters);
        fabBot = view.findViewById(R.id.fabBot);
        fabMessage = view.findViewById(R.id.fabMessages);
        cardDailyQuiz = view.findViewById(R.id.cardDailyQuiz);
        recyclerCourses = view.findViewById(R.id.recyclerCourses);
        currentUsername = requireContext().getSharedPreferences("SkillzEraPrefs", MODE_PRIVATE)
                .getString("username", null);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CourseAdapter(courseList);
        recyclerCourses.setAdapter(adapter);
        featuredAdapter = new FeaturedCourseAdapter(featuredList, getContext(), this::updateFeaturedDetails);
        viewPagerFeatured.setAdapter(featuredAdapter);
        // Setup horizontal filter buttons for language and category
        setupFilterButtons(layoutLanguageFilters, languages, true);
        setupFilterButtons(layoutCategoryFilters, categoryList, false);
        setupQuizLauncher();
        setupListeners();
        setupSearchBot();
        loadFeaturedCourses();
        loadUserSkillsAndFilterCourses();
        fetchAndShowStreak();
        fetchAndShowXP();
        return view;
    }

    private void setupFilterButtons(LinearLayout container, String[] items, boolean isLanguage) {
        container.removeAllViews();
        for (String item : items) {
            Button btn = new Button(getContext());
            btn.setText(item);
            btn.setAllCaps(false);
            btn.setBackgroundResource(R.drawable.rounded_bg);
            btn.setTextColor(getResources().getColor(android.R.color.black));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            // Highlight "All" filter button initially
            if (item.equalsIgnoreCase("all")) {
                btn.setBackgroundResource(R.drawable.selected_filter_bg);
                btn.setTextColor(getResources().getColor(android.R.color.white));
            }
            btn.setOnClickListener(v -> {
                // Reset all buttons in this container to default
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof Button) {
                        child.setBackgroundResource(R.drawable.rounded_bg);
                        ((Button)child).setTextColor(getResources().getColor(android.R.color.black));
                    }
                }
                // Highlight selected button
                btn.setBackgroundResource(R.drawable.selected_filter_bg);
                btn.setTextColor(getResources().getColor(android.R.color.white));

                if (isLanguage) {
                    selectedLanguage = item.toLowerCase();
                } else {
                    selectedCategory = item.toLowerCase();
                }
                filterCourses();
            });

            container.addView(btn);
        }
    }
    private void filterCourses() {
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses");

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Course course = snap.getValue(Course.class);
                    Boolean approved = snap.child("approved").getValue(Boolean.class);

                    if (course != null && Boolean.TRUE.equals(approved)) {
                        boolean matchesLang = selectedLanguage.equals("all") ||
                                (course.getLanguage() != null &&
                                        course.getLanguage().toLowerCase().equals(selectedLanguage));
                        boolean matchesCategory = selectedCategory.equals("all") ||
                                (course.getCategory() != null &&
                                        course.getCategory().toLowerCase().equals(selectedCategory));
                        if (matchesLang && matchesCategory) {
                            courseList.add(course);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Filter failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupQuizLauncher() {
        quizLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("quizCompleted", false)) {
                            fetchAndShowXP();
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
    }
    private void setupListeners() {
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
    }
    private void fetchAndShowStreak() {
        if (currentUsername == null || currentUsername.isEmpty()) return;

        FirebaseDatabase.getInstance().getReference("users").child(currentUsername)
                .child("streak").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long streak = snapshot.getValue(Long.class);
                        streakTextView.setText("🔥 Streak: " + (streak != null ? streak : 0) + " days");
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load streak", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void fetchAndShowXP() {
        if (currentUsername == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(currentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long streak = snapshot.child("streak").getValue(Long.class);
                        streakTextView.setText("🔥 Streak: " + (streak != null ? streak : 0) + " days");

                        Long xp = snapshot.child("xp").getValue(Long.class);
                        currentXP = xp != null ? xp.intValue() : 0;
                        updateXPUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load XP and streak", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateXPUI() {
        xpProgress.setMax(xpGoal);
        xpProgress.setProgress(currentXP);
        xpStatus.setText("XP: " + currentXP + " / " + xpGoal);
        // Calculate level based on XP (100 XP per level)
        int level = currentXP / 100 + 1;
        levelText.setText("Level: " + level);
    }
    private void openChatBot() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void setupSearchBot() {
        ArrayList<String> courseNames = new ArrayList<>();
        ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_dropdown,
                R.id.dropdownItem,
                courseNames);
        etSearchBar.setAdapter(autoAdapter);
        etSearchBar.setThreshold(1);
        FirebaseDatabase.getInstance().getReference("courses")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        courseNames.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Course course = snap.getValue(Course.class);
                            Boolean approved = snap.child("approved").getValue(Boolean.class);
                            if (course != null && course.getCourseName() != null  && Boolean.TRUE.equals(approved)) {
                                courseNames.add(course.getCourseName());
                            }
                        }
                        autoAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
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
        FirebaseDatabase.getInstance().getReference("courses")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Course> resultList = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Course course = snap.getValue(Course.class);
                            Boolean approved = snap.child("approved").getValue(Boolean.class);
                            if (course != null && course.getCourseName() != null &&
                                    Boolean.TRUE.equals(approved) &&
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
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadUserSkillsAndFilterCourses() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(user.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String userSkills = userSnap.child("skills").getValue(String.class);
                            if (userSkills != null && !userSkills.isEmpty()) {
                                fetchCoursesMatchingSkills(userSkills);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void fetchCoursesMatchingSkills(String userSkills) {
        FirebaseDatabase.getInstance().getReference("courses")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        courseList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Course course = snap.getValue(Course.class);
                            Boolean approved = snap.child("approved").getValue(Boolean.class);

                            if (course != null && course.getSkills() != null   &&Boolean.TRUE.equals(approved)) {
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
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void loadFeaturedCourses() {
        FirebaseDatabase.getInstance().getReference("courses")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        featuredList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Course course = snap.getValue(Course.class);
                            Boolean approved = snap.child("approved").getValue(Boolean.class);

                            if (course != null && course.getNoofvideos() > 30 &&  Boolean.TRUE.equals(approved)) {
                                featuredList.add(course);
                            }
                        }
                        featuredAdapter.notifyDataSetChanged();
                        if (!featuredList.isEmpty()) {
                            updateFeaturedDetails(featuredList.get(0));
                            startCarousel();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void startCarousel() {
        carouselHandler.postDelayed(new Runnable() {
            @Override public void run() {
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
    private void updateFeaturedDetails(Course course) {
        featuredTitle.setText(course.getCourseName());
        featuredDescription.setText(course.getDescription());
        featuredDuration.setText(course.getNoofvideos() + " videos");
    }
}
