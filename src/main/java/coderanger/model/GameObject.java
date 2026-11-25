package coderanger.model;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public abstract class GameObject {
    private double x;
    private double y;
    private final int width;
    private final int height;
    private boolean alive = true;

    public GameObject(double x, double y, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update(double dt);

    public abstract void render(Graphics2D g);

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getCenterX() {
        return this.x + this.width / 2.0;
    }

    public double getCenterY() {
        return this.y + this.height / 2.0;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
