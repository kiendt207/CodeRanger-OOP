package coderanger.manager;

import coderanger.model.BonusMonster;
import coderanger.model.Monster;
import coderanger.model.NormalMonster;
import coderanger.util.GameConstants;
import java.util.ArrayList;
import java.util.List;

public class WaveManager {
    private final Difficulty difficulty;
    private final MonsterFactory monsterFactory;
    private double normalElapsed;
    private double bonusElapsed;
    private int currentWave;
    private int lastTier;
    private boolean bossSpawned;

    public WaveManager(Difficulty difficulty, MonsterFactory monsterFactory) {
        this.difficulty = difficulty;
        this.monsterFactory = monsterFactory;
        reset();
    }

    public List<Monster> update(double dt, double now, List<Monster> currentMonsters) {
        return update(dt, now, currentMonsters, 0);
    }

    public List<Monster> update(double dt, double now, List<Monster> currentMonsters, int streak) {
        List<Monster> newSpawns = new ArrayList<>();

        if (now >= GameConstants.BOSS_START) {
            this.currentWave = GameConstants.BOSS_WAVE;
            return newSpawns;
        }

        double clampedNow = Math.max(0.0, now);
        this.currentWave = Math.min(
                GameConstants.NORMAL_WAVES,
                (int) Math.floor(clampedNow / GameConstants.WAVE_DURATION) + 1
        );
        int currentTier = difficulty.level(clampedNow);
        int wordLvl = difficulty.wordLevel(currentTier);

        double nInterval = difficulty.effectiveNormalInterval(clampedNow, streak);
        int maxNormals = difficulty.effectiveMaxNormals(clampedNow, streak);

        if (currentTier != lastTier) {
            lastTier = currentTier;
            if (normalElapsed > nInterval) {
                normalElapsed = nInterval;
            }
        }

        normalElapsed += dt;

        long activeNormals = currentMonsters.stream()
                .filter(m -> m.isAlive() && (m instanceof NormalMonster))
                .count();

        if (activeNormals >= maxNormals) {
            if (normalElapsed > nInterval) {
                normalElapsed = nInterval;
            }
        } else if (normalElapsed >= nInterval) {
            normalElapsed -= nInterval;
            Monster normal = monsterFactory.createNormal(wordLvl, clampedNow, false, streak);
            if (normal != null) {
                newSpawns.add(normal);
            }
        }

        boolean hasActiveBonus = currentMonsters.stream()
                .anyMatch(m -> m.isAlive() && (m instanceof BonusMonster));

        if (!hasActiveBonus) {
            bonusElapsed += dt;
            double bInterval = difficulty.bonusInterval(clampedNow);
            if (bonusElapsed >= bInterval) {
                bonusElapsed = 0.0;
                Monster bonus = monsterFactory.createBonus(wordLvl, clampedNow);
                if (bonus != null) {
                    newSpawns.add(bonus);
                }
            }
        } else {
            bonusElapsed = 0.0;
        }

        return newSpawns;
    }

    public void primeInitialSpawn() {
        double initialInterval = difficulty.normalInterval(0.0);
        this.normalElapsed = Math.max(0.0, initialInterval - 0.5);
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public boolean isBossDue(double now) {
        return now >= GameConstants.BOSS_START && !bossSpawned;
    }

    public void markBossSpawned() {
        this.bossSpawned = true;
    }

    public void reset() {
        normalElapsed = 0.0;
        bonusElapsed = 0.0;
        currentWave = 1;
        lastTier = 0;
        bossSpawned = false;
    }
}
