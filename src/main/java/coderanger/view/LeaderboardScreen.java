package coderanger.view;

import coderanger.manager.GameRecord;
import coderanger.manager.HistoryStore;
import coderanger.util.GameConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Màn Leaderboard (mục 13, 17): top 10 score và 10 trận gần nhất, cảnh báo
 * nhỏ nếu có dòng lịch sử hỏng. Fields private: tables (cùng store và
 * warning phục vụ refresh). Constructor: (HistoryStore, Runnable onBack).
 */
@SuppressWarnings("serial")
public class LeaderboardScreen extends JPanel {

    private final HistoryStore store;
    /** tables[0]=top 10 theo score, tables[1]=10 trận gần nhất. */
    private final JTable[] tables;
    private final JLabel warning;

    public LeaderboardScreen(HistoryStore store, Runnable onBack) {
        this.store = Objects.requireNonNull(store, "store");
        this.tables = new JTable[] {
                createTable(new String[] {"#", "Name", "Result", "Score", "Time", "Acc"}),
                createTable(new String[] {"Date", "Name", "Result", "Score", "Time", "Acc"})
        };
        this.warning = new JLabel(" ");
        warning.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        warning.setForeground(new Color(255, 200, 90));
        warning.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton back = new JButton("Back");
        back.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        back.setForeground(Color.WHITE);
        back.setBackground(new Color(24, 52, 90));
        back.setFocusPainted(false);
        back.setBorder(BorderFactory.createLineBorder(new Color(0, 230, 160), 2));
        back.setPreferredSize(new Dimension(240, 52));
        back.setMaximumSize(new Dimension(240, 52));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> onBack.run());

        setBackground(new Color(8, 14, 34));
        setPreferredSize(new Dimension((int) GameConstants.WIDTH, (int) GameConstants.HEIGHT));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        add(heading("LEADERBOARD", 34));
        add(Box.createVerticalStrut(16));
        add(heading("Top 10 scores", 18));
        add(Box.createVerticalStrut(6));
        add(wrap(tables[0]));
        add(Box.createVerticalStrut(18));
        add(heading("Recent 10 games", 18));
        add(Box.createVerticalStrut(6));
        add(wrap(tables[1]));
        add(Box.createVerticalStrut(10));
        add(warning);
        add(Box.createVerticalGlue());
        add(back);

        refresh();
    }

    /** Đọc lại store và cập nhật hai bảng cùng cảnh báo. */
    public void refresh() {
        fill(tables[0], store.top10(), true);
        List<GameRecord> recent = store.readRecent();
        fill(tables[1], recent.subList(0, Math.min(10, recent.size())), false);
        int bad = store.countCorruptLines();
        warning.setText(bad > 0
                ? "Warning: " + bad + " damaged history line(s) were skipped" : " ");
    }

    // ------------------------------------------------------------ private

    private void fill(JTable table, List<GameRecord> records, boolean ranked) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        DateTimeFormatter date = DateTimeFormatter.ofPattern("MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());
        int rank = 1;
        for (GameRecord r : records) {
            model.addRow(new Object[] {
                    ranked ? String.valueOf(rank++) : date.format(r.timestamp()),
                    r.name(),
                    r.outcome(),
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
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(20, 30, 55));
        table.setGridColor(new Color(40, 60, 100));
        table.setFillsViewportHeight(true);
        table.setRowSelectionAllowed(false);
        int[] widths = {100, 130, 60, 80, 60, 70};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(24, 52, 90));
        table.getTableHeader().setForeground(new Color(0, 230, 160));
        return table;
    }

    private JScrollPane wrap(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(new Color(0, 230, 160), 1));
        pane.getViewport().setBackground(new Color(20, 30, 55));
        pane.setPreferredSize(new Dimension(500, 24 * 10 + 28));
        pane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24 * 10 + 28));
        pane.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pane;
    }

    private JLabel heading(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
        l.setForeground(new Color(0, 230, 160));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
}