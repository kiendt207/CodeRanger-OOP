package coderanger.view;

import coderanger.audio.SoundEffect;
import coderanger.audio.SoundEngine;
import coderanger.manager.GameRecord;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import coderanger.view.pixel.PixelButton;
import coderanger.view.pixel.PixelTheme;
import coderanger.view.pixel.SanbakaQuotes;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Results screen modeled directly after docs/sanbaka_demo.jpeg:
 * - Crimson halftone backdrop + double-framed Exam Cream dialog card
 * - Sanbaka KO drool ("ぽかーん…") or Graduation Win ("クリア！") portrait
 * - Tilted "KỶ LỤC MỚI!" stamp badge on personal best
 * - Semester-specific OOP (IT3100) quote speech bubble + GPA rank
 * - 8-Semester Graduation Track + "One More Run!" Near-Miss hook + instant [ENTER] retry
 */
@SuppressWarnings("serial")
public class ResultsScreen extends JPanel {
    private final ResourceManager resources;
    private final SoundEngine soundEngine;
    private final JButton[] buttons; // [0]=Restart, [1]=Menu

    private GameRecord currentRecord;
    private boolean newHighScore;
    private long bestScore;
    private String selectedQuote = "";
    private String nearMissHook = "";
    private SanbakaQuotes.GradeEvaluation gradeEval;
    private String saveErrorText = "";

    public ResultsScreen(Runnable onRestart, Runnable onMenu) {
        this(onRestart, onMenu, new ResourceManager(), new SoundEngine(true));
    }

