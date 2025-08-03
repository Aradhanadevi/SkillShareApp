package anjali.learning.skilshare.Adapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.VideoItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<VideoItem> videoList;
    private String courseName;
    private int currentPlayingIndex = -1;
    private OnVideoClickListener listener;
    private Set<String> watchedVideos = new HashSet<>();
    private String username;
    String API_KEY="AIzaSyBJ9-G9y1PJiSpVEpvBM7J6V4qxCe-X_FI";

    public interface OnVideoClickListener {
        void onVideoClick(VideoItem video);
    }

    public VideoAdapter(Context context, List<VideoItem> videoList, String courseName, OnVideoClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.courseName = courseName;
        this.listener = listener;

        // ✅ Get username from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("SkillSharePrefs", Context.MODE_PRIVATE);
        username = prefs.getString("currentUsername", null);

        fetchWatchedVideos();
        fetchDurationsForVideos();
    }

    private void fetchWatchedVideos() {
        if (username == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(username).child("progress").child(courseName);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot videoSnap : snapshot.getChildren()) {
                    watchedVideos.add(videoSnap.getKey());
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem video = videoList.get(position);
        holder.title.setText(video.getTitle());
        holder.duration.setText(formatDuration(video.getDuration()));

        // ✅ Mark watched based on videoId, not title
        boolean watched = watchedVideos.contains(video.getVideoId());
        holder.itemView.setAlpha(watched ? 0.5f : 1f);

        holder.itemView.setOnClickListener(v -> {
            listener.onVideoClick(video);
            markAsWatched(video);
            currentPlayingIndex = position;
        });

        holder.arrowIcon.setOnClickListener(v -> {
            int nextIndex = position + 1;
            if (nextIndex < videoList.size()) {
                VideoItem nextVideo = videoList.get(nextIndex);
                listener.onVideoClick(nextVideo);
                markAsWatched(nextVideo);
                currentPlayingIndex = nextIndex;
            }
        });
    }

    private void markAsWatched(VideoItem video) {
        if (username == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(username).child("progress").child(courseName);

        ref.child(video.getVideoId()).setValue(true);
        watchedVideos.add(video.getVideoId());
        notifyDataSetChanged();
    }

    private String formatDuration(long durationMillis) {
        long totalSeconds = durationMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView title, duration;
        ImageView arrowIcon;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.videoTitle);
            duration = itemView.findViewById(R.id.videoDuration);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }

    private long parseIsoDuration(String iso) {
        long sec = 0;
        Matcher m = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?").matcher(iso);
        if (m.matches()) {
            if (m.group(1) != null) sec += Integer.parseInt(m.group(1)) * 3600;
            if (m.group(2) != null) sec += Integer.parseInt(m.group(2)) * 60;
            if (m.group(3) != null) sec += Integer.parseInt(m.group(3));
        }
        return sec * 1000;
    }

    public void fetchDurationsForVideos() {
        String ids = TextUtils.join(",", videoList.stream()
                .map(VideoItem::getVideoId)
                .collect(Collectors.toList()));

        String url = "https://www.googleapis.com/youtube/v3/videos"
                + "?part=contentDetails&id=" + ids
                + "&key=" + API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("VideoAdapter", "Failed to fetch durations", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;

                String responseBody = response.body().string();
                JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray items = root.getAsJsonArray("items");
                Map<String, String> durMap = new HashMap<>();

                for (JsonElement e : items) {
                    JsonObject obj = e.getAsJsonObject();
                    String vid = obj.get("id").getAsString();
                    String isoDur = obj.getAsJsonObject("contentDetails").get("duration").getAsString();
                    durMap.put(vid, isoDur);
                }

                // ✅ Update UI on main thread
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> {
                    for (VideoItem vi : videoList) {
                        String iso = durMap.get(vi.getVideoId());
                        if (iso != null) {
                            vi.setDuration(parseIsoDuration(iso));
                        }
                    }
                    notifyDataSetChanged();
                });
            }
        });

    }


}
