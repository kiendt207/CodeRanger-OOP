package coderanger.model;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BossMonsterTest {

    private List<List<String>> createWords() {
        return new ArrayList<>(List.of(
                new ArrayList<>(List.of("apple", "brain", "cloud")),
                new ArrayList<>(List.of("monster", "keyboard", "program")),
                new ArrayList<>(List.of("adventure", "technology", "controller"))
        ));
    }

    private BossMonster createBoss() {
        return new BossMonster(createWords(), 0, 1);
    }

    private void typeCurrentWord(BossMonster boss) {
        for (char c : boss.getCurrentWord().toCharArray()) {
            assertTrue(boss.checkNextChar(c));
        }
    }

    private void completeCurrentWord(BossMonster boss) {
        typeCurrentWord(boss);
        boss.completeWord();
    }

    // ================= Constructor =================

    @Test
    void constructorShouldInitializeValues() {
        BossMonster boss = createBoss();

        assertEquals(270, boss.getX());
        assertEquals(205, boss.getY());
        assertEquals(150, boss.getWidth());
        assertEquals(90, boss.getHeight());

        assertEquals("apple", boss.getCurrentWord());
        assertEquals("apple", boss.getRemainingWord());

        assertEquals(9, boss.getHp());
        assertEquals(1, boss.getPhase());
        assertEquals(14.0, boss.getCountdown());

        assertTrue(boss.isAlive());
        assertFalse(boss.isCompleted());
    }

    @Test
    void constructorShouldRejectNullWordLists() {
        assertThrows(NullPointerException.class,
                () -> new BossMonster(null, 0, 1));
    }

    @Test
    void constructorShouldRejectWrongNumberOfPhases() {
        List<List<String>> words = new ArrayList<>();
        words.add(List.of("apple", "brain", "cloud"));

        assertThrows(IllegalArgumentException.class,
                () -> new BossMonster(words, 0, 1));
    }

    @Test
    void constructorShouldRejectPhaseWithWrongNumberOfWords() {
        List<List<String>> words = createWords();
        words.set(0, List.of("apple", "brain"));

        assertThrows(IllegalArgumentException.class,
                () -> new BossMonster(words, 0, 1));
    }

    @Test
    void constructorShouldRejectNullPhase() {
        List<List<String>> words = createWords();
        words.set(1, null);

        assertThrows(IllegalArgumentException.class,
                () -> new BossMonster(words, 0, 1));
    }

    @Test
    void constructorShouldRejectWordTooShortForPhaseOne() {
        List<List<String>> words = createWords();
        words.set(0, List.of("abcd", "brain", "cloud"));

        assertThrows(IllegalArgumentException.class,
                () -> new BossMonster(words, 0, 1));
    }

    @Test
    void constructorShouldRejectWordTooLongForPhaseOne() {
        List<List<String>> words = createWords();
        words.set(0, List.of("abcdefg", "brain", "cloud"));

        assertThrows(IllegalArgumentException.class,
                () -> new BossMonster(words, 0, 1));
    }

    @Test
void constructorShouldRejectWordTooShortForPhaseTwo() {
    List<List<String>> words = createWords();

    words.set(1, new ArrayList<>(
            List.of("monstr", "keyboard", "program")
    ));

    assertThrows(IllegalArgumentException.class,
            () -> new BossMonster(words, 0, 1));
}

    @Test
    void constructorShouldRejectWordTooShortForPhaseThree() {
        List<List<String>> words = createWords();
        words.set(2, List.of("adventure", "technology", "control"));

        assertThrows(IllegalArgumentException.class,
                () -> new BossMonster(words, 0, 1));
    }

    // ================= Typing =================

    @Test
    void checkNextCharShouldAcceptCorrectCharacter() {
        BossMonster boss = createBoss();

        assertTrue(boss.checkNextChar('a'));
        assertEquals(1, boss.getCurrentWordIndex());
        assertEquals("pple", boss.getRemainingWord());
    }

    @Test
    void checkNextCharShouldRejectWrongCharacter() {
        BossMonster boss = createBoss();

        assertFalse(boss.checkNextChar('x'));
        assertEquals(0, boss.getCurrentWordIndex());
        assertEquals("apple", boss.getRemainingWord());
    }

    @Test
    void checkNextCharShouldNotAdvanceAfterWrongInput() {
        BossMonster boss = createBoss();

        boss.checkNextChar('a');
        assertFalse(boss.checkNextChar('x'));

        assertEquals(1, boss.getCurrentWordIndex());
        assertEquals("pple", boss.getRemainingWord());
    }

    @Test
    void checkNextCharShouldMarkWordCompleted() {
        BossMonster boss = createBoss();

        typeCurrentWord(boss);

        assertTrue(boss.isCompleted());
        assertEquals("", boss.getRemainingWord());
    }

    @Test
    void checkNextCharShouldRejectInputAfterWordCompleted() {
        BossMonster boss = createBoss();

        typeCurrentWord(boss);

        assertFalse(boss.checkNextChar('a'));
    }

    // ================= Complete Word =================

    @Test
    void completeWordShouldReturnFalseIfWordNotCompleted() {
        BossMonster boss = createBoss();

        assertFalse(boss.completeWord());
        assertEquals(9, boss.getHp());
        assertEquals(1, boss.getPhase());
    }

    @Test
    void completeWordShouldDecreaseHp() {
        BossMonster boss = createBoss();

        completeCurrentWord(boss);

        assertEquals(8, boss.getHp());
        assertTrue(boss.isAlive());
    }

    @Test
    void completeWordShouldResetTypingProgress() {
        BossMonster boss = createBoss();

        completeCurrentWord(boss);

        assertEquals(0, boss.getCurrentWordIndex());
        assertFalse(boss.isCompleted());
    }

    @Test
    void completeWordShouldAdvancePhaseAfterThreeWords() {
        BossMonster boss = createBoss();

        completeCurrentWord(boss);
        completeCurrentWord(boss);
        completeCurrentWord(boss);

        assertEquals(2, boss.getPhase());
        assertEquals(6, boss.getHp());
        assertEquals(16.0, boss.getCountdown());
    }

    @Test
    void bossShouldDieAfterNineCompletedWords() {
        BossMonster boss = createBoss();

        for (int i = 0; i < 9; i++) {
            completeCurrentWord(boss);
        }

        assertEquals(0, boss.getHp());
        assertFalse(boss.isAlive());
    }

    @Test
    void completeWordShouldReturnTrueWhenBossDies() {
        BossMonster boss = createBoss();

        boolean result = false;

        for (int i = 0; i < 9; i++) {
            typeCurrentWord(boss);
            result = boss.completeWord();
        }

        assertTrue(result);
    }

    // ================= Countdown =================

    @Test
    void countdownShouldDecreaseWithUpdate() {
        BossMonster boss = createBoss();

        boss.update(2.0);

        assertEquals(12.0, boss.getCountdown());
    }

    @Test
    void countdownShouldNotBecomeNegative() {
        BossMonster boss = createBoss();

        boss.update(20.0);

        assertEquals(14.0, boss.getCountdown());
    }

    @Test
    void timeoutShouldTriggerSkill() {
        BossMonster boss = createBoss();

        boss.update(14.0);

        assertTrue(boss.consumeSkillPending());
    }

    @Test
    void skillPendingShouldBeConsumedOnlyOnce() {
        BossMonster boss = createBoss();

        boss.update(14.0);

        assertTrue(boss.consumeSkillPending());
        assertFalse(boss.consumeSkillPending());
    }

    @Test
    void timeoutShouldResetTypingProgress() {
        BossMonster boss = createBoss();

        boss.checkNextChar(boss.getCurrentWord().charAt(0));

        boss.update(14.0);

        assertEquals(0, boss.getCurrentWordIndex());
    }

    @Test
    void timeoutShouldKeepHpUnchanged() {
        BossMonster boss = createBoss();

        boss.update(14.0);

        assertEquals(9, boss.getHp());
    }

    @Test
    void timeoutShouldKeepPhaseUnchanged() {
        BossMonster boss = createBoss();

        boss.update(14.0);

        assertEquals(1, boss.getPhase());
    }

    // ================= Summon Minions =================

    @Test
    void phaseOneShouldNotSummonMinions() {
        BossMonster boss = createBoss();

        boss.update(14.0);

        assertFalse(boss.summonMinions());
    }

    @Test
    void phaseTwoShouldSummonMinionAfterTenSeconds() {
        BossMonster boss = createBoss();

        // Chuyển sang phase 2
        completeCurrentWord(boss);
        completeCurrentWord(boss);
        completeCurrentWord(boss);

        boss.update(10.0);

        assertTrue(boss.summonMinions());
    }

    @Test
    void summonRequestShouldBeConsumed() {
        BossMonster boss = createBoss();

        completeCurrentWord(boss);
        completeCurrentWord(boss);
        completeCurrentWord(boss);

        boss.update(10.0);

        assertTrue(boss.summonMinions());
        assertFalse(boss.summonMinions());
    }

    // ================= Death =================

    @Test
    void onDeathShouldMarkBossDead() {
        BossMonster boss = createBoss();

        boss.onDeath();

        assertFalse(boss.isAlive());
    }

    @Test
    void updateShouldNotChangeDeadBoss() {
        BossMonster boss = createBoss();

        boss.onDeath();
        boss.update(5.0);

        assertEquals(9, boss.getHp());
        assertEquals(1, boss.getPhase());
    }

    // ================= Bounds =================

    @Test
    void boundsShouldMatchBossDimensions() {
        BossMonster boss = createBoss();

        assertEquals(270, boss.getBounds().getX());
        assertEquals(205, boss.getBounds().getY());
        assertEquals(150, boss.getBounds().getWidth());
        assertEquals(90, boss.getBounds().getHeight());
    }

    // ================= Rendering =================

    @Test
    void renderShouldNotThrowException() {
        BossMonster boss = createBoss();

        BufferedImage image = new BufferedImage(
                600, 900, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        try {
            assertDoesNotThrow(() -> boss.render(g));
        } finally {
            g.dispose();
        }
    }

    @Test
    void renderShouldNotThrowWhenBossIsDead() {
        BossMonster boss = createBoss();

        boss.onDeath();

        BufferedImage image = new BufferedImage(
                600, 900, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        try {
            assertDoesNotThrow(() -> boss.render(g));
        } finally {
            g.dispose();
        }
    }
}