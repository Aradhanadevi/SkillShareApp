package anjali.learning.skilshare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
        MediaType mediaType = MediaType.parse("application/json");

        JSONObject systemMessage = new JSONObject();
        JSONObject userMessageObj = new JSONObject();
        try {
            String introPrompt = "You are an AI assistant inside a Skillshare-style app. " +
                    "The user's name is \"" + userName + "\" and they are interested in: " + userSkills + ". " +
                    "You help the user by answering questions about the number of courses available on different topics or skills, " +
                    "summarizing specific courses when requested, and suggesting 3 relevant courses based on their skills. " +
                    "If the user asks how many courses there are on a topic, provide a friendly and helpful answer. " +
                    "Be concise and clear in all responses.";


            systemMessage.put("role", "system");
            systemMessage.put("content", introPrompt);

            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);

            org.json.JSONArray messagesArray = new org.json.JSONArray();
            messagesArray.put(systemMessage);
            messagesArray.put(userMessageObj);

            JSONObject requestBodyJson = new JSONObject();
            requestBodyJson.put("model", "mistralai/mistral-7b-instruct");
            requestBodyJson.put("messages", messagesArray);

            RequestBody body = RequestBody.create(requestBodyJson.toString(), mediaType);

            Request request = new Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .addHeader("Authorization", "Bearer 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")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        String botReply = "No reply.";
                        try {
                            JSONObject jsonObject = new JSONObject(responseStr);
                            JSONObject messageObj = jsonObject
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message");
                            botReply = messageObj.getString("content");
                        } catch (JSONException e) {
                            botReply = "Failed to parse bot response.";
                        }

                        String finalBotReply = botReply;
                        requireActivity().runOnUiThread(() -> addMessage(finalBotReply, false));
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });

        } catch (JSONException e) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Error building request", Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            View bottomSheet = getDialog().findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }
    }
}
