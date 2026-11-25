package coderanger.manager;

public record GameRecord(
    String timestamp, String name, String outcome, long score, double seconds,
    int correctChars, int wrongChars, double accuracy, int monstersKilled,
    int quickKills, int bonusKills, int bossWords, double wpm, int wave,
    int hp, int shield, int peakStreak
) {
    public GameRecord(
            String timestamp, String name, String outcome, long score, double seconds,
            int correctChars, int wrongChars, double accuracy, int monstersKilled,
            int quickKills, int bonusKills, int bossWords, double wpm, int wave,
            int hp, int shield) {
        this(timestamp, name, outcome, score, seconds, correctChars, wrongChars,
                accuracy, monstersKilled, quickKills, bonusKills, bossWords,
                wpm, wave, hp, shield, 0);
    }

    public int kills() { return monstersKilled; }
    public int quick() { return quickKills; }
    public int bonus() { return bonusKills; }
    public int correct() { return correctChars; }
    public int wrong() { return wrongChars; }
}
