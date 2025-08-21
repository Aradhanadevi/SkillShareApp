package anjali.learning.skilshare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
//This is trying
public class Splashscreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 4000; // 4 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        FrameLayout orbitSmall = findViewById(R.id.orbit_small);
        FrameLayout orbitMedium = findViewById(R.id.orbit_medium);
        FrameLayout orbitLarge = findViewById(R.id.orbit_large);

        // Orbit 1 - fast clockwise
        startOrbitAnimation(orbitSmall, 2000, true);

        // Orbit 2 - medium clockwise
        startOrbitAnimation(orbitMedium, 3000, true);

        // Orbit 3 - slow counter-clockwise
        startOrbitAnimation(orbitLarge, 4000, false);

        // Move to SignInActivity after splash
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splashscreen.this, SignInActivity.class));
            finish();
        }, SPLASH_DURATION);
    }

    private void startOrbitAnimation(FrameLayout orbit, int duration, boolean clockwise) {
        RotateAnimation rotate = new RotateAnimation(
                0, clockwise ? 360 : -360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(duration);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        orbit.startAnimation(rotate);
    }
}
