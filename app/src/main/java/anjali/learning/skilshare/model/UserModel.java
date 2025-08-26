package anjali.learning.skilshare.model;

public class UserModel {
    public String name, email, username;
    public int xp;
    public int level;
    public int streak;

    public UserModel() {}

    public UserModel(String name, String email, String username, int xp, int level, int streak) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.xp = xp;
        this.level = level;
        this.streak = streak;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public int getStreak() {
        return streak;
    }
}