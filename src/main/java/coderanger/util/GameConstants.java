package coderanger.util;

public final class GameConstants {
    public static final int WIDTH = 540;
    public static final int HEIGHT = 900;
    public static final int PLAYER_X = 270;
    public static final int PLAYER_Y = 820;
    public static final int DANGER_Y = 760;

    public static final double WAVE_DURATION = 35.0;
    public static final int NORMAL_WAVES = 7;
    public static final int BOSS_WAVE = 8;
    public static final double BOSS_START = WAVE_DURATION * NORMAL_WAVES; // 245.0s (Kì 8)

    public static final int BOSS_CENTER_X = 270;
    public static final int BOSS_CENTER_Y = 205;

    private GameConstants() {}
}
