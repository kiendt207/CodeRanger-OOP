package coderanger.view;

import coderanger.input.InputHandler;
import coderanger.manager.GameManager;
import coderanger.manager.GameRecord;
import coderanger.resource.ResourceManager;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.function.Consumer;

public class PlayingScreen extends JPanel {
    private final GamePanel panel;
    private final InputHandler inputHandler;
    private final GameManager manager;
    private final Timer gameTimer;
    private final Consumer<GameRecord> onGameFinished;
    private long lastTime;
    
    // Biến đếm thời gian cho Hoạt cảnh chiến thắng
    private double animTimer = 0.0; 

    public PlayingScreen(GameManager manager, ResourceManager resources) {
        this(manager, resources, record -> { });
    }

    public PlayingScreen(
            GameManager manager,
            ResourceManager resources,
            Consumer<GameRecord> onGameFinished) {
        this.manager = manager;
        this.onGameFinished = onGameFinished;
        
        setLayout(new BorderLayout());
        
        panel = new GamePanel(manager, resources);
        inputHandler = new InputHandler(manager);
        
        add(panel, BorderLayout.CENTER);
        addKeyListener(inputHandler);
        setFocusable(true);

        gameTimer = new Timer(16, event -> {
            long now = System.nanoTime();
            double dt = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            
            if (dt > 0.05) dt = 0.05; 

            String state = manager.getState().toString();
            
            if (state.equals("PLAYING")) {
                manager.update(dt);
            } 
            else if (state.equals("VICTORY_ANIMATION")) {
                animTimer += dt;
                panel.setVictoryAnimTime(animTimer);
                
                if (animTimer >= 2.5) {
                    stopLoop();
                    GameFrame frame = (GameFrame) SwingUtilities.getWindowAncestor(this);
                    if (frame != null) {
                        frame.showResults(manager.getResult());
                    }
                }
            } 
            else if (state.equals("LOSS") || state.equals("WIN")) {
                stopLoop();
                GameFrame frame = (GameFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) {
                    frame.showResults(manager.getResult());
                }
            }
            
            panel.repaint();

            GameManager.GameState currentState = manager.getState();
            if ((currentState == GameManager.GameState.WIN || currentState == GameManager.GameState.LOSS)
                    && manager.getResult() != null) {
                onGameFinished.accept(manager.getResult());
            }
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
        requestFocusInWindow();
    }
}