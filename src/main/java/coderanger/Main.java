package coderanger;

import coderanger.manager.GameManager;
import coderanger.view.GameFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo các dependency cốt lõi theo kiến trúc dự án
            // GameManager gameManager = new GameManager(...);
            // GameFrame frame = new GameFrame(gameManager);
            // frame.setVisible(true);
            
            System.out.println("CodeRanger Main Application Initialized.");
        });
    }
}