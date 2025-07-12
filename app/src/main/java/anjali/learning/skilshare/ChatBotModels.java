package anjali.learning.skilshare;

public class ChatBotModels {

    public static class ChatRequest {
        String message;

        public ChatRequest(String message) {
            this.message = message;
        }
    }

    public static class ChatResponse {
        String reply;

        public String getReply() {
            return reply;
        }
    }
}
