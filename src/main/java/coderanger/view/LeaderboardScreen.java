package coderanger.view;

import coderanger.audio.SoundEffect;
import coderanger.audio.SoundEngine;
import coderanger.manager.GameRecord;
import coderanger.manager.HistoryStore;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import coderanger.view.pixel.PixelButton;
import coderanger.view.pixel.PixelTheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * HUST x Sanbaka Pixel-Art LeaderboardScreen showing Top 10 High Scores
 * and 10 Most Recent Semesters, plus corrupt history row warnings.
 */
@SuppressWarnings("serial")
public class LeaderboardScreen extends JPanel {
    private final HistoryStore store;
    private final ResourceManager resources;
    private final SoundEngine soundEngine;
    private final JTable[] tables;
    private final JLabel warning;

    public LeaderboardScreen(HistoryStore store, Runnable onBack) {
        this(store, onBack, new ResourceManager(), new SoundEngine(true));
    }

    public LeaderboardScreen(
            HistoryStore store,
            Runnable onBack,
            ResourceManager resources,
            SoundEngine soundEngine) {
        this.store = Objects.requireNonNull(store, "store");
        this.resources = resources != null ? resources : new ResourceManager();
        this.soundEngine = soundEngine;

        this.tables = new JTable[] {
            createTable(new String[] {"#", "Sinh viên", "K.Quả", "ĐRL", "T.Gian", "Chính xác"}),
            createTable(new String[] {"Ngày giờ", "Sinh viên", "K.Quả", "ĐRL", "T.Gian", "Chính xác"})
        };

        this.warning = new JLabel(" ");
        warning.setFont(this.resources.getPixelFont(12f));
        warning.setForeground(PixelTheme.HUST_GOLD);
        warning.setAlignmentX(Component.CENTER_ALIGNMENT);

        Runnable backWithSfx = () -> {
            if (this.soundEngine != null) {
                this.soundEngine.play(SoundEffect.UI_CLICK);
            }
            onBack.run();
        };

        JButton back = new PixelButton("QUAY LẠI MENU [ESC]", this.resources.getPixelFont(15f), true);
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> backWithSfx.run());

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "goBack");
        getActionMap().put("goBack", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing()) {
                    backWithSfx.run();
                }
            }
        });

        setOpaque(false);
        setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(116, 26, 26, 26));

        add(sectionLabel("♦ TOP 10 THỦ KHOA ĐRL ♦"));
        add(Box.createVerticalStrut(6));
        add(wrap(tables[0]));
        add(Box.createVerticalStrut(16));
        add(sectionLabel("► 10 LƯỢT THI GẦN NHẤT"));
        add(Box.createVerticalStrut(6));
        add(wrap(tables[1]));
        add(Box.createVerticalStrut(8));
        add(warning);
        add(Box.createVerticalGlue());
        add(back);

        refresh();
    }

    public void refresh() {
        fill(tables[0], store.top10(), true);
        List<GameRecord> recent = store.readRecent();
        fill(tables[1], recent.subList(0, Math.min(10, recent.size())), false);
        int bad = store.countCorruptLines();
        warning.setText(bad > 0
                ? ("« Cảnh báo: Đã bỏ qua " + bad + " dòng lịch sử bị hỏng »")
                : " ");
    }

    private void fill(JTable table, List<GameRecord> records, boolean ranked) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        DateTimeFormatter date = DateTimeFormatter.ofPattern("MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());
        int rank = 1;
        for (GameRecord r : records) {
            boolean win = "WIN".equalsIgnoreCase(r.outcome());
            model.addRow(new Object[] {
                ranked ? ("#" + rank++) : date.format(Instant.parse(r.timestamp())),
                r.name(),
                win ? "QUA MÔN" : "TOANG",
                r.score(),
                String.format(Locale.ROOT, "%.0fs", r.seconds()),
                String.format(Locale.ROOT, "%.1f%%", r.accuracy())
            });
        }
    }

    private JTable createTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(resources.getPixelFont(12f));
        table.setForeground(PixelTheme.PIXEL_INK);
        table.setBackground(PixelTheme.EXAM_CREAM);
        table.setGridColor(new Color(220, 205, 185));
        table.setFillsViewportHeight(true);
        table.setRowSelectionAllowed(false);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setBackground(row % 2 == 0 ? PixelTheme.EXAM_CREAM : PixelTheme.EXAM_CREAM_SHADE);
                if (col == 2 && value != null) {
                    String s = value.toString();
                    c.setForeground("QUA MÔN".equals(s) ? new Color(22, 163, 74) : PixelTheme.HUST_CRIMSON);
                } else if (col == 3) {
                    c.setForeground(PixelTheme.HUST_CRIMSON_DARK);
                } else {
                    c.setForeground(PixelTheme.PIXEL_INK);
                }
                return c;
            }
        };
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        int[] widths = {88, 125, 76, 75, 58, 68};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(resources.getPixelFont(12f));
        table.getTableHeader().setPreferredSize(new Dimension(480, 26));
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col);
                lbl.setOpaque(true);
                lbl.setFont(resources.getPixelFont(12f));
                lbl.setBackground(PixelTheme.PIXEL_INK);
                lbl.setForeground(PixelTheme.HUST_GOLD);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, PixelTheme.HUST_GOLD));
                return lbl;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        return table;
    }

    private JScrollPane wrap(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setColumnHeaderView(table.getTableHeader());
        pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PixelTheme.PIXEL_INK, 3),
                BorderFactory.createLineBorder(PixelTheme.HUST_GOLD, 2)));
        pane.getViewport().setBackground(PixelTheme.EXAM_CREAM);
        pane.setPreferredSize(new Dimension(488, 24 * 10 + 32));
        pane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24 * 10 + 32));
        pane.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pane;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(resources.getPixelFont(15f));
        l.setForeground(PixelTheme.HUST_GOLD);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            PixelTheme.enableCrispPixel(g);
            PixelTheme.drawCrimsonHalftoneBackground(g, getWidth(), getHeight());

            int cx = getWidth() / 2;
            PixelTheme.drawChunkyPixelBanner(
                    g,
                    resources.getPixelFont(14f),
                    "BẢNG VÀNG ĐRL",
                    cx,
                    30,
                    3,
                    PixelTheme.HUST_GOLD,
                    PixelTheme.PIXEL_INK
            );
            g.setFont(resources.getPixelFont(12f));
            java.awt.image.BufferedImage hyakka = resources.getImage("badge_hyakka").orElse(null);
            String sub = " • LỊCH SỬ CHIẾN ĐẤU SINH VIÊN BÁCH KHOA";
            int pillW = hyakka != null ? hyakka.getWidth() : 0;
            int totalW = pillW + g.getFontMetrics().stringWidth(sub);
            int sx = cx - totalW / 2;
            if (hyakka != null) {
                g.drawImage(hyakka, sx, 80, null);
            }
            PixelTheme.drawShadowString(g, sub, sx + pillW, 96, Color.WHITE, PixelTheme.PIXEL_INK);
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }
}
