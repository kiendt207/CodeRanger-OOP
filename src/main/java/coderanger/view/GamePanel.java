package coderanger.view;

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

    // Bỏ field resourceManager dư thừa đi để hết báo vàng
    public GamePanel(GameManager manager, ResourceManager resources) {
        this.manager = manager;
        this.setBackground(new Color(5, 5, 10)); // Nền cyber
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vạch Danger Line
        g2d.setColor(new Color(255, 50, 50, 100));
        g2d.drawLine(0, GameConstants.DANGER_Y, GameConstants.WIDTH, GameConstants.DANGER_Y);

        if (manager.getPlayer() != null) manager.getPlayer().render(g2d);
        manager.getMonsters().forEach(m -> m.render(g2d));
        manager.getEffects().forEach(e -> e.render(g2d));

        drawHUD(g2d);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        // Cột trái
        g2d.setColor(Color.CYAN);
        g2d.drawString("SCORE: " + manager.getStats().getScore(), 10, 25);
        
        if (manager.getPlayer() != null) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("HP: " + manager.getPlayer().getHp() + "/5", 10, 45);
            
            // Cột phải hiển thị Kỹ năng
            g2d.setColor(new Color(100, 200, 255));
            g2d.drawString("KHIÊN [ENTER]: " + manager.getPlayer().getShieldLayers() + "/4", 320, 25);
            
            g2d.setColor(Color.ORANGE);
            g2d.drawString("BOM [1]: READY", 320, 45); // Tạm fix cứng Text chờ backend update
            
            g2d.setColor(Color.MAGENTA);
            g2d.drawString("T.CÔNG [2]: READY", 320, 65);
            
            g2d.setColor(Color.YELLOW);
            // Bạn nhắc người D code thêm biến combo trong GameStats nhé
            g2d.drawString("COMBO: " + manager.getStats().getStreak(), 320, 85); 
        }
    }
}