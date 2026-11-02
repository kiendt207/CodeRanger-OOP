package coderanger.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import coderanger.model.BonusMonster;
import org.junit.jupiter.api.Test;

class BonusBuffIntegrationTest {
    @Test
    void bonusKillAddsShieldBeforeHealing() {
        GameManager manager = new GameManager(new HistoryStore(), new MonsterFactory());
        manager.start("Tester");
        BonusMonster bonus = new BonusMonster(249, 100, "b", 0, 1, 0);
        manager.addMonster(bonus);

        manager.handleCharacter('b');

        assertEquals(2, manager.getPlayer().getShieldLayers());
        assertEquals(5, manager.getPlayer().getHp());
        assertEquals(1, manager.getStats().getBonusKills());
    }

    @Test
    void bonusKillHealsWhenShieldCapacityIsFull() {
        GameManager manager = new GameManager(new HistoryStore(), new MonsterFactory());
        manager.start("Tester");
        manager.getPlayer().takeDamage(3);
        for (int i = 0; i < 3; i++) {
            manager.getPlayer().addShield();
        }
        assertEquals(3, manager.getPlayer().getShieldLayers());
        assertEquals(3, manager.getPlayer().getHp());

        manager.addMonster(new BonusMonster(249, 100, "b", 0, 1, 0));
        manager.handleCharacter('b');

        assertEquals(3, manager.getPlayer().getShieldLayers());
        assertEquals(4, manager.getPlayer().getHp());
    }

    @Test
    void bonusCrossingDangerLineConsumesShieldBeforeHp() {
        GameManager manager = new GameManager(new HistoryStore(), new MonsterFactory());
        manager.start("Tester");
        BonusMonster bonus = new BonusMonster(249, 759, "b", 0, 1, 100);
        manager.addMonster(bonus);

        manager.update(0.02);

        assertEquals(0, manager.getPlayer().getShieldLayers());
        assertEquals(5, manager.getPlayer().getHp());
        assertEquals(0, manager.getStats().getStreak());
    }
}
