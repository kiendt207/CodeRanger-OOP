package coderanger.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class NormalMonster extends Monster {
    public static final int SIZE = 36;
    private static final double PLAYER_X = 270.0;
    private static final double PLAYER_Y = 820.0;
    
    private final double speed;
    private final boolean minion;

    public NormalMonster(double x, double y, String word, double elapsed,
                        long seq, double speed, boolean minion){
        super(x, y, SIZE, SIZE, word, elapsed, seq, minion ? 80 : 100);
        
        if (speed < 0 || !Double.isFinite(speed)){
            throw new IllegalArgumentException("Speed must be finite and non-negative");
        }

        this.speed = speed;
        this.minion = minion;
    }

    @Override
    public void update(double dt){
        if (!isAlive() || isCompleted() || dt <= 0){
            return;
        }

        if (!Double.isFinite(dt)){
            throw new IllegalArgumentException("dt must be finite");
        }

        double centerX = getX() + getWidth()/2.0;
        double centerY = getY() + getHeight()/2.0;

        // minion spawn phía trên và di chuyển xuống
        if (minion){
            setPosition(getX(), getY() + speed * dt);
            return;
        }

        // quái thông thường di chuyển về phía player
        double dx = PLAYER_X - centerX;
        double dy = PLAYER_Y - centerY;
        double distance = Math.hypot(dx, dy);

        if (distance == 0){
            return;
        }

        double step = speed * dt;
        double move = Math.min(step, distance);

        setPosition(
            getX() + dx / distance * move,
            getY() + dy / distance * move
        );
    }

    @Override
    public void render(Graphics2D g){
        if (!isAlive()) {
            return;
        }

        Color oldColor = g.getColor();
        Font oldFont = g.getFont();

        // Thân quái.
        g.setColor(minion
            ? new Color(220, 125, 65)
            : new Color(220, 75, 85));

        g.fill(new Ellipse2D.Double(
            getX(), getY(), getWidth(), getHeight()
        ));

        g.setColor(Color.WHITE);
        g.draw(new Ellipse2D.Double(
            getX(), getY(), getWidth(), getHeight()
        ));

        // Từ nằm phía trên quái.
        renderWord(g);

        g.setColor(oldColor);
        g.setFont(oldFont);
    }

    protected void renderWord(Graphics2D g) {
        String word = getFullWord();
        int typed = getTypedCharacters();

        Font oldFont = g.getFont();
        Color oldColor = g.getColor();

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));

        // Căn giữa từ theo vị trí quái.
        int totalWidth = g.getFontMetrics().stringWidth(word);
        int textX = (int) (
            getX() + getWidth() / 2.0 - totalWidth / 2.0
        );
        int textY = (int) getY() - 6;

        if (typed > 0) {
            String typedPart = word.substring(0, typed);
            g.setColor(Color.GRAY);
            g.drawString(typedPart, textX, textY);
        }

        if (typed < word.length()) {
            String remainingPart = word.substring(typed);
            int typedWidth = g.getFontMetrics()
                .stringWidth(word.substring(0, typed));

            g.setColor(Color.WHITE);
            g.drawString(
                remainingPart,
                textX + typedWidth,
                textY
            );
        }

        g.setFont(oldFont);
        g.setColor(oldColor);
    }

    @Override
    public boolean isMinion(){
        return minion;
    }
    public double getSpeed(){
        return speed;
    }
}
