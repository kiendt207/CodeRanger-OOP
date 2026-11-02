package coderanger.manager;

import coderanger.model.BonusMonster;
import coderanger.model.BossMonster;
import coderanger.model.NormalMonster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonsterFactory {
    private WordProvider wordProvider;
    private Random random;
    private long sequence;

    public MonsterFactory(WordProvider wordProvider, Random random) {
        this.wordProvider = wordProvider;
        this.random = random;
        this.sequence = 0;
    }

    // Hàm tiện ích tạo tọa độ X ngẫu nhiên (từ 65 đến 475 theo đặc tả)
    private double getRandomSpawnX() {
        return 65 + random.nextDouble() * (475 - 65);
    }

    public NormalMonster createNormal(int level, double now, boolean minion) {
        String word = wordProvider.nextNormal(level);
        double spawnX = getRandomSpawnX();
        double spawnY = minion ? 250 : -30; // Minion sinh ở Y=250, Normal sinh ở Y=-30
        
        // Tính tốc độ dựa trên công thức trong tài liệu, hoặc lấy từ Difficulty (ở đây làm mẫu tĩnh)
        double speed = 55 + (level * 5); 
        
        sequence++;
        return new NormalMonster(spawnX, spawnY, word, now, sequence, speed, minion);
    }

    public BonusMonster createBonus(int level, double now) {
        String word = wordProvider.nextBonus();
        double spawnX = getRandomSpawnX();
        double spawnY = -30;
        
        double speed = (55 + (level * 5)) * 1.35; // Nhanh hơn Normal
        sequence++;
        return new BonusMonster(spawnX, spawnY, word, now, sequence, speed);
    }

    public BossMonster createBoss(double now) {
        List<List<String>> bossWords = new ArrayList<>();
        bossWords.add(wordProvider.phaseWords(1));
        bossWords.add(wordProvider.phaseWords(2));
        bossWords.add(wordProvider.phaseWords(3));
        
        sequence++;
        // Boss đứng cố định ở (270, 205)
        return new BossMonster( bossWords, now, sequence);
    }
}