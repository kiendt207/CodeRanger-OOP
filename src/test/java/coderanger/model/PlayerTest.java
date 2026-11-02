package coderanger.model;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player createPlayer() {
        return new Player();
    }

    // ================= Constructor =================

    @Test
    void constructorShouldInitializeValues() {
        Player player = createPlayer();

        assertEquals(5, player.getHp());
        assertEquals(1, player.getShieldLayers());
        assertTrue(player.isAlive());

        assertEquals(238, player.getX());
        assertEquals(792, player.getY());
        assertEquals(64, player.getWidth());
        assertEquals(56, player.getHeight());
    }

    // ================= Take Damage =================

    @Test
    void takeDamageShouldConsumeShieldFirst() {
        Player player = createPlayer();

        int hpLost = player.takeDamage(1);

        assertEquals(0, hpLost);
        assertEquals(5, player.getHp());
        assertEquals(0, player.getShieldLayers());
    }

    @Test
    void takeDamageShouldReduceHpAfterShieldIsGone() {
        Player player = createPlayer();

        player.takeDamage(1);
        int hpLost = player.takeDamage(1);

        assertEquals(1, hpLost);
        assertEquals(4, player.getHp());
        assertEquals(0, player.getShieldLayers());
    }

    @Test
    void takeDamageShouldHandleMultipleDamagePoints() {
        Player player = createPlayer();

        int hpLost = player.takeDamage(3);

        assertEquals(2, hpLost);
        assertEquals(3, player.getHp());
        assertEquals(0, player.getShieldLayers());
    }

    @Test
    void takeDamageShouldNotReduceHpBelowZero() {
        Player player = createPlayer();

        int hpLost = player.takeDamage(100);

        assertEquals(5, hpLost);
        assertEquals(0, player.getHp());
    }

    @Test
    void playerShouldDieWhenHpReachesZero() {
        Player player = createPlayer();

        player.takeDamage(6);

        assertEquals(0, player.getHp());
        assertFalse(player.isAlive());
    }

    @Test
    void takeDamageShouldRejectNegativeDamage() {
        Player player = createPlayer();

        assertThrows(IllegalArgumentException.class,
                () -> player.takeDamage(-1));
    }

    @Test
    void takeZeroDamageShouldNotChangePlayer() {
        Player player = createPlayer();

        int hpLost = player.takeDamage(0);

        assertEquals(0, hpLost);
        assertEquals(5, player.getHp());
        assertEquals(1, player.getShieldLayers());
        assertTrue(player.isAlive());
    }

    @Test
    void takeDamageShouldNotReduceHpAfterDeath() {
        Player player = createPlayer();

        player.takeDamage(6);

        int hpLost = player.takeDamage(3);

        assertEquals(0, hpLost);
        assertEquals(0, player.getHp());
        assertFalse(player.isAlive());
    }

    // ================= Shield =================

    @Test
    void addShieldShouldIncreaseShieldLayers() {
        Player player = createPlayer();

        assertTrue(player.addShield());
        assertEquals(2, player.getShieldLayers());
    }

    @Test
    void addShieldShouldAllowMaximumThreeLayers() {
        Player player = createPlayer();

        player.addShield();
        player.addShield();

        assertEquals(3, player.getShieldLayers());
    }

    @Test
    void addShieldShouldReturnFalseAtMaximum() {
        Player player = createPlayer();

        player.addShield();
        player.addShield();

        assertFalse(player.addShield());
        assertEquals(3, player.getShieldLayers());
    }

    @Test
    void addShieldShouldWorkAfterShieldIsConsumed() {
        Player player = createPlayer();

        player.takeDamage(1);

        assertEquals(0, player.getShieldLayers());

        assertTrue(player.addShield());
        assertEquals(1, player.getShieldLayers());
    }

    // ================= Heal =================

    @Test
    void healShouldIncreaseHp() {
    Player player = createPlayer();

    player.takeDamage(2); // Shield bị trừ 1, HP còn 4

    assertTrue(player.heal(1));
    assertEquals(5, player.getHp());
}

    @Test
    void healShouldReturnFalseWhenHpIsFull() {
        Player player = createPlayer();

        assertFalse(player.heal(1));
        assertEquals(5, player.getHp());
    }

    @Test
    void healShouldNotExceedMaximumHp() {
        Player player = createPlayer();

        player.takeDamage(4); // HP còn 2
        assertTrue(player.heal(1));
        assertEquals(3, player.getHp());

        assertTrue(player.heal(100));
        assertEquals(5, player.getHp());

        assertFalse(player.heal(1));
        assertEquals(5, player.getHp());
    }

    @Test
    void healShouldReturnFalseWhenPlayerIsDead() {
        Player player = createPlayer();

        player.takeDamage(6); // HP về 0, Player chết

        assertFalse(player.isAlive());
        assertFalse(player.heal(1));
        assertEquals(0, player.getHp());
        assertFalse(player.isAlive());
    }

    // ================= Update =================

    @Test
    void updateShouldNotChangePlayerStats() {
        Player player = createPlayer();

        player.update(1.0);

        assertEquals(5, player.getHp());
        assertEquals(1, player.getShieldLayers());
    }

    // ================= Rendering =================

    @Test
    void renderShouldNotThrowException() {
        Player player = createPlayer();

        BufferedImage image = new BufferedImage(
                600, 900, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        try {
            assertDoesNotThrow(() -> player.render(g));
        } finally {
            g.dispose();
        }
    }

    @Test
    void renderShouldNotThrowWhenPlayerIsDead() {
        Player player = createPlayer();

        player.takeDamage(6);

        BufferedImage image = new BufferedImage(
                600, 900, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        try {
            assertDoesNotThrow(() -> player.render(g));
        } finally {
            g.dispose();
        }
    }
}