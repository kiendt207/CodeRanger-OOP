package coderanger.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

public class BonusMonster extends Monster {
    private static final int SIZE = 42;
    private static final double PLAYER_X = 270.0;
    private static final double PLAYER_Y = 820.0;

    private final double speed;

    public BonusMonster(double x, double y, String word,
                        double elapsed, long seq, double speed){
        super(x, y, SIZE, SIZE, word, elapsed, seq, 150);
        
        if (speed < 0 || !Double.isFinite(speed)){
            throw new IllegalArgumentException("Speed must be finite and non-negative");
        }
        
        this.speed = speed;
    }

    @Override 
    public void update(double dt) {
        if (!isAlive() || isCompleted() || dt <= 0) {
            return;
        }

        if (!Double.isFinite(dt)) {
            throw new IllegalArgumentException("dt must be finite");
        }

        double centerX = getX() + getWidth() / 2.0;
        double centerY = getY() + getHeight() / 2.0;

        double dx = PLAYER_X - centerX;
        double dy = PLAYER_Y - centerY;
        double distance = Math.hypot(dx, dy);

        if (distance == 0) {
            return;
        }

        double move = Math.min(speed * dt, distance);

        setPosition(
            getX() + dx / distance * move,
            getY() + dy / distance * move
        );
    }

    @Override
    public void render(Graphics2D g) {
        if (!isAlive()) {
            return;
        }

        Color oldColor = g.getColor();
        Font oldFont = g.getFont();

        // Hình vuông bo góc để phân biệt với quái thường.
        g.setColor(new Color(65, 190, 220));
        g.fill(new RoundRectangle2D.Double(
            getX(), getY(),
            getWidth(), getHeight(),
            12, 12
        ));

        g.setColor(Color.WHITE);
        g.draw(new RoundRectangle2D.Double(
            getX(), getY(),
            getWidth(), getHeight(),
            12, 12
        ));

        renderWord(g);

        g.setColor(oldColor);
        g.setFont(oldFont);
    }

    private void renderWord(Graphics2D g) {
        String word = getFullWord();
        int typed = getTypedCharacters();

        Font oldFont = g.getFont();
        Color oldColor = g.getColor();

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));

        int totalWidth = g.getFontMetrics().stringWidth(word);
        int textX = (int) (
            getX() + getWidth() / 2.0 - totalWidth / 2.0
        );
        int textY = (int) getY() - 6;

        if (typed > 0) {
            g.setColor(Color.GRAY);
            g.drawString(word.substring(0, typed), textX, textY);
        }

        if (typed < word.length()) {
            int typedWidth = g.getFontMetrics()
                .stringWidth(word.substring(0, typed));

            g.setColor(Color.WHITE);
            g.drawString(
                word.substring(typed),
                textX + typedWidth,
                textY
            );
        }

        g.setFont(oldFont);
        g.setColor(oldColor);
    }

    public double getSpeed() {
        return speed;
    }
}
