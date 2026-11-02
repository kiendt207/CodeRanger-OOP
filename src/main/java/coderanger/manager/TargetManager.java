package coderanger.manager;

import coderanger.model.Monster;
import coderanger.model.BossMonster;
import coderanger.model.NormalMonster;
import coderanger.model.BonusMonster;

import java.util.Comparator;
import java.util.List;

public class TargetManager {
    private Monster lockedTarget;

    public TargetManager() {
        lockedTarget = null;
    }

    public Monster findAndLockTarget(char c, List<Monster> monsters) {
        if (lockedTarget != null && lockedTarget.isAlive() && !lockedTarget.isCompleted()) {
            return lockedTarget;
        }

        Comparator<Monster> dangerComparator = createDangerComparator();

        lockedTarget = monsters.stream()
                .filter(Monster::isAlive)
                .filter(m -> !m.isCompleted())
                .filter(m -> m.getRemainingWord().length() > 0 && m.getRemainingWord().charAt(0) == c)
                .min(dangerComparator)
                .orElse(null);

        return lockedTarget;
    }

    public boolean processInput(char c) {
        if (lockedTarget != null && lockedTarget.isAlive() && !lockedTarget.isCompleted()) {
            boolean isCorrect = lockedTarget.checkNextChar(c);
            if (!isCorrect) {
                return false; 
            }
            return true;
        }
        
        lockedTarget = null;
        return false;
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
                .filter(m -> !m.isCompleted())
                .filter(m -> m != exclude)
                .filter(m -> !(m instanceof BossMonster))
                .min(createDangerComparator())
                .orElse(null);
    }

    // --- HÀM MỚI THÊM ĐỂ LẤY TỐC ĐỘ TỪ CÁC LỚP CON ---
    private double getMonsterSpeed(Monster m) {
        if (m instanceof NormalMonster) {
            return ((NormalMonster) m).getSpeed();
        } else if (m instanceof BonusMonster) {
            return ((BonusMonster) m).getSpeed();
        }
        // Boss không di chuyển hoặc các trường hợp khác trả về 0
        return 0.0; 
    }

    private Comparator<Monster> createDangerComparator() {
        return (m1, m2) -> {
            boolean isBoss1 = m1 instanceof BossMonster;
            boolean isBoss2 = m2 instanceof BossMonster;
            
            // Theo luật: Boss luôn ưu tiên sau mọi quái thường
            if (isBoss1 && !isBoss2) return 1;
            if (!isBoss1 && isBoss2) return -1;
            if (isBoss1 && isBoss2) return 0;

            // Lấy tốc độ thông qua hàm helper vừa tạo
            double speed1 = getMonsterSpeed(m1);
            double speed2 = getMonsterSpeed(m2);

            // Tính thời gian tới line (y = 760)
            double timeToLine1 = (760 - m1.getY()) / Math.max(1.0, speed1);
            double timeToLine2 = (760 - m2.getY()) / Math.max(1.0, speed2);

            if (Math.abs(timeToLine1 - timeToLine2) > 0.01) {
                return Double.compare(timeToLine1, timeToLine2);
            }

            // Hòa thời gian: Ưu tiên khoảng cách Euclid đến Player (270, 820)
            double dist1 = Math.hypot(270 - m1.getX(), 820 - m1.getY());
            double dist2 = Math.hypot(270 - m2.getX(), 820 - m2.getY());
            
            if (Math.abs(dist1 - dist2) > 0.01) {
                return Double.compare(dist1, dist2);
            }

            // Vẫn hòa: Ưu tiên quái sinh ra trước
            return Long.compare(m1.getSpawnSequence(), m2.getSpawnSequence());
        };
    }
}
























