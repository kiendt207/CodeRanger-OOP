package coderanger.view.pixel;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HUST x Sanbaka retro pixel-art color palette and drawing helpers
 * modeled directly after docs/sanbaka_demo.jpeg.
 */
public final class PixelTheme {
    public static final Color HUST_CRIMSON = new Color(183, 28, 28);
    public static final Color HUST_CRIMSON_DARK = new Color(136, 18, 24);
    public static final Color HUST_CRIMSON_DOT = new Color(204, 42, 42);
    public static final Color HUST_CRIMSON_DEEP = new Color(74, 14, 22);

    public static final Color EXAM_CREAM = new Color(255, 252, 242);
    public static final Color EXAM_CREAM_SHADE = new Color(245, 236, 218);

    public static final Color PIXEL_INK = new Color(26, 16, 20);
    public static final Color CARD_CAVITY = new Color(46, 18, 26);

    public static final Color HUST_GOLD = new Color(255, 213, 79);
    public static final Color HUST_GOLD_BRIGHT = new Color(255, 235, 130);
    public static final Color HUST_GOLD_DARK = new Color(194, 110, 12);

    public static final Color SANBAKA_PINK = new Color(252, 228, 236);
    public static final Color SANBAKA_PINK_DOT = new Color(248, 187, 208);

    public static final Color ARENA_NIGHT = new Color(22, 11, 20);
    public static final Color ARENA_GRID = new Color(46, 22, 38);
    public static final Color HUD_BG = new Color(34, 14, 24);

    public static final Color CYAN_SHIELD = new Color(56, 189, 248);
    public static final Color CYAN_BRIGHT = new Color(165, 243, 252);
    public static final Color TEA_GREEN = new Color(163, 230, 53);
    public static final Color ALERT_RED = new Color(255, 77, 90);
    public static final Color TEXT_MUTED = new Color(138, 122, 132);

