package anjali.learning.skilshare.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.VideoItem;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(String videoId);
    }

    private List<VideoItem> videos;
    private final OnVideoClickListener listener;

    public VideoAdapter(OnVideoClickListener listener) {
        this.listener = listener;
    }

    public void setVideos(List<VideoItem> videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem video = videos.get(position);
        holder.videoTitle.setText(video.title);
        holder.videoDuration.setText("15 min"); // or whatever logic for duration you want
        holder.itemView.setOnClickListener(v -> listener.onVideoClick(video.videoId));
    }

    @Override
    public int getItemCount() {
        return videos != null ? videos.size() : 0;
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView videoTitle, videoDuration;
        ImageView playIcon, arrowIcon;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.videoTitle);
            videoDuration = itemView.findViewById(R.id.videoDuration);
            playIcon = itemView.findViewById(R.id.playIcon);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }
}
