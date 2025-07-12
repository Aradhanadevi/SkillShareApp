package anjali.learning.skilshare;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import anjali.learning.skilshare.Adapter.ChatAdapter;
import anjali.learning.skilshare.model.ChatMessage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText editMessage;
    private ImageView buttonSend;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerChat = findViewById(R.id.recyclerChat);
        editMessage = findViewById(R.id.editMessage);
        buttonSend = findViewById(R.id.buttonSend);

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, this);

        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(view -> {
            String userMessage = editMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                sendMessage(userMessage);
                editMessage.setText("");
            }
        });
    }

    private void sendMessage(String messageText) {
        messages.add(new ChatMessage(messageText, true));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerChat.scrollToPosition(messages.size() - 1);

        OkHttpClient client = new OkHttpClient();

        // Build OpenRouter JSON message list
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", "You are an AI assistant inside a Skillshare-style app. Help users with courses, summaries, and notes.");

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", messageText);

        // Create message array
        com.google.gson.JsonArray messagesArray = new com.google.gson.JsonArray();
        messagesArray.add(systemMsg);
        messagesArray.add(userMsg);

        // Final JSON body
        JsonObject json = new JsonObject();
        json.addProperty("model", "mistralai/mistral-7b-instruct");
        json.add("messages", messagesArray);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", "Bearer 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") // Replace with your own key later
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    messages.add(new ChatMessage("Failed to connect: " + e.getMessage(), false));
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.d("CHAT_RESPONSE", responseStr); // Optional debug

                String botReply = "No reply.";
                try {
                    JsonObject jsonObject = new Gson().fromJson(responseStr, JsonObject.class);
                    botReply = jsonObject.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String finalBotReply = botReply;
                runOnUiThread(() -> {
                    messages.add(new ChatMessage(finalBotReply, false));
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerChat.scrollToPosition(messages.size() - 1);
                });
            }
        });
    }
}
