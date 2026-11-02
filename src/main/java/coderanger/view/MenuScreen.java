package coderanger.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import coderanger.util.GameConstants;

/**
 * MenuScreen theo CodeRanger Spec v1.0 (mục 5, 15, 17).
 *
 * Fields private: nameField, buttons.
 * Constructor: (Consumer<String> onStart, Runnable onLeaderboard).
 * API công khai: getPlayerName():String. Frame -> callbacks.
 * Menu có Start, Leaderboard, Quit và ô tên 1-16 ký tự chữ, số hoặc
 * khoảng trắng; rỗng thành "Player".
 */
@SuppressWarnings("serial")
public class MenuScreen extends JPanel {
private static final long serialVersionUID = 1L;    
private final JTextField nameField;
    private final JButton[] buttons;   // [0]=Start, [1]=Leaderboard, [2]=Quit

    public MenuScreen(Consumer<String> onStart, Runnable onLeaderboard) {
        nameField = new JTextField(16);
        buttons = new JButton[] {
                new JButton("Start"), new JButton("Leaderboard"), new JButton("Quit")
        };

        setupNameField();
        for (JButton b : buttons) {
            setupButton(b);
        }
        setupLayout();

        // Callbacks chỉ được bắt trong lambda, không lưu thành field.
        buttons[0].addActionListener(e -> onStart.accept(getPlayerName()));
        nameField.addActionListener(e -> onStart.accept(getPlayerName())); // Enter
        buttons[1].addActionListener(e -> onLeaderboard.run());
        buttons[2].addActionListener(e -> quit());

        // CardLayout không tự cấp focus: xin focus cho ô tên mỗi khi menu hiện.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(nameField::requestFocusInWindow);
            }
        });
    }

    /** Tên đã trim; rỗng thì "Player". Ô nhập đã lọc nên luôn hợp lệ (<= 16 ký tự). */
    public String getPlayerName() {
        String name = nameField.getText().trim();
        return name.isEmpty() ? "Player" : name;
    }

    // ---------------------------------------------------------------- setup

    private void setupNameField() {
        ((PlainDocument) nameField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                replace(fb, offset, 0, text, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                    throws BadLocationException {
                StringBuilder clean = new StringBuilder();
                if (text != null) {
                    for (char c : text.toCharArray()) {
                        if (Character.isLetterOrDigit(c) || c == ' ') {
                            clean.append(c);
                        }
                    }
                }
                int room = 16 - (fb.getDocument().getLength() - length);
                if (clean.length() > room) {
                    clean.setLength(Math.max(room, 0));
                }
                if (clean.length() > 0 || length > 0) {
                    super.replace(fb, offset, length, clean.toString(), attr);
                }
            }
        });
        nameField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(new Color(0, 230, 160));
        nameField.setBackground(new Color(20, 30, 55));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 230, 160), 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        nameField.setMaximumSize(new Dimension(320, 46));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void setupButton(JButton button) {
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(24, 52, 90));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 230, 160), 2));
        button.setPreferredSize(new Dimension(240, 52));
        button.setMaximumSize(new Dimension(240, 52));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.MONOSPACED, style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void setupLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension((int) GameConstants.WIDTH, (int) GameConstants.HEIGHT));

        add(Box.createVerticalGlue());
        add(label("CODE RANGER", Font.BOLD, 40, new Color(0, 230, 160)));
        add(Box.createVerticalStrut(60));
        add(label("Player name", Font.PLAIN, 16, Color.WHITE));
        add(Box.createVerticalStrut(8));
        add(nameField);
        add(Box.createVerticalStrut(36));
        for (int i = 0; i < buttons.length; i++) {
            if (i > 0) {
                add(Box.createVerticalStrut(14));
            }
            add(buttons[i]);
        }
        add(Box.createVerticalGlue());
    }

    /** Quit: đóng cửa sổ chứa menu; nếu chưa gắn cửa sổ thì thoát chương trình. */
    private void quit() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        } else {
            System.exit(0);
        }
    }

    // --------------------------------------------------------------- render

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(8, 14, 34));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Tọa độ logic 540x900, scale đồng đều min(w/540, h/900) + letterboxing.
            double scale = Math.min((double) getWidth() / GameConstants.WIDTH,
                                    (double) getHeight() / GameConstants.HEIGHT);
            g.translate((getWidth() - GameConstants.WIDTH * scale) / 2.0,
                        (getHeight() - GameConstants.HEIGHT * scale) / 2.0);
            g.scale(scale, scale);

            // Phi thuyền tại vị trí player (tâm PLAYER_X, PLAYER_Y), kích thước 64 x 56.
            int cx = (int) Math.round(GameConstants.PLAYER_X);
            int cy = (int) Math.round(GameConstants.PLAYER_Y);
            g.setColor(new Color(0, 230, 160));
            g.fillPolygon(new int[] {cx, cx + 32, cx + 14, cx - 14, cx - 32},
                          new int[] {cy - 28, cy + 28, cy + 14, cy + 14, cy + 28}, 5);
        } finally {
            g.dispose();
        }
    }
}