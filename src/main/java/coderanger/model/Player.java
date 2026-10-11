package coderanger.model;

public class Player {
    public Player(double x, double y) {}
    public int takeDamage(int damage) { return damage; }
    public boolean addShield() { return true; }
    public int heal(int amount) { return amount; }
    public boolean isDead() { return false; }
    public int getHp() { return 5; }
    public int getShieldLayers() { return 1; }
}