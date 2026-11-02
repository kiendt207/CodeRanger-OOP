package coderanger.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import coderanger.model.BonusMonster;
import coderanger.model.BossMonster;
import coderanger.model.Monster;
import coderanger.model.NormalMonster;
import java.util.List;
import org.junit.jupiter.api.Test;

class BossOrchestrationTest {
    @Test
    void bossWavePurgesOtherMonstersWithoutAwardingKills() {
        GameManager manager = new GameManager(new HistoryStore(), bossFactory());
        manager.start("Tester");
        manager.update(239.9);
        manager.addMonster(new NormalMonster(100, 100, "normal", 239.9, 1, 0, false));
        manager.addMonster(new BonusMonster(100, 100, "star", 239.9, 2, 0));

        manager.update(0.1);

        assertEquals(7, manager.getCurrentWave());
        assertEquals(1, manager.getMonsters().size());
        assertInstanceOf(BossMonster.class, manager.getMonsters().get(0));
        assertEquals(0, manager.getStats().getMonstersKilled());
    }

    @Test
    void bossWordsAdvanceThroughThreePhasesAndMinionsRespectTheCap() {
        GameManager manager = new GameManager(new HistoryStore(), bossFactory());
        manager.start("Tester");
        BossMonster boss = createBoss();
        manager.addMonster(boss);

        completeCurrentWord(manager, boss);
        completeCurrentWord(manager, boss);
        completeCurrentWord(manager, boss);
        assertEquals(2, boss.getPhase());
        assertEquals(16.0, boss.getCountdown());

        manager.update(10.0);
        manager.update(10.0);
        manager.update(10.0);
        manager.update(10.0);
        assertEquals(3, manager.getMonsters().stream().filter(Monster::isMinion).count());

        completeCurrentWord(manager, boss);
        completeCurrentWord(manager, boss);
        completeCurrentWord(manager, boss);
        assertEquals(3, boss.getPhase());
        assertEquals(18.0, boss.getCountdown());
    }

    @Test
    void timeoutResetsWordAndInputCompletingBeforeTimerUpdateWins() {
        GameManager manager = new GameManager(new HistoryStore(), bossFactory());
        manager.start("Tester");
        BossMonster boss = createBoss();
        manager.addMonster(boss);

        manager.update(13.999);
        String completedWord = boss.getCurrentWord();
        for (char c : completedWord.toCharArray()) {
            manager.handleCharacter(c);
        }
        manager.update(0.001);
        assertEquals(8, boss.getHp());
        assertEquals(13.999, boss.getCountdown(), 0.0001);
        assertEquals(5, manager.getPlayer().getHp());

        double countdownBeforeTimeout = boss.getCountdown();
        manager.update(countdownBeforeTimeout);
        assertEquals(0, manager.getPlayer().getShieldLayers());
        assertFalse(boss.consumeSkillPending());
        assertEquals(14.0, boss.getCountdown());
    }

    @Test
    void normalTargetsWinNewLocksButCannotStealAnExistingBossLock() {
        BossMonster boss = createBoss();
        NormalMonster normal = new NormalMonster(252, 100, "aster", 0, 2, 0, false);
        TargetManager targets = new TargetManager();

        assertSame(normal, targets.findAndLockTarget('a', List.of(boss, normal)));

        targets.unlockTarget();
        assertSame(boss, targets.findAndLockTarget('a', List.of(boss)));
        NormalMonster minion = new NormalMonster(252, 100, "aster", 0, 3, 0, true);
        assertSame(boss, targets.findAndLockTarget('m', List.of(boss, minion)));
    }

    private static void completeCurrentWord(GameManager manager, BossMonster boss) {
        for (char c : boss.getCurrentWord().toCharArray()) {
            manager.handleCharacter(c);
        }
    }

    private static BossMonster createBoss() {
        return new BossMonster(List.of(
                List.of("aster", "comet", "planet"),
                List.of("voyager", "nebulae", "orbitals"),
                List.of("supernova", "blackholes", "starcluster")), 0, 1);
    }

    private static MonsterFactory bossFactory() {
        return new MonsterFactory() {
            private long sequence = 10;

            @Override
            public Monster createNormal(int level, double now, boolean minion) {
                if (!minion) return null;
                return new NormalMonster(252, 110, "minion", now, sequence++, 0, true);
            }

            @Override
            public Monster createBonus(int level, double now) {
                return null;
            }

            @Override
            public BossMonster createBoss(double now) {
                return BossOrchestrationTest.createBoss();
            }
        };
    }
}
