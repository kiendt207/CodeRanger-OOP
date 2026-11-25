package coderanger.view.pixel;

import coderanger.audio.SoundEngine;
import coderanger.manager.Difficulty;
import coderanger.manager.GameManager;
import coderanger.model.BossMonster;
import coderanger.model.LaserEffect;
import coderanger.model.Monster;
import coderanger.model.Player;
import coderanger.model.ShockwaveEffect;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Renders the 540x900 logical gameplay arena, HUD, Sanbaka battle animations,
 * pixel monsters, Boss phases, Victory Parabol portal, and Pause Tea Break overlay.
 */
public class PixelSpriteRenderer {
    public static final Rectangle MUTE_BUTTON_BOUNDS = new Rectangle(504, 8, 28, 28);

    private final ResourceManager resources;
    private final SoundEngine soundEngine;

    public PixelSpriteRenderer(ResourceManager resources, SoundEngine soundEngine) {
        this.resources = resources;
        this.soundEngine = soundEngine;
    }

    public void renderArenaBackground(
            Graphics2D g,
            double animClock,
            int streak,
            List<Monster> monsters) {
        PixelTheme.enableCrispPixel(g);

        // Night campus sky
        g.setColor(PixelTheme.ARENA_NIGHT);
        g.fillRect(0, 0, GameConstants.WIDTH, GameConstants.HEIGHT);

        // Subtle pixel perspective grid
        g.setColor(PixelTheme.ARENA_GRID);
        int scrollY = ((int) (animClock * 22.0)) % 36;
        for (int y = 96 + scrollY; y < GameConstants.DANGER_Y; y += 36) {
            g.fillRect(0, y, GameConstants.WIDTH, 1);
        }
        for (int x = 30; x < GameConstants.WIDTH; x += 60) {
            g.fillRect(x, 96, 1, GameConstants.DANGER_Y - 96);
        }

        // Twinkling pixel stars / binary code dust
        for (int i = 0; i < 28; i++) {
            int sx = (i * 73 + 19) % (GameConstants.WIDTH - 16) + 8;
            int sy = 104 + ((i * 97 + (int) (animClock * (14 + (i % 5) * 4))) % (GameConstants.DANGER_Y - 130));
            boolean blink = ((int) (animClock * 4.0) + i) % 5 == 0;
            g.setColor(blink ? PixelTheme.HUST_GOLD : new Color(120, 90, 115));
            g.fillRect(sx, sy, 2, 2);
        }

        // Overdrive side energy rails when streak >= 10!
        if (streak >= 10) {
            Color rail = streak >= 30 ? PixelTheme.HUST_GOLD : PixelTheme.HUST_CRIMSON_DOT;
            int pulse = ((int) (animClock * 12)) % 2 == 0 ? 150 : 85;
            g.setColor(new Color(rail.getRed(), rail.getGreen(), rail.getBlue(), pulse));
            g.fillRect(0, 96, 5, GameConstants.HEIGHT - 96);
            g.fillRect(GameConstants.WIDTH - 5, 96, 5, GameConstants.HEIGHT - 96);
        }

        // HUST Parabol Gate & Library silhouette above Danger Line
        Optional<BufferedImage> parabol = resources.getImage("bg_parabol");
        if (parabol.isPresent()) {
            BufferedImage bg = parabol.get();
            g.drawImage(bg, 0, GameConstants.DANGER_Y - 160, 540, 160, null);
        }

        // Player bunker floor zone (y = 762..900)
        g.setColor(new Color(32, 14, 24));
        g.fillRect(0, GameConstants.DANGER_Y + 2, GameConstants.WIDTH, GameConstants.HEIGHT - GameConstants.DANGER_Y);

        // Danger Line (y = 760) with caution stripes & proximity warning!
        boolean dangerClose = monsters.stream()
                .anyMatch(m -> m.isAlive() && !m.isBoss() && m.getY() >= GameConstants.DANGER_Y - 130);
        boolean flash = dangerClose && (((int) (animClock * 10.0)) % 2 == 0);

        g.setColor(PixelTheme.PIXEL_INK);
        g.fillRect(0, GameConstants.DANGER_Y - 3, GameConstants.WIDTH, 7);
        for (int x = 0; x < GameConstants.WIDTH; x += 24) {
            g.setColor(flash ? PixelTheme.ALERT_RED : PixelTheme.HUST_GOLD);
            g.fillRect(x, GameConstants.DANGER_Y - 2, 12, 5);
            g.setColor(flash ? Color.WHITE : PixelTheme.HUST_CRIMSON_DARK);
            g.fillRect(x + 12, GameConstants.DANGER_Y - 2, 12, 5);
        }

        if (dangerClose) {
            g.setFont(resources.getPixelFont(12f));
            String warn = "« CẢNH BÁO HỌC VỤ: DEADLINE SÁT VẠCH! »";
            FontMetrics fm = g.getFontMetrics();
            int wx = (GameConstants.WIDTH - fm.stringWidth(warn)) / 2;
            PixelTheme.drawShadowString(
                    g, warn, wx, GameConstants.DANGER_Y - 8,
                    flash ? Color.WHITE : PixelTheme.ALERT_RED, PixelTheme.PIXEL_INK);
        }
    }

