package coderanger.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import coderanger.model.BossMonster;
import coderanger.model.Monster;
import coderanger.model.NormalMonster;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameLifecycleTest {
    @Test
    void ninthBossWordFreezesClockThenSavesWinExactlyOnce() {
        CountingHistoryStore history = new CountingHistoryStore();
        GameManager manager = new GameManager(history, noSpawnFactory());
        manager.start("Tester");
        BossMonster boss = createBoss();
        manager.addMonster(boss);
        manager.addMonster(new NormalMonster(252, 100, "minion", 0, 2, 0, true));

        for (int i = 0; i < 9; i++) {
            for (char c : boss.getCurrentWord().toCharArray()) {
                manager.handleCharacter(c);
            }
        }

        assertEquals(GameManager.GameState.VICTORY_ANIMATION, manager.getState());
        assertEquals(0, manager.getMonsters().stream().filter(Monster::isMinion).count());
        double frozenElapsed = manager.getElapsedSeconds();
        manager.update(2.49);
        assertEquals(GameManager.GameState.VICTORY_ANIMATION, manager.getState());
        manager.update(0.01);
        manager.update(1.0);

        assertEquals(GameManager.GameState.WIN, manager.getState());
        assertEquals(frozenElapsed, manager.getElapsedSeconds());
        assertEquals(1, history.saveCount);
        assertEquals("WIN", manager.getResult().outcome());
    }

    @Test
    void lossSavesOnceAndRestartResetsPlayerEntitiesAndStats() {
        CountingHistoryStore history = new CountingHistoryStore();
        GameManager manager = new GameManager(history, noSpawnFactory());
        manager.start("Tester");
        manager.getPlayer().takeDamage(5);
        manager.addMonster(new NormalMonster(252, 760, "danger", 0, 1, 0, false));

        manager.update(0.0);
        manager.update(1.0);

        assertEquals(GameManager.GameState.LOSS, manager.getState());
        assertEquals(1, history.saveCount);
        manager.start("Replay");
        assertEquals(5, manager.getPlayer().getHp());
        assertEquals(1, manager.getPlayer().getShieldLayers());
        assertEquals(0, manager.getStats().getScore());
        assertEquals(0, manager.getMonsters().size());
        assertEquals(0, manager.getEffects().size());
        assertNull(manager.getResult());
        assertFalse(manager.getMonsters().stream().anyMatch(Monster::isAlive));
    }

    @Test
    void simultaneousCrossingsUseSpawnOrderAndStopImmediatelyOnDeath() {
        GameManager manager = new GameManager(new CountingHistoryStore(), noSpawnFactory());
        manager.start("Tester");
        manager.getPlayer().takeDamage(5);
        NormalMonster first = new NormalMonster(252, 760, "first", 0, 1, 0, false);
        NormalMonster second = new NormalMonster(252, 760, "second", 0, 2, 0, false);
        manager.addMonster(second);
        manager.addMonster(first);

        manager.update(0.0);

        assertEquals(GameManager.GameState.LOSS, manager.getState());
        assertFalse(first.isAlive());
        assertEquals(1, manager.getMonsters().size());
        assertEquals(second, manager.getMonsters().get(0));
    }

    @Test
    void historyStoreWritesEightTabSeparatedFields(@TempDir Path tempDir) throws IOException {
        Path historyPath = tempDir.resolve("history.tsv");
        HistoryStore store = new HistoryStore(historyPath);
        store.save(new GameRecord(
                "2026-11-02T00:00:00Z", "A\tB", "WIN", 10, 2.5, 5, 1, 83.3,
                2, 1, 0, 9, 24.0, 7, 5, 1));

        var lines = Files.readAllLines(historyPath, StandardCharsets.UTF_8);
        assertEquals("timestamp\tname\toutcome\tscore\tseconds\tcorrect\twrong\taccuracy", lines.get(0));
        String[] fields = lines.get(1).split("\t");
        assertEquals(8, fields.length);
        assertEquals("A B", fields[1]);

        store.save(new GameRecord(
                "2026-11-02T00:01:00Z", "Second", "LOSS", 20, 2.5, 5, 1, 83.3,
                2, 1, 0, 0, 24.0, 1, 0, 0));
        Files.writeString(historyPath, "corrupt row" + System.lineSeparator(),
                StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        assertEquals(1, store.countCorruptLines());
        assertEquals("Second", store.top10().get(0).name());
        assertEquals("Second", store.readRecent().get(0).name());
    }

    @Test
    void historyStoreRetainsOnlyTwentyMostRecentRecords(@TempDir Path tempDir) throws IOException {
        Path historyPath = tempDir.resolve("history.tsv");
        HistoryStore store = new HistoryStore(historyPath);

        for (int i = 0; i < 25; i++) {
            store.save(new GameRecord(
                    Instant.parse("2026-11-02T00:00:00Z").plusSeconds(i).toString(),
                    "Game " + i, "WIN", i, 1.0, 1, 0, 100.0,
                    0, 0, 0, 0, 0.0, 1, 5, 1));
        }

        assertEquals(21, Files.readAllLines(historyPath, StandardCharsets.UTF_8).size());
        assertEquals(20, store.readRecent().size());
        assertEquals("Game 24", store.readRecent().get(0).name());
        assertEquals("Game 5", store.readRecent().get(19).name());
        assertEquals(10, store.top10().size());
    }

    @Test
    void historyStoreReadsLegacySixteenFieldRecords(@TempDir Path tempDir) throws IOException {
        Path historyPath = tempDir.resolve("history.tsv");
        Files.writeString(historyPath,
                "2026-11-02T00:00:00Z\tLegacy\tWIN\t10\t2.5\t5\t1\t83.3"
                        + "\t2\t1\t0\t9\t24.0\t7\t5\t1" + System.lineSeparator(),
                StandardCharsets.UTF_8);

        GameRecord record = new HistoryStore(historyPath).readRecent().get(0);
        assertEquals("Legacy", record.name());
        assertEquals(2, record.monstersKilled());
        assertEquals(9, record.bossWords());
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

    private static BossMonster createBoss() {
        return new BossMonster(java.util.List.of(
                java.util.List.of("aster", "comet", "planet"),
                java.util.List.of("voyager", "nebulae", "orbitals"),
                java.util.List.of("supernova", "blackholes", "starcluster")), 0, 1);
    }

    private static final class CountingHistoryStore extends HistoryStore {
        private int saveCount;

        @Override
        public void save(GameRecord record) {
            saveCount++;
        }
    }
}
