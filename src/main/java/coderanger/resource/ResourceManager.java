package coderanger.resource;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

public class ResourceManager {
    private final Map<String, Optional<BufferedImage>> imageCache = new ConcurrentHashMap<>();
    private final Map<Integer, Font> pixelFontCache = new ConcurrentHashMap<>();
    private final Font basePixelFont;

    public ResourceManager() {
        this.basePixelFont = loadBundledFont("/fonts/pixel-mono.ttf");
    }

    public Optional<BufferedImage> getImage(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalizeKey(name);
        return imageCache.computeIfAbsent(normalized, this::loadFromClasspath);
    }

    public Font getPixelFont(int style, float size) {
        int cacheKey = ((style & 0x3) << 16) | Math.round(size * 10f);
        return pixelFontCache.computeIfAbsent(cacheKey, k -> basePixelFont.deriveFont(style, size));
    }

    public Font getPixelFont(float size) {
        return getPixelFont(Font.BOLD, size);
    }

    private String normalizeKey(String name) {
        String clean = name.trim();
        if (clean.startsWith("/")) {
            clean = clean.substring(1);
        }
        if (!clean.startsWith("sprites/") && !clean.contains("/")) {
            clean = "sprites/" + clean;
        }
        if (!clean.endsWith(".png")) {
            clean = clean + ".png";
        }
        return "/" + clean;
    }

    private Optional<BufferedImage> loadFromClasspath(String resourcePath) {
        try (InputStream stream = ResourceManager.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return Optional.empty();
            }
            BufferedImage img = ImageIO.read(stream);
            return Optional.ofNullable(img);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Font loadBundledFont(String path) {
        try (InputStream stream = ResourceManager.class.getResourceAsStream(path)) {
            if (stream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, stream);
            }
        } catch (FontFormatException | IOException ignored) {
            // Fall back to system Monospaced font safely
        }
        return new Font(Font.MONOSPACED, Font.BOLD, 16);
    }
}