    private static final Map<String, BufferedImage> BANNER_CACHE = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            return size() > 64;
        }
    };

    private PixelTheme() {}

    public static void enableCrispPixel(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    /**
     * Draws the iconic HUST crimson polka-dot halftone background from sanbaka_demo.jpeg.
     */
    public static void drawCrimsonHalftoneBackground(Graphics2D g, int w, int h) {
        g.setColor(HUST_CRIMSON);
        g.fillRect(0, 0, w, h);

        // Diagonal subtle stripes + halftone pixel dots
        g.setColor(HUST_CRIMSON_DARK);
        for (int y = 0; y < h; y += 24) {
            g.fillRect(0, y, w, 2);
        }

        g.setColor(HUST_CRIMSON_DOT);
        int spacing = 18;
        for (int row = 0, y = 8; y < h; row++, y += spacing) {
            int xShift = (row % 2 == 0) ? 8 : 17;
            for (int x = xShift; x < w; x += spacing) {
                g.fillRect(x, y, 4, 4);
            }
        }

        // Top & bottom crisp white-gold retro framing bars
        g.setColor(EXAM_CREAM);
        g.fillRect(32, 12, Math.max(0, w - 64), 4);
        g.setColor(HUST_GOLD_DARK);
        g.fillRect(32, h - 16, Math.max(0, w - 64), 4);
    }

    /**
     * Draws the double-bordered retro dialog card (outer ink + gold border + dark cavity + cream paper)
     * matching sanbaka_demo.jpeg.
     */
    public static void drawExamDialogCard(Graphics2D g, int x, int y, int w, int h) {
        // Hard pixel drop shadow
        g.setColor(new Color(12, 6, 10, 170));
        g.fillRect(x + 6, y + 6, w, h);

        // Outer ink border
        g.setColor(PIXEL_INK);
        g.fillRect(x, y, w, h);

        // Bright HUST gold frame
        g.setColor(HUST_GOLD);
        g.fillRect(x + 3, y + 3, w - 6, h - 6);

        // Dark burgundy inner cavity
        g.setColor(CARD_CAVITY);
        g.fillRect(x + 6, y + 6, w - 12, h - 12);

        // Inner ink outline + warm exam cream sheet
        g.setColor(PIXEL_INK);
        g.fillRect(x + 12, y + 12, w - 24, h - 24);
        g.setColor(EXAM_CREAM);
        g.fillRect(x + 15, y + 15, w - 30, h - 30);

        // Crisp crimson top & bottom accent hairlines on the paper
        g.setColor(HUST_CRIMSON);
        g.fillRect(x + 22, y + 20, w - 44, 2);
        g.fillRect(x + 22, y + h - 22, w - 44, 2);
    }

    /**
     * Draws a simpler cream or dark pixel sub-panel with crisp 2px ink border.
     */
    public static void drawSpeechBox(Graphics2D g, int x, int y, int w, int h, boolean withLeftTail) {
        g.setColor(PIXEL_INK);
        g.fillRect(x + 3, y + 3, w, h);
        g.setColor(PIXEL_INK);
        g.fillRect(x, y, w, h);
        g.setColor(Color.WHITE);
        g.fillRect(x + 2, y + 2, w - 4, h - 4);

        if (withLeftTail) {
            int ty = y + 14;
            g.setColor(PIXEL_INK);
            g.fillRect(x - 7, ty, 7, 8);
            g.setColor(Color.WHITE);
            g.fillRect(x - 5, ty + 2, 7, 4);
        }
    }

    /**
     * Draws Sanbaka's sakura-pink halftone portrait frame with caption underneath.
     */
    public static void drawPortraitBox(
            Graphics2D g,
            BufferedImage portrait,
            String caption,
            Font captionFont,
            int x,
            int y,
            int w,
            int h) {
        drawPortraitBox(g, portrait, null, caption, captionFont, x, y, w, h);
    }

    public static void drawPortraitBox(
            Graphics2D g,
            BufferedImage portrait,
            BufferedImage captionSprite,
            String caption,
            Font captionFont,
            int x,
            int y,
            int w,
            int h) {
        g.setColor(PIXEL_INK);
        g.fillRect(x, y, w, h);
        g.setColor(SANBAKA_PINK);
        g.fillRect(x + 3, y + 3, w - 6, h - 6);

        // Halftone pink dots in lower half
        g.setColor(SANBAKA_PINK_DOT);
        for (int py = y + h / 2; py < y + h - 4; py += 6) {
            for (int px = x + 5 + ((py / 6) % 2) * 3; px < x + w - 5; px += 6) {
                g.fillRect(px, py, 2, 2);
            }
        }

        boolean hasFooter = captionSprite != null || (caption != null && !caption.isEmpty());
        if (portrait != null) {
            int maxSpriteSize = Math.min(w - 10, h - (hasFooter ? 24 : 8));
            int scale = Math.max(1, maxSpriteSize / portrait.getWidth());
            int sw = portrait.getWidth() * scale;
            int sh = portrait.getHeight() * scale;
            int sx = x + (w - sw) / 2;
            int sy = y + 4 + Math.max(0, (h - (hasFooter ? 22 : 6) - sh) / 2);
            g.drawImage(portrait, sx, sy, sw, sh, null);
        }

        if (captionSprite != null) {
            int sx = x + (w - captionSprite.getWidth()) / 2;
            int sy = y + h - captionSprite.getHeight() - 5;
            g.drawImage(captionSprite, sx, sy, null);
        } else if (caption != null && !caption.isEmpty() && captionFont != null) {
            g.setFont(captionFont);
            g.setColor(PIXEL_INK);
            FontMetrics fm = g.getFontMetrics();
            int tx = x + (w - fm.stringWidth(caption)) / 2;
            g.drawString(caption, tx, y + h - 6);
        }
    }

    /**
     * Renders text onto a low-res bitmap without anti-aliasing and scales it up by integer
     * nearest-neighbor so Vietnamese & Japanese headlines look authentically 8-bit pixelated!
     */
    public static void drawChunkyPixelBanner(
            Graphics2D g,
            Font smallFont,
            String text,
            int centerX,
            int topY,
            int scale,
            Color fill,
            Color shadow) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String key = smallFont.getFontName() + "|" + smallFont.getSize() + "|" + scale + "|"
                + fill.getRGB() + "|" + shadow.getRGB() + "|" + text;
        BufferedImage cached = BANNER_CACHE.computeIfAbsent(key, k -> {
            BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D pg = probe.createGraphics();
            pg.setFont(smallFont);
            FontMetrics fm = pg.getFontMetrics();
            int tw = Math.max(4, fm.stringWidth(text) + 6);
            int th = Math.max(4, fm.getAscent() + fm.getDescent() + 4);
            pg.dispose();

            BufferedImage bmp = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg = bmp.createGraphics();
            enableCrispPixel(bg);
            bg.setFont(smallFont);
            int baseline = fm.getAscent() + 1;
            bg.setColor(shadow);
            bg.drawString(text, 3, baseline + 1);
            bg.drawString(text, 2, baseline + 1);
            bg.setColor(fill);
            bg.drawString(text, 1, baseline);
            bg.dispose();
            return bmp;
        });

        int dw = cached.getWidth() * scale;
        int dh = cached.getHeight() * scale;
        enableCrispPixel(g);
        g.drawImage(cached, centerX - dw / 2, topY, dw, dh, null);
    }

    public static void drawShadowString(Graphics2D g, String text, int x, int y, Color fg, Color shadow) {
        g.setColor(shadow);
        g.drawString(text, x + 2, y + 2);
        g.setColor(fg);
        g.drawString(text, x, y);
    }

    public static void drawTiltedRecordStamp(Graphics2D g, Font font, String label, int cx, int cy) {
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.rotate(Math.toRadians(11));

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int bw = fm.stringWidth(label) + 18;
        int bh = fm.getHeight() + 8;

        g.setColor(PIXEL_INK);
        g.fillRect(-bw / 2 + 2, -bh / 2 + 2, bw, bh);
        g.setColor(HUST_CRIMSON);
        g.fillRect(-bw / 2, -bh / 2, bw, bh);
        g.setColor(HUST_GOLD);
        g.fillRect(-bw / 2 + 2, -bh / 2 + 2, bw - 4, bh - 4);
        g.setColor(HUST_CRIMSON);
        g.drawRect(-bw / 2 + 4, -bh / 2 + 4, bw - 9, bh - 9);
        g.drawString(label, -fm.stringWidth(label) / 2, fm.getAscent() - bh / 2 + 4);

        g.setTransform(old);
    }
}
