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
    public void keyTyped(KeyEvent e) {
        // Chỉ nhận sự kiện gõ chữ và truyền ký tự tới GameManager xử lý logic
        char c = e.getKeyChar();
        manager.handleCharacter(c);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Chỉ bắt phím Esc để bật/tắt tạm dừng game
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            manager.togglePause();
        }
    }
}