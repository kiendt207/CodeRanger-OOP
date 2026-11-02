package coderanger.view;

import coderanger.manager.GameRecord;
import coderanger.util.GameConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Locale;

/**
 * Màn kết quả (mục 5, 12, 17): "You win" hoặc "Game over" cùng thống kê
 * gõ phím, score, wave và HP/shield cuối. Fields private: labels, buttons.
 * Constructor: (Runnable onRestart, Runnable onMenu). Frame -> screen.
 */
@SuppressWarnings("serial")
public class ResultsScreen extends JPanel {

    /** labels[0]=tiêu đề, [1]=tên, [2..13]=12 chỉ số, [14]=thông báo lỗi lưu. */
    private final JLabel[] labels;
    /** buttons[0]=Restart, buttons[1]=Menu. */
    private final JButton[] buttons;

    public ResultsScreen(Runnable onRestart, Runnable onMenu) {
        labels = new JLabel[15];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel(" ");
            labels[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            labels[i].setForeground(Color.WHITE);
            labels[i].setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        }
        labels[0].setFont(new Font(Font.MONOSPACED, Font.BOLD, 44));
        labels[1].setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        labels[1].setForeground(new Color(150, 170, 200));
        labels[labels.length - 1].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        labels[labels.length - 1].setForeground(new Color(255, 120, 120));

        buttons = new JButton[] {new JButton("Restart"), new JButton("Menu")};
        for (JButton b : buttons) {
            b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(24, 52, 90));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(new Color(0, 230, 160), 2));
            b.setPreferredSize(new Dimension(240, 52));
            b.setMaximumSize(new Dimension(240, 52));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        buttons[0].addActionListener(e -> onRestart.run());
        buttons[1].addActionListener(e -> onMenu.run());

        setBackground(new Color(8, 14, 34));
        setPreferredSize(new Dimension((int) GameConstants.WIDTH, (int) GameConstants.HEIGHT));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());
        add(labels[0]);
        add(Box.createVerticalStrut(6));
        add(labels[1]);
        add(Box.createVerticalStrut(28));
        for (int i = 2; i < labels.length - 1; i++) {
            add(labels[i]);
            add(Box.createVerticalStrut(6));
        }
        add(Box.createVerticalStrut(4));
        add(labels[labels.length - 1]);
        add(Box.createVerticalStrut(24));
        add(buttons[0]);
        add(Box.createVerticalStrut(14));
        add(buttons[1]);
        add(Box.createVerticalGlue());
    }

    /** Hiện kết quả của phiên chơi; xóa thông báo lỗi lưu cũ. */
    public void showResult(GameRecord record) {
        boolean win = "WIN".equalsIgnoreCase(record.outcome());
        labels[0].setText(win ? "You win" : "Game over");
        labels[0].setForeground(win ? new Color(0, 230, 160) : new Color(255, 90, 90));
        labels[1].setText(record.name());

        String[] lines = {
                "Score: " + record.score(),
                "Wave: " + record.wave(),
                "HP: " + record.hp() + "   Shield: " + record.shield(),
                String.format(Locale.ROOT, "Survived: %.1f s", record.seconds()),
                "Monsters killed: " + record.kills(),
                "Quick kills: " + record.quick(),
                "Bonus kills: " + record.bonus(),
                "Boss words: " + record.bossWords(),
                "Correct chars: " + record.correct(),
                "Wrong chars: " + record.wrong(),
                String.format(Locale.ROOT, "Accuracy: %.1f%%", record.accuracy()),
                String.format(Locale.ROOT, "WPM: %.1f", record.wpm())
        };
        for (int i = 0; i < lines.length; i++) {
            labels[2 + i].setText(lines[i]);
        }
        labels[labels.length - 1].setText(" ");
    }

    /**
     * Báo lỗi lưu lịch sử (HistoryStore.save ném IOException) nhưng vẫn giữ
     * kết quả phiên chơi trên màn hình (mục 13). Bổ sung ngoài API mục 17.
     */
    public void showSaveError(String message) {
        labels[labels.length - 1].setText(message == null || message.isBlank()
                ? "Could not save history" : message);
    }
}