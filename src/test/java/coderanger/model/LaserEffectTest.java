package coderanger.model;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class LaserEffectTest {

    private LaserEffect createLaser() {
        return new LaserEffect(10, 20, 100, 200);
    }

    @Test
    void constructorShouldSetPositionAndSize() {
        LaserEffect laser = createLaser();

        assertEquals(10, laser.getX());
        assertEquals(20, laser.getY());
        assertEquals(90, laser.getWidth());
        assertEquals(180, laser.getHeight());
        assertTrue(laser.isAlive());
        assertEquals(0.0, laser.getElapsed());
    }

    @Test
    void constructorShouldHandleReversedCoordinates() {
        LaserEffect laser = new LaserEffect(100, 200, 10, 20);

        assertEquals(10, laser.getX());
        assertEquals(20, laser.getY());
        assertEquals(90, laser.getWidth());
        assertEquals(180, laser.getHeight());
    }

    @Test
    void constructorShouldEnsureMinimumWidthAndHeight() {
        LaserEffect laser = new LaserEffect(50, 50, 50, 50);

        assertEquals(1, laser.getWidth());
        assertEquals(1, laser.getHeight());
    }

    @Test
    void constructorShouldRoundUpWidthAndHeight() {
        LaserEffect laser = new LaserEffect(10.2, 20.3, 15.1, 25.1);

        assertEquals(5, laser.getWidth());
        assertEquals(5, laser.getHeight());
    }

    @Test
    void getDurationShouldReturnExpectedDuration() {
        LaserEffect laser = createLaser();

        assertEquals(0.12, laser.getDuration());
    }

    @Test
    void updateShouldIncreaseElapsedTime() {
        LaserEffect laser = createLaser();

        laser.update(0.05);

        assertEquals(0.05, laser.getElapsed());
        assertTrue(laser.isAlive());
    }

    @Test
    void updateShouldRemainAliveBeforeDuration() {
        LaserEffect laser = createLaser();

        laser.update(0.119);

        assertTrue(laser.isAlive());
        assertEquals(0.119, laser.getElapsed());
    }

    @Test
    void updateShouldDieAtDurationBoundary() {
        LaserEffect laser = createLaser();

        laser.update(0.12);

        assertFalse(laser.isAlive());
        assertEquals(0.12, laser.getElapsed());
    }

    @Test
    void updateShouldDieAfterDuration() {
        LaserEffect laser = createLaser();

        laser.update(0.2);

        assertFalse(laser.isAlive());
        assertEquals(0.2, laser.getElapsed());
    }

    @Test
    void updateShouldAccumulateElapsedTime() {
        LaserEffect laser = createLaser();

        laser.update(0.04);
        laser.update(0.04);
        laser.update(0.03);

        assertEquals(0.11, laser.getElapsed(), 1e-9);
        assertTrue(laser.isAlive());

        laser.update(0.01);

        assertFalse(laser.isAlive());
    }

    @Test
    void updateShouldAcceptZeroDt() {
        LaserEffect laser = createLaser();

        laser.update(0);

        assertEquals(0.0, laser.getElapsed());
        assertTrue(laser.isAlive());
    }

    @Test
    void updateShouldRejectNegativeDt() {
        LaserEffect laser = createLaser();

        assertThrows(IllegalArgumentException.class,
                () -> laser.update(-0.01));
    }

    @Test
    void updateShouldRejectNanDt() {
        LaserEffect laser = createLaser();

        assertThrows(IllegalArgumentException.class,
                () -> laser.update(Double.NaN));
    }

    @Test
    void updateShouldRejectInfiniteDt() {
        LaserEffect laser = createLaser();

        assertThrows(IllegalArgumentException.class,
                () -> laser.update(Double.POSITIVE_INFINITY));

        assertThrows(IllegalArgumentException.class,
                () -> laser.update(Double.NEGATIVE_INFINITY));
    }

    @Test
    void updateShouldNotChangeElapsedWhenDead() {
        LaserEffect laser = createLaser();

        laser.update(0.12);
        double elapsed = laser.getElapsed();

        // Object đã chết nên update sẽ return trước khi kiểm tra dt.
        laser.update(0.05);

        assertEquals(elapsed, laser.getElapsed());
        assertFalse(laser.isAlive());
    }

    @Test
    void updateShouldIgnoreInvalidDtWhenDead() {
        LaserEffect laser = createLaser();

        laser.update(0.12);

        assertDoesNotThrow(() -> laser.update(Double.NaN));
        assertDoesNotThrow(() -> laser.update(-1));
    }

    @Test
    void renderShouldNotThrowWhenAlive() {
        LaserEffect laser = createLaser();

        BufferedImage image = new BufferedImage(
                300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        try {
            assertDoesNotThrow(() -> laser.render(g));
        } finally {
            g.dispose();
        }
    }

    @Test
    void renderShouldNotThrowWhenDead() {
        LaserEffect laser = createLaser();
        laser.update(0.12);

        BufferedImage image = new BufferedImage(
                300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        try {
            assertDoesNotThrow(() -> laser.render(g));
        } finally {
            g.dispose();
        }
    }

    @Test
    void renderShouldRestoreGraphicsColorAndStroke() {
        LaserEffect laser = createLaser();

        BufferedImage image = new BufferedImage(
                300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        Color originalColor = Color.RED;
        BasicStroke originalStroke = new BasicStroke(3.0f);

        try {
            g.setColor(originalColor);
            g.setStroke(originalStroke);

            laser.render(g);

            assertEquals(originalColor, g.getColor());
            assertEquals(originalStroke, g.getStroke());
        } finally {
            g.dispose();
        }
    }
}