    public void renderSanbakaPlayer(
            Graphics2D g,
            Player player,
            double animClock,
            double castFlashRemaining,
            double hurtFlashRemaining,
            int streak) {
        if (player == null || !player.isAlive()) {
            return;
        }
        int cx = GameConstants.PLAYER_X;
        int cy = GameConstants.PLAYER_Y;

        // Fever / Overdrive golden floor aura
        if (streak >= 10) {
            int r = 38 + ((int) (animClock * 10) % 2) * 4;
            g.setColor(new Color(255, 213, 79, streak >= 30 ? 95 : 55));
            g.fillOval(cx - r, cy - r + 8, r * 2, r * 2 - 12);
        }

        // Shield layers (1..3 pixel barrier rings)
        int shields = player.getShieldLayers();
        Stroke oldStroke = g.getStroke();
        for (int s = 1; s <= shields; s++) {
            int radius = 33 + s * 5;
            g.setColor(s == shields ? PixelTheme.CYAN_BRIGHT : PixelTheme.CYAN_SHIELD);
            g.setStroke(new BasicStroke(2.0f));
            g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

            // Orbiting pixel shield nodes
            double angle = animClock * (1.8 + s * 0.5) + s * 2.1;
            int nx = cx + (int) Math.round(Math.cos(angle) * radius);
            int ny = cy + (int) Math.round(Math.sin(angle) * radius);
            g.setColor(PixelTheme.HUST_GOLD);
            g.fillRect(nx - 2, ny - 2, 5, 5);
        }
        g.setStroke(oldStroke);

        // Choose Sanbaka battle sprite (32x32 -> drawn 2x = 64x64)
        int frame = ((int) (animClock * 5.0)) % 2;
        String spriteKey;
        if (hurtFlashRemaining > 0.0) {
            spriteKey = "sanbaka_hurt";
        } else if (castFlashRemaining > 0.0) {
            spriteKey = "sanbaka_cast_" + frame;
        } else {
            spriteKey = "sanbaka_idle_" + frame;
        }

        Optional<BufferedImage> sprite = resources.getImage(spriteKey);
        if (sprite.isPresent()) {
            g.drawImage(sprite.get(), cx - 32, cy - 32, 64, 64, null);
        } else {
            player.render(g);
        }

        // Small HUST nameplate under Sanbaka
        g.setFont(resources.getPixelFont(11f));
        String tag = streak >= 20 ? "♦ SANBAKA OVERDRIVE ♦" : "SANBAKA • BÁCH KHOA";
        FontMetrics fm = g.getFontMetrics();
        Color tagColor = streak >= 20 ? PixelTheme.HUST_GOLD : PixelTheme.EXAM_CREAM;
        PixelTheme.drawShadowString(
                g, tag, cx - fm.stringWidth(tag) / 2, cy + 44, tagColor, PixelTheme.PIXEL_INK);
    }

