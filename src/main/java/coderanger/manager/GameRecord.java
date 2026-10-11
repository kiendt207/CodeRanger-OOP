package coderanger.manager;

public record GameRecord(
    String timestamp, String name, String outcome, long score, double seconds,
    int correctChars, int wrongChars, double accuracy, int monstersKilled,
    int quickKills, int bonusKills, int bossWords, double wpm, int wave,
    int hp, int shield
) {}