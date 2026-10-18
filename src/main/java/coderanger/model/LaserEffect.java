package coderanger.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class LaserEffect extends GameObject {
    private static final double DURATION = 0.12;

    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;

    private double elapsed;


    public LaserEffect(double startX, double startY, double endX, double endY) {
        super(
            Math.min(startX, endX),
            Math.min(startY, endY),
            Math.max(1, (int) Math.ceil(Math.abs(endX - startX))),
            Math.max(1, (int) Math.ceil(Math.abs(endY - startY)))
        );

        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
        this.elapsed = 0.0;
    }

    @Override 
    public void update(double dt) {
        if (!isAlive()) return;

        if (!Double.isFinite(dt) || dt < 0){
            throw new IllegalArgumentException("dt must be finite and non-negative");
        }

        elapsed += dt;

        if (elapsed >= DURATION) setAlive(false);
    }

    @Override
    public void render(Graphics2D g) {
        if (!isAlive()) {
            return;
        }

        Color oldColor = g.getColor();
        BasicStroke oldStroke = (BasicStroke) g.getStroke();

        // Tia sáng bên ngoài.
        g.setColor(new Color(50, 210, 255, 100));
        g.setStroke(new BasicStroke(
            6.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));
        g.drawLine(
            (int) Math.round(startX),
            (int) Math.round(startY),
            (int) Math.round(endX),
            (int) Math.round(endY)
        );

        // Lõi laser sáng.
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(
            2.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));
        g.drawLine(
            (int) Math.round(startX),
            (int) Math.round(startY),
            (int) Math.round(endX),
            (int) Math.round(endY)
        );

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    public double getElapsed(){
        return elapsed;
    }

    public double getDuration(){
        return DURATION;
    }
}