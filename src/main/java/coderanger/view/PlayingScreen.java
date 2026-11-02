package coderanger.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.Timer;

import coderanger.input.InputHandler;
import coderanger.manager.GameManager;
import coderanger.resource.ResourceManager;

public class PlayingScreen extends JPanel {
    private final GamePanel panel;
    private final InputHandler inputHandler;
    private final Timer gameTimer;
    private long lastTime;

    public PlayingScreen(GameManager manager, ResourceManager resources) {
        this.setLayout(new BorderLayout());
        
        this.panel = new GamePanel(manager, resources);
        this.inputHandler = new InputHandler(manager);
        
        this.add(panel, BorderLayout.CENTER);
        this.addKeyListener(inputHandler);
        this.setFocusable(true);

        this.gameTimer = new Timer(16, e -> {
            long now = System.nanoTime();
            double dt = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            
            if (dt > 0.05) dt = 0.05; 
            
            manager.update(dt);
            panel.repaint();
        });
    }

    public void startLoop() {
        lastTime = System.nanoTime();
        gameTimer.start();
        requestGameFocus();
    }

    public void stopLoop() {
        gameTimer.stop();
    }

    public void requestGameFocus() {
        this.requestFocusInWindow();
    }
}