package anjali.learning.skilshare.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Rect;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import anjali.learning.skilshare.R;

public class DinoGameActivity extends AppCompatActivity {

    private View dino;
    private View obstacle;
    private Handler handler = new Handler();
    private int score = 0;
    private TextView scoreText;
    private boolean isJumping = false;
    private boolean isGamePaused = false;

    private float obstacleX;
    private float obstacleSpeed = 20f; // start slower
    private final float maxSpeed = 30f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dino_game);

        dino = findViewById(R.id.dino);
        obstacle = findViewById(R.id.obstacle);
        scoreText = findViewById(R.id.scoreText);

        // Ensure dino translation starts at 0
        dino.setTranslationY(0f);

        obstacleX = 1500f; // initial obstacle position

        startGame();

        dino.setOnClickListener(v -> {
            if (!isJumping && !isGamePaused) {
                jump();
            }
        });
    }

    private void startGame() {
        handler.postDelayed(gameRunnable, 1000);
    }

    private Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (isGamePaused) {
                handler.postDelayed(this, 400);
                return;
            }

            // Move obstacle left
            obstacleX -= obstacleSpeed;
            obstacle.setX(obstacleX);

            // Reset obstacle when off screen
            if (obstacleX < -100) {
                obstacleX = 1000;
                score++;
                scoreText.setText("Score: " + score);

                // Slowly increase speed
                if (obstacleSpeed < maxSpeed) {
                    obstacleSpeed += 0.3f;
                }

                // Quiz trigger every 5 points
                if (score % 5 == 0) {
                    pauseGameAndShowQuiz();
                }
            }

            // Collision check
            if (isColliding(dino, obstacle)) {
                gameOver();
                return;
            }

            handler.postDelayed(this, 25);
        }
    };

    private boolean isColliding(View a, View b) {
        Rect rectA = new Rect(a.getLeft(),
                (int)(a.getTop() + a.getTranslationY()),
                a.getRight(),
                (int)(a.getBottom() + a.getTranslationY()));
        Rect rectB = new Rect();
        b.getHitRect(rectB);
        return Rect.intersects(rectA, rectB);
    }

    private void jump() {
        if (isJumping) return;
        isJumping = true;

        float jumpHeight = 500f;  // pixels
        long duration = 750;      // up/down duration

        ObjectAnimator up = ObjectAnimator.ofFloat(dino, "translationY", dino.getTranslationY(), dino.getTranslationY() - jumpHeight);
        up.setDuration(duration);

        ObjectAnimator down = ObjectAnimator.ofFloat(dino, "translationY", dino.getTranslationY() - jumpHeight, 0f);
        down.setDuration(duration);

        AnimatorSet jumpSet = new AnimatorSet();
        jumpSet.playSequentially(up, down);
        jumpSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isJumping = false;
            }
        });

        jumpSet.start();
    }

    private void pauseGameAndShowQuiz() {
        isGamePaused = true;

        new AlertDialog.Builder(this)
                .setTitle("Quiz Time!")
                .setMessage("What is 2 + 2?")
                .setPositiveButton("4", (dialog, which) -> {
                    Toast.makeText(this, "Correct! Boost activated!", Toast.LENGTH_SHORT).show();
                    // TODO: add boost logic
                    isGamePaused = false;
                })
                .setNegativeButton("Wrong", (dialog, which) -> {
                    Toast.makeText(this, "Wrong! Game gets harder!", Toast.LENGTH_SHORT).show();
                    // TODO: increase difficulty
                    isGamePaused = false;
                })
                .setCancelable(false)
                .show();
    }

    private void gameOver() {
        Toast.makeText(this, "Game Over! Score: " + score, Toast.LENGTH_LONG).show();
        finish();
    }
}
