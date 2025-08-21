package anjali.learning.skilshare;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.*;

import java.io.IOException;

public class ChatBotApiHelper {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "Bearer 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";

    private final OkHttpClient client;

    public ChatBotApiHelper() {
        client = new OkHttpClient();
    }

    public void sendMessage(String message, String userName, String userSkills, ChatBotCallback callback) {
        // Build system prompt with context
        String introPrompt = "You are an AI assistant inside a Skillshare-style app. " +
                "The user's name is \"" + userName + "\" and they are interested in: " + userSkills + ". " +
                "Help them by suggesting courses, giving summaries, and taking notes.";

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", introPrompt);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", message);

        JsonArray messagesArray = new JsonArray();
        messagesArray.add(systemMsg);
        messagesArray.add(userMsg);

        JsonObject json = new JsonObject();
        json.addProperty("model", "mistralai/mistral-7b-instruct");
        json.add("messages", messagesArray);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(OPENROUTER_URL)
                .addHeader("Authorization", API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "null";
                    callback.onFailure(new IOException("HTTP " + response.code() + ": " + errorBody));
                    return;
                }

                String responseStr = response.body().string();
                try {
                    JsonObject jsonObject = new Gson().fromJson(responseStr, JsonObject.class);
                    String botReply = jsonObject.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();
                    callback.onSuccess(botReply);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        });
    }


    public interface ChatBotCallback {
        void onSuccess(String reply);
        void onFailure(Exception e);
    }
}
