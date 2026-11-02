package coderanger.input;

import coderanger.manager.GameManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    private final GameManager manager;

    public InputHandler(GameManager manager) {
        this.manager = manager;
    }

    @Override
    public void keyTyped(KeyEvent event) {
        char character = event.getKeyChar();
        if (character >= 'a' && character <= 'z') {
            manager.handleCharacter(character);
        } else if (character >= 'A' && character <= 'Z') {
            manager.handleCharacter((char) (character - 'A' + 'a'));
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            manager.togglePause();
        }
    }
}
