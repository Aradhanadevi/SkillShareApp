package anjali.learning.skilshare;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.gson.*;

import java.io.IOException;
import java.util.*;

import anjali.learning.skilshare.Adapter.VideoAdapter;
import anjali.learning.skilshare.model.VideoItem;
import okhttp3.*;

public class PlaylistFragment extends Fragment {

    private static final String API_KEY = "AIzaSyBJ9-G9y1PJiSpVEpvBM7J6V4qxCe-X_FI";

    private TextView courseTitleText;
    private WebView videoWebView;
    private ProgressBar courseProgressBar;
    private RecyclerView videoListRecyclerView;

    private String courseName;
    private String playlistId;
    private String lastPlayedVideoId = "";
    private List<VideoItem> videoList = new ArrayList<>();
//    private ProgressBar progressBar;
    private TextView progressText;
    private VideoAdapter adapter;

    private String username;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        courseTitleText = view.findViewById(R.id.courseTitle);
        videoWebView = view.findViewById(R.id.videoWebView);
        courseProgressBar = view.findViewById(R.id.courseProgressBar);
        videoListRecyclerView = view.findViewById(R.id.videoListRecyclerView);
        progressText = view.findViewById(R.id.progressText);

        videoWebView.getSettings().setJavaScriptEnabled(true);
        videoWebView.setWebChromeClient(new WebChromeClient());
        videoWebView.setWebViewClient(new WebViewClient());
        videoWebView.addJavascriptInterface(new VideoCompleteListener(), "Android");

        username = requireContext()
                .getSharedPreferences("SkillSharePrefs", Context.MODE_PRIVATE)
                .getString("currentUsername", null);

        Bundle args = getArguments();
        courseName = args != null ? args.getString("courseName") : "Your Course";
        String playlistLink = args != null ? args.getString("playlistLink") : "";
        playlistId = extractPlaylistId(playlistLink);

        courseTitleText.setText(courseName);
        videoListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VideoAdapter(getContext(), videoList, courseName, video -> {
            loadVideo(video.getVideoId());
        });

        videoListRecyclerView.setAdapter(adapter);

        fetchPlaylistVideos(playlistId);


        return view;
    }

    private void loadVideo(String videoId) {
        lastPlayedVideoId = videoId;

        String html = "<html><body style=\"margin:0px;padding:0px;\">" +
                "<div id=\"player\"></div>" +
                "<script>" +
                "var tag = document.createElement('script');" +
                "tag.src = \"https://www.youtube.com/iframe_api\";" +
                "var firstScriptTag = document.getElementsByTagName('script')[0];" +
                "firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);" +
                "var player;" +
                "function onYouTubeIframeAPIReady() {" +
                "  player = new YT.Player('player', {" +
                "    height: '100%', width: '100%', videoId: '" + videoId + "'," +
                "    events: { 'onStateChange': onPlayerStateChange }" +
                "  });" +
                "}" +
                "function onPlayerStateChange(event) {" +
                "  if (event.data == YT.PlayerState.ENDED) {" +
                "    Android.onVideoEnded();" +
                "  }" +
                "}" +
                "</script></body></html>";

        videoWebView.loadData(html, "text/html", "utf-8");
    }

    private void fetchPlaylistVideos(String playlistId) {
        OkHttpClient client = new OkHttpClient();
        fetchVideosRecursive(client, playlistId, null);
    }

    private void fetchVideosRecursive(OkHttpClient client, String playlistId, @Nullable String pageToken) {
        String url = "https://www.googleapis.com/youtube/v3/playlistItems" +
                "?part=snippet&maxResults=50&playlistId=" + playlistId + "&key=" + API_KEY;

        if (pageToken != null) {
            url += "&pageToken=" + pageToken;
        }

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
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
                        JsonObject resourceId = snippet.getAsJsonObject("resourceId");
                        String title = snippet.get("title").getAsString();
                        String videoId = resourceId.get("videoId").getAsString();
                        videoList.add(new VideoItem(videoId, title, "", 0));
                    }

                    if (jsonObject.has("nextPageToken")) {
                        String nextPageToken = jsonObject.get("nextPageToken").getAsString();
                        fetchVideosRecursive(client, playlistId, nextPageToken);
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            adapter.fetchDurationsForVideos();
                            if (!videoList.isEmpty()) {
                                loadVideo(videoList.get(0).getVideoId());
                            }
                            updateProgressBar();
                        });
                    }
                }
            }
        });
    }

    private void updateProgressBar() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(username).child("progress").child(courseName);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int watched = (int) snapshot.getChildrenCount();
                int total = videoList.size();
                int progress = (int) ((watched * 100.0f) / videoList.size());
                int progressPercent = 0;
                if (total > 0) {
                    progressPercent = (int) ((watched * 100.0f) / total);
                    courseProgressBar.setProgress(progressPercent);
                    progressText.setText("Progress: " + progressPercent + "% (" + watched + "/" + total + ")");
                } else {
                    courseProgressBar.setProgress(0);
                    progressText.setText("No videos");
                }


                ref.child("progress").setValue(progressPercent);
                courseProgressBar.setProgress(progress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void markVideoAsWatched(String courseId, String videoId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(username).child("progress").child(courseName);
        ref.child(videoId).setValue(true);
        updateProgressBar();
        playNextVideo();
    }

    private void playNextVideo() {
        int index = -1;
        for (int i = 0; i < videoList.size(); i++) {
            if (videoList.get(i).getVideoId().equals(lastPlayedVideoId)) {
                index = i;
                break;
            }
        }
        if (index != -1 && index + 1 < videoList.size()) {
            loadVideo(videoList.get(index + 1).getVideoId());
        }
    }

    private class VideoCompleteListener {
        @JavascriptInterface
        public void onVideoEnded() {
            requireActivity().runOnUiThread(() -> {
                markVideoAsWatched(courseName, lastPlayedVideoId);
            });
        }
    }

    private String extractPlaylistId(String url) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter("list");
    }
}
