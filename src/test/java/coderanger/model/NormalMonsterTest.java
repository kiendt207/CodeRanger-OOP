package coderanger.model;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class NormalMonsterTest {

    private NormalMonster createMonster(
            double x, double y, double speed, boolean minion) {
        return new NormalMonster(x, y, "cat", 0, 1, speed, minion);
    }

    // ================= Constructor =================

    @Test
    void constructorShouldInitializeValues() {
        NormalMonster monster =
                new NormalMonster(100, 200, "cat", 5, 10, 50, false);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
        assertEquals(NormalMonster.SIZE, monster.getWidth());
        assertEquals(NormalMonster.SIZE, monster.getHeight());

        assertEquals("cat", monster.getFullWord());
        assertEquals(5, monster.getSpawnElapsed());
        assertEquals(10, monster.getSpawnSequence());

        assertEquals(100, monster.getRewardScore());
        assertEquals(50, monster.getSpeed());

        assertFalse(monster.isMinion());
        assertTrue(monster.isAlive());
    }

    @Test
    void minionShouldReceiveDifferentReward() {
        NormalMonster monster =
                new NormalMonster(100, 200, "cat", 0, 1, 50, true);

        assertEquals(80, monster.getRewardScore());
        assertTrue(monster.isMinion());
    }

    @Test
    void constructorShouldRejectNegativeSpeed() {
        assertThrows(IllegalArgumentException.class,
                () -> createMonster(0, 0, -1, false));
    }

    @Test
    void constructorShouldRejectInfiniteSpeed() {
        assertThrows(IllegalArgumentException.class,
                () -> createMonster(0, 0, Double.POSITIVE_INFINITY, false));
    }

    @Test
    void constructorShouldRejectNaNSpeed() {
        assertThrows(IllegalArgumentException.class,
                () -> createMonster(0, 0, Double.NaN, false));
    }

    // ================= Normal Monster Movement =================

    @Test
    void normalMonsterShouldMoveTowardPlayer() {
        NormalMonster monster = createMonster(0, 0, 100, false);

        double oldX = monster.getX();
        double oldY = monster.getY();

        monster.update(1.0);

        assertTrue(monster.getX() > oldX);
        assertTrue(monster.getY() > oldY);
    }

    @Test
    void normalMonsterShouldMoveCorrectDistance() {
        NormalMonster monster = createMonster(0, 0, 100, false);

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
    void normalMonsterShouldStopAtPlayerPosition() {
        // Player ở (270, 820).
        // Monster kích thước 36x36 nên center = (270, 820)
        NormalMonster monster =
                createMonster(252, 802, 100, false);

        monster.update(1.0);

        assertEquals(252, monster.getX(), 1e-6);
        assertEquals(802, monster.getY(), 1e-6);
    }

    @Test
    void normalMonsterShouldNotOvershootPlayer() {
        NormalMonster monster = createMonster(0, 0, 10000, false);

        monster.update(10.0);

        assertEquals(252, monster.getX(), 1e-6);
        assertEquals(802, monster.getY(), 1e-6);
    }

    @Test
    void normalMonsterShouldNotMoveWhenAlreadyAtPlayer() {
        NormalMonster monster =
                createMonster(252, 802, 100, false);

        monster.update(1.0);

        assertEquals(252, monster.getX());
        assertEquals(802, monster.getY());
    }

    // ================= Minion Movement =================

    @Test
    void minionShouldMoveDownward() {
        NormalMonster minion = createMonster(100, 200, 50, true);

        minion.update(2.0);

        assertEquals(100, minion.getX());
        assertEquals(300, minion.getY());
    }

    @Test
    void minionShouldNotMoveHorizontally() {
        NormalMonster minion = createMonster(100, 200, 50, true);

        minion.update(1.0);

        assertEquals(100, minion.getX());
    }

    // ================= Update Conditions =================

    @Test
    void monsterShouldNotMoveWhenDtIsZero() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        monster.update(0);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    @Test
    void monsterShouldNotMoveWhenDtIsNegative() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        monster.update(-1);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    @Test
    void monsterShouldNotMoveWhenDead() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        monster.onDeath();
        monster.update(1.0);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    @Test
    void monsterShouldNotMoveWhenCompleted() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        for (char c : "cat".toCharArray()) {
            monster.checkNextChar(c);
        }

        monster.update(1.0);

        assertEquals(100, monster.getX());
        assertEquals(200, monster.getY());
    }

    @Test
    void updateShouldRejectInfiniteDt() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        assertThrows(IllegalArgumentException.class,
                () -> monster.update(Double.POSITIVE_INFINITY));
    }

    @Test
    void updateShouldRejectNaNDt() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        assertThrows(IllegalArgumentException.class,
                () -> monster.update(Double.NaN));
    }

    // ================= Death =================

    @Test
    void onDeathShouldMarkMonsterDead() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        monster.onDeath();

        assertFalse(monster.isAlive());
    }

    // ================= Rendering =================

    @Test
    void renderShouldNotThrowException() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        BufferedImage image =
                new BufferedImage(600, 900, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        assertDoesNotThrow(() -> monster.render(g));

        g.dispose();
    }

    @Test
    void renderShouldNotThrowWhenMonsterIsDead() {
        NormalMonster monster = createMonster(100, 200, 50, false);

        monster.onDeath();

        BufferedImage image =
                new BufferedImage(600, 900, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        assertDoesNotThrow(() -> monster.render(g));

        g.dispose();
    }
}