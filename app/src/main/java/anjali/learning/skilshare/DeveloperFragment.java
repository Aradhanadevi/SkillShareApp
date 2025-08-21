package anjali.learning.skilshare;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeveloperFragment extends Fragment {

    public DeveloperFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_developer, container, false);

        // Email click actions
        TextView emailDev1 = view.findViewById(R.id.email_dev1);
        TextView emailDev2 = view.findViewById(R.id.email_dev2);
        TextView emailGuide = view.findViewById(R.id.email_guide);

        emailDev1.setOnClickListener(v -> openEmail("anjalivalani728@gmail.com"));
        emailDev2.setOnClickListener(v -> openEmail("aradhanajadeja81@gmail.com"));
        emailGuide.setOnClickListener(v -> openEmail("jigar.dave@marwadieducation.edu.in"));

        return view;
    }

    private void openEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}
