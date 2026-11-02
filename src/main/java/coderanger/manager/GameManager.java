package coderanger.manager;

import coderanger.model.BossMonster;
import coderanger.model.LaserEffect;
import coderanger.model.Monster;
import coderanger.model.Player;
import coderanger.util.GameConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    private double victoryAnimationRemaining;
    private String saveErrorMessage;

    private static final double SHOCKWAVE_COOLDOWN = 5.0;
    private double shockwaveCooldown;   // giây còn lại, 0 = sẵn sàng

    public GameManager(HistoryStore historyStore, MonsterFactory monsterFactory) {
        this(historyStore, monsterFactory, new TargetManager());
    }

    GameManager(HistoryStore historyStore, MonsterFactory monsterFactory, TargetManager tm) {
        this.historyStore = Objects.requireNonNull(historyStore, "historyStore");
        this.monsterFactory = Objects.requireNonNull(monsterFactory, "monsterFactory");

        this.player = new Player();
        this.targetManager = Objects.requireNonNull(tm, "targetManager");
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
        player.reset();
        stats.reset();
        monsters.clear();
        effects.clear();
        targetManager.unlockTarget();
        waveManager.reset();
        monsterFactory.reset();
        this.state = GameState.PLAYING;
        this.elapsedSeconds = 0.0;
        this.resultSaved = false;
        this.finalRecord = null;
        this.saveErrorMessage = null;
        this.shockwaveCooldown = 0.0;
        this.victoryAnimationRemaining = 0.0;
    }

    public void update(double dt) {
        if (state == GameState.VICTORY_ANIMATION) {
            if (!Double.isFinite(dt) || dt < 0.0) {
                throw new IllegalArgumentException("dt must be finite and non-negative");
            }
            victoryAnimationRemaining = Math.max(0.0, victoryAnimationRemaining - dt);
            if (victoryAnimationRemaining == 0.0) {
                state = GameState.WIN;
                saveAndSnapshot("WIN");
            }
            return;
        }
        if (state != GameState.PLAYING) return;
        if (!Double.isFinite(dt) || dt < 0.0) {
            throw new IllegalArgumentException("dt must be finite and non-negative");
        }

        elapsedSeconds += dt;

        if (shockwaveCooldown > 0.0) {
            shockwaveCooldown = Math.max(0.0, shockwaveCooldown - dt);
        }

        List<Monster> spawned = waveManager.update(dt, elapsedSeconds, monsters);
        monsters.addAll(spawned);
        if (waveManager.isBossDue(elapsedSeconds)) {
            monsters.removeIf(monster -> !monster.isBoss());
            targetManager.unlockTarget();
            Monster boss = monsterFactory.createBoss(elapsedSeconds);
            if (boss == null || !boss.isBoss()) {
                throw new IllegalStateException("MonsterFactory must create a boss at the boss wave");
            }
            monsters.add(boss);
            waveManager.markBossSpawned();
        }

        for (Monster m : monsters) {
            if (m.isAlive()) {
                m.update(dt);
            }
        }

        BossMonster boss = monsters.stream()
                .filter(Monster::isBoss)
                .filter(Monster::isAlive)
                .map(BossMonster.class::cast)
                .findFirst()
                .orElse(null);
        if (boss != null) {
            if (boss.consumeSkillPending()) {
                player.takeDamage(1);
                stats.resetStreak();
                if (player.getHp() == 0) {
                    enterLossState();
                    return;
                }
            }

            long activeMinions = monsters.stream()
                    .filter(Monster::isAlive)
                    .filter(Monster::isMinion)
                    .count();
            if (boss.summonMinions() && activeMinions < 3) {
                Monster minion = monsterFactory.createNormal(
                        new Difficulty().level(elapsedSeconds), elapsedSeconds, true);
                if (minion == null || !minion.isMinion()) {
                    throw new IllegalStateException("MonsterFactory must create a minion");
                }
                monsters.add(minion);
            }
        }

        List<Monster> crossing = monsters.stream()
                .filter(m -> m.isAlive() && m.getY() >= GameConstants.DANGER_Y)
                .sorted(Comparator.comparingLong(Monster::getSpawnSequence))
                .toList();

        for (Monster m : crossing) {
            m.setAlive(false);
            if (targetManager.getLockedTarget() == m) {
                targetManager.unlockTarget();
            }
            player.takeDamage(1);
            stats.resetStreak();

            if (player.getHp() == 0) {
                enterLossState();
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
                if (target instanceof BossMonster boss) {
                    stats.recordBossWord();
                    boolean defeated = boss.completeWord();
                    if (defeated) {
                        stats.recordBossDeath();
                        beginVictoryAnimation();
                    }
                    return;
                }

                boolean isQuick = target.isQuickKill(elapsedSeconds);
                stats.recordKill(target, isQuick, false);
                if (target.isBonus()) {
                    buffManager.applyBonus(player);
                }

                target.onDeath();
                targetManager.unlockTarget();

                if (isQuick) tryShockwave(target);
            }
        }
    }

    void addMonster(Monster monster) {
        monsters.add(Objects.requireNonNull(monster, "monster"));
    }

    private void tryShockwave(Monster source) {
        if (shockwaveCooldown > 0.0 || source.isBoss() || source.isMinion()) return;

        Monster victim = targetManager.selectMostDangerous(monsters, source);
        if (victim == null) return;

        effects.add(new LaserEffect(
                source.getX(), source.getY(), victim.getX(), victim.getY()));
        stats.recordKill(victim, false, true);
        victim.onDeath();
        if (targetManager.getLockedTarget() == victim) {
            targetManager.unlockTarget();
        }
        shockwaveCooldown = SHOCKWAVE_COOLDOWN;
    }

    public void togglePause() {
        if (state == GameState.PLAYING) state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.PLAYING;
    }

    public void pause() {
        if (state == GameState.PLAYING) state = GameState.PAUSED;
    }

    private void saveAndSnapshot(String outcome) {
        if (resultSaved) return;
        finalRecord = stats.snapshot(playerName, elapsedSeconds, outcome, waveManager.getCurrentWave(), player);
        try {
            historyStore.save(finalRecord);
        } catch (java.io.IOException e) {
            saveErrorMessage = e.getMessage();
            System.err.println("Unable to save game history: " + e.getMessage());
        }
        resultSaved = true;
    }

    private void enterLossState() {
        state = GameState.LOSS;
        targetManager.unlockTarget();
        effects.clear();
        monsters.removeIf(monster -> !monster.isAlive());
        saveAndSnapshot("LOSS");
    }

    private void beginVictoryAnimation() {
        state = GameState.VICTORY_ANIMATION;
        victoryAnimationRemaining = 2.5;
        targetManager.unlockTarget();
        effects.clear();
        monsters.removeIf(Monster::isMinion);
    }

    // Các getter cho UI vẽ snapshot
    public GameState getState() { return state; }
    public Player getPlayer() { return player; }
    public List<Monster> getMonsters() { return List.copyOf(monsters); }
    public List<LaserEffect> getEffects() { return List.copyOf(effects); }
    public GameStats getStats() { return stats; }
    public TargetManager getTargetManager() { return targetManager; }
    public GameRecord getResult() { return finalRecord; }
    public double getElapsedSeconds() { return elapsedSeconds; }
    public double getVictoryAnimationRemaining() { return victoryAnimationRemaining; }
    public int getCurrentWave() { return waveManager.getCurrentWave(); }
    public HistoryStore getHistoryStore() { return historyStore; }
    public String getPlayerName() { return playerName; }
    public String getSaveErrorMessage() { return saveErrorMessage; }
}
