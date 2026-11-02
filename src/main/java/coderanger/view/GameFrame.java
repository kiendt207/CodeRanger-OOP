package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.manager.GameRecord;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

public class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    
    // Khai báo final cho các màn hình thành phần thuộc CardLayout
    private final MenuScreen menuScreen;
    private final PlayingScreen playingScreen;
    private final ResultsScreen resultsScreen;
    private final LeaderboardScreen leaderboardScreen;

    public GameFrame(GameManager manager, ResourceManager resources) {
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        
        this.setTitle("CodeRanger - Cyber Defense v1.0");
        this.getContentPane().setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.add(mainPanel);
        this.pack();
        this.setLocationRelativeTo(null);

        // Khởi tạo các Screen với Callback Runnable/Consumer
        this.menuScreen = new MenuScreen(
            playerName -> {
                manager.start(playerName);
                showPlay();
            },
            this::showLeaderboard
        );
        
        this.playingScreen = new PlayingScreen(manager, resources);
        this.resultsScreen = new ResultsScreen(this::showMenu, this::showMenu); 
        this.leaderboardScreen = new LeaderboardScreen(manager.getHistoryStore(), this::showMenu);

        // Đăng ký các thẻ màn hình vào CardLayout
        mainPanel.add(menuScreen, "MENU");
        mainPanel.add(playingScreen, "PLAY");
        mainPanel.add(resultsScreen, "RESULTS");
        mainPanel.add(leaderboardScreen, "LEADERBOARD");
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
        cardLayout.show(mainPanel, "RESULTS");
    }

    public void showLeaderboard() {
        leaderboardScreen.refresh();
        cardLayout.show(mainPanel, "LEADERBOARD");
    }
}