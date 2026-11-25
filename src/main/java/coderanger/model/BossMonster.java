package coderanger.model;

import coderanger.util.GameConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BossMonster extends Monster {

    public static final int MAX_HP = 9;
    public static final int WORDS_PER_PHASE = 3;
    public static final int BOSS_WIDTH = 150;
    public static final int BOSS_HEIGHT = 90;
    public static final int MAX_MINIONS = 4;

    private static final double[] PHASE_COUNTDOWNS = {
        11.0, 13.0, 15.0
    };

    private static final double[] SUMMON_INTERVALS = {
        0.0, 7.0, 5.5
    };

    private final List<List<String>> wordLists;

    private int phase;
    private int phaseWordIndex;
    private int hp;

    private String currentWord;
    private int currentWordIndex;

    private double wordElapsed;
    private double summonElapsed;

    private boolean skillPending;
    private boolean summonPending;

    public BossMonster(
            List<List<String>> phaseWords,
            double elapsed,
            long seq) {

        super(
            GameConstants.BOSS_CENTER_X - BOSS_WIDTH / 2.0,
            GameConstants.BOSS_CENTER_Y - BOSS_HEIGHT / 2.0,
            BOSS_WIDTH, BOSS_HEIGHT,
            getFirstWord(phaseWords),
            elapsed, seq, 100
        );

        validateWordLists(phaseWords);

        this.wordLists = new ArrayList<>();
        for (List<String> words : phaseWords) {
            this.wordLists.add(
                Collections.unmodifiableList(new ArrayList<>(words))
            );
        }

        this.phase = 0;
        this.phaseWordIndex = 0;
        this.hp = MAX_HP;

        this.currentWord = getFirstWord(phaseWords);
        this.currentWordIndex = 0;

        this.wordElapsed = 0.0;
        this.summonElapsed = 0.0;

        this.skillPending = false;
        this.summonPending = false;
    }

    private static String getFirstWord(List<List<String>> words) {
        Objects.requireNonNull(words, "phaseWords không được null");

        if (words.size() != 3
                || words.get(0) == null
                || words.get(0).isEmpty()) {
            throw new IllegalArgumentException(
                "Boss cần 3 phase và mỗi phase phải có từ"
            );
        }

        return words.get(0).get(0);
    }

    private static void validateWordLists(List<List<String>> words) {
        if (words.size() != 3) {
            throw new IllegalArgumentException("Boss phải có đúng 3 phase");
        }

        for (int p = 0; p < 3; p++) {
            List<String> phaseWords = words.get(p);

            if (phaseWords == null || phaseWords.size() != WORDS_PER_PHASE) {
                throw new IllegalArgumentException("Mỗi phase phải có đúng 3 từ");
            }

            int minLength = (p == 0) ? 5 : (p == 1) ? 7 : 9;
            int maxLength = (p == 0) ? 6 : (p == 1) ? 8 : 11;

            for (String word : phaseWords) {
                if (word == null
                        || word.length() < minLength
                        || word.length() > maxLength) {
                    throw new IllegalArgumentException(
                        "Độ dài từ không hợp lệ ở phase " + (p + 1)
                    );
                }
            }
        }
    }

    @Override
    public void update(double dt) {
        if (!isAlive() || dt <= 0) {
            return;
        }

        wordElapsed += dt;

        if (phase >= 1) {
            summonElapsed += dt;

            if (summonElapsed >= SUMMON_INTERVALS[phase]) {
                summonElapsed = 0.0;
                summonPending = true;
            }
        }

        if (wordElapsed >= PHASE_COUNTDOWNS[phase]) {
            executeSkill();

            currentWordIndex = 0;
            currentWord = chooseNextWord();
            wordElapsed = 0.0;
        }
    }

    @Override
    public boolean checkNextChar(char input) {
        if (!isAlive() || currentWordIndex >= currentWord.length()) {
            return false;
        }

        if (input != currentWord.charAt(currentWordIndex)) {
            return false;
        }

        currentWordIndex++;
        return true;
    }

    public boolean completeWord() {
        if (!isAlive() || currentWordIndex != currentWord.length()) {
            return false;
        }

        hp--;
        phaseWordIndex++;

        if (hp <= 0) {
            hp = 0;
            onDeath();
            return true;
        }

        if (phaseWordIndex >= WORDS_PER_PHASE) {
            phase++;

            if (phase >= 3) {
                onDeath();
                return true;
            }

            phaseWordIndex = 0;
            summonElapsed = 0.0;
        }

        currentWord = chooseNextWord();
        currentWordIndex = 0;
        wordElapsed = 0.0;

        return false;
    }

    private String chooseNextWord() {
        List<String> words = wordLists.get(phase);
        String nextWord = words.get(phaseWordIndex);

        if (nextWord.equals(currentWord) && words.size() > 1) {
            int nextIndex = (phaseWordIndex + 1) % words.size();
            nextWord = words.get(nextIndex);
        }

        return nextWord;
    }

    public void executeSkill() {
        skillPending = true;
    }

    public boolean consumeSkillPending() {
        boolean result = skillPending;
        skillPending = false;
        return result;
    }

    public boolean summonMinions() {
        boolean result = summonPending;
        summonPending = false;
        return result;
    }

    @Override
    public void onDeath() {
        if (isAlive()) {
            setAlive(false);
        }
    }

    public double getCountdown() {
        return Math.max(0.0, PHASE_COUNTDOWNS[phase] - wordElapsed);
    }

    public double getPhaseDuration() {
        return PHASE_COUNTDOWNS[phase];
    }

    public int getPhase() {
        return phase + 1;
    }

    public int getHp() {
        return hp;
    }

    @Override
    public String getFullWord() {
        return currentWord;
    }

    @Override
    public int getTypedCharacters() {
        return currentWordIndex;
    }

    @Override
    public String getRemainingWord() {
        return currentWord.substring(currentWordIndex);
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public int getCurrentWordIndex() {
        return currentWordIndex;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public boolean isCompleted() {
        return currentWordIndex >= currentWord.length();
    }

    @Override
    public void render(Graphics2D g) {
        if (!isAlive()) {
            return;
        }

        Color oldColor = g.getColor();
        Font oldFont = g.getFont();

        int x = (int) getX();
        int y = (int) getY();

        g.setColor(new Color(120, 35, 160));
        g.fillRoundRect(x, y, BOSS_WIDTH, BOSS_HEIGHT, 24, 24);

        g.setColor(Color.MAGENTA);
        g.drawRoundRect(x, y, BOSS_WIDTH, BOSS_HEIGHT, 24, 24);

        g.setColor(Color.RED);
        g.fillRect(x, y - 12, BOSS_WIDTH, 7);

        g.setColor(Color.GREEN);
        g.fillRect(
            x, y - 12,
            (int) (BOSS_WIDTH * (hp / (double) MAX_HP)), 7
        );

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        g.drawString("BOSS  " + hp + "/" + MAX_HP, x + 8, y + 22);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        if (currentWordIndex > 0) {
            String typed = currentWord.substring(0, currentWordIndex);
            g.setColor(Color.GRAY);
            g.drawString(typed, x + 8, y + 52);
            int typedWidth = g.getFontMetrics().stringWidth(typed);
            g.setColor(Color.WHITE);
            g.drawString(currentWord.substring(currentWordIndex), x + 8 + typedWidth, y + 52);
        } else {
            g.setColor(Color.WHITE);
            g.drawString(currentWord, x + 8, y + 52);
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.drawString(
            "Phase " + (phase + 1)
                + " | " + String.format("%.1f", getCountdown()) + "s",
            x + 8, y + 72
        );

        g.setColor(oldColor);
        g.setFont(oldFont);
    }

    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(getX(), getY(), BOSS_WIDTH, BOSS_HEIGHT);
    }
}