    public void renderMonster(
            Graphics2D g,
            Monster monster,
            boolean locked,
            double animClock) {
        if (monster == null || !monster.isAlive()) {
            return;
        }
        if (monster instanceof BossMonster boss) {
            renderBoss(g, boss, locked, animClock);
            return;
        }

        int frame = ((int) (animClock * 6.0 + monster.getSpawnSequence())) % 2;
        String spriteKey;
        if (monster.isBonus()) {
            spriteKey = "enemy_bonus_" + frame;
        } else if (monster.isMinion()) {
            spriteKey = "enemy_minion_" + frame;
        } else {
            int len = monster.getFullWord().length();
            if (len <= 4) {
                spriteKey = "enemy_bug_" + frame;
            } else if (len <= 6) {
                spriteKey = "enemy_deadline_" + frame;
            } else {
                spriteKey = "enemy_skull_" + frame;
            }
        }

        int cx = (int) Math.round(monster.getCenterX());
        int cy = (int) Math.round(monster.getCenterY());

        Optional<BufferedImage> sprite = resources.getImage(spriteKey);
        if (sprite.isPresent()) {
            BufferedImage img = sprite.get();
            int dw = img.getWidth() * 2;
            int dh = img.getHeight() * 2;
            g.drawImage(img, cx - dw / 2, cy - dh / 2, dw, dh, null);
        } else {
            monster.render(g);
        }

        if (locked) {
            drawTargetReticle(g, cx, cy, monster.getWidth() / 2 + 8, monster.getHeight() / 2 + 8, animClock);
        }

        drawWordPill(g, monster.getFullWord(), monster.getTypedCharacters(),
                cx, (int) Math.round(monster.getY()) - 8, locked, monster.isBonus());
    }

    private void renderBoss(
            Graphics2D g,
            BossMonster boss,
            boolean locked,
            double animClock) {
        int phase = Math.max(1, Math.min(3, boss.getPhase()));
        int frame = ((int) (animClock * (4.0 + phase * 1.5))) % 2;
        int cx = (int) Math.round(boss.getCenterX());
        int cy = (int) Math.round(boss.getCenterY());

        Optional<BufferedImage> sprite = resources.getImage("boss_p" + phase + "_" + frame);
        if (sprite.isPresent()) {
            BufferedImage img = sprite.get();
            int dw = img.getWidth() * 2; // 160
            int dh = img.getHeight() * 2; // 96
            g.drawImage(img, cx - dw / 2, cy - dh / 2, dw, dh, null);
        } else {
            boss.render(g);
        }

        if (locked) {
            drawTargetReticle(g, cx, cy, 86, 52, animClock);
        }

        // Boss overhead status card (Phase + 9-segment HP bar + Countdown bar)
        int barW = 240;
        int barX = cx - barW / 2;
        int barY = 104;

        g.setColor(PixelTheme.PIXEL_INK);
        g.fillRect(barX - 2, barY - 2, barW + 4, 38);
        g.setColor(PixelTheme.HUD_BG);
        g.fillRect(barX, barY, barW, 34);
        g.setColor(phase == 3 ? PixelTheme.HUST_GOLD : PixelTheme.HUST_CRIMSON_DOT);
        g.drawRect(barX, barY, barW - 1, 33);

        String phaseTitle = switch (phase) {
            case 1 -> "KÌ 8 • P1: GIỮA KÌ OOP";
            case 2 -> "KÌ 8 • P2: ĐỒ ÁN NHÓM";
            default -> "KÌ 8 • P3: HỘI ĐỒNG CUỐI KÌ!";
        };
        g.setFont(resources.getPixelFont(11f));
        g.setColor(PixelTheme.HUST_GOLD);
        g.drawString(phaseTitle, barX + 6, barY + 12);

        String timerStr = String.format(Locale.ROOT, "%.1fs", boss.getCountdown());
        boolean urgent = boss.getCountdown() <= 3.5;
        g.setColor(urgent ? PixelTheme.ALERT_RED : PixelTheme.CYAN_BRIGHT);
        g.drawString(timerStr, barX + barW - g.getFontMetrics().stringWidth(timerStr) - 6, barY + 12);

        // 9 segmented HP blocks
        int segGap = 2;
        int totalTrackW = barW - 12;
        int segW = (totalTrackW - segGap * 8) / 9;
        for (int i = 0; i < BossMonster.MAX_HP; i++) {
            int sx = barX + 6 + i * (segW + segGap);
            g.setColor(i < boss.getHp() ? PixelTheme.ALERT_RED : new Color(55, 30, 40));
            g.fillRect(sx, barY + 16, segW, 8);
            if (i < boss.getHp()) {
                g.setColor(new Color(255, 160, 160));
                g.fillRect(sx, barY + 16, segW, 2);
            }
        }

        // Countdown timer bar under HP
        double ratio = Math.max(0.0, Math.min(1.0, boss.getCountdown() / boss.getPhaseDuration()));
        g.setColor(new Color(40, 25, 35));
        g.fillRect(barX + 6, barY + 27, totalTrackW, 4);
        g.setColor(urgent ? PixelTheme.ALERT_RED : PixelTheme.CYAN_SHIELD);
        g.fillRect(barX + 6, barY + 27, (int) Math.round(totalTrackW * ratio), 4);

        // Large prominent Boss word pill right below Boss chassis!
        drawBossWordPill(g, boss.getCurrentWord(), boss.getCurrentWordIndex(), cx, cy + 64, locked);
    }

