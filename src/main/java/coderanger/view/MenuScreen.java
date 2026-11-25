package coderanger.view;

import coderanger.audio.SoundEffect;
import coderanger.audio.SoundEngine;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import coderanger.view.pixel.PixelButton;
import coderanger.view.pixel.PixelTheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 * Pixel-Art HUST x Sanbaka MenuScreen inspired by docs/sanbaka_demo.jpeg.
 * Preserves public getPlayerName() and 1-16 character alphanumeric+space filter.
 */
@SuppressWarnings("serial")
public class MenuScreen extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ResourceManager resources;
    private final SoundEngine soundEngine;
    private final JTextField nameField;
    private final JButton[] buttons; // [0]=Start, [1]=Leaderboard, [2]=Quit
    private final JButton soundToggleBtn;
    private final Timer idleTimer;
    private int animFrame;

    public MenuScreen(Consumer<String> onStart, Runnable onLeaderboard) {
        this(onStart, onLeaderboard, new ResourceManager(), new SoundEngine(true));
    }

    public MenuScreen(
            Consumer<String> onStart,
            Runnable onLeaderboard,
            ResourceManager resources,
            SoundEngine soundEngine) {
        this.resources = resources != null ? resources : new ResourceManager();
        this.soundEngine = soundEngine;

        Font btnFont = this.resources.getPixelFont(15f);
        nameField = new JTextField(16);
        buttons = new JButton[] {
            new PixelButton("VÀO PHÒNG THI (START)", btnFont, true),
            new PixelButton("BẢNG VÀNG ĐRL", btnFont, false),
            new PixelButton("NGHỈ HỌC (QUIT)", btnFont, false)
        };
        soundToggleBtn = new PixelButton(soundButtonLabel(), this.resources.getPixelFont(12f), false);
        soundToggleBtn.setPreferredSize(new Dimension(210, 34));
        soundToggleBtn.setMaximumSize(new Dimension(220, 34));
        soundToggleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        setupNameField();
        for (JButton b : buttons) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        setupLayout();

        buttons[0].addActionListener(e -> {
            playClick();
            onStart.accept(getPlayerName());
        });
        nameField.addActionListener(e -> {
            playClick();
            onStart.accept(getPlayerName());
        });
        buttons[1].addActionListener(e -> {
            playClick();
            onLeaderboard.run();
        });
        buttons[2].addActionListener(e -> {
            playClick();
            quit();
        });
        soundToggleBtn.addActionListener(e -> {
            if (this.soundEngine != null) {
                boolean nowMuted = this.soundEngine.toggleMute();
                soundToggleBtn.setText(soundButtonLabel());
                if (!nowMuted) {
                    this.soundEngine.play(SoundEffect.UI_CLICK);
                }
            }
        });

        idleTimer = new Timer(320, e -> {
            animFrame = (animFrame + 1) % 2;
            soundToggleBtn.setText(soundButtonLabel());
            repaint();
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    idleTimer.start();
                    SwingUtilities.invokeLater(nameField::requestFocusInWindow);
                } else {
                    idleTimer.stop();
                }
            }
        });
    }

    private String soundButtonLabel() {
        boolean muted = soundEngine != null && soundEngine.isMuted();
        return muted ? "♦ ÂM THANH 8-BIT: TẮT" : "► ÂM THANH 8-BIT: BẬT";
    }

    private void playClick() {
        if (soundEngine != null) {
            soundEngine.play(SoundEffect.UI_CLICK);
        }
    }

    public String getPlayerName() {
        String name = nameField.getText().trim();
        return name.isEmpty() ? "Player" : name;
    }

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
        nameField.setFont(resources.getPixelFont(18f));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setForeground(PixelTheme.PIXEL_INK);
        nameField.setCaretColor(PixelTheme.HUST_CRIMSON);
        nameField.setBackground(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PixelTheme.PIXEL_INK, 3),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        nameField.setMaximumSize(new Dimension(290, 42));
        nameField.setPreferredSize(new Dimension(290, 42));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void setupLayout() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));

        // Leave top space for header + Sanbaka showcase card painted in paintComponent
        add(Box.createVerticalStrut(360));

        JLabel prompt = new JLabel("MÃ SỐ / TÊN SINH VIÊN (1–16 KÝ TỰ):");
        prompt.setFont(resources.getPixelFont(13f));
        prompt.setForeground(PixelTheme.HUST_CRIMSON_DARK);
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(prompt);
        add(Box.createVerticalStrut(6));
        add(nameField);
        add(Box.createVerticalStrut(18));

        for (int i = 0; i < buttons.length; i++) {
            if (i > 0) {
                add(Box.createVerticalStrut(10));
            }
            add(buttons[i]);
        }
        add(Box.createVerticalStrut(10));
        add(soundToggleBtn);
        add(Box.createVerticalGlue());
    }

    private void quit() {
        idleTimer.stop();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        } else {
            System.exit(0);
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            PixelTheme.enableCrispPixel(g);
            PixelTheme.drawCrimsonHalftoneBackground(g, getWidth(), getHeight());

            int cx = getWidth() / 2;

            // Retro yellow-gold 8-bit title like "SANBAKA RUN" in sanbaka_demo.jpeg
            PixelTheme.drawChunkyPixelBanner(
                    g,
                    resources.getPixelFont(15f),
                    "SANBAKA CODE",
                    cx,
                    38,
                    3,
                    PixelTheme.HUST_GOLD,
                    PixelTheme.PIXEL_INK
            );

            // Black pill "百科ちゃん" + subtitle "BÁCH KHOA OOP SURVIVAL • IT3100!"
            g.setFont(resources.getPixelFont(13f));
            FontMetrics fm = g.getFontMetrics();
            BufferedImage hyakka = resources.getImage("badge_hyakka").orElse(null);
            String sub = " • BÁCH KHOA OOP SURVIVAL • IT3100!";
            int pillW = hyakka != null ? hyakka.getWidth() : 82;
            int totalSubW = pillW + fm.stringWidth(sub);
            int subX = cx - totalSubW / 2;

            if (hyakka != null) {
                g.drawImage(hyakka, subX, 101, null);
            } else {
                g.setColor(PixelTheme.PIXEL_INK);
                g.fillRect(subX, 101, pillW, 22);
            }
            PixelTheme.drawShadowString(
                    g, sub, subX + pillW, 118, Color.WHITE, PixelTheme.PIXEL_INK);

            // Main Exam Dialog Card housing Sanbaka & the interactive form!
            int cardW = Math.min(484, getWidth() - 32);
            int cardX = cx - cardW / 2;
            PixelTheme.drawExamDialogCard(g, cardX, 140, cardW, 565);

            // Inside top of card: Sanbaka portrait box + Speech bubble!
            BufferedImage portrait = resources.getImage(
                    animFrame == 0 ? "portrait_happy" : "portrait_focus").orElse(null);
            PixelTheme.drawPortraitBox(
                    g,
                    portrait,
                    resources.getImage("jp_yaruzo").orElse(null),
                    "SANBAKA",
                    resources.getPixelFont(11f),
                    cardX + 28,
                    172,
                    112,
                    136
            );

            int bubbleX = cardX + 154;
            int bubbleW = cardW - 182;
            PixelTheme.drawChunkyPixelBanner(
                    g,
                    resources.getPixelFont(12f),
                    "8 KÌ BÁCH KHOA!",
                    bubbleX + bubbleW / 2,
                    174,
                    2,
                    PixelTheme.HUST_CRIMSON,
                    PixelTheme.PIXEL_INK
            );

            PixelTheme.drawSpeechBox(g, bubbleX, 214, bubbleW, 92, true);
            g.setFont(resources.getPixelFont(12f));
            g.setColor(PixelTheme.PIXEL_INK);
            g.drawString("Cùng Sanbaka gõ sạch Bug &", bubbleX + 10, 235);
            g.drawString("Deadline qua đủ 8 Học Kì!", bubbleX + 10, 253);
            g.setColor(PixelTheme.HUST_CRIMSON_DARK);
            g.drawString("► Kì 8: Bảo vệ Đồ Án OOP!", bubbleX + 10, 273);
            g.setColor(new Color(18, 115, 135));
            g.drawString("► Combo ≥10 mở OVERDRIVE!", bubbleX + 10, 292);

            // Divider line above form
            g.setColor(PixelTheme.HUST_GOLD_DARK);
            g.fillRect(cardX + 28, 326, cardW - 56, 2);

            // Bottom footer instructions (just like sanbaka_demo.jpeg!)
            int footY = 742;
            g.setFont(resources.getPixelFont(13f));
            drawCenteredFooterLine(g, "Gõ phím [a–z] để Sanbaka tung chiêu khóa mục tiêu", cx, footY, Color.WHITE);
            drawCenteredFooterLine(g, "Diệt từ ≤ 1.5s nổ ► SHOCKWAVE • Diệt ♦ Trà Đá hồi Giáp/HP", cx, footY + 24, PixelTheme.HUST_GOLD);
            drawCenteredFooterLine(g, "Nhấn [ENTER] vào thi ngay • [ESC] tạm nghỉ uống trà đá", cx, footY + 48, Color.WHITE);

            // Animated mini battle Sanbaka at bottom center!
            BufferedImage chibi = resources.getImage("sanbaka_cast_" + animFrame).orElse(null);
            if (chibi != null) {
                g.drawImage(chibi, cx - 24, 804, 48, 48, null);
            }
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }

    private static void drawCenteredFooterLine(Graphics2D g, String text, int cx, int y, Color fg) {
        FontMetrics fm = g.getFontMetrics();
        PixelTheme.drawShadowString(g, text, cx - fm.stringWidth(text) / 2, y, fg, PixelTheme.PIXEL_INK);
    }
}
