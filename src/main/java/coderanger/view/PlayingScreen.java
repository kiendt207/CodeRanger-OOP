package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.resource.ResourceManager;
import coderanger.input.InputHandler;

import javax.swing.*;
import java.awt.*;

public class PlayingScreen extends JPanel {
    private final GamePanel panel;
    private final GameManager manager;
    private final InputHandler input;
    private Timer timer;

    public PlayingScreen(GameManager manager, ResourceManager resources) {
        this.manager = manager;
        setLayout(new BorderLayout()); // Để GamePanel lấp đầy không gian

        // Khởi tạo GamePanel chứa logic vẽ giao diện
        panel = new GamePanel(manager, resources);
        add(panel, BorderLayout.CENTER);

        // Khởi tạo InputHandler và thiết lập nhận tiêu điểm
        input = new InputHandler(manager);
        addKeyListener(input);
        setFocusable(true);
        requestFocusInWindow();
    }

    public void startLoop() {
        // Thiết lập vòng lặp game ~60 FPS (16ms) thông qua EDT Timer
        if (timer == null) {
            timer = new Timer(16, e -> {
                // Ở phiên bản hoàn chỉnh, dt sẽ được tính toán thực tế.
                // Ở đây là stub gọi update() và repaint().
                manager.update(0.016); 
                panel.repaint();
            });
        }
        timer.start();
        requestGameFocus();
    }

    public void stopLoop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void requestGameFocus() {
        requestFocusInWindow();
    }
}