    private void drawTargetReticle(Graphics2D g, int cx, int cy, int rx, int ry, double animClock) {
        int pulse = ((int) (animClock * 10.0)) % 2 == 0 ? 0 : 2;
        int x0 = cx - rx - pulse;
        int y0 = cy - ry - pulse;
        int x1 = cx + rx + pulse;
        int y1 = cy + ry + pulse;
        int arm = 8;

        g.setColor(PixelTheme.HUST_GOLD);
        // Top-left bracket
        g.fillRect(x0, y0, arm, 3);
        g.fillRect(x0, y0, 3, arm);
        // Top-right bracket
        g.fillRect(x1 - arm + 1, y0, arm, 3);
        g.fillRect(x1 - 2, y0, 3, arm);
        // Bottom-left bracket
        g.fillRect(x0, y1 - 2, arm, 3);
        g.fillRect(x0, y1 - arm + 1, 3, arm);
        // Bottom-right bracket
        g.fillRect(x1 - arm + 1, y1 - 2, arm, 3);
        g.fillRect(x1 - 2, y1 - arm + 1, 3, arm);
    }

    private void drawWordPill(
            Graphics2D g,
            String word,
            int typed,
            int cx,
            int baselineY,
            boolean locked,
            boolean bonus) {
        g.setFont(resources.getPixelFont(15f));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(word);
        int pillW = tw + 12;
        int pillH = fm.getHeight() + 4;
        int px = Math.max(4, Math.min(GameConstants.WIDTH - pillW - 4, cx - pillW / 2));
        int py = baselineY - fm.getAscent() - 2;

        g.setColor(PixelTheme.PIXEL_INK);
        g.fillRect(px, py, pillW, pillH);

        Color border = locked
                ? PixelTheme.HUST_GOLD
                : (bonus ? PixelTheme.CYAN_SHIELD : new Color(95, 60, 80));
        g.setColor(border);
        g.drawRect(px, py, pillW - 1, pillH - 1);

        int textX = px + 6;
        int textY = py + fm.getAscent() + 2;
        drawSplitTypedWord(g, fm, word, typed, textX, textY, locked);
    }

