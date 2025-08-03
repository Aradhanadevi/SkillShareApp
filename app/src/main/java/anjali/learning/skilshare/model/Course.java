package anjali.learning.skilshare.model;

import java.io.Serializable;

public class Course implements Serializable {
    private String description, Tutor, Category, language, location, imageUrl, courseName, skills;
    private int noofvideos, price;
    private String playlistlink;
    private int progress = 0;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    // THIS is the only extra field you add
    private String skils;

    public Course() {}

    // Getters
    public String getDescription() { return description; }
    public String getTutor() { return Tutor; }
    public String getLanguage() { return language; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }
    public int getNoofvideos() { return noofvideos; }
    public String getCategory() { return Category; }
    public String getSkills() { return skills; }
    public int getPrice() { return price; }
    public String getCourseName() { return courseName; }
    public String getPlaylistlink() { return playlistlink; }

    public String getSkils() { return skils; }

    // Setters
    public void setDescription(String description) { this.description = description; }
    public void setTutor(String Tutor) { this.Tutor = Tutor; }
    public void setLanguage(String language) { this.language = language; }
    public void setLocation(String location) { this.location = location; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setNoofvideos(int noofvideos) { this.noofvideos = noofvideos; }
    public void setCategory(String Category) { this.Category = Category; }
    public void setSkills(String skills) { this.skills = skills; }
    public void setPrice(int price) { this.price = price; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setPlaylistlink(String playlistlink) { this.playlistlink = playlistlink; }

    //  THIS is the mapping function: anytime Firebase finds "skils", it will copy it to "skills"
    public void setSkils(String skils) {
        this.skils = skils;
        this.skills = skils;
    }
}
