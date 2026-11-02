package coderanger.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import coderanger.model.Monster;
import coderanger.model.NormalMonster;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShockwaveIntegrationTest {
    @Test
    void quickKillTriggersOneShockwaveAndCooldownPreventsChaining() {
        GameManager manager = new GameManager(new HistoryStore(), noSpawnFactory());
        manager.start("Tester");
        NormalMonster firstSource = monster("a", 252, 100, 0, 0);
        NormalMonster firstVictim = monster("victim", 252, 650, 1, 0);
        manager.addMonster(firstSource);
        manager.addMonster(firstVictim);

        manager.handleCharacter('a');

        assertFalse(firstVictim.isAlive());
        assertEquals(2, manager.getStats().getMonstersKilled());
        assertEquals(1, manager.getStats().getQuickKills());
        assertEquals(175, manager.getStats().getScore());

        NormalMonster secondSource = monster("b", 252, 100, 2, 0);
        NormalMonster secondVictim = monster("victim", 252, 650, 3, 0);
        manager.addMonster(secondSource);
        manager.addMonster(secondVictim);
        manager.handleCharacter('b');

        assertTrue(secondVictim.isAlive());
        assertEquals(2, manager.getStats().getQuickKills());

        manager.update(5.0);
        NormalMonster thirdSource = monster("c", 252, 100, 4, 5.0);
        NormalMonster thirdVictim = monster("victim", 252, 650, 5, 5.0);
        manager.addMonster(thirdSource);
        manager.addMonster(thirdVictim);
        manager.handleCharacter('c');

        assertFalse(secondVictim.isAlive());
        assertTrue(thirdVictim.isAlive());
        assertEquals(3, manager.getStats().getQuickKills());
        assertEquals(5, manager.getStats().getMonstersKilled());
    }

    @Test
    void targetManagerLocksTheMonsterWithLeastTimeToDangerLine() {
        TargetManager targetManager = new TargetManager();
        Monster slower = new NormalMonster(252, 700, "a", 0, 1, 10, false);
        Monster faster = new NormalMonster(252, 650, "a", 0, 2, 100, false);

        assertEquals(faster, targetManager.findAndLockTarget('a', List.of(slower, faster)));
    }

    private static NormalMonster monster(String word, double x, double y, long sequence, double spawnedAt) {
        return new NormalMonster(x, y, word, spawnedAt, sequence, 0, false);
    }

    private static MonsterFactory noSpawnFactory() {
        return new MonsterFactory() {
            @Override
            public Monster createNormal(int level, double now, boolean minion) {
                return null;
            }

            @Override
            public Monster createBonus(int level, double now) {
                return null;
            }
        };
    }
}