    private void drawBossWordPill(
            Graphics2D g,
            String word,
            int typed,
            int cx,
            int baselineY,
            boolean locked) {
        g.setFont(resources.getPixelFont(19f));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(word);
        int pillW = tw + 20;
        int pillH = fm.getHeight() + 8;
        int px = cx - pillW / 2;
        int py = baselineY - fm.getAscent() - 4;

        g.setColor(PixelTheme.PIXEL_INK);
        g.fillRect(px + 2, py + 2, pillW, pillH);
        g.setColor(new Color(38, 12, 26));
        g.fillRect(px, py, pillW, pillH);
        g.setColor(locked ? PixelTheme.HUST_GOLD : PixelTheme.ALERT_RED);
        g.drawRect(px, py, pillW - 1, pillH - 1);
        g.drawRect(px + 1, py + 1, pillW - 3, pillH - 3);

        drawSplitTypedWord(g, fm, word, typed, px + 10, py + fm.getAscent() + 4, true);
    }

    private static void drawSplitTypedWord(
            Graphics2D g,
            FontMetrics fm,
            String word,
            int typed,
            int x,
            int y,
            boolean highlightNext) {
        int clampedTyped = Math.max(0, Math.min(word.length(), typed));
        int cursorX = x;

        if (clampedTyped > 0) {
            String done = word.substring(0, clampedTyped);
            g.setColor(new Color(120, 140, 155));
            g.drawString(done, cursorX, y);
            cursorX += fm.stringWidth(done);
        }
        if (clampedTyped < word.length()) {
            if (highlightNext) {
                String nextCh = word.substring(clampedTyped, clampedTyped + 1);
                g.setColor(PixelTheme.HUST_GOLD);
                g.drawString(nextCh, cursorX, y);
                cursorX += fm.stringWidth(nextCh);
                if (clampedTyped + 1 < word.length()) {
                    g.setColor(Color.WHITE);
                    g.drawString(word.substring(clampedTyped + 1), cursorX, y);
                }
            } else {
                g.setColor(Color.WHITE);
                g.drawString(word.substring(clampedTyped), cursorX, y);
            }
        }
    }

