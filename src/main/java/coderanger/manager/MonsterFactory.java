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
    private final WordProvider wordProvider;
    private final Random random;
    private final Long resetSeed;
    private final Difficulty difficulty = new Difficulty();
    private long sequence;

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
        double x = 65.0 + random.nextDouble() * (GameConstants.WIDTH - 130.0);
        double y = minion ? 250.0 : -30.0;
        double speed = difficulty.normalSpeed(now);
        return new NormalMonster(x, y, wordProvider.nextNormal(level), now,
                sequence++, speed, minion);
    }

    public Monster createBonus(int level, double now) {
        double x = 65.0 + random.nextDouble() * (GameConstants.WIDTH - 130.0);
        double speed = difficulty.bonusSpeed(now);
        return new BonusMonster(x, -30.0, wordProvider.nextBonus(), now, sequence++, speed);
    }

    public BossMonster createBoss(double now) {
        List<List<String>> wordsByPhase = new ArrayList<>();
        for (int phase = 1; phase <= 3; phase++) {
            wordsByPhase.add(wordProvider.phaseWords(phase));
        }
        return new BossMonster(wordsByPhase, now, sequence++);
    }

    public void reset() {
        sequence = 0;
        wordProvider.reset();
        if (resetSeed != null) {
            random.setSeed(resetSeed);
        }
    }
}