    public ResultsScreen(
            Runnable onRestart,
            Runnable onMenu,
            ResourceManager resources,
            SoundEngine soundEngine) {
        this.resources = resources != null ? resources : new ResourceManager();
        this.soundEngine = soundEngine;

        Font btnFont = this.resources.getPixelFont(15f);
        buttons = new JButton[] {
            new PixelButton("ĐĂNG KÝ HỌC LẠI [ENTER]", btnFont, true),
            new PixelButton("VỀ CỔNG PARABOL [ESC]", btnFont, false)
        };
        for (JButton b : buttons) {
            b.setPreferredSize(new Dimension(320, 44));
            b.setMaximumSize(new Dimension(340, 44));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        Runnable restartWithSfx = () -> {
            if (this.soundEngine != null) {
                this.soundEngine.play(SoundEffect.UI_CLICK);
            }
            onRestart.run();
        };
        Runnable menuWithSfx = () -> {
            if (this.soundEngine != null) {
                this.soundEngine.play(SoundEffect.UI_CLICK);
            }
            onMenu.run();
        };

        buttons[0].addActionListener(e -> restartWithSfx.run());
        buttons[1].addActionListener(e -> menuWithSfx.run());

        // Zero-friction instant keyboard retry ([ENTER] / [SPACE]) & menu ([ESCAPE])!
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "instantRetry");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "instantRetry");
        getActionMap().put("instantRetry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing()) {
                    restartWithSfx.run();
                }
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "backToMenu");
        getActionMap().put("backToMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing()) {
                    menuWithSfx.run();
                }
            }
        });

        setOpaque(false);
        setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());
        add(buttons[0]);
        add(Box.createVerticalStrut(10));
        add(buttons[1]);
        add(Box.createVerticalStrut(28));
    }

    public void showResult(GameRecord record) {
        showResult(record, false, record != null ? record.score() : 0L, 0);
    }

    public void showResult(
            GameRecord record,
            boolean isNewHighScore,
            long previousBestScore,
            int lastBossHp) {
        this.currentRecord = record;
        this.newHighScore = isNewHighScore;
        this.bestScore = Math.max(previousBestScore, record.score());
        this.saveErrorText = "";

        boolean win = "WIN".equalsIgnoreCase(record.outcome());
        this.selectedQuote = SanbakaQuotes.pickQuote(win, record.wave());
        this.gradeEval = SanbakaQuotes.evaluateGrade(record);
        this.nearMissHook = SanbakaQuotes.buildNearMissHook(record, previousBestScore, lastBossHp);

        buttons[0].setText(win ? "CHƠI LẠI SĂN ĐIỂM A+ [ENTER]" : "ĐĂNG KÝ HỌC LẠI [ENTER]");
        repaint();
    }

    public void showSaveError(String message) {
        this.saveErrorText = (message == null || message.isBlank())
                ? "Không lưu được lịch sử phiên chơi"
                : ("Lỗi lưu lịch sử: " + message);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            PixelTheme.enableCrispPixel(g);
            PixelTheme.drawCrimsonHalftoneBackground(g, getWidth(), getHeight());

            if (currentRecord == null) {
                return;
            }

            int cx = getWidth() / 2;
            boolean win = "WIN".equalsIgnoreCase(currentRecord.outcome());

            // Top header like sanbaka_demo.jpeg
            PixelTheme.drawChunkyPixelBanner(
                    g,
                    resources.getPixelFont(14f),
                    "SANBAKA CODE",
                    cx,
                    26,
                    3,
                    PixelTheme.HUST_GOLD,
                    PixelTheme.PIXEL_INK
            );
            g.setFont(resources.getPixelFont(12f));
            BufferedImage hyakka = resources.getImage("badge_hyakka").orElse(null);
            String topSub = " • BÁCH KHOA SURVIVAL • BẢNG ĐIỂM HỌC KÌ";
            FontMetrics tfm = g.getFontMetrics();
            int pillW = hyakka != null ? hyakka.getWidth() : 0;
            int totalTopW = pillW + tfm.stringWidth(topSub);
            int topX = cx - totalTopW / 2;
            if (hyakka != null) {
                g.drawImage(hyakka, topX, 76, null);
            }
            PixelTheme.drawShadowString(
                    g, topSub, topX + pillW, 92, Color.WHITE, PixelTheme.PIXEL_INK);

            // Main Exam Dialog Card (y = 106..746)
            int cardW = Math.min(504, getWidth() - 24);
            int cardX = cx - cardW / 2;
            int cardY = 106;
            int cardH = 640;
            PixelTheme.drawExamDialogCard(g, cardX, cardY, cardW, cardH);

            // 1. Portrait Box (Left) + Headline & OOP Speech Bubble (Right) — exact sanbaka_demo.jpeg layout!
            BufferedImage portrait = resources.getImage(win ? "portrait_win" : "portrait_ko").orElse(null);
            BufferedImage jpCap = resources.getImage(win ? "jp_clear" : "jp_pokaan").orElse(null);
            PixelTheme.drawPortraitBox(
                    g,
                    portrait,
                    jpCap,
                    win ? "CLEAR!" : "TOANG...",
                    resources.getPixelFont(11f),
                    cardX + 26,
                    cardY + 34,
                    118,
                    152
            );

            int rightX = cardX + 156;
            int rightW = cardW - 182;

            PixelTheme.drawChunkyPixelBanner(
                    g,
                    resources.getPixelFont(13f),
                    win ? "QUA MÔN RỒI!" : "TOANG RỒI!",
                    rightX + 96,
                    cardY + 32,
                    2,
                    win ? new Color(22, 163, 74) : PixelTheme.HUST_CRIMSON,
                    PixelTheme.PIXEL_INK
            );
            BufferedImage jpSub = resources.getImage(win ? "jp_clear" : "jp_gameover").orElse(null);
            if (jpSub != null) {
                g.drawImage(jpSub, rightX + (win ? 198 : 178), cardY + 43, null);
            }

            // Speech bubble with wrapped OOP quote
            PixelTheme.drawSpeechBox(g, rightX, cardY + 68, rightW, 68, true);
            g.setFont(resources.getPixelFont(11f));
            g.setColor(PixelTheme.PIXEL_INK);
            List<String> wrapped = wrapText(g.getFontMetrics(), selectedQuote, rightW - 18);
            for (int i = 0; i < Math.min(3, wrapped.size()); i++) {
                g.drawString(wrapped.get(i), rightX + 9, cardY + 86 + i * 17);
            }

            // Summary line: ĐRL • KỶ LỤC • KÌ + XẾP LOẠI GPA
            g.setFont(resources.getPixelFont(12f));
            g.setColor(PixelTheme.PIXEL_INK);
            String summaryLine = String.format(Locale.ROOT,
                    "ĐRL: %d  •  KỶ LỤC: %d  •  KÌ %d/8",
                    currentRecord.score(), bestScore, currentRecord.wave());
            g.drawString(summaryLine, rightX, cardY + 156);

            if (gradeEval != null) {
                g.setFont(resources.getPixelFont(13f));
                g.setColor(gradeEval.badgeColor());
                g.drawString(
                        "XẾP LOẠI [" + gradeEval.letterGrade() + "]: " + gradeEval.rankTitle(),
                        rightX,
                        cardY + 176);

                g.setFont(resources.getPixelFont(11f));
                g.setColor(new Color(95, 75, 80));
                g.drawString("\"" + gradeEval.subtitle() + "\"", cardX + 28, cardY + 206);
            }

            // Tilted "KỶ LỤC MỚI!" stamp badge at top-right of card (exact sanbaka_demo.jpeg!)
            if (newHighScore) {
                PixelTheme.drawTiltedRecordStamp(
                        g,
                        resources.getPixelFont(12f),
                        "KỶ LỤC MỚI!",
                        cardX + cardW - 68,
                        cardY + 34
                );
            }

            // 2. 8-Semester Graduation Progress Track (K1 -> K8)
            int trackY = cardY + 220;
            g.setColor(PixelTheme.EXAM_CREAM_SHADE);
            g.fillRect(cardX + 26, trackY, cardW - 52, 46);
            g.setColor(PixelTheme.PIXEL_INK);
            g.drawRect(cardX + 26, trackY, cardW - 53, 45);

            g.setFont(resources.getPixelFont(11f));
            g.drawString("TIẾN ĐỘ 8 HỌC KÌ BÁCH KHOA (SV: " + currentRecord.name() + "):",
                    cardX + 34, trackY + 15);

            int nodeStartX = cardX + 44;
            int nodeStep = (cardW - 88) / 7;
            g.setColor(PixelTheme.PIXEL_INK);
            g.fillRect(nodeStartX, trackY + 29, nodeStep * 7, 3);

            for (int k = 1; k <= 8; k++) {
                int nx = nodeStartX + (k - 1) * nodeStep;
                boolean passed = win || k < currentRecord.wave();
                boolean current = !win && k == currentRecord.wave();
                Color nodeColor = passed
                        ? new Color(22, 163, 74)
                        : (current ? PixelTheme.ALERT_RED : new Color(180, 170, 165));
                g.setColor(PixelTheme.PIXEL_INK);
                g.fillRect(nx - 9, trackY + 21, 18, 18);
                g.setColor(nodeColor);
                g.fillRect(nx - 7, trackY + 23, 14, 14);
                g.setColor(Color.WHITE);
                g.setFont(resources.getPixelFont(10f));
                String kl = k == 8 ? "B8" : ("K" + k);
                g.drawString(kl, nx - g.getFontMetrics().stringWidth(kl) / 2, trackY + 34);
            }

            // 3. Detailed 12+ Gameplay & Typing Statistics Grid (2 crisp columns)
            int gridY = cardY + 276;
            int gridH = 264;
            g.setColor(Color.WHITE);
            g.fillRect(cardX + 26, gridY, cardW - 52, gridH);
            g.setColor(PixelTheme.PIXEL_INK);
            g.drawRect(cardX + 26, gridY, cardW - 53, gridH - 1);

            String[][] leftStats = {
                {"Điểm rèn luyện:", String.valueOf(currentRecord.score())},
                {"Học kì đạt tới:", "Kì " + currentRecord.wave() + " / 8"},
                {"HP / Giáp cuối:", currentRecord.hp() + "/5  •  " + currentRecord.shield() + "/3"},
                {"Thời gian trụ:", String.format(Locale.ROOT, "%.1f giây", currentRecord.seconds())},
                {"Quái đã diệt:", String.valueOf(currentRecord.kills())},
                {"Quick kills (≤1.5s):", String.valueOf(currentRecord.quick())}
            };
            String[][] rightStats = {
                {"Tốc độ gõ (WPM):", String.format(Locale.ROOT, "%.1f WPM", currentRecord.wpm())},
                {"Độ chính xác:", String.format(Locale.ROOT, "%.1f%%", currentRecord.accuracy())},
                {"Combo cao nhất:", String.valueOf(currentRecord.peakStreak())},
                {"Phím Đúng / Sai:", currentRecord.correct() + " / " + currentRecord.wrong()},
                {"Trà đá Bonus:", String.valueOf(currentRecord.bonus())},
                {"Từ Boss Đồ Án:", currentRecord.bossWords() + " / 9"}
            };

            int colW = (cardW - 64) / 2;
            int leftColX = cardX + 34;
            int rightColX = cardX + 36 + colW;
            g.setColor(new Color(225, 215, 200));
            g.fillRect(cardX + cardW / 2, gridY + 8, 2, gridH - 16);

            g.setFont(resources.getPixelFont(12f));
            for (int r = 0; r < leftStats.length; r++) {
                int ry = gridY + 28 + r * 41;
                if (r > 0) {
                    g.setColor(new Color(242, 236, 225));
                    g.fillRect(cardX + 30, ry - 26, cardW - 60, 1);
                }
                drawStatPair(g, leftColX, colW - 12, ry, leftStats[r][0], leftStats[r][1]);
                drawStatPair(g, rightColX, colW - 12, ry, rightStats[r][0], rightStats[r][1]);
            }

            // 4. "One More Run!" Near-Miss Hook Banner at bottom of card!
            int hookY = cardY + 550;
            g.setColor(PixelTheme.PIXEL_INK);
            g.fillRect(cardX + 26, hookY, cardW - 52, 62);
            g.setColor(win ? new Color(22, 101, 52) : new Color(58, 18, 28));
            g.fillRect(cardX + 28, hookY + 2, cardW - 56, 58);
            g.setColor(PixelTheme.HUST_GOLD);
            g.drawRect(cardX + 28, hookY + 2, cardW - 57, 57);

            g.setFont(resources.getPixelFont(11f));
            List<String> hookLines = wrapText(g.getFontMetrics(), nearMissHook, cardW - 72);
            g.setColor(PixelTheme.HUST_GOLD);
            for (int i = 0; i < Math.min(2, hookLines.size()); i++) {
                g.drawString(hookLines.get(i), cardX + 36, hookY + 20 + i * 16);
            }
            if (gradeEval != null) {
                g.setColor(PixelTheme.EXAM_CREAM);
                g.drawString("► " + gradeEval.nextRankHint(), cardX + 36, hookY + 52);
            }

            if (!saveErrorText.isEmpty()) {
                g.setFont(resources.getPixelFont(11f));
                g.setColor(PixelTheme.ALERT_RED);
                g.drawString(saveErrorText, cardX + 30, cardY + cardH - 8);
            }
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }

    private static void drawStatPair(
            Graphics2D g,
            int x,
            int width,
            int y,
            String label,
            String value) {
        g.setColor(new Color(90, 75, 85));
        g.drawString(label, x, y);
        g.setColor(PixelTheme.HUST_CRIMSON_DARK);
        int vw = g.getFontMetrics().stringWidth(value);
        g.drawString(value, x + width - vw, y);
    }

    private static List<String> wrapText(FontMetrics fm, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String w : words) {
            if (current.isEmpty()) {
                current.append(w);
            } else if (fm.stringWidth(current + " " + w) <= maxWidth) {
                current.append(' ').append(w);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(w);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }
}