    public void renderPixelLaser(Graphics2D g, LaserEffect laser, boolean fever) {
        if (laser == null || !laser.isAlive()) {
            return;
        }
        Stroke oldStroke = g.getStroke();
        int x1 = (int) Math.round(laser.getStartX());
        int y1 = (int) Math.round(laser.getStartY());
        int x2 = (int) Math.round(laser.getEndX());
        int y2 = (int) Math.round(laser.getEndY());

        Color outer = fever ? new Color(255, 213, 79, 175) : new Color(56, 189, 248, 165);
        g.setColor(outer);
        g.setStroke(new BasicStroke(6.0f));
        g.drawLine(x1, y1, x2, y2);

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.0f));
        g.drawLine(x1, y1, x2, y2);

        // Pixel muzzle & hit cross-sparks
        g.setColor(PixelTheme.HUST_GOLD);
        g.fillRect(x1 - 5, y1 - 5, 10, 10);
        g.fillRect(x2 - 6, y2 - 2, 12, 4);
        g.fillRect(x2 - 2, y2 - 6, 4, 12);
        g.setStroke(oldStroke);
    }

    public void renderShockwave(Graphics2D g, ShockwaveEffect sw) {
        if (sw == null || !sw.isAlive()) {
            return;
        }
        sw.render(g);
    }

    public void renderHud(Graphics2D g, GameManager manager, double animClock) {
        // Top HUD frame (y = 0..95)
        g.setColor(PixelTheme.HUD_BG);
        g.fillRect(0, 0, GameConstants.WIDTH, 94);
        g.setColor(PixelTheme.HUST_GOLD);
        g.fillRect(0, 92, GameConstants.WIDTH, 2);
        g.setColor(PixelTheme.PIXEL_INK);
        g.fillRect(0, 94, GameConstants.WIDTH, 2);

        Player player = manager.getPlayer();
        int hp = player != null ? player.getHp() : 0;
        int shields = player != null ? player.getShieldLayers() : 0;
        int streak = manager.getStats().getStreak();
        int wave = manager.getCurrentWave();
        Difficulty diff = manager.getDifficulty();

        // 1. Live Sanbaka Reactive Portrait Box (x=8, y=6, 78x82)
        String portraitKey;
        String caption;
        BufferedImage jpCap = null;
        if (hp <= 1) {
            portraitKey = "portrait_panic";
            caption = "CỨU BÉ!!";
        } else if (wave >= GameConstants.BOSS_WAVE || hp <= 2) {
            portraitKey = "portrait_sweat";
            caption = "CĂNG QUÁ";
        } else if (streak >= 10) {
            portraitKey = "portrait_focus";
            caption = "OVERDRIVE";
        } else {
            portraitKey = "portrait_happy";
            caption = "SANBAKA";
            jpCap = resources.getImage("jp_yaruzo").orElse(null);
        }
        PixelTheme.drawPortraitBox(
                g,
                resources.getImage(portraitKey).orElse(null),
                jpCap,
                caption,
                resources.getPixelFont(10f),
                8, 6, 78, 82
        );

        // 2. HP Hearts & Shield Icons (x=96..255)
        g.setFont(resources.getPixelFont(12f));
        g.setColor(PixelTheme.EXAM_CREAM);
        g.drawString("TÍN CHỈ SINH TỒN", 96, 20);

        BufferedImage heartFull = resources.getImage("icon_heart_full").orElse(null);
        BufferedImage heartEmpty = resources.getImage("icon_heart_empty").orElse(null);
        for (int i = 0; i < 5; i++) {
            BufferedImage hImg = i < hp ? heartFull : heartEmpty;
            int hx = 96 + i * 24;
            if (hImg != null) {
                g.drawImage(hImg, hx, 25, 20, 20, null);
            } else {
                g.setColor(i < hp ? PixelTheme.ALERT_RED : Color.DARK_GRAY);
                g.fillRect(hx, 25, 18, 18);
            }
        }

        BufferedImage shieldImg = resources.getImage("icon_shield").orElse(null);
        g.setColor(PixelTheme.CYAN_BRIGHT);
        g.drawString("GIÁP:", 96, 63);
        for (int i = 0; i < 3; i++) {
            int sx = 140 + i * 22;
            if (i < shields && shieldImg != null) {
                g.drawImage(shieldImg, sx, 49, 18, 18, null);
            } else {
                g.setColor(i < shields ? PixelTheme.CYAN_SHIELD : new Color(60, 45, 55));
                g.fillRect(sx + 2, 52, 14, 14);
            }
        }

        // Shockwave Ultimate cooldown bar (x=96, y=72..84)
        double swCd = manager.getShockwaveCooldown();
        boolean swReady = swCd <= 0.001;
        g.setFont(resources.getPixelFont(10f));
        g.setColor(swReady ? PixelTheme.TEA_GREEN : PixelTheme.TEXT_MUTED);
        g.drawString(swReady ? "► SHOCKWAVE: READY!" : String.format(Locale.ROOT, "► NẠP CHIÊU: %.1fs", swCd), 96, 83);

        // 3. Center Semester & Clock Badge (x=240..365)
        g.setColor(PixelTheme.PIXEL_INK);
        g.fillRect(236, 10, 126, 74);
        g.setColor(wave >= GameConstants.BOSS_WAVE ? PixelTheme.HUST_CRIMSON_DARK : new Color(48, 22, 36));
        g.fillRect(238, 12, 122, 70);
        g.setColor(PixelTheme.HUST_GOLD);
        g.drawRect(238, 12, 121, 69);

        g.setFont(resources.getPixelFont(14f));
        String waveText = wave >= GameConstants.BOSS_WAVE
                ? "KÌ 8: ĐỒ ÁN"
                : ("HỌC KÌ " + wave + "/8");
        FontMetrics wfm = g.getFontMetrics();
        g.setColor(PixelTheme.HUST_GOLD);
        g.drawString(waveText, 299 - wfm.stringWidth(waveText) / 2, 30);

        int totalSec = (int) Math.floor(manager.getElapsedSeconds());
        String clockStr = String.format(Locale.ROOT, "%02d:%02d", totalSec / 60, totalSec % 60);
        g.setFont(resources.getPixelFont(18f));
        g.setColor(Color.WHITE);
        FontMetrics cfm = g.getFontMetrics();
        g.drawString(clockStr, 299 - cfm.stringWidth(clockStr) / 2, 53);

        // Semester mini progress bar inside center badge
        double semRatio = wave >= GameConstants.BOSS_WAVE
                ? 1.0
                : (manager.getElapsedSeconds() % GameConstants.WAVE_DURATION) / GameConstants.WAVE_DURATION;
        g.setColor(PixelTheme.PIXEL_INK);
        g.fillRect(248, 63, 102, 8);
        g.setColor(wave >= GameConstants.BOSS_WAVE ? PixelTheme.ALERT_RED : PixelTheme.CYAN_SHIELD);
        g.fillRect(249, 64, (int) Math.round(100 * semRatio), 6);

        // 4. Right Score, Combo & Overdrive Badge (x=372..532)
        g.setFont(resources.getPixelFont(12f));
        g.setColor(PixelTheme.EXAM_CREAM);
        g.drawString("ĐRL:", 372, 24);
        g.setFont(resources.getPixelFont(16f));
        g.setColor(PixelTheme.HUST_GOLD);
        g.drawString(String.valueOf(manager.getStats().getScore()), 408, 25);

        double mult = Math.min(2.0, 1.0 + 0.1 * Math.floor(streak / 5.0));
        g.setFont(resources.getPixelFont(12f));
        g.setColor(streak >= 10 ? PixelTheme.CYAN_BRIGHT : Color.WHITE);
        g.drawString(String.format(Locale.ROOT, "COMBO: %d (x%.1f)", streak, mult), 372, 48);

        String odLabel = diff.streakLabel(streak);
        if (!odLabel.isEmpty()) {
            boolean blink = ((int) (animClock * 8.0)) % 2 == 0;
            g.setColor(blink ? PixelTheme.HUST_GOLD : PixelTheme.ALERT_RED);
            g.fillRect(372, 58, 126, 20);
            g.setColor(PixelTheme.PIXEL_INK);
            g.drawRect(372, 58, 125, 19);
            g.setFont(resources.getPixelFont(11f));
            g.drawString("► " + odLabel, 378, 72);
        } else {
            g.setFont(resources.getPixelFont(10f));
            g.setColor(PixelTheme.TEXT_MUTED);
            g.drawString("[ESC] Nghỉ trà đá", 372, 72);
        }

        // 5. Clickable Speaker Mute button at top-right (504, 8, 28, 28)
        g.setColor(PixelTheme.PIXEL_INK);
        g.fillRect(MUTE_BUTTON_BOUNDS.x, MUTE_BUTTON_BOUNDS.y, MUTE_BUTTON_BOUNDS.width, MUTE_BUTTON_BOUNDS.height);
        g.setColor(new Color(58, 26, 40));
        g.fillRect(MUTE_BUTTON_BOUNDS.x + 2, MUTE_BUTTON_BOUNDS.y + 2,
                MUTE_BUTTON_BOUNDS.width - 4, MUTE_BUTTON_BOUNDS.height - 4);
        String spKey = (soundEngine != null && soundEngine.isMuted()) ? "icon_speaker_off" : "icon_speaker_on";
        resources.getImage(spKey).ifPresent(img ->
                g.drawImage(img, MUTE_BUTTON_BOUNDS.x + 3, MUTE_BUTTON_BOUNDS.y + 3, 22, 22, null));
    }

    public void renderVictoryPortal(Graphics2D g, double victoryAnimTime) {
        double progress = Math.min(1.0, Math.max(0.0, victoryAnimTime / 2.5));
        PixelTheme.enableCrispPixel(g);

        g.setColor(PixelTheme.ARENA_NIGHT);
        g.fillRect(0, 0, GameConstants.WIDTH, GameConstants.HEIGHT);

        resources.getImage("bg_parabol").ifPresent(bg ->
                g.drawImage(bg, 0, 210, 540, 160, null));

        // Concentric Parabol graduation portal rings at (270, 320)
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(3.0f));
        for (int i = 0; i < 5; i++) {
            double r = 18 + (115 - 18) * progress * (1.0 - i * 0.12);
            if (r > 2) {
                int alpha = Math.max(30, (int) (245 * (1.0 - progress * 0.75)));
                Color c = (i % 2 == 0)
                        ? new Color(255, 213, 79, alpha)
                        : new Color(56, 189, 248, alpha);
                g.setColor(c);
                int ir = (int) Math.round(r);
                g.drawOval(GameConstants.PLAYER_X - ir, 320 - ir, ir * 2, ir * 2);
            }
        }
        g.setStroke(oldStroke);

        // Sanbaka ascending from (270, 820) -> (270, 350), shrinking to 10%
        int sy = (int) Math.round(GameConstants.PLAYER_Y - progress * (GameConstants.PLAYER_Y - 350));
        double scale = Math.max(0.10, 1.0 - 0.90 * progress);
        int drawSize = Math.max(8, (int) Math.round(64 * scale));

        Optional<BufferedImage> winSprite = resources.getImage("sanbaka_cast_0");
        if (winSprite.isPresent()) {
            g.drawImage(winSprite.get(),
                    GameConstants.PLAYER_X - drawSize / 2,
                    sy - drawSize / 2,
                    drawSize,
                    drawSize,
                    null);
        }

        PixelTheme.drawChunkyPixelBanner(
                g,
                resources.getPixelFont(14f),
                "BẢO VỆ THÀNH CÔNG!",
                GameConstants.WIDTH / 2,
                110,
                2,
                PixelTheme.HUST_GOLD,
                PixelTheme.PIXEL_INK
        );
        g.setFont(resources.getPixelFont(15f));
        String sub = "Sanbaka tiến qua Cổng Parabol tốt nghiệp...";
        FontMetrics fm = g.getFontMetrics();
        PixelTheme.drawShadowString(g, sub,
                (GameConstants.WIDTH - fm.stringWidth(sub)) / 2, 168,
                PixelTheme.EXAM_CREAM, PixelTheme.PIXEL_INK);
    }

    public void renderPauseOverlay(Graphics2D g) {
        g.setColor(new Color(14, 6, 12, 185));
        g.fillRect(0, 96, GameConstants.WIDTH, GameConstants.HEIGHT - 96);

        int cw = 440;
        int ch = 220;
        int cx = (GameConstants.WIDTH - cw) / 2;
        int cy = 330;

        PixelTheme.drawExamDialogCard(g, cx, cy, cw, ch);
        PixelTheme.drawPortraitBox(
                g,
                resources.getImage("portrait_tea").orElse(null),
                resources.getImage("jp_ochatime").orElse(null),
                "TRÀ ĐÁ",
                resources.getPixelFont(11f),
                cx + 28,
                cy + 34,
                108,
                150
        );

        PixelTheme.drawChunkyPixelBanner(
                g,
                resources.getPixelFont(14f),
                "NGHỈ TRÀ ĐÁ!",
                cx + 282,
                cy + 38,
                2,
                PixelTheme.HUST_CRIMSON,
                PixelTheme.PIXEL_INK
        );

        PixelTheme.drawSpeechBox(g, cx + 150, cy + 86, 258, 56, true);
        g.setFont(resources.getPixelFont(13f));
        g.setColor(PixelTheme.PIXEL_INK);
        g.drawString("Đồng hồ đang tạm dừng.", cx + 162, cy + 108);
        g.drawString("Uống ngụm trà đá rồi cày tiếp!", cx + 162, cy + 128);

        g.setFont(resources.getPixelFont(13f));
        g.setColor(PixelTheme.HUST_CRIMSON);
        g.drawString("► Nhấn [ESC] quay lại phòng thi", cx + 152, cy + 172);
    }
}
