package coderanger.view;

import coderanger.input.InputHandler;
import coderanger.manager.GameManager;
import coderanger.manager.GameRecord;
import coderanger.resource.ResourceManager;
import java.awt.BorderLayout;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PlayingScreen extends JPanel {
    private final GameManager manager;
    private final GamePanel panel;
    private final Timer gameTimer;
    private final Consumer<GameRecord> onGameFinished;
    private long lastTime;

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
        add(panel, BorderLayout.CENTER);
        addKeyListener(new InputHandler(manager));
        setFocusable(true);

        gameTimer = new Timer(16, event -> {
            long now = System.nanoTime();
            double dt = Math.min((now - lastTime) / 1_000_000_000.0, 0.05);
            lastTime = now;
            manager.update(dt);
            panel.repaint();

            GameManager.GameState state = manager.getState();
            if ((state == GameManager.GameState.WIN || state == GameManager.GameState.LOSS)
                    && manager.getResult() != null) {
                onGameFinished.accept(manager.getResult());
            }
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
        requestFocusInWindow();
    }
}
