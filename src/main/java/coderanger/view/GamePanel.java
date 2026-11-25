package coderanger.view;

import coderanger.audio.SoundEffect;
import coderanger.audio.SoundEngine;
import coderanger.manager.GameManager;
import coderanger.model.LaserEffect;
import coderanger.model.Monster;
import coderanger.model.ShockwaveEffect;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import coderanger.view.pixel.PixelParticleSystem;
import coderanger.view.pixel.PixelSpriteRenderer;
import coderanger.view.pixel.PixelTheme;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.List;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GamePanel extends JPanel {
    private final GameManager manager;
    private final ResourceManager resources;
    private final SoundEngine soundEngine;
    private final PixelSpriteRenderer renderer;
    private final PixelParticleSystem particles;

    private double victoryAnimTime = 0.0;
    private double animClock = 0.0;
    private double castFlashRemaining = 0.0;
    private double hurtFlashRemaining = 0.0;

    // Observed read-only counters for non-intrusive visual/audio triggers
    private long seenStrikes;
    private long seenMisses;
    private long seenKills;
    private long seenShockwaves;
    private long seenBonusBuffs;
    private long seenShieldBreaks;
    private long seenHpLosses;
    private long seenBossBreaks;
    private long seenBossSkills;
    private long seenBossAppears;
    private int seenWave = 1;
    private int seenStreakTier = 0;

    public GamePanel(GameManager manager, ResourceManager resources) {
        this(manager, resources, new SoundEngine(true));
    }

    public GamePanel(GameManager manager, ResourceManager resources, SoundEngine soundEngine) {
        this.manager = manager;
        this.resources = resources != null ? resources : new ResourceManager();
        this.soundEngine = soundEngine;
        this.renderer = new PixelSpriteRenderer(this.resources, this.soundEngine);
        this.particles = new PixelParticleSystem();
        setBackground(PixelTheme.PIXEL_INK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleLogicalClick(e.getX(), e.getY());
            }
        });
    }

    private void handleLogicalClick(int rawX, int rawY) {
        int panelW = Math.max(1, getWidth());
        int panelH = Math.max(1, getHeight());
        double scale = Math.min(
                (double) panelW / GameConstants.WIDTH,
                (double) panelH / GameConstants.HEIGHT);
        int xOffset = (int) ((panelW - GameConstants.WIDTH * scale) / 2);
        int yOffset = (int) ((panelH - GameConstants.HEIGHT * scale) / 2);

        int lx = (int) Math.round((rawX - xOffset) / scale);
        int ly = (int) Math.round((rawY - yOffset) / scale);
        if (PixelSpriteRenderer.MUTE_BUTTON_BOUNDS.contains(lx, ly) && soundEngine != null) {
            soundEngine.toggleMute();
            if (!soundEngine.isMuted()) {
                soundEngine.play(SoundEffect.UI_CLICK);
            }
            repaint();
        }
    }

    public void resetVisualTracking() {
        particles.clear();
        animClock = 0.0;
        castFlashRemaining = 0.0;
        hurtFlashRemaining = 0.0;
        seenStrikes = manager.getStrikeEvents();
        seenMisses = manager.getMissEvents();
        seenKills = manager.getKillEvents();
        seenShockwaves = manager.getShockwaveEvents();
        seenBonusBuffs = manager.getBonusBuffEvents();
        seenShieldBreaks = manager.getShieldBreakEvents();
        seenHpLosses = manager.getHpLostEvents();
        seenBossBreaks = manager.getBossWordBreakEvents();
        seenBossSkills = manager.getBossSkillEvents();
        seenBossAppears = manager.getBossAppearEvents();
        seenWave = manager.getCurrentWave();
        seenStreakTier = 0;
    }

    public void tickVisuals(double dt) {
        if (!Double.isFinite(dt) || dt < 0.0) {
            return;
        }
        animClock += dt;
        castFlashRemaining = Math.max(0.0, castFlashRemaining - dt);
        hurtFlashRemaining = Math.max(0.0, hurtFlashRemaining - dt);
        particles.update(dt);

        if (manager.getState() == GameManager.GameState.VICTORY_ANIMATION) {
            particles.spawnVictoryConfetti();
        }

        int streak = manager.getStats().getStreak();
        boolean fever = streak >= 10;

        if (manager.getStrikeEvents() > seenStrikes) {
            seenStrikes = manager.getStrikeEvents();
            castFlashRemaining = 0.22;
            if (soundEngine != null) {
                soundEngine.playStrike(streak);
            }
            List<LaserEffect> lasers = manager.getEffects();
            if (!lasers.isEmpty()) {
                LaserEffect latest = lasers.get(lasers.size() - 1);
                particles.spawnStrikeSparks(latest.getEndX(), latest.getEndY(), fever);
            }
        }

        if (manager.getMissEvents() > seenMisses) {
            seenMisses = manager.getMissEvents();
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.MISS);
            }
            particles.spawnFloatingText(
                    GameConstants.PLAYER_X, GameConstants.PLAYER_Y - 46,
                    "TRƯỢT PHÍM!", PixelTheme.ALERT_RED);
        }

        if (manager.getShockwaveEvents() > seenShockwaves) {
            seenShockwaves = manager.getShockwaveEvents();
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.SHOCKWAVE_ULT);
            }
            particles.triggerShake(0.20, 5.0);
            List<ShockwaveEffect> sws = manager.getShockwaves();
            if (!sws.isEmpty()) {
                ShockwaveEffect latest = sws.get(sws.size() - 1);
                particles.spawnKillBurst(
                        latest.getTargetX(), latest.getTargetY(),
                        PixelTheme.CYAN_BRIGHT, PixelTheme.HUST_GOLD);
                particles.spawnFloatingText(
                        latest.getOriginX(), latest.getOriginY() - 18,
                        "► SHOCKWAVE! +75", PixelTheme.CYAN_BRIGHT);
            }
        } else if (manager.getKillEvents() > seenKills) {
            seenKills = manager.getKillEvents();
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.ENEMY_KILL);
            }
            List<LaserEffect> lasers = manager.getEffects();
            if (!lasers.isEmpty()) {
                LaserEffect latest = lasers.get(lasers.size() - 1);
                particles.spawnKillBurst(
                        latest.getEndX(), latest.getEndY(),
                        PixelTheme.HUST_GOLD, PixelTheme.ALERT_RED);
            }
        } else {
            seenKills = manager.getKillEvents();
        }

        if (manager.getBonusBuffEvents() > seenBonusBuffs) {
            seenBonusBuffs = manager.getBonusBuffEvents();
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.BONUS_BUFF);
            }
            particles.spawnFloatingText(
                    GameConstants.PLAYER_X, GameConstants.PLAYER_Y - 52,
                    "♦ TRÀ ĐÁ BÁCH KHOA! +BUFF", PixelTheme.TEA_GREEN);
        }

        if (manager.getBossWordBreakEvents() > seenBossBreaks) {
            seenBossBreaks = manager.getBossWordBreakEvents();
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.BOSS_BREAK);
            }
            particles.triggerShake(0.16, 4.0);
            particles.spawnKillBurst(
                    GameConstants.BOSS_CENTER_X, GameConstants.BOSS_CENTER_Y,
                    PixelTheme.HUST_GOLD, PixelTheme.CYAN_BRIGHT);
            particles.spawnFloatingText(
                    GameConstants.BOSS_CENTER_X, GameConstants.BOSS_CENTER_Y - 40,
                    "PHÁ ĐỀ! +100", PixelTheme.HUST_GOLD);
        }

        if (manager.getBossAppearEvents() > seenBossAppears) {
            seenBossAppears = manager.getBossAppearEvents();
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.BOSS_WARNING);
            }
            particles.triggerShake(0.30, 6.0);
            particles.spawnFloatingText(
                    GameConstants.WIDTH / 2.0, 360,
                    "« KÌ 8: BẢO VỆ ĐỒ ÁN OOP! »", PixelTheme.HUST_GOLD);
        }

        if (manager.getBossSkillEvents() > seenBossSkills) {
            seenBossSkills = manager.getBossSkillEvents();
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.BOSS_SKILL);
            }
            particles.triggerShake(0.28, 7.0);
            particles.spawnFloatingText(
                    GameConstants.BOSS_CENTER_X, 332,
                    "« HẾT GIỜ! TRỪ TÍN CHỈ! »", PixelTheme.ALERT_RED);
        }

        if (manager.getHpLostEvents() > seenHpLosses) {
            seenHpLosses = manager.getHpLostEvents();
            seenShieldBreaks = manager.getShieldBreakEvents();
            hurtFlashRemaining = 0.32;
            particles.triggerShake(0.24, 6.0);
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.PLAYER_HURT);
            }
            particles.spawnFloatingText(
                    GameConstants.PLAYER_X, GameConstants.PLAYER_Y - 48,
                    "-1 HP!", PixelTheme.ALERT_RED);
        } else if (manager.getShieldBreakEvents() > seenShieldBreaks) {
            seenShieldBreaks = manager.getShieldBreakEvents();
            hurtFlashRemaining = 0.22;
            particles.triggerShake(0.16, 4.0);
            if (soundEngine != null) {
                soundEngine.play(SoundEffect.SHIELD_BREAK);
            }
            particles.spawnFloatingText(
                    GameConstants.PLAYER_X, GameConstants.PLAYER_Y - 48,
                    "VỠ GIÁP!", PixelTheme.CYAN_BRIGHT);
        }

        int currentWave = manager.getCurrentWave();
        if (currentWave > seenWave && currentWave < GameConstants.BOSS_WAVE) {
            seenWave = currentWave;
            particles.spawnFloatingText(
                    GameConstants.WIDTH / 2.0, 150,
                    "♦ BƯỚC SANG HỌC KÌ " + currentWave + "/8 ♦", PixelTheme.HUST_GOLD);
        }

        int currentTier = manager.getDifficulty().streakTier(streak);
        if (currentTier > seenStreakTier) {
            particles.spawnFloatingText(
                    GameConstants.WIDTH / 2.0, 185,
                    "► " + manager.getDifficulty().streakLabel(streak) + " — QUÁI TĂNG TỐC!",
                    PixelTheme.HUST_GOLD);
        }
        seenStreakTier = currentTier;
    }

    public void setVictoryAnimTime(double time) {
        this.victoryAnimTime = time;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            PixelTheme.enableCrispPixel(g2d);

            int panelW = getWidth();
            int panelH = getHeight();
            g2d.setColor(PixelTheme.PIXEL_INK);
            g2d.fillRect(0, 0, panelW, panelH);

            double scale = Math.min(
                    (double) panelW / GameConstants.WIDTH,
                    (double) panelH / GameConstants.HEIGHT);
            int xOffset = (int) ((panelW - GameConstants.WIDTH * scale) / 2);
            int yOffset = (int) ((panelH - GameConstants.HEIGHT * scale) / 2);

            g2d.translate(xOffset, yOffset);
            g2d.scale(scale, scale);
            g2d.clipRect(0, 0, GameConstants.WIDTH, GameConstants.HEIGHT);

            GameManager.GameState state = manager.getState();
            if (state == GameManager.GameState.VICTORY_ANIMATION) {
                renderer.renderVictoryPortal(g2d, victoryAnimTime);
                particles.render(g2d, resources.getPixelFont(13f));
                return;
            }

            List<Monster> monsters = manager.getMonsters();
            int streak = manager.getStats().getStreak();
            Monster locked = manager.getTargetManager().getLockedTarget();

            AffineTransform preShake = g2d.getTransform();
            g2d.translate(particles.currentShakeX(), particles.currentShakeY());

            renderer.renderArenaBackground(g2d, animClock, streak, monsters);
            renderer.renderSanbakaPlayer(
                    g2d,
                    manager.getPlayer(),
                    animClock,
                    castFlashRemaining,
                    hurtFlashRemaining,
                    streak
            );

            for (Monster m : monsters) {
                if (m != locked) {
                    renderer.renderMonster(g2d, m, false, animClock);
                }
            }
            // Draw locked monster on top so its target reticle & word pill are never occluded!
            if (locked != null && locked.isAlive()) {
                renderer.renderMonster(g2d, locked, true, animClock);
            }

            boolean fever = streak >= 10;
            for (LaserEffect effect : manager.getEffects()) {
                renderer.renderPixelLaser(g2d, effect, fever);
            }
            for (ShockwaveEffect sw : manager.getShockwaves()) {
                renderer.renderShockwave(g2d, sw);
            }

            particles.render(g2d, resources.getPixelFont(13f));
            g2d.setTransform(preShake);

            renderer.renderHud(g2d, manager, animClock);

            if (state == GameManager.GameState.PAUSED) {
                renderer.renderPauseOverlay(g2d);
            }
        } finally {
            g2d.dispose();
        }
    }
}
