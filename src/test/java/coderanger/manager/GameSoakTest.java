package coderanger.manager;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class GameSoakTest {
    @Test
    void fifteenMinuteHeadlessSimulationKeepsCollectionsBounded() {
        GameManager manager = new GameManager(new HistoryStore() {
            @Override
            public void save(GameRecord record) {
            }
        }, new MonsterFactory(2026L));
        Random inputRandom = new Random(2026L);
        manager.start("Soak");

        int frames = (int) (15 * 60 / 0.016);
        for (int frame = 0; frame < frames; frame++) {
            if (manager.getState() == GameManager.GameState.LOSS
                    || manager.getState() == GameManager.GameState.WIN) {
                manager.start("Soak");
            }

            manager.update(0.016);
            if (inputRandom.nextInt(40) == 0) {
                manager.handleCharacter((char) inputRandom.nextInt(128));
            }

            assertTrue(manager.getMonsters().size() < 20);
            assertTrue(manager.getEffects().size() < 20);
        }
    }
}
