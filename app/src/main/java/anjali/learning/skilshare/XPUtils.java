package anjali.learning.skilshare;

public class XPUtils {

    public static int getLevelFromXP(long xp) {
        return (int) (Math.sqrt(xp) / 10); // Example: XP 100 → Level 1, XP 400 → Level 2
    }

    public static int getXPForNextLevel(int currentLevel) {
        int nextLevel = currentLevel + 1;
        return (int) Math.pow((nextLevel * 10), 2); // Reverse formula to get XP needed
    }

    public static int getXPProgressInLevel(long xp) {
        int level = getLevelFromXP(xp);
        int currentLevelXP = getXPForNextLevel(level - 1);
        int nextLevelXP = getXPForNextLevel(level);
        return (int) (xp - currentLevelXP);
    }

    public static int getXPRangeForLevel(int level) {
        int currentLevelXP = getXPForNextLevel(level - 1);
        int nextLevelXP = getXPForNextLevel(level);
        return nextLevelXP - currentLevelXP;
    }
}
