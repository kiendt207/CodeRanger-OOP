package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.input.InputHandler;
import coderanger.resource.ResourceManager;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class PlayingScreen extends JPanel {
    private final GamePanel panel;
    private final InputHandler inputHandler;
    private final GameManager manager;
    private final Timer gameTimer;
    private long lastTime;
    
    // Biến đếm thời gian cho Hoạt cảnh chiến thắng (Tuần 6)
    private double animTimer = 0.0; 

    public PlayingScreen(GameManager manager, ResourceManager resources) {
        this.manager = manager;
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
            
            // Tuần 7: Tối ưu FPS và chống giật khung hình (Soak Test)
            if (dt > 0.05) dt = 0.05; 

            String state = manager.getState().toString();
            
            if (state.equals("PLAYING")) {
                manager.update(dt);
            } 
            else if (state.equals("VICTORY_ANIMATION")) {
                // Xử lý hoạt cảnh đúng 2.5 giây 
                animTimer += dt;
                panel.setVictoryAnimTime(animTimer);
                
                if (animTimer >= 2.5) {
                    stopLoop();
                    GameFrame frame = (GameFrame) SwingUtilities.getWindowAncestor(this);
                    frame.showResults(manager.getResult());
                }
            } 
            else if (state.equals("LOSS") || state.equals("WIN")) {
                // Xử lý chuyển cảnh ngay lập tức khi Thua (hoặc Thắng bỏ qua animation)
                stopLoop();
                GameFrame frame = (GameFrame) SwingUtilities.getWindowAncestor(this);
                frame.showResults(manager.getResult());
            }
            
            panel.repaint();
        });
    }

    public void startLoop() {
        animTimer = 0.0;
        panel.setVictoryAnimTime(0.0);
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