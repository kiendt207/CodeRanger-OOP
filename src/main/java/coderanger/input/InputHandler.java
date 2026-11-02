package coderanger.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import coderanger.manager.GameManager;

public class InputHandler extends KeyAdapter {
    private final GameManager manager;

    public InputHandler(GameManager manager) {
        this.manager = manager;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        // Đặc tả gốc: Chỉ nhận ký tự a-z ASCII và tự chuyển thành lowercase
        if (Character.isLetter(c)) {
            manager.handleCharacter(Character.toLowerCase(c));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Dùng switch-case theo gợi ý của IDE để xử lý các phím chức năng
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                manager.togglePause();
                break;
            default:
                break;
        }
    }
}