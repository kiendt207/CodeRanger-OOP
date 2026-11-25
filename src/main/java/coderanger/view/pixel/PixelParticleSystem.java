package coderanger.view.pixel;

import coderanger.util.GameConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Pure UI-side pixel particle, floating combat text, and screen-shake manager.
 * Never mutates domain models.
 */
public class PixelParticleSystem {
    private static final int MAX_PARTICLES = 180;
    private static final int MAX_TEXTS = 24;

    private static final class Particle {
        double x, y, vx, vy;
        double age, life;
        int size;
        Color color;
        double gravity;
    }

    private static final class FloatingLabel {
        double x, y, vy;
        double age, life;
        String text;
        Color color;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingLabel> labels = new ArrayList<>();
    private final Random rng = new Random(2026L);

    private double shakeRemaining;
    private double shakeMagnitude;

    public void clear() {
        particles.clear();
        labels.clear();
        shakeRemaining = 0.0;
        shakeMagnitude = 0.0;
    }

    public void update(double dt) {
        if (dt <= 0 || !Double.isFinite(dt)) {
            return;
        }
        if (shakeRemaining > 0.0) {
            shakeRemaining = Math.max(0.0, shakeRemaining - dt);
        }

        Iterator<Particle> pit = particles.iterator();
        while (pit.hasNext()) {
            Particle p = pit.next();
            p.age += dt;
            if (p.age >= p.life) {
                pit.remove();
                continue;
            }
            p.vy += p.gravity * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
        }

        Iterator<FloatingLabel> lit = labels.iterator();
        while (lit.hasNext()) {
            FloatingLabel l = lit.next();
            l.age += dt;
            if (l.age >= l.life) {
                lit.remove();
                continue;
            }
            l.y += l.vy * dt;
        }
    }

    public void spawnStrikeSparks(double x, double y, boolean fever) {
        Color c1 = fever ? PixelTheme.HUST_GOLD : PixelTheme.CYAN_BRIGHT;
        Color c2 = fever ? PixelTheme.ALERT_RED : Color.WHITE;
        for (int i = 0; i < 6 && particles.size() < MAX_PARTICLES; i++) {
            Particle p = new Particle();
            double angle = rng.nextDouble() * Math.PI * 2.0;
            double speed = 45.0 + rng.nextDouble() * 95.0;
            p.x = x;
            p.y = y;
            p.vx = Math.cos(angle) * speed;
            p.vy = Math.sin(angle) * speed;
            p.life = 0.16 + rng.nextDouble() * 0.12;
            p.size = rng.nextBoolean() ? 4 : 3;
            p.color = (i % 2 == 0) ? c1 : c2;
            p.gravity = 90.0;
            particles.add(p);
        }
    }

    public void spawnKillBurst(double x, double y, Color primary, Color secondary) {
        for (int i = 0; i < 16 && particles.size() < MAX_PARTICLES; i++) {
            Particle p = new Particle();
            double angle = (Math.PI * 2.0 * i) / 16.0 + (rng.nextDouble() - 0.5) * 0.35;
            double speed = 55.0 + rng.nextDouble() * 145.0;
            p.x = x;
            p.y = y;
            p.vx = Math.cos(angle) * speed;
            p.vy = Math.sin(angle) * speed - 25.0;
            p.life = 0.28 + rng.nextDouble() * 0.22;
            p.size = (i % 3 == 0) ? 6 : 4;
            p.color = (i % 2 == 0) ? primary : secondary;
            p.gravity = 180.0;
            particles.add(p);
        }
    }

    public void spawnVictoryConfetti() {
        if (particles.size() >= MAX_PARTICLES) {
            return;
        }
        Color[] palette = {
            PixelTheme.HUST_GOLD, PixelTheme.CYAN_BRIGHT,
            PixelTheme.ALERT_RED, PixelTheme.TEA_GREEN, Color.WHITE
        };
        for (int i = 0; i < 3 && particles.size() < MAX_PARTICLES; i++) {
            Particle p = new Particle();
            p.x = 40 + rng.nextDouble() * (GameConstants.WIDTH - 80);
            p.y = 110 + rng.nextDouble() * 60;
            p.vx = (rng.nextDouble() - 0.5) * 80.0;
            p.vy = 60.0 + rng.nextDouble() * 90.0;
            p.life = 1.4;
            p.size = 5;
            p.color = palette[rng.nextInt(palette.length)];
            p.gravity = 45.0;
            particles.add(p);
        }
    }

    public void spawnFloatingText(double x, double y, String text, Color color) {
        if (labels.size() >= MAX_TEXTS) {
            labels.remove(0);
        }
        FloatingLabel l = new FloatingLabel();
        l.x = Math.max(48, Math.min(GameConstants.WIDTH - 48, x));
        l.y = Math.max(120, y);
        l.vy = -42.0;
        l.life = 0.85;
        l.text = text;
        l.color = color;
        labels.add(l);
    }

    public void triggerShake(double duration, double magnitude) {
        this.shakeRemaining = Math.max(this.shakeRemaining, duration);
        this.shakeMagnitude = Math.max(this.shakeMagnitude, magnitude);
    }

    public int currentShakeX() {
        if (shakeRemaining <= 0.0) return 0;
        return (int) Math.round((rng.nextDouble() * 2.0 - 1.0) * shakeMagnitude);
    }

    public int currentShakeY() {
        if (shakeRemaining <= 0.0) return 0;
        return (int) Math.round((rng.nextDouble() * 2.0 - 1.0) * shakeMagnitude);
    }

    public void render(Graphics2D g, Font labelFont) {
        for (Particle p : particles) {
            double alphaRatio = Math.max(0.0, 1.0 - (p.age / p.life));
            int alpha = Math.min(255, (int) Math.round(255 * alphaRatio));
            g.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha));
            int px = ((int) Math.round(p.x) / 2) * 2;
            int py = ((int) Math.round(p.y) / 2) * 2;
            g.fillRect(px - p.size / 2, py - p.size / 2, p.size, p.size);
        }

        if (labelFont != null && !labels.isEmpty()) {
            g.setFont(labelFont);
            FontMetrics fm = g.getFontMetrics();
            for (FloatingLabel l : labels) {
                int tx = (int) Math.round(l.x) - fm.stringWidth(l.text) / 2;
                int ty = (int) Math.round(l.y);
                PixelTheme.drawShadowString(g, l.text, tx, ty, l.color, PixelTheme.PIXEL_INK);
            }
        }
    }
}
