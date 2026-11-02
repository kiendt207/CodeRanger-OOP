
package coderanger.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;

import static org.junit.jupiter.api.Assertions.*;

class GameObjectTest {

    private TestGameObject object;

    /**
     * Concrete class dùng để test abstract GameObject.
     */
    private static class TestGameObject extends GameObject {

        TestGameObject(double x, double y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void update(double dt) {
            // Không cần logic cho test GameObject.
        }

        @Override
        public void render(Graphics2D g) {
            // Không cần logic cho test GameObject.
        }

        // Cho phép test protected setPosition().
        void moveTo(double x, double y) {
            setPosition(x, y);
        }
    }

    @BeforeEach
    void setUp() {
        object = new TestGameObject(10.0, 20.0, 30, 40);
    }

    // =========================
    // Constructor tests
    // =========================

    @Test
    void constructorShouldInitializePositionAndSize() {
        assertEquals(10.0, object.getX());
        assertEquals(20.0, object.getY());
        assertEquals(30, object.getWidth());
        assertEquals(40, object.getHeight());
    }

    @Test
    void constructorShouldInitializeAliveAsTrue() {
        assertTrue(object.isAlive());
    }

    @Test
    void constructorShouldRejectZeroWidth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestGameObject(0, 0, 0, 10)
        );
    }

    @Test
    void constructorShouldRejectNegativeWidth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestGameObject(0, 0, -1, 10)
        );
    }

    @Test
    void constructorShouldRejectZeroHeight() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestGameObject(0, 0, 10, 0)
        );
    }

    @Test
    void constructorShouldRejectNegativeHeight() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestGameObject(0, 0, 10, -1)
        );
    }

    @Test
    void constructorShouldAcceptPositiveDimensions() {
        assertDoesNotThrow(
                () -> new TestGameObject(0, 0, 1, 1)
        );
    }

    // =========================
    // getBounds() tests
    // =========================

    @Test
    void getBoundsShouldReturnCorrectRectangle() {
        Rectangle2D bounds = object.getBounds();

        assertEquals(10.0, bounds.getX());
        assertEquals(20.0, bounds.getY());
        assertEquals(30.0, bounds.getWidth());
        assertEquals(40.0, bounds.getHeight());
    }

    @Test
    void getBoundsShouldReturnRectangleWithCorrectCenter() {
        Rectangle2D bounds = object.getBounds();

        assertEquals(25.0, bounds.getCenterX());
        assertEquals(40.0, bounds.getCenterY());
    }

    @Test
    void getBoundsShouldReflectUpdatedPosition() {
        object.moveTo(50.0, 60.0);

        Rectangle2D bounds = object.getBounds();

        assertEquals(50.0, bounds.getX());
        assertEquals(60.0, bounds.getY());
        assertEquals(30.0, bounds.getWidth());
        assertEquals(40.0, bounds.getHeight());
    }

    // =========================
    // Alive state tests
    // =========================

    @Test
    void setAliveFalseShouldMarkObjectDead() {
        object.setAlive(false);

        assertFalse(object.isAlive());
    }

    @Test
    void setAliveTrueShouldMarkObjectAlive() {
        object.setAlive(false);
        object.setAlive(true);

        assertTrue(object.isAlive());
    }

    @Test
    void setAliveShouldNotChangePositionOrSize() {
        object.setAlive(false);

        assertEquals(10.0, object.getX());
        assertEquals(20.0, object.getY());
        assertEquals(30, object.getWidth());
        assertEquals(40, object.getHeight());
    }

    // =========================
    // setPosition() tests
    // =========================

    @Test
    void setPositionShouldUpdateCoordinates() {
        object.moveTo(100.0, 200.0);

        assertEquals(100.0, object.getX());
        assertEquals(200.0, object.getY());
    }

    @Test
    void setPositionShouldNotChangeDimensions() {
        object.moveTo(100.0, 200.0);

        assertEquals(30, object.getWidth());
        assertEquals(40, object.getHeight());
    }

    // =========================
    // render() tests
    // =========================

    @Test
    void renderShouldNotThrowException() {
        BufferedImage image = new BufferedImage(
                100,
                100,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = image.createGraphics();

        try {
            assertDoesNotThrow(() -> object.render(graphics));
        } finally {
            graphics.dispose();
        }
    }

    // =========================
    // update() tests
    // =========================

    @Test
    void updateShouldNotThrowException() {
        assertDoesNotThrow(() -> object.update(0.016));
    }
}