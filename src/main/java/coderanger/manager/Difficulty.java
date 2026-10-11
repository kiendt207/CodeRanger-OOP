package coderanger.manager;

public class Difficulty {

    public Difficulty() {
    }

    public int level(double survivalSeconds) {
        if (survivalSeconds < 0) survivalSeconds = 0;
        int lvl = (int) Math.floor(survivalSeconds / 60.0);
        return Math.min(4, lvl);
    }

    public double normalInterval(double now) {
        int lvl = level(now);
        return switch (lvl) {
            case 0 -> 3.0;
            case 1 -> 2.7;
            case 2 -> 2.4;
            case 3 -> 2.1;
            default -> Double.MAX_VALUE;
        };
    }

    public double normalSpeed(double now) {
        int lvl = level(now);
        return switch (lvl) {
            case 0 -> 55.0;
            case 1 -> 60.0;
            case 2 -> 65.0;
            case 3 -> 70.0;
            default -> 68.0;
        };
    }

    public double bonusInterval(double now) {
        int lvl = level(now);
        return switch (lvl) {
            case 0 -> 18.0;
            case 1 -> 17.0;
            case 2 -> 16.0;
            case 3 -> 15.0;
            default -> Double.MAX_VALUE;
        };
    }

    public double bonusSpeed(double now) {
        int lvl = level(now);
        return switch (lvl) {
            case 0 -> 71.5;
            case 1 -> 81.0;
            case 2 -> 87.75;
            case 3 -> 98.0;
            default -> 0.0;
        };
    }

    public int maxNormals(double now) {
        int lvl = level(now);
        return switch (lvl) {
            case 0 -> 5;
            case 1 -> 6;
            case 2 -> 7;
            case 3 -> 8;
            default -> 3; 
        };
    }
}
