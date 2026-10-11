package coderanger.manager;

import coderanger.model.Player;

public class BuffManager {

    public BuffManager() {
    }

    public void applyBonus(Player player) {
        if (player == null) return;
        
        boolean shieldAdded = player.addShield();
        if (!shieldAdded) {
            player.heal(1);
        }
    }
}
