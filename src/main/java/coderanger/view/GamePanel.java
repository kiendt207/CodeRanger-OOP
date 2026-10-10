package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.util.GameConstants;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final GameManager gameManager;

    public GamePanel(GameManager gameManager) {
        this.gameManager = gameManager;
        setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(Color.WHITE);
        g2d.drawString("CodeRanger Game Panel", 180, 50);
    }
}