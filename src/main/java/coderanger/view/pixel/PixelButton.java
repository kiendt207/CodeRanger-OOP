package coderanger.view.pixel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Chunky retro pixel-art button with hard 3D offset shadow and hover indicator.
 */
@SuppressWarnings("serial")
public class PixelButton extends JButton {
    private final Color baseBg;
    private final Color hoverBg;
    private final Color textFg;
    private final Color borderAccent;
    private boolean hovered;

    public PixelButton(String text, Font font, boolean primary) {
        super(text);
        this.baseBg = primary ? PixelTheme.HUST_GOLD : PixelTheme.EXAM_CREAM;
        this.hoverBg = primary ? PixelTheme.HUST_GOLD_BRIGHT : new Color(255, 243, 205);
        this.textFg = primary ? PixelTheme.PIXEL_INK : PixelTheme.HUST_CRIMSON_DARK;
        this.borderAccent = primary ? PixelTheme.HUST_CRIMSON : PixelTheme.HUST_GOLD_DARK;

        setFont(font);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(280, 46));
        setMaximumSize(new Dimension(300, 46));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            PixelTheme.enableCrispPixel(g);
            int w = getWidth();
            int h = getHeight();
            boolean pressed = getModel().isArmed() && getModel().isPressed();
            int offset = pressed ? 3 : 0;

            // Hard pixel drop shadow
            g.setColor(PixelTheme.PIXEL_INK);
            g.fillRect(4, 4, w - 4, h - 4);

            // Outer ink border
            g.setColor(PixelTheme.PIXEL_INK);
            g.fillRect(offset, offset, w - 4, h - 4);

            // Button face
            g.setColor(hovered ? hoverBg : baseBg);
            g.fillRect(offset + 3, offset + 3, w - 10, h - 10);

            // Top pixel highlight & bottom bevel
            g.setColor(Color.WHITE);
            g.fillRect(offset + 3, offset + 3, w - 10, 2);
            g.setColor(borderAccent);
            g.fillRect(offset + 3, offset + h - 9, w - 10, 2);

            // Label
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            String display = hovered ? ("► " + getText()) : getText();
            int tx = offset + (w - 4 - fm.stringWidth(display)) / 2;
            int ty = offset + (h - 4 - fm.getHeight()) / 2 + fm.getAscent();
            g.setColor(textFg);
            g.drawString(display, tx, ty);
        } finally {
            g.dispose();
        }
    }
}
