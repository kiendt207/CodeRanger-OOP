package coderanger.manager;

import static org.junit.jupiter.api.Assertions.*;

import coderanger.audio.SoundEffect;
import coderanger.audio.SoundEngine;
import coderanger.model.Monster;
import coderanger.model.NormalMonster;
import coderanger.model.ShockwaveEffect;
import coderanger.resource.ResourceManager;
import coderanger.view.GamePanel;
import coderanger.view.LeaderboardScreen;
import coderanger.view.MenuScreen;
import coderanger.view.ResultsScreen;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SanbakaUpgradeTest {

    @Test
    void consecutiveMonsterSpawnsMaintainMinimumEightyPixelSeparation() {
        MonsterFactory factory = new MonsterFactory(2026L);
        Monster previous = factory.createNormal(0, 0.0, false);
        for (int i = 0; i < 40; i++) {
            Monster current = factory.createNormal(i % 4, i * 1.5, false);
            double sep = Math.abs(current.getCenterX() - previous.getCenterX());
            assertTrue(sep >= 80.0 - 1e-6,
                    "Expected >= 80px separation between consecutive spawns, got " + sep);
            assertTrue(current.getCenterX() >= 65.0 && current.getCenterX() <= 475.0);
            previous = current;
        }
    }

    @Test
    void wordProviderLoadsExpandedOopPoolsAndSelectsDistinctBossPhaseWords() {
        WordProvider provider = new WordProvider(2026L);
        assertTrue(provider.getWordPool("normal0").contains("class"));
        assertTrue(provider.getWordPool("normal3").contains("override"));
        assertTrue(provider.getWordPool("boss3").contains("inheritance"));

        for (int phase = 1; phase <= 3; phase++) {
            List<String> selected = provider.phaseWords(phase);
            assertEquals(3, selected.size());
            assertEquals(3, new HashSet<>(selected).size(),
                    "Boss phase words must be distinct within a phase");
        }
    }

    @Test
    void resourceManagerLoadsAllSanbakaSpritesAndPixelFont() {
        ResourceManager resources = new ResourceManager();
        String[] expectedSprites = {
            "sanbaka_idle_0", "sanbaka_idle_1", "sanbaka_cast_0", "sanbaka_cast_1", "sanbaka_hurt",
            "portrait_happy", "portrait_focus", "portrait_sweat", "portrait_panic",
            "portrait_ko", "portrait_win", "portrait_tea",
            "enemy_bug_0", "enemy_deadline_0", "enemy_skull_0", "enemy_minion_0", "enemy_bonus_0",
            "boss_p1_0", "boss_p2_0", "boss_p3_0", "bg_parabol",
            "icon_heart_full", "icon_shield", "icon_speaker_on"
        };
        for (String key : expectedSprites) {
            assertTrue(resources.getImage(key).isPresent(), "Missing sprite: " + key);
        }
        assertTrue(resources.getImage("nonexistent_sprite").isEmpty());
        assertNotNull(resources.getPixelFont(16f));
    }

    @Test
    void soundEngineSynthesizesAllActionEffectsSafelyInMutedMode() {
        SoundEngine sound = new SoundEngine(true);
        assertTrue(sound.isMuted());
        for (SoundEffect effect : SoundEffect.values()) {
            assertTrue(sound.getPcmSample(effect).length > 100,
                    "Expected synthesized PCM data for " + effect);
            assertDoesNotThrow(() -> sound.play(effect));
        }
        assertDoesNotThrow(() -> sound.playStrike(35));
    }

    @Test
    void shockwaveEffectExpandsForPointThreeFiveSecondsAndRendersCleanly() {
        ShockwaveEffect sw = new ShockwaveEffect(200, 300, 280, 420);
        assertTrue(sw.isAlive());
        assertEquals(0.35, sw.getDuration());

        BufferedImage canvas = new BufferedImage(540, 900, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            sw.update(0.15);
            assertTrue(sw.isAlive());
            assertDoesNotThrow(() -> sw.render(g));
            sw.update(0.20);
            assertFalse(sw.isAlive());
        } finally {
            g.dispose();
        }
    }

    @Test
    void allPixelScreensRenderWithoutException(@TempDir Path tempDir) {
        ResourceManager resources = new ResourceManager();
        SoundEngine sound = new SoundEngine(true);
        HistoryStore store = new HistoryStore(tempDir.resolve("history.tsv"));
        GameManager manager = new GameManager(store, new MonsterFactory(2026L));

        MenuScreen menu = new MenuScreen(name -> { }, () -> { }, resources, sound);
        menu.setSize(540, 900);

        manager.start("KienHUST");
        manager.update(1.0);
        manager.addMonster(new NormalMonster(200, 260, "class", 1.0, 99, 70, false));
        manager.handleCharacter('c');

        GamePanel panel = new GamePanel(manager, resources, sound);
        panel.setSize(540, 900);
        panel.tickVisuals(0.016);

        ResultsScreen results = new ResultsScreen(() -> { }, () -> { }, resources, sound);
        results.setSize(540, 900);
        GameRecord sampleRecord = manager.getStats().snapshot(
                "KienHUST", 142.5, "LOSS", 5, manager.getPlayer());
        results.showResult(sampleRecord, true, 900L, 0);

        LeaderboardScreen leaderboard = new LeaderboardScreen(store, () -> { }, resources, sound);
        leaderboard.setSize(540, 900);

        BufferedImage img = new BufferedImage(540, 900, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            assertDoesNotThrow(() -> menu.paint(g));
            assertDoesNotThrow(() -> panel.paint(g));
            manager.togglePause();
            assertDoesNotThrow(() -> panel.paint(g));
            assertDoesNotThrow(() -> results.paint(g));
            assertDoesNotThrow(() -> leaderboard.paint(g));
        } finally {
            g.dispose();
        }
    }
}
