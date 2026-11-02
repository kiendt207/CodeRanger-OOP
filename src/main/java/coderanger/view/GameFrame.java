package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.manager.GameRecord;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final GameManager manager;
    private final MenuScreen menuScreen;
    private final PlayingScreen playingScreen;
    private final ResultsScreen resultsScreen;
    private final LeaderboardScreen leaderboardScreen;

    public GameFrame(GameManager manager, ResourceManager resources) {
        this.manager = manager;
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        setTitle("CodeRanger - Cyber Defense v1.0");
        getContentPane().setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);

        menuScreen = new MenuScreen(playerName -> {
            manager.start(playerName);
            showPlay();
        }, this::showLeaderboard);
        playingScreen = new PlayingScreen(manager, resources, this::showResults);
        resultsScreen = new ResultsScreen(() -> {
            manager.start(manager.getPlayerName());
            showPlay();
        }, this::showMenu);
        leaderboardScreen = new LeaderboardScreen(manager.getHistoryStore(), this::showMenu);

        mainPanel.add(menuScreen, "MENU");
        mainPanel.add(playingScreen, "PLAY");
        mainPanel.add(resultsScreen, "RESULTS");
        mainPanel.add(leaderboardScreen, "LEADERBOARD");

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent event) {
                manager.pause();
            }
        });
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
        resultsScreen.showResult(record);
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
