package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel containerPanel;
    private final GameManager gameManager;
    private final ResourceManager resourceManager;

    public GameFrame(GameManager gameManager, ResourceManager resourceManager) {
        this.gameManager = gameManager;
        this.resourceManager = resourceManager;
        
        setTitle("CodeRanger - 2D Typing Defense");
        setSize(GameConstants.WIDTH, GameConstants.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        // Đã sửa lỗi: Truyền đủ 2 tham số vào GamePanel
        GamePanel gamePanel = new GamePanel(gameManager, resourceManager);
        containerPanel.add(gamePanel, "PLAYING");

        add(containerPanel);
    }

    public void showPlay() {
        cardLayout.show(containerPanel, "PLAYING");
    }
}