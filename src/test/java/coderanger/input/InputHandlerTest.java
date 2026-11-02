package coderanger.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import coderanger.manager.GameManager;
import coderanger.manager.GameRecord;
import coderanger.manager.HistoryStore;
import coderanger.manager.MonsterFactory;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class InputHandlerTest {
    @Test
    void normalizesUppercaseLettersAndIgnoresNonAlphabeticInput() {
        GameManager manager = new GameManager(new HistoryStore(), new MonsterFactory());
        manager.start("Tester");
        InputHandler handler = new InputHandler(manager);
        JPanel source = new JPanel();

        handler.keyTyped(new KeyEvent(source, KeyEvent.KEY_TYPED, 0, 0,
                KeyEvent.VK_UNDEFINED, 'A'));
        handler.keyTyped(new KeyEvent(source, KeyEvent.KEY_TYPED, 0, 0,
                KeyEvent.VK_UNDEFINED, '!'));

        GameRecord snapshot = manager.getStats().snapshot(
                "Tester", 0, "PLAYING", 1, manager.getPlayer());
        assertEquals(1, snapshot.wrongChars());
    }

    @Test
    void escapePausesAndResumesWithoutAcceptingPausedTyping() {
        GameManager manager = new GameManager(new HistoryStore(), new MonsterFactory());
        manager.start("Tester");
        InputHandler handler = new InputHandler(manager);
        JPanel source = new JPanel();
        KeyEvent escape = new KeyEvent(source, KeyEvent.KEY_PRESSED, 0, 0,
                KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);

        handler.keyPressed(escape);
        handler.keyTyped(new KeyEvent(source, KeyEvent.KEY_TYPED, 0, 0,
                KeyEvent.VK_UNDEFINED, 'a'));
        assertEquals(GameManager.GameState.PAUSED, manager.getState());
        assertEquals(0, manager.getStats().getStreak());

        handler.keyPressed(escape);
        assertEquals(GameManager.GameState.PLAYING, manager.getState());
    }
}
