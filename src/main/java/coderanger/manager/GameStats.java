package coderanger.manager;

import coderanger.model.Monster;
import coderanger.model.Player;
import java.time.Instant;

public class GameStats {
    private long score;
    private int streak;
    private int peakStreak;
    private int correctChars;
    private int wrongChars;
    private int monstersKilled;
    private int quickKills;
    private int bonusKills;
    private int bossWords;
    private boolean bossKilled;

    public GameStats() {
        this.score = 0;
        this.streak = 0;
        this.peakStreak = 0;
        this.correctChars = 0;
        this.wrongChars = 0;
        this.monstersKilled = 0;
        this.quickKills = 0;
        this.bonusKills = 0;
        this.bossWords = 0;
        this.bossKilled = false;
    }

    public void recordKey(boolean isCorrect) {
        if (isCorrect) {
            correctChars++;
            streak++;
            if (streak > peakStreak) {
                peakStreak = streak;
            }
        } else {
            wrongChars++;
            streak = 0;
        }
    }

    public void recordKill(Monster monster, boolean isQuickKill, boolean isShockwave) {
        if (isShockwave) {
            score = Math.min(Long.MAX_VALUE, score + 25);
            monstersKilled++;
            return;
        }

        // Kill trực tiếp
        double multiplier = Math.min(2.0, 1.0 + 0.1 * Math.floor(streak / 5.0));
        long points = (long) Math.floor(monster.getRewardScore() * multiplier);
        if (isQuickKill) {
            points += 50;
            quickKills++;
        }
        score = Math.min(Long.MAX_VALUE, score + points);
        monstersKilled++;
    }

    public void recordBossWord() {
        bossWords++;
        score = Math.min(Long.MAX_VALUE, score + 100);
    }

    public void recordBossDeath() {
        bossKilled = true;
        score = Math.min(Long.MAX_VALUE, score + 1000);
    }

    public void resetStreak() {
        this.streak = 0;
    }

    public GameRecord snapshot(String playerName, double seconds, String outcome, int wave, Player player) {
        double accuracy = 0.0;
        if (correctChars + wrongChars > 0) {
            accuracy = ((double) correctChars / (correctChars + wrongChars)) * 100.0;
        }
        double minutes = seconds / 60.0;
        double wpm = (minutes > 0) ? (correctChars / 5.0) / minutes : 0.0;

        return new GameRecord(
            Instant.now().toString(),
            playerName,
            outcome,
            score,
            seconds,
            correctChars,
            wrongChars,
            accuracy,
            monstersKilled,
            quickKills,
            bonusKills,
            bossWords,
            wpm,
            wave,
            player != null ? player.getHp() : 0,
            player != null ? player.getShieldLayers() : 0
        );
    }

    public long getScore() { return score; }
    public int getStreak() { return streak; }
    public int getPeakStreak() { return peakStreak; }
}