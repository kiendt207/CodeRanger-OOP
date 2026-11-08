package coderanger.input;

import coderanger.manager.GameManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class InputHandler extends KeyAdapter {
    private final GameManager manager;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Set<Integer> typedKeys = new HashSet<>();

    public InputHandler(GameManager manager) {
        this.manager = manager;
    }

    @Override
    public void keyTyped(KeyEvent event) {
        char character = event.getKeyChar();
        if (character >= 'a' && character <= 'z') {
            handleLetter(character);
        } else if (character >= 'A' && character <= 'Z') {
            handleLetter((char) (character - 'A' + 'a'));
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (!pressedKeys.add(keyCode)) {
            return;
        }
        if (keyCode == KeyEvent.VK_ESCAPE) {
            manager.togglePause();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        pressedKeys.remove(event.getKeyCode());
        typedKeys.remove(event.getKeyCode());
    }

    private void handleLetter(char character) {
        int keyCode = KeyEvent.VK_A + character - 'a';
        if (pressedKeys.contains(keyCode) && !typedKeys.add(keyCode)) {
            return;
        }
        manager.handleCharacter(character);
    }
}
