package coderanger.model;

import java.util.Locale;

public abstract class Monster extends GameObject {
    private static final double QUICK_KILL_WINDOW = 1.5;

    private final String fullWord;
    private final double spawnElapsed;
    private final long spawnSequence;
    private final int rewardScore;

    private int index;
    private boolean completed;
    private boolean completedDirectly;

    protected Monster(double x, double y, int width, int height,
                    String word, double spawnElapsed, long spawnSequence, int rewardScore){
        super(x, y, width, height);

        if (word == null || word.isBlank()){
            throw new IllegalArgumentException("Monster word must not be empty");
        }
        if (!word.equals(word.toLowerCase(Locale.ROOT))){
            throw new IllegalArgumentException("Monster word must be lowercase");
        }
        if (spawnElapsed < 0){
            throw new IllegalArgumentException("Spawn time must be non-negative");
        }
        if (rewardScore < 0){
            throw new IllegalArgumentException("Reward score must be non-negative");
        }

        this.fullWord = word;
        this.spawnElapsed = spawnElapsed;
        this.spawnSequence = spawnSequence;
        this.rewardScore = rewardScore;
        this.index = 0;
        this.completed = false;
        this.completedDirectly = false;
    }

    public boolean checkNextChar(char c) {
        if (!isAlive() || completed || index >= fullWord.length()){
            return false;
        }

        if (c != fullWord.charAt(index)){
            return false;
        }

        index++;

        if (index == fullWord.length()){
            completed = true;
            completedDirectly = true;
        }

        return true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getFullWord(){
        return fullWord;
    }

    public String getRemainingWord(){
        return fullWord.substring(index);
    }

    public int getTypedCharacters(){
        return index;
    }

    public int getRewardScore(){
        return rewardScore;
    }

    public double getSpawnElapsed(){
        return spawnElapsed;
    }

    public long getSpawnSequence(){
        return spawnSequence;
    }

    // now và spawnElapsed phải cùng đơn vị là giây, tính từ khi bắt đầu ván
    public boolean isQuickKill(double now) {
        double elapsedSinceSpawn = now - spawnElapsed;

        return isAlive()
            && completed
            && completedDirectly
            && !isMinion()
            && elapsedSinceSpawn >= 0
            && elapsedSinceSpawn <= QUICK_KILL_WINDOW;
    }

    // các loại quái thông thường không phải minion nhưng vẫn thuộc NormalMonster
    public boolean isMinion(){
        return false;
    }
    
    public void onDeath() {
        setAlive(false);
    }
}