package anjali.learning.skilshare.model;

public class VideoItem {
    private String videoId;
    private String title;
    private String videoUrl;
    private long duration; // in milliseconds

    public VideoItem() {}

    public VideoItem(String videoId, String title, String videoUrl, long duration) {
        this.videoId = videoId;
        this.title = title;
        this.videoUrl = videoUrl;
        this.duration = duration;
    }

    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
    public String getVideoUrl() { return videoUrl; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) {
        this.duration = duration;
    }
}
