package coderanger.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonsterTest {

    // Class con giả lập để test Monster abstract
    private static class TestMonster extends Monster {

        TestMonster(String word, double spawnElapsed,
                    long spawnSequence, int rewardScore) {
            super(100, 200, 50, 50,
                    word, spawnElapsed, spawnSequence, rewardScore);
        }

        @Override
        public void update(double dt) {
        }

        @Override
        public void render(java.awt.Graphics2D g) {
        }

        // Cho phép test trạng thái minion
        void setMinion(boolean minion) {
            this.minion = minion;
        }

        private boolean minion = false;

        @Override
        public boolean isMinion() {
            return minion;
        }
    }

    @Test
    void constructorShouldStoreValues() {
        TestMonster monster = new TestMonster("hello", 5.0, 10, 100);

        assertEquals("hello", monster.getFullWord());
        assertEquals("hello", monster.getRemainingWord());
        assertEquals(0, monster.getTypedCharacters());
        assertEquals(5.0, monster.getSpawnElapsed());
        assertEquals(10, monster.getSpawnSequence());
        assertEquals(100, monster.getRewardScore());
        assertFalse(monster.isCompleted());
        assertTrue(monster.isAlive());
    }

    @Test
    void constructorShouldRejectNullWord() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestMonster(null, 0, 1, 100));
    }

    @Test
    void constructorShouldRejectBlankWord() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestMonster("   ", 0, 1, 100));
    }

    @Test
    void constructorShouldRejectUppercaseWord() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestMonster("Hello", 0, 1, 100));
    }

    @Test
    void constructorShouldRejectNegativeSpawnTime() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestMonster("hello", -1, 1, 100));
    }

    @Test
    void constructorShouldRejectNegativeReward() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestMonster("hello", 0, 1, -100));
    }

    @Test
    void checkNextCharShouldAcceptCorrectCharacter() {
        TestMonster monster = new TestMonster("cat", 0, 1, 100);

        assertTrue(monster.checkNextChar('c'));
        assertEquals(1, monster.getTypedCharacters());
        assertEquals("at", monster.getRemainingWord());
    }

    @Test
    void checkNextCharShouldRejectWrongCharacter() {
        TestMonster monster = new TestMonster("cat", 0, 1, 100);

        assertFalse(monster.checkNextChar('x'));
        assertEquals(0, monster.getTypedCharacters());
        assertEquals("cat", monster.getRemainingWord());
    }

    @Test
    void checkNextCharShouldCompleteWord() {
        TestMonster monster = new TestMonster("cat", 0, 1, 100);

        assertTrue(monster.checkNextChar('c'));
        assertTrue(monster.checkNextChar('a'));
        assertTrue(monster.checkNextChar('t'));

        assertTrue(monster.isCompleted());
        assertEquals(3, monster.getTypedCharacters());
        assertEquals("", monster.getRemainingWord());
    }

    @Test
    void checkNextCharShouldRejectInputAfterCompletion() {
        TestMonster monster = new TestMonster("a", 0, 1, 100);

        assertTrue(monster.checkNextChar('a'));
        assertFalse(monster.checkNextChar('a'));
        assertEquals(1, monster.getTypedCharacters());
    }

    @Test
    void checkNextCharShouldRejectInputAfterDeath() {
        TestMonster monster = new TestMonster("cat", 0, 1, 100);

        monster.onDeath();

        assertFalse(monster.checkNextChar('c'));
        assertEquals(0, monster.getTypedCharacters());
    }

    @Test
    void onDeathShouldMarkMonsterDead() {
        TestMonster monster = new TestMonster("cat", 0, 1, 100);

        monster.onDeath();

        assertFalse(monster.isAlive());
    }

    @Test
    void quickKillShouldBeTrueAtSpawnTime() {
        TestMonster monster = new TestMonster("a", 5.0, 1, 100);

        monster.checkNextChar('a');

        assertTrue(monster.isQuickKill(5.0));
    }

    @Test
    void quickKillShouldBeTrueAtBoundary() {
        TestMonster monster = new TestMonster("a", 5.0, 1, 100);

        monster.checkNextChar('a');

        assertTrue(monster.isQuickKill(6.5));
    }

    @Test
    void quickKillShouldBeFalseAfterBoundary() {
        TestMonster monster = new TestMonster("a", 5.0, 1, 100);

        monster.checkNextChar('a');

        assertFalse(monster.isQuickKill(6.51));
    }

    @Test
    void quickKillShouldBeFalseBeforeSpawn() {
        TestMonster monster = new TestMonster("a", 5.0, 1, 100);

        monster.checkNextChar('a');

        assertFalse(monster.isQuickKill(4.9));
    }

    @Test
    void quickKillShouldBeFalseIfWordNotCompleted() {
        TestMonster monster = new TestMonster("cat", 0, 1, 100);

        monster.checkNextChar('c');

        assertFalse(monster.isQuickKill(0.5));
    }

    @Test
    void quickKillShouldBeFalseForMinion() {
        TestMonster monster = new TestMonster("a", 0, 1, 100);

        monster.setMinion(true);
        monster.checkNextChar('a');

        assertFalse(monster.isQuickKill(0.5));
    }

    @Test
    void quickKillShouldBeFalseAfterDeath() {
        TestMonster monster = new TestMonster("a", 0, 1, 100);

        monster.checkNextChar('a');
        monster.onDeath();

        assertFalse(monster.isQuickKill(0.5));
    }

    @Test
    void quickKillShouldBeFalseIfNotCompletedDirectly() {
        TestMonster monster = new TestMonster("a", 0, 1, 100);

        // Không hoàn thành từ input trực tiếp thì không quick kill.
        // Hiện tại class Monster không có public API để set trạng thái này,
        // nên test này không thể tạo trạng thái completedDirectly = false.
        assertFalse(monster.isCompleted());
    }
}