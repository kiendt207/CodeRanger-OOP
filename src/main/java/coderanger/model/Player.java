package coderanger.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class Player extends GameObject {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 56;

    private static final int MAX_HP = 5;
    private static final int MAX_SHIELD = 3;

    private int hp;
    private int shieldLayers;
    public Player() {
        super(270 - WIDTH / 2.0, 820 - HEIGHT / 2.0, WIDTH, HEIGHT);
        this.hp = MAX_HP;
        this.shieldLayers = 1;
    }

    @Override
    public void update(double dt) {
        // Player đứng yên theo đặc tả.
        // Không xử lý input tại model này.
    }

    @Override 
    public void render(Graphics2D g){
        if (!isAlive()) {
            return;
        }

        Color oldColor = g.getColor();
        BasicStroke oldStroke = (BasicStroke) g.getStroke();

        double cx = getX() + getWidth() / 2.0;
        double cy = getY() + getHeight() / 2.0;

        // Vẽ shield bên ngoài phi thuyền.
        if (shieldLayers > 0) {
            g.setColor(new Color(70, 190, 255, 180));
            g.setStroke(new BasicStroke(2.0f));
            int radius = 34 + shieldLayers * 3;

            g.drawOval(
                (int) cx - radius,
                (int) cy - radius,
                radius * 2,
                radius * 2
            );
        }

        // Vẽ phi thuyền bằng Polygon, không cần ảnh.
        Polygon ship = new Polygon();
        ship.addPoint((int) cx, (int) getY());
        ship.addPoint((int) (getX() + getWidth()), (int) (cy + 12));
        ship.addPoint((int) (cx + 13), (int) (getY() + getHeight()));
        ship.addPoint((int) cx, (int) (getY() + getHeight() - 8));
        ship.addPoint((int) (cx - 13), (int) (getY() + getHeight()));
        ship.addPoint((int) getX(), (int) (cy + 12));

        g.setColor(new Color(80, 220, 240));
        g.fillPolygon(ship);

        g.setColor(Color.WHITE);
        g.drawPolygon(ship);

        // Buồng lái.
        g.setColor(new Color(35, 65, 115));
        g.fillOval((int) cx - 7, (int) cy - 9, 14, 20);

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    public int takeDamage(int damage) {
        if (damage < 0){
            throw new IllegalArgumentException("Damage must be non-negative");
        }

        int hpLost = 0;
        for (int i = 0; i < damage && hp > 0; i++){
            if (shieldLayers > 0) shieldLayers--;
            else{
                hp--;
                hpLost++;
            }
        }

        if (hp == 0) setAlive(false);

        return hpLost;
    }

    public boolean addShield() {
        if (shieldLayers >= MAX_SHIELD){
            return false;
        }

        shieldLayers++;
        return true;
    }

    public boolean heal(int amount) {
        if (hp >= MAX_HP){
            return false;
        }

        hp++;
        return true;
    }

    public int getHp() {
        return hp;
    }

    public int getShieldLayers() {
        return shieldLayers;
    }
}