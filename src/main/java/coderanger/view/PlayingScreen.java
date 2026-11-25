package coderanger.view;

import coderanger.audio.SoundEffect;
import coderanger.audio.SoundEngine;
import coderanger.input.InputHandler;
import coderanger.manager.GameManager;
import coderanger.manager.GameRecord;
import coderanger.resource.ResourceManager;
import java.awt.BorderLayout;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class PlayingScreen extends JPanel {
    private final GamePanel panel;
    private final GameManager manager;
    private final SoundEngine soundEngine;
    private final Timer gameTimer;
    private final Consumer<GameRecord> onGameFinished;
    private long lastTime;

    public PlayingScreen(GameManager manager, ResourceManager resources) {
        this(manager, resources, new SoundEngine(true), record -> { });
    }

    public PlayingScreen(
            GameManager manager,
            ResourceManager resources,
            Consumer<GameRecord> onGameFinished) {
        this(manager, resources, new SoundEngine(true), onGameFinished);
    }

    public PlayingScreen(
            GameManager manager,
            ResourceManager resources,
            SoundEngine soundEngine,
            Consumer<GameRecord> onGameFinished) {
        this.manager = manager;
        this.soundEngine = soundEngine;
        this.onGameFinished = onGameFinished;
        setLayout(new BorderLayout());
        panel = new GamePanel(manager, resources, soundEngine);
        add(panel, BorderLayout.CENTER);
        addKeyListener(new InputHandler(manager));
        setFocusable(true);

        gameTimer = new Timer(16, event -> {
            long now = System.nanoTime();
            double dt = Math.min((now - lastTime) / 1_000_000_000.0, 0.05);
            lastTime = now;
            manager.update(dt);
            panel.tickVisuals(dt);

            GameManager.GameState state = manager.getState();
            if (state == GameManager.GameState.VICTORY_ANIMATION
                    || state == GameManager.GameState.WIN) {
                panel.setVictoryAnimTime(2.5 - manager.getVictoryAnimationRemaining());
            }
            panel.repaint();

            if ((state == GameManager.GameState.WIN || state == GameManager.GameState.LOSS)
                    && manager.getResult() != null) {
                stopLoop();
                if (this.soundEngine != null) {
                    this.soundEngine.play(
                            state == GameManager.GameState.WIN
                                    ? SoundEffect.VICTORY
                                    : SoundEffect.GAME_OVER);
                }
                onGameFinished.accept(manager.getResult());
            }
        });
    }

    public void startLoop() {
        panel.setVictoryAnimTime(0.0);
        panel.resetVisualTracking();
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
