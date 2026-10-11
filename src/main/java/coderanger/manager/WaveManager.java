package coderanger.manager;

import coderanger.model.Monster;
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
        if (now >= 240.0) {
            this.currentWave = 7;
            return newSpawns; 
        }

        this.currentWave = (int) Math.floor(now / 40.0) + 1;

        normalElapsed += dt;
        double interval = difficulty.normalInterval(now);
        int maxAllowed = difficulty.maxNormals(now);

        long activeNormals = currentMonsters.stream()
                .filter(m -> m.isAlive())
                .count();

        if (activeNormals >= maxAllowed) {
            if (normalElapsed > interval) normalElapsed = interval;
        } else if (normalElapsed >= interval) {
            normalElapsed -= interval;
            newSpawns.add(monsterFactory.createNormal(difficulty.level(now), now, false));
        }

        return newSpawns;
    }

    public int getCurrentWave() { return currentWave; }
    public boolean isBossDue(double now) { return now >= 240.0 && !bossSpawned; }
    public void markBossSpawned() { this.bossSpawned = true; }
}