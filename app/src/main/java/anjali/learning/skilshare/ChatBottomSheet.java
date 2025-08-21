package anjali.learning.skilshare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import anjali.learning.skilshare.Adapter.ChatAdapter;
import anjali.learning.skilshare.model.ChatMessage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageView btnSend;

    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    private OkHttpClient client = new OkHttpClient();

    private String name;
    private String skills;

    public static ChatBottomSheet newInstance(String name, String skills) {
        ChatBottomSheet sheet = new ChatBottomSheet();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("skills", skills);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerChat);
        inputMessage = view.findViewById(R.id.editTextMessage);
        btnSend = view.findViewById(R.id.sendButton);

        adapter = new ChatAdapter(messages, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Get name & skills from arguments
        if (getArguments() != null) {
            name = getArguments().getString("name");
            skills = getArguments().getString("skills");
        }

        // Show welcome message
        if (name != null && skills != null) {
            String welcome = "👋 Welcome, " + name + "! I'm your Skillshare assistant.\n" +
                    "I see you're interested in: " + skills + ".\n" +
                    "Ask me anything or let’s find you the perfect course!";
            addMessage(welcome, false);
        }

        btnSend.setOnClickListener(v -> {
            String userMessage = inputMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                addMessage(userMessage, true);
                inputMessage.setText("");
                sendToBot(userMessage, name, skills);
            }
        });

        return view;
    }

    private void addMessage(String text, boolean isUser) {
        messages.add(new ChatMessage(text, isUser));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    private void sendToBot(String userMessage, String userName, String userSkills) {
        ChatBotApiHelper helper = new ChatBotApiHelper();
        helper.sendMessage(userMessage, name, skills, new ChatBotApiHelper.ChatBotCallback() {
            @Override
            public void onSuccess(String reply) {
                requireActivity().runOnUiThread(() -> addMessage(reply, false));
            }

            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        addMessage("⚠️ Bot Error: " + e.getMessage(), false)
                );
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            View bottomSheet = getDialog().findViewById(R.id.bottom_sheet_root); // ✅ use your id
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }
    }
}
