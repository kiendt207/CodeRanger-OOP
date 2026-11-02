package coderanger.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import coderanger.model.Monster;
import coderanger.model.NormalMonster;
import coderanger.model.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoreManagerTest {
    @Test
    void difficultyUsesSixtySecondLevelsAndCapsAtLevelFour() {
        Difficulty difficulty = new Difficulty();

        assertEquals(0, difficulty.level(-1));
        assertEquals(0, difficulty.level(59.99));
        assertEquals(1, difficulty.level(60));
        assertEquals(4, difficulty.level(240));
        assertEquals(Double.MAX_VALUE, difficulty.normalInterval(240));
    }

    @Test
    void bonusBuffAddsShieldBeforeHealingAndHealingCanRestoreMultipleHp() {
        Player player = new Player();
        player.takeDamage(5);
        BuffManager buffs = new BuffManager();

        buffs.applyBonus(player);
        assertEquals(1, player.getShieldLayers());
        assertEquals(1, player.getHp());

        player.addShield();
        player.addShield();
        player.addShield();
        buffs.applyBonus(player);
        assertEquals(3, player.getShieldLayers());
        assertEquals(2, player.getHp());
        assertTrue(player.heal(99));
        assertEquals(5, player.getHp());
    }

    @Test
    void waveSpawnerClampsBacklogAndSpawnsAtMostOneNormalPerUpdate() {
        CountingFactory factory = new CountingFactory();
        WaveManager waves = new WaveManager(new Difficulty(), factory);

        waves.update(3.0, 3.0, List.of());
        assertEquals(1, factory.normalCount);

        List<Monster> capped = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            capped.add(new NormalMonster(100, 100, "word", 0, i, 0, false));
        }
        waves.update(30.0, 30.0, capped);
        assertEquals(1, factory.normalCount);

        waves.update(0.0, 30.0, List.of());
        assertEquals(2, factory.normalCount);
    }

    @Test
    void seededFactoriesProduceTheSameInitialMonster() {
        Monster first = new MonsterFactory().createNormal(0, 0, false);
        Monster second = new MonsterFactory().createNormal(0, 0, false);

        assertEquals(first.getFullWord(), second.getFullWord());
        assertEquals(first.getX(), second.getX());
        assertEquals(first.getSpawnSequence(), second.getSpawnSequence());
    }

    @Test
    void bossFactoryUsesWordsThatMatchEachPhaseLength() {
        Monster boss = new MonsterFactory(2026L).createBoss(240.0);
        assertTrue(boss.isBoss());
        assertTrue(boss.getRemainingWord().length() >= 5);
        assertTrue(boss.getRemainingWord().length() <= 6);
    }

    @Test
    void gameStatsApplyStreakMultiplierAndResetAfterMistypedKey() {
        GameStats stats = new GameStats();
        Monster normal = new NormalMonster(100, 100, "word", 0, 1, 0, false);
        for (int i = 0; i < 5; i++) {
            stats.recordKey(true);
        }
        stats.recordKill(normal, false, false);
        assertEquals(110, stats.getScore());

        stats.recordKey(false);
        stats.recordKill(normal, false, false);
        assertEquals(210, stats.getScore());
        assertEquals(0, stats.getStreak());
    }

    private static final class CountingFactory extends MonsterFactory {
        private int normalCount;

        @Override
        public Monster createNormal(int level, double now, boolean minion) {
            normalCount++;
            return null;
        }

        @Override
        public Monster createBonus(int level, double now) {
            return null;
        }
    }
}
