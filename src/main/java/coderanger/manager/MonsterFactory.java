package coderanger.manager;

import coderanger.model.BonusMonster;
import coderanger.model.BossMonster;
import coderanger.model.Monster;
import coderanger.model.NormalMonster;
import coderanger.util.GameConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MonsterFactory {
    private static final double MIN_SPAWN_X = 65.0;
    private static final double MAX_SPAWN_X = GameConstants.WIDTH - 65.0;
    private static final double MIN_SPAWN_SEPARATION = 80.0;
    private static final double[] FALLBACK_POINTS = {75.0, 170.0, 270.0, 370.0, 465.0};

    private final WordProvider wordProvider;
    private final Random random;
    private final Long resetSeed;
    private final Difficulty difficulty = new Difficulty();
    private long sequence;
    private boolean hasLastSpawnX;
    private double lastSpawnX;
    private int pendingStreak;

    public MonsterFactory() {
        this(new WordProvider(2026L), new Random(2026L), 2026L);
    }

    public MonsterFactory(long seed) {
        this(new WordProvider(seed), new Random(seed), seed);
    }

    public MonsterFactory(WordProvider wordProvider, Random random) {
        this.wordProvider = Objects.requireNonNull(wordProvider, "wordProvider");
        this.random = Objects.requireNonNull(random, "random");
        this.resetSeed = null;
    }

    private MonsterFactory(WordProvider wordProvider, Random random, long seed) {
        this.wordProvider = Objects.requireNonNull(wordProvider, "wordProvider");
        this.random = Objects.requireNonNull(random, "random");
        this.resetSeed = seed;
    }

    public Monster createNormal(int level, double now, boolean minion) {
        double centerX = selectSpawnCenterX();
        double centerY = minion ? 250.0 : -30.0;
        double speed = minion
                ? difficulty.normalSpeed(now)
                : difficulty.effectiveNormalSpeed(now, pendingStreak);
        return new NormalMonster(
                centerX - NormalMonster.SIZE / 2.0,
                centerY - NormalMonster.SIZE / 2.0,
                wordProvider.nextNormal(level),
                now,
                sequence++,
                speed,
                minion
        );
    }

    public Monster createNormal(int level, double now, boolean minion, int streak) {
        int previous = this.pendingStreak;
        this.pendingStreak = Math.max(0, streak);
        try {
            return createNormal(level, now, minion);
        } finally {
            this.pendingStreak = previous;
        }
    }

    public Monster createBonus(int level, double now) {
        double centerX = selectSpawnCenterX();
        double centerY = -30.0;
        double speed = difficulty.bonusSpeed(now);
        return new BonusMonster(
                centerX - BonusMonster.SIZE / 2.0,
                centerY - BonusMonster.SIZE / 2.0,
                wordProvider.nextBonus(),
                now,
                sequence++,
                speed
        );
    }

    public BossMonster createBoss(double now) {
        List<List<String>> wordsByPhase = new ArrayList<>();
        for (int phase = 1; phase <= 3; phase++) {
            wordsByPhase.add(wordProvider.phaseWords(phase));
        }
        return new BossMonster(wordsByPhase, now, sequence++);
    }

    private double selectSpawnCenterX() {
        if (!hasLastSpawnX) {
            double cx = MIN_SPAWN_X + random.nextDouble() * (MAX_SPAWN_X - MIN_SPAWN_X);
            lastSpawnX = cx;
            hasLastSpawnX = true;
            return cx;
        }

        for (int attempt = 0; attempt < 12; attempt++) {
            double candidate = MIN_SPAWN_X + random.nextDouble() * (MAX_SPAWN_X - MIN_SPAWN_X);
            if (Math.abs(candidate - lastSpawnX) >= MIN_SPAWN_SEPARATION) {
                lastSpawnX = candidate;
                return candidate;
            }
        }

        double bestPoint = FALLBACK_POINTS[0];
        double bestDist = Math.abs(bestPoint - lastSpawnX);
        for (int i = 1; i < FALLBACK_POINTS.length; i++) {
            double dist = Math.abs(FALLBACK_POINTS[i] - lastSpawnX);
            if (dist > bestDist) {
                bestDist = dist;
                bestPoint = FALLBACK_POINTS[i];
            }
        }
        lastSpawnX = bestPoint;
        return bestPoint;
    }

    public void reset() {
        sequence = 0;
        hasLastSpawnX = false;
        lastSpawnX = 0.0;
        wordProvider.reset();
        if (resetSeed != null) {
            random.setSeed(resetSeed);
        }
    }
}
