package coderanger.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class ShockwaveEffect extends GameObject {
    public static final double DURATION = 0.35;
    private static final double MIN_RADIUS = 16.0;
    private static final double MAX_RADIUS = 150.0;

    private final double originX;
    private final double originY;
    private final double targetX;
    private final double targetY;
    private double elapsed;

    public ShockwaveEffect(double originX, double originY, double targetX, double targetY) {
        super(
            originX - MAX_RADIUS,
            originY - MAX_RADIUS,
            (int) (MAX_RADIUS * 2),
            (int) (MAX_RADIUS * 2)
        );
        this.originX = originX;
        this.originY = originY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.elapsed = 0.0;
    }

    @Override
    public void update(double dt) {
        if (!isAlive()) {
            return;
        }
        if (!Double.isFinite(dt) || dt < 0.0) {
            throw new IllegalArgumentException("dt must be finite and non-negative");
        }
        elapsed += dt;
        if (elapsed >= DURATION) {
            setAlive(false);
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!isAlive()) {
            return;
        }
        Color oldColor = g.getColor();
        Stroke oldStroke = g.getStroke();

        double progress = Math.min(1.0, elapsed / DURATION);
        double radius = MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * progress;
        int alpha = Math.max(0, (int) Math.round(230 * (1.0 - progress)));

        g.setColor(new Color(56, 189, 248, alpha));
        g.setStroke(new BasicStroke(4.0f));
        int r = (int) Math.round(radius);
        g.drawOval((int) Math.round(originX) - r, (int) Math.round(originY) - r, r * 2, r * 2);

        int innerR = Math.max(4, (int) Math.round(radius * 0.65));
        g.setColor(new Color(255, 213, 79, alpha));
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval((int) Math.round(originX) - innerR, (int) Math.round(originY) - innerR, innerR * 2, innerR * 2);

        g.drawLine(
            (int) Math.round(originX),
            (int) Math.round(originY),
            (int) Math.round(targetX),
            (int) Math.round(targetY)
        );

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    public double getProgress() {
        return Math.min(1.0, elapsed / DURATION);
    }

    public double getElapsed() {
        return elapsed;
    }

    public double getDuration() {
        return DURATION;
    }

    public double getOriginX() {
        return originX;
    }

    public double getOriginY() {
        return originY;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }
}
