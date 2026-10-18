package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import coderanger.model.Monster;
import coderanger.model.Player;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private final GameManager gameManager;
    private final ResourceManager resourceManager;

    public GamePanel(GameManager gameManager, ResourceManager resourceManager) {
        this.gameManager = gameManager;
        this.resourceManager = resourceManager;
        setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Bật tính năng khử răng cưa cho text và shape
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vẽ ranh giới nguy hiểm (Danger line) tại y = 760[cite: 2]
        g2d.setColor(Color.RED);
        g2d.drawLine(0, GameConstants.DANGER_Y, GameConstants.WIDTH, GameConstants.DANGER_Y);

        // 2. Vẽ khu vực HUD (Hiển thị thông số) tại khoảng y = 0..95[cite: 2]
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        
        // Vì GameManager Tuần 1 chưa có stats thật nên chúng ta giả lập thông số (mock) nếu object bị null
        int score = (gameManager.getStats() != null) ? (int)gameManager.getStats().getScore() : 0;
        int hp = (gameManager.getPlayer() != null) ? gameManager.getPlayer().getHp() : 5;
        int shield = (gameManager.getPlayer() != null) ? gameManager.getPlayer().getShieldLayers() : 1;
        
        g2d.drawString("SCORE: " + score, 15, 30);
        g2d.drawString("HP: " + hp + "/5", 15, 55);
        g2d.drawString("SHIELD: " + shield + "/3", 15, 80);

        // 3. Vẽ Phi thuyền người chơi (Tâm ở 270, 820 kích thước 64x56)[cite: 2]
        if (gameManager.getPlayer() != null) {
            Player player = gameManager.getPlayer();
            // Fallback vẽ bằng hình khối nếu chưa có ảnh[cite: 2]
            g2d.setColor(Color.CYAN);
            g2d.fillRect((int) player.getX() - 32, (int) player.getY() - 28, 64, 56); 
        }

        // 4. Vẽ danh sách quái vật đang tồn tại (Mock/Stub data từ Thành viên B)[cite: 2]
        if (gameManager.getMonsters() != null) {
            List<Monster> monsters = gameManager.getMonsters();
            for (Monster m : monsters) {
                if (m.isAlive()) {
                    g2d.setColor(Color.GREEN);
                    // Giả lập vẽ tâm quái vật
                    g2d.fillOval((int) m.getX() - 15, (int) m.getY() - 15, 30, 30);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
                    // Vẽ từ (word) chưa gõ bên trên đầu quái
                    g2d.drawString(m.getRemainingWord(), (int) m.getX() - 15, (int) m.getY() - 20);
                }
            }
        }
    }
}