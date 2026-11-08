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
    private boolean bossSpawned;

    public WaveManager(Difficulty difficulty, MonsterFactory monsterFactory) {
        this.difficulty = difficulty;
        this.monsterFactory = monsterFactory;
        this.normalElapsed = 0.0;
        this.bonusElapsed = 0.0;
        this.currentWave = 1;
        this.bossSpawned = false;
    }

    public List<Monster> update(double dt, double now, List<Monster> currentMonsters) {
        List<Monster> newSpawns = new ArrayList<>();

        if (now >= GameConstants.BOSS_START) {
            this.currentWave = 7;
            return newSpawns;
        }

        this.currentWave = (int) Math.floor(now / 40.0) + 1;
        int currentLevel = difficulty.level(now);

        normalElapsed += dt;
        double nInterval = difficulty.normalInterval(now);
        int maxNormals = difficulty.maxNormals(now);

        long activeNormals = currentMonsters.stream()
                .filter(m -> m.isAlive() && (m instanceof NormalMonster))
                .count();

        if (activeNormals >= maxNormals) {
            if (normalElapsed > nInterval) {
                normalElapsed = nInterval;
            }
        } else if (normalElapsed >= nInterval) {
            normalElapsed -= nInterval;
            Monster normal = monsterFactory.createNormal(currentLevel, now, false);
            if (normal != null) {
                newSpawns.add(normal);
            }
        }

        boolean hasActiveBonus = currentMonsters.stream()
                .anyMatch(m -> m.isAlive() && (m instanceof BonusMonster));

        if (!hasActiveBonus) {
            bonusElapsed += dt;
            double bInterval = difficulty.bonusInterval(now);
            if (bonusElapsed >= bInterval) {
                bonusElapsed = 0.0; 
                Monster bonus = monsterFactory.createBonus(currentLevel, now);
                if (bonus != null) {
                    newSpawns.add(bonus);
                }
            }
        } else {
            bonusElapsed = 0.0;
        }

        return newSpawns;
    }

    public int getCurrentWave() { return currentWave; }
    public boolean isBossDue(double now) {
        return now >= GameConstants.BOSS_START && !bossSpawned;
    }
    public void markBossSpawned() { this.bossSpawned = true; }

    public void reset() {
        normalElapsed = 0.0;
        bonusElapsed = 0.0;
        currentWave = 1;
        bossSpawned = false;
    }
}