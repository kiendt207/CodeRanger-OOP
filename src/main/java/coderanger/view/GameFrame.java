package coderanger.view;

import coderanger.audio.SoundEngine;
import coderanger.manager.GameManager;
import coderanger.manager.GameRecord;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final GameManager manager;
    private final MenuScreen menuScreen;
    private final PlayingScreen playingScreen;
    private final ResultsScreen resultsScreen;
    private final LeaderboardScreen leaderboardScreen;
    private long previousBestScore;

    public GameFrame(GameManager manager, ResourceManager resources) {
        this(manager, resources, new SoundEngine());
    }

    public GameFrame(GameManager manager, ResourceManager resources, SoundEngine soundEngine) {
        this.manager = manager;
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        setTitle("Sanbaka CodeRanger • Bách Khoa OOP Survival");
        if (resources != null) {
            resources.getImage("portrait_happy").ifPresent(this::setIconImage);
        }
        getContentPane().setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setMinimumSize(new Dimension(380, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        add(mainPanel);

        menuScreen = new MenuScreen(playerName -> {
            snapshotPreviousBest();
            manager.start(playerName);
            showPlay();
        }, this::showLeaderboard, resources, soundEngine);

        playingScreen = new PlayingScreen(manager, resources, soundEngine, this::showResults);

        resultsScreen = new ResultsScreen(() -> {
            snapshotPreviousBest();
            manager.start(manager.getPlayerName());
            showPlay();
        }, this::showMenu, resources, soundEngine);

        leaderboardScreen = new LeaderboardScreen(
                manager.getHistoryStore(), this::showMenu, resources, soundEngine);

        mainPanel.add(menuScreen, "MENU");
        mainPanel.add(playingScreen, "PLAY");
        mainPanel.add(resultsScreen, "RESULTS");
        mainPanel.add(leaderboardScreen, "LEADERBOARD");

        pack();
        setLocationRelativeTo(null);

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent event) {
                manager.pause();
            }
        });
    }

    private void snapshotPreviousBest() {
        try {
            List<GameRecord> top = manager.getHistoryStore().top10();
            previousBestScore = top.isEmpty() ? 0L : top.get(0).score();
        } catch (Exception e) {
            previousBestScore = 0L;
        }
    }

    public void showMenu() {
        playingScreen.stopLoop();
        cardLayout.show(mainPanel, "MENU");
    }

    public void showPlay() {
        cardLayout.show(mainPanel, "PLAY");
        playingScreen.startLoop();
    }

    public void showResults(GameRecord record) {
        playingScreen.stopLoop();
        boolean isNewHigh = record != null && record.score() > 0 && record.score() > previousBestScore;
        resultsScreen.showResult(
                record,
                isNewHigh,
                previousBestScore,
                manager.getLastBossHpAtEnd()
        );
        if (manager.getSaveErrorMessage() != null) {
            resultsScreen.showSaveError(manager.getSaveErrorMessage());
        }
        cardLayout.show(mainPanel, "RESULTS");
    }

    public void showLeaderboard() {
        leaderboardScreen.refresh();
        cardLayout.show(mainPanel, "LEADERBOARD");
    }
}
