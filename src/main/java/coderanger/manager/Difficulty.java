package coderanger.manager;

import coderanger.util.GameConstants;

public class Difficulty {

    public Difficulty() {
    }

    public int level(double survivalSeconds) {
        if (survivalSeconds < 0) {
            survivalSeconds = 0;
        }
        int tier = (int) Math.floor(survivalSeconds / GameConstants.WAVE_DURATION);
        return Math.min(GameConstants.NORMAL_WAVES, tier); // 0..7 (7 = Kì 8 Boss)
    }

    public int wordLevel(int tier) {
        return switch (Math.max(0, Math.min(6, tier))) {
            case 0, 1 -> 0;
            case 2, 3 -> 1;
            case 4 -> 2;
            default -> 3;
        };
    }

    public double normalInterval(double now) {
        return switch (level(now)) {
            case 0 -> 2.40;
            case 1 -> 2.10;
            case 2 -> 1.85;
            case 3 -> 1.65;
            case 4 -> 1.45;
            case 5 -> 1.30;
            case 6 -> 1.15;
            default -> Double.MAX_VALUE;
        };
    }

    public double normalSpeed(double now) {
        return switch (level(now)) {
            case 0 -> 68.0;
            case 1 -> 78.0;
            case 2 -> 88.0;
            case 3 -> 98.0;
            case 4 -> 108.0;
            case 5 -> 118.0;
            case 6 -> 128.0;
            default -> 92.0; // Boss minions
        };
    }

    public double bonusInterval(double now) {
        return switch (level(now)) {
            case 0 -> 18.0;
            case 1 -> 17.0;
            case 2 -> 16.0;
            case 3 -> 15.5;
            case 4 -> 15.0;
            case 5 -> 14.5;
            case 6 -> 14.0;
            default -> Double.MAX_VALUE;
        };
    }

    public double bonusSpeed(double now) {
        return switch (level(now)) {
            case 0 -> 92.0;
            case 1 -> 105.0;
            case 2 -> 121.0;
            case 3 -> 135.0;
            case 4 -> 151.0;
            case 5 -> 168.0;
            case 6 -> 185.0;
            default -> 0.0;
        };
    }

    public int maxNormals(double now) {
        return switch (level(now)) {
            case 0 -> 6;
            case 1 -> 7;
            case 2 -> 8;
            case 3 -> 9;
            case 4 -> 10;
            case 5 -> 11;
            case 6 -> 12;
            default -> 4;
        };
    }

    // ================ Streak Escalation (Overdrive) ================

    public int streakTier(int streak) {
        if (streak >= 45) return 4;
        if (streak >= 30) return 3;
        if (streak >= 20) return 2;
        if (streak >= 10) return 1;
        return 0;
    }

    public double streakIntervalMultiplier(int streak) {
        return switch (streakTier(streak)) {
            case 1 -> 0.88;
            case 2 -> 0.78;
            case 3 -> 0.68;
            case 4 -> 0.58;
            default -> 1.00;
        };
    }

    public int streakExtraMaxNormals(int streak) {
        return streakTier(streak);
    }

    public double streakSpeedMultiplier(int streak) {
        return switch (streakTier(streak)) {
            case 1 -> 1.05;
            case 2 -> 1.10;
            case 3 -> 1.14;
            case 4 -> 1.18;
            default -> 1.00;
        };
    }

    public double effectiveNormalInterval(double now, int streak) {
        double base = normalInterval(now);
        if (base == Double.MAX_VALUE) {
            return Double.MAX_VALUE;
        }
        return base * streakIntervalMultiplier(streak);
    }

    public int effectiveMaxNormals(double now, int streak) {
        if (level(now) >= GameConstants.NORMAL_WAVES) {
            return maxNormals(now);
        }
        return maxNormals(now) + streakExtraMaxNormals(streak);
    }

    public double effectiveNormalSpeed(double now, int streak) {
        if (level(now) >= GameConstants.NORMAL_WAVES) {
            return normalSpeed(now);
        }
        return normalSpeed(now) * streakSpeedMultiplier(streak);
    }

    public String streakLabel(int streak) {
        return switch (streakTier(streak)) {
            case 1 -> "OVERDRIVE I";
            case 2 -> "OVERDRIVE II";
            case 3 -> "OVERDRIVE III";
            case 4 -> "FEVER MAX!";
            default -> "";
        };
    }
}
