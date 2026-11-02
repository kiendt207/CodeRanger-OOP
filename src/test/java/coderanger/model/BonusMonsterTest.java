package coderanger.model;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class BonusMonsterTest {

    private BonusMonster createMonster(
            double x, double y, double speed) {
        return new BonusMonster(x, y, "bonus", 0, 1, speed);
    }

    // ================= Constructor =================

    @Test
    void constructorShouldInitializeValues() {
        BonusMonster monster =
                new BonusMonster(100, 200, "bonus", 5, 10, 50);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());

        assertEquals(42, monster.getWidth());
        assertEquals(42, monster.getHeight());

        assertEquals("bonus", monster.getFullWord());
        assertEquals(5, monster.getSpawnElapsed());
        assertEquals(10, monster.getSpawnSequence());

        assertEquals(150, monster.getRewardScore());
        assertEquals(50, monster.getSpeed());

        assertTrue(monster.isAlive());
        assertFalse(monster.isCompleted());
    }

    @Test
    void constructorShouldRejectNegativeSpeed() {
        assertThrows(IllegalArgumentException.class,
                () -> createMonster(0, 0, -1));
    }

    @Test
    void constructorShouldRejectInfiniteSpeed() {
        assertThrows(IllegalArgumentException.class,
                () -> createMonster(0, 0, Double.POSITIVE_INFINITY));
    }

    @Test
    void constructorShouldRejectNaNSpeed() {
        assertThrows(IllegalArgumentException.class,
                () -> createMonster(0, 0, Double.NaN));
    }

    @Test
    void constructorShouldAcceptZeroSpeed() {
        BonusMonster monster = createMonster(0, 0, 0);

        assertEquals(0, monster.getSpeed());
    }

    // ================= Movement =================

    @Test
    void bonusMonsterShouldMoveTowardPlayer() {
        BonusMonster monster = createMonster(0, 0, 100);

        double oldX = monster.getX();
        double oldY = monster.getY();

        monster.update(1.0);

        assertTrue(monster.getX() > oldX);
        assertTrue(monster.getY() > oldY);
    }

    @Test
    void bonusMonsterShouldMoveCorrectDistance() {
        BonusMonster monster = createMonster(0, 0, 100);

        double oldX = monster.getX();
        double oldY = monster.getY();

        monster.update(1.0);

        double distance = Math.hypot(
                monster.getX() - oldX,
                monster.getY() - oldY
        );

        assertEquals(100.0, distance, 1e-6);
    }

    @Test
    void bonusMonsterShouldStopAtPlayerPosition() {
        // Player center: (270, 820)
        // BonusMonster size: 42x42
        // Monster top-left: (249, 799)
        BonusMonster monster =
                createMonster(249, 799, 100);

        monster.update(1.0);

        assertEquals(249, monster.getX(), 1e-6);
        assertEquals(799, monster.getY(), 1e-6);
    }

    @Test
    void bonusMonsterShouldNotOvershootPlayer() {
        BonusMonster monster = createMonster(0, 0, 10000);

        monster.update(10.0);

        assertEquals(249, monster.getX(), 1e-6);
        assertEquals(799, monster.getY(), 1e-6);
    }

    @Test
    void bonusMonsterShouldNotMoveWhenAlreadyAtPlayer() {
        BonusMonster monster =
                createMonster(249, 799, 100);

        monster.update(1.0);

        assertEquals(249, monster.getX());
        assertEquals(799, monster.getY());
    }

    @Test
    void bonusMonsterShouldNotMoveWithZeroSpeed() {
        BonusMonster monster = createMonster(100, 200, 0);

        monster.update(1.0);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    // ================= Update Conditions =================

    @Test
    void bonusMonsterShouldNotMoveWhenDtIsZero() {
        BonusMonster monster = createMonster(100, 200, 50);

        monster.update(0);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    @Test
    void bonusMonsterShouldNotMoveWhenDtIsNegative() {
        BonusMonster monster = createMonster(100, 200, 50);

        monster.update(-1);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    @Test
    void bonusMonsterShouldNotMoveWhenDead() {
        BonusMonster monster = createMonster(100, 200, 50);

        monster.onDeath();
        monster.update(1.0);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    @Test
    void bonusMonsterShouldNotMoveWhenCompleted() {
        BonusMonster monster = createMonster(100, 200, 50);

        for (char c : "bonus".toCharArray()) {
            monster.checkNextChar(c);
        }

        monster.update(1.0);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    @Test
    void updateShouldRejectInfiniteDt() {
        BonusMonster monster = createMonster(100, 200, 50);

        assertThrows(IllegalArgumentException.class,
                () -> monster.update(Double.POSITIVE_INFINITY));
    }

    @Test
    void updateShouldRejectNaNDt() {
        BonusMonster monster = createMonster(100, 200, 50);

        assertThrows(IllegalArgumentException.class,
                () -> monster.update(Double.NaN));
    }

    // ================= Death =================

    @Test
    void onDeathShouldMarkMonsterDead() {
        BonusMonster monster = createMonster(100, 200, 50);

        monster.onDeath();

        assertFalse(monster.isAlive());
    }

    // ================= Rendering =================

    @Test
    void renderShouldNotThrowException() {
        BonusMonster monster = createMonster(100, 200, 50);

        BufferedImage image =
                new BufferedImage(600, 900,
                        BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        try {
            assertDoesNotThrow(() -> monster.render(g));
        } finally {
            g.dispose();
        }
    }

    @Test
    void renderShouldNotThrowWhenMonsterIsDead() {
        BonusMonster monster = createMonster(100, 200, 50);

        monster.onDeath();

        BufferedImage image =
                new BufferedImage(600, 900,
                        BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        try {
            assertDoesNotThrow(() -> monster.render(g));
        } finally {
            g.dispose();
        }
    }
}