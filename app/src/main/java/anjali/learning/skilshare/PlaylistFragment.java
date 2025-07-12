
//    youtubedataapiv3 key: AIzaSyBJ9-G9y1PJiSpVEpvBM7J6V4qxCe-X_FI
package anjali.learning.skilshare;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import anjali.learning.skilshare.Adapter.VideoAdapter;
import anjali.learning.skilshare.model.VideoItem;
import okhttp3.*;

    public class PlaylistFragment extends Fragment {

        private static final String API_KEY = "AIzaSyBJ9-G9y1PJiSpVEpvBM7J6V4qxCe-X_FI";

        private TextView courseTitleText;
        private WebView videoWebView;
        private RecyclerView videoListRecyclerView;
        private VideoAdapter adapter;
        private String firstVideoId = "";

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_playlist, container, false);

            courseTitleText = view.findViewById(R.id.courseTitle);
            videoWebView = view.findViewById(R.id.videoWebView);
            videoListRecyclerView = view.findViewById(R.id.videoListRecyclerView);

            Bundle args = getArguments();
            String courseName = args != null ? args.getString("courseName") : "Your Course";
            String playlistLink = args != null ? args.getString("playlistLink") : "";
            String playlistId = extractPlaylistId(playlistLink);

            courseTitleText.setText(courseName);

            videoWebView.getSettings().setJavaScriptEnabled(true);
            videoWebView.setWebChromeClient(new WebChromeClient());

            videoListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            adapter = new VideoAdapter(videoId -> {
                loadVideo(videoId);
            });
            videoListRecyclerView.setAdapter(adapter);

            fetchPlaylistVideos(playlistId);

            return view;
        }

        private void loadVideo(String videoId) {
            String html = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" +
                    videoId + "\" frameborder=\"0\" allowfullscreen></iframe>";
            videoWebView.loadData(html, "text/html", "utf-8");
        }

        private void fetchPlaylistVideos(String playlistId) {
            OkHttpClient client = new OkHttpClient();
            List<VideoItem> videoList = new ArrayList<>();
            fetchVideosRecursive(client, playlistId, null, videoList);
        }

        private void fetchVideosRecursive(OkHttpClient client, String playlistId, @Nullable String pageToken, List<VideoItem> videoList) {
            String url = "https://www.googleapis.com/youtube/v3/playlistItems" +
                    "?part=snippet&maxResults=50&playlistId=" + playlistId + "&key=" + API_KEY;

            if (pageToken != null) {
                url += "&pageToken=" + pageToken;
            }

            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                        JsonArray items = jsonObject.getAsJsonArray("items");

                        for (JsonElement item : items) {
                            JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                            if (snippet.has("resourceId")) {
                                JsonObject resourceId = snippet.getAsJsonObject("resourceId");
                                if (resourceId.has("videoId")) {
                                    String title = snippet.get("title").getAsString();
                                    String videoId = resourceId.get("videoId").getAsString();
                                    videoList.add(new VideoItem(title, videoId));
                                }
                            }
                        }

                        // Check for more pages
                        if (jsonObject.has("nextPageToken")) {
                            String nextPageToken = jsonObject.get("nextPageToken").getAsString();
                            fetchVideosRecursive(client, playlistId, nextPageToken, videoList); // 🔁 Call next page
                        } else {
                            // 🚀 All videos loaded — update UI
                            requireActivity().runOnUiThread(() -> {
                                adapter.setVideos(videoList);
                                if (!videoList.isEmpty()) {
                                    loadVideo(videoList.get(0).videoId); // Auto-play first video
                                }
                            });
                        }
                    }
                }
            });
        }


        private String extractPlaylistId(String url) {
            Uri uri = Uri.parse(url);
            return uri.getQueryParameter("list");
        }
    }
