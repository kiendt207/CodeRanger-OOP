package coderanger.model;

public abstract class Monster {
    public abstract void update(double dt);
    public boolean isAlive() { return true; }
    public void setAlive(boolean alive) {}
    public double getX() { return 0; }
    public double getY() { return 0; }
    public long getSpawnSequence() { return 0L; }
    public int getRewardScore() { return 100; }
    public boolean checkNextChar(char c) { return true; }
    public boolean isCompleted() { return false; }
    public boolean isQuickKill(double now) { return false; }
    public void onDeath() {}
}