package coderanger.manager;

import coderanger.model.Monster;
import coderanger.util.GameConstants;
import java.util.Comparator;
import java.util.List;

public class TargetManager {
    private Monster lockedTarget;

    public Monster findAndLockTarget(char character, List<Monster> monsters) {
        if (lockedTarget != null && lockedTarget.isAlive() && !lockedTarget.isCompleted()) {
            return lockedTarget;
        }

        lockedTarget = monsters.stream()
                .filter(Monster::isAlive)
                .filter(monster -> !monster.isCompleted())
                .filter(monster -> !monster.getRemainingWord().isEmpty())
                .filter(monster -> monster.getRemainingWord().charAt(0) == character)
                .min(dangerComparator())
                .orElse(null);
        return lockedTarget;
    }

    public boolean processInput(char character) {
        if (lockedTarget == null || !lockedTarget.isAlive() || lockedTarget.isCompleted()) {
            lockedTarget = null;
            return false;
        }
        return lockedTarget.checkNextChar(character);
    }

    public Monster getLockedTarget() {
        return lockedTarget;
    }

    public void unlockTarget() {
        lockedTarget = null;
    }

    public Monster selectMostDangerous(List<Monster> monsters, Monster exclude) {
        return monsters.stream()
                .filter(Monster::isAlive)
                .filter(monster -> !monster.isCompleted())
                .filter(monster -> monster != exclude)
                .filter(monster -> !monster.isBoss())
                .min(dangerComparator())
                .orElse(null);
    }

    private Comparator<Monster> dangerComparator() {
        return Comparator.comparing(Monster::isBoss)
                .thenComparingDouble(this::dangerMetric)
                .thenComparingDouble(this::distanceToPlayer)
                .thenComparingLong(Monster::getSpawnSequence);
    }

    private double dangerMetric(Monster monster) {
        return (GameConstants.DANGER_Y - monster.getY())
                / Math.max(1.0, monster.getVerticalSpeed());
    }

    private double distanceToPlayer(Monster monster) {
        return Math.hypot(GameConstants.PLAYER_X - monster.getX(),
                GameConstants.PLAYER_Y - monster.getY());
    }
}
