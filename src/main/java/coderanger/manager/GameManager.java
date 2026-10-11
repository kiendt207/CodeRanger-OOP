package coderanger.manager;

import coderanger.model.LaserEffect;
import coderanger.model.Monster;
import coderanger.model.Player;
import coderanger.util.GameConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameManager {
    public enum GameState { MENU, PLAYING, PAUSED, VICTORY_ANIMATION, WIN, LOSS, RESULTS }

    private GameState state;
    private final HistoryStore historyStore;
    private final MonsterFactory monsterFactory;

    private final Player player;
    private final TargetManager targetManager;
    private final WaveManager waveManager;
    private final BuffManager buffManager;
    private final GameStats stats;

    private final List<Monster> monsters;
    private final List<LaserEffect> effects;

    private double elapsedSeconds;
    private String playerName;
    private boolean resultSaved;
    private GameRecord finalRecord;

    public GameManager(HistoryStore historyStore, MonsterFactory monsterFactory) {
        this.historyStore = historyStore;
        this.monsterFactory = monsterFactory;

        this.player = new Player(GameConstants.PLAYER_X, GameConstants.PLAYER_Y);
        this.targetManager = new TargetManager();
        this.waveManager = new WaveManager(new Difficulty(), monsterFactory);
        this.buffManager = new BuffManager();
        this.stats = new GameStats();

        this.monsters = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.state = GameState.MENU;
        this.elapsedSeconds = 0.0;
        this.playerName = "Player";
        this.resultSaved = false;
    }

    public void start(String name) {
        this.playerName = (name == null || name.trim().isEmpty()) ? "Player" : name.trim();
        this.state = GameState.PLAYING;
        this.elapsedSeconds = 0.0;
        this.resultSaved = false;
    }

    public void update(double dt) {
        if (state != GameState.PLAYING) return;

        elapsedSeconds += dt;

        List<Monster> spawned = waveManager.update(dt, elapsedSeconds, monsters);
        monsters.addAll(spawned);

        for (Monster m : monsters) {
            if (m.isAlive()) {
                m.update(dt);
            }
        }

        List<Monster> crossing = monsters.stream()
                .filter(m -> m.isAlive() && m.getY() >= GameConstants.DANGER_Y)
                .sorted(Comparator.comparingLong(Monster::getSpawnSequence))
                .toList();

        for (Monster m : crossing) {
            m.setAlive(false);
            player.takeDamage(1);
            stats.resetStreak();

            if (player.isDead()) {
                state = GameState.LOSS;
                targetManager.unlockTarget();
                saveAndSnapshot("LOSS");
                return; 
            }
        }

        for (LaserEffect effect : effects) {
            effect.update(dt);
        }
        effects.removeIf(e -> !e.isAlive());

        monsters.removeIf(m -> !m.isAlive());
    }

    public void handleCharacter(char c) {
        if (state != GameState.PLAYING) return;

        Monster target = targetManager.findAndLockTarget(c, monsters);
        if (target == null) {
            stats.recordKey(false);
            return;
        }

        boolean matched = targetManager.processInput(c);
        stats.recordKey(matched);

        if (matched) {
            effects.add(new LaserEffect(GameConstants.PLAYER_X, GameConstants.PLAYER_Y, target.getX(), target.getY()));

            if (target.isCompleted()) {
                boolean isQuick = target.isQuickKill(elapsedSeconds);
                stats.recordKill(target, isQuick, false);
                target.onDeath();
                targetManager.unlockTarget();
            }
        }
    }

    public void togglePause() {
        if (state == GameState.PLAYING) state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.PLAYING;
    }

    private void saveAndSnapshot(String outcome) {
        if (resultSaved) return;
        finalRecord = stats.snapshot(playerName, elapsedSeconds, outcome, waveManager.getCurrentWave(), player);
        try {
            historyStore.save(finalRecord);
        } catch (Exception ignored) {}
        resultSaved = true;
    }

    // Các getter cho UI vẽ snapshot
    public GameState getState() { return state; }
    public Player getPlayer() { return player; }
    public List<Monster> getMonsters() { return monsters; }
    public List<LaserEffect> getEffects() { return effects; }
    public GameStats getStats() { return stats; }
    public TargetManager getTargetManager() { return targetManager; }
    public GameRecord getResult() { return finalRecord; }
    public double getElapsedSeconds() { return elapsedSeconds; }
}
