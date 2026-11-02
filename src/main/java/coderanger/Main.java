package coderanger;

import coderanger.manager.GameManager;
import coderanger.manager.HistoryStore;
import coderanger.manager.MonsterFactory;
import coderanger.manager.WordProvider;
import coderanger.resource.ResourceManager;
import coderanger.view.GameFrame;
import java.nio.file.Paths;
import java.util.Random;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ResourceManager resources = new ResourceManager();
            HistoryStore historyStore = new HistoryStore(
                    Paths.get(System.getProperty("user.home"), ".coderanger", "history.tsv"));
            WordProvider wordProvider = new WordProvider(2026L);
            MonsterFactory monsterFactory = new MonsterFactory(wordProvider, new Random(2026L));
            GameManager manager = new GameManager(historyStore, monsterFactory);

            GameFrame frame = new GameFrame(manager, resources);
            frame.setVisible(true);
            frame.showMenu();
        });
    }
}
