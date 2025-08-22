package anjali.learning.skilshare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
//Bro i swear to god if this doesnot work then i will crash out
public class DailyQuizActivity extends AppCompatActivity {

    private TextView questionText, questionCounter, scoreText;
    private RadioGroup optionsGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button submitButton;

    private List<Map<String, Object>> questionsList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    private String selectedCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_quiz);

        questionText = findViewById(R.id.questionText);
        questionCounter = findViewById(R.id.questionCounter);
        scoreText = findViewById(R.id.scoreText);
        optionsGroup = findViewById(R.id.optionsGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        submitButton = findViewById(R.id.submitButton);

        loadRegisteredCourseAndStartQuiz();

        submitButton.setOnClickListener(v -> checkAnswerAndProceed());
    }

    private void loadRegisteredCourseAndStartQuiz() {
        SharedPreferences prefs = getSharedPreferences("SkillSharePrefs", MODE_PRIVATE);
        String username = prefs.getString("currentUsername", null);

        if (username == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference userCoursesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(username)
                .child("registeredCourses");

        userCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    Toast.makeText(DailyQuizActivity.this, "No registered courses found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    selectedCourse = courseSnap.getKey(); // use the first course
                    break;
                }

                if (selectedCourse != null) {
                    Log.d("QUIZ_DEBUG", "Selected course: " + selectedCourse);
                    fetchQuizQuestions();
                } else {
                    Toast.makeText(DailyQuizActivity.this, "Failed to determine course.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyQuizActivity.this, "Error loading course: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchQuizQuestions() {
        DatabaseReference quizRef = FirebaseDatabase.getInstance()
                .getReference("courses")
                .child(selectedCourse)
                .child("quiz");

        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot questionSnap : snapshot.getChildren()) {
                    Map<String, Object> questionData = (Map<String, Object>) questionSnap.getValue();
                    questionsList.add(questionData);
                }

                if (!questionsList.isEmpty()) {
                    showQuestion();
                } else {
                    Toast.makeText(DailyQuizActivity.this, "No quiz available for selected course.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyQuizActivity.this, "Failed to load quiz.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuestion() {
        Map<String, Object> currentQuestion = questionsList.get(currentQuestionIndex);
        questionText.setText((String) currentQuestion.get("question"));
        questionCounter.setText("Q" + (currentQuestionIndex + 1) + "/" + questionsList.size());

        optionA.setText(String.valueOf(currentQuestion.get("optionA")));
        optionB.setText(String.valueOf(currentQuestion.get("optionB")));
        optionC.setText(String.valueOf(currentQuestion.get("optionC")));
        optionD.setText(String.valueOf(currentQuestion.get("optionD")));
        optionsGroup.clearCheck();
    }

    private void checkAnswerAndProceed() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedButton = findViewById(selectedId);
        String selectedAnswer = selectedButton.getText().toString();

        Map<String, Object> currentQuestion = questionsList.get(currentQuestionIndex);
        String correctAnswerKey = (String) currentQuestion.get("answer"); // e.g., optionA
        String correctAnswerValue = String.valueOf(currentQuestion.get(correctAnswerKey));

        if (selectedAnswer.equals(correctAnswerValue)) {
            score++;
            Toast.makeText(this, "Correct! 🎉", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! Correct answer was: " + correctAnswerValue, Toast.LENGTH_SHORT).show();
        }

        currentQuestionIndex++;

        new android.os.Handler().postDelayed(() -> {
            if (currentQuestionIndex < questionsList.size()) {
                showQuestion();
            } else {
                endQuiz();
            }
        }, 800);
    }

    private void endQuiz() {
        questionText.setVisibility(View.GONE);
        optionsGroup.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        questionCounter.setVisibility(View.GONE);

        scoreText.setVisibility(View.VISIBLE);
        scoreText.setText("Score: " + score + "/" + questionsList.size());

        SharedPreferences prefs = getSharedPreferences("SkillSharePrefs", MODE_PRIVATE);
        String username = prefs.getString("currentUsername", null);
        if (username != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
            userRef.child("level").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long level = snapshot.getValue(Long.class);
                    if (level != null) {
                        Toast.makeText(DailyQuizActivity.this, "Level: " + level, Toast.LENGTH_LONG).show();
                        scoreText.append("\nLevel: " + level);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        addXPAfterQuiz(score * 10); // 10 XP per correct answer
    }

    private void addXPAfterQuiz(int xpToAdd) {
        SharedPreferences prefs = getSharedPreferences("SkillSharePrefs", MODE_PRIVATE);
        String username = prefs.getString("currentUsername", null);
        if (username == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long currentXp = snapshot.child("xp").getValue(Long.class);
                String lastQuizDate = snapshot.child("lastQuizDate").getValue(String.class);
                if (currentXp == null) currentXp = 0L;

                String today = java.time.LocalDate.now().toString(); // YYYY-MM-DD

                if (today.equals(lastQuizDate)) {
                    Toast.makeText(DailyQuizActivity.this, "You've already completed today's quiz!", Toast.LENGTH_LONG).show();
                    return;
                }

                long newXp = currentXp + xpToAdd;
                userRef.child("xp").setValue(newXp);
                userRef.child("level").setValue(newXp / 100);
                userRef.child("lastQuizDate").setValue(today);

                String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());

                DatabaseReference streakRef = FirebaseDatabase.getInstance().getReference("users").child(username);
                streakRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String lastActive = snapshot.child("lastActiveDate").getValue(String.class);
                        Long currentStreak = snapshot.child("streak").getValue(Long.class);
                        if (currentStreak == null) currentStreak = 0L;

                        if (lastActive == null || !lastActive.equals(todayDate)) {
                            currentStreak += 1;
                        }

                        streakRef.child("streak").setValue(currentStreak);
                        streakRef.child("lastActiveDate").setValue(todayDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

                Toast.makeText(DailyQuizActivity.this, "XP earned: " + xpToAdd, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("DailyQuiz", "Failed to update XP: " + error.getMessage());
            }
        });
    }
}
