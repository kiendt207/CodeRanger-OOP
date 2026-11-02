package coderanger.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import coderanger.manager.GameManager;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;

public class GamePanel extends JPanel {
    private final GameManager manager;
    private double victoryAnimTime = 0.0;

    public GamePanel(GameManager manager, ResourceManager resources) {
        this.manager = manager;
        this.setBackground(Color.BLACK); 
    }

    public void setVictoryAnimTime(double time) {
        this.victoryAnimTime = time;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelW = getWidth();
        int panelH = getHeight();
        double scale = Math.min((double) panelW / GameConstants.WIDTH, (double) panelH / GameConstants.HEIGHT);
        int xOffset = (int) ((panelW - GameConstants.WIDTH * scale) / 2);
        int yOffset = (int) ((panelH - GameConstants.HEIGHT * scale) / 2);

        g2d.translate(xOffset, yOffset);
        g2d.scale(scale, scale);
        g2d.setClip(0, 0, GameConstants.WIDTH, GameConstants.HEIGHT);
        
        g2d.setColor(new Color(5, 5, 10));
        g2d.fillRect(0, 0, GameConstants.WIDTH, GameConstants.HEIGHT);

        g2d.setColor(new Color(255, 50, 50, 100));
        g2d.drawLine(0, GameConstants.DANGER_Y, GameConstants.WIDTH, GameConstants.DANGER_Y);

        if (!manager.getState().toString().equals("VICTORY_ANIMATION")) {
            if (manager.getPlayer() != null) manager.getPlayer().render(g2d);
            manager.getMonsters().forEach(m -> m.render(g2d));
            manager.getEffects().forEach(e -> e.render(g2d));
            drawHUD(g2d);
        } else {
            drawVictoryAnimation(g2d);
        }
    }

    private void drawVictoryAnimation(Graphics2D g2d) {
        double progress = Math.min(victoryAnimTime / 2.5, 1.0);
        
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 5; i++) {
            double r = 18 + (115 - 18) * progress * (1.0 - i * 0.1);
            if (r > 0) {
                g2d.setColor(new Color(0, 255, 204, (int)(255 * (1 - progress))));
                g2d.drawOval(GameConstants.PLAYER_X - (int)r, 320 - (int)r, (int)r * 2, (int)r * 2);
            }
        }
        
        int shipY = (int) (GameConstants.PLAYER_Y - progress * (GameConstants.PLAYER_Y - 350));
        double shipScale = 1.0 - (0.9 * progress);
        
        g2d.translate(GameConstants.PLAYER_X, shipY);
        g2d.scale(shipScale, shipScale);
        
        g2d.setColor(Color.CYAN);
        g2d.fillPolygon(new int[]{0, -25, 25}, new int[]{-30, 20, 20}, 3);
        
        g2d.scale(1 / shipScale, 1 / shipScale);
        g2d.translate(-GameConstants.PLAYER_X, -shipY);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(Color.CYAN);
        g2d.drawString("SCORE: " + manager.getStats().getScore(), 10, 25);
        
        if (manager.getPlayer() != null) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("HP: " + manager.getPlayer().getHp() + "/5", 10, 45);
            
            g2d.setColor(new Color(100, 200, 255));
            g2d.drawString("KHIÊN [ENTER]: " + manager.getPlayer().getShieldLayers() + "/4", 320, 25);
            g2d.setColor(Color.ORANGE);
            g2d.drawString("BOM [1]: READY", 320, 45);
            g2d.setColor(Color.MAGENTA);
            g2d.drawString("T.CÔNG [2]: READY", 320, 65);
            g2d.setColor(Color.YELLOW);
            g2d.drawString("COMBO: " + manager.getStats().getStreak(), 320, 85); 
        }
    }
}