package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.util.GameConstants;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel containerPanel;
    private final GameManager gameManager;

    public GameFrame(GameManager gameManager) {
        this.gameManager = gameManager;
        setTitle("CodeRanger - 2D Typing Defense");
        setSize(GameConstants.WIDTH, GameConstants.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        // Thêm các màn hình cơ bản
        GamePanel gamePanel = new GamePanel(gameManager);
        containerPanel.add(gamePanel, "PLAYING");

        add(containerPanel);
    }

    public void showPlay() {
        cardLayout.show(containerPanel, "PLAYING");
    }
}