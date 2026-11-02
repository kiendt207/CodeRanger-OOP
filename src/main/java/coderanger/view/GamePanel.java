package coderanger.view;

import coderanger.manager.GameManager;
import coderanger.model.BossMonster;
import coderanger.model.Monster;
import coderanger.resource.ResourceManager;
import coderanger.util.GameConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class GamePanel extends JPanel {
    private final GameManager manager;

    public GamePanel(GameManager manager, ResourceManager resources) {
        this.manager = manager;
        setBackground(new Color(5, 5, 10));
        setPreferredSize(new java.awt.Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 50, 50, 150));
            g.drawLine(0, GameConstants.DANGER_Y, GameConstants.WIDTH, GameConstants.DANGER_Y);

            manager.getPlayer().render(g);
            for (Monster monster : manager.getMonsters()) {
                monster.render(g);
            }
            manager.getEffects().forEach(effect -> effect.render(g));
            drawHud(g);
            drawState(g);
        } finally {
            g.dispose();
        }
    }

    private void drawHud(Graphics2D g) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        g.setColor(Color.CYAN);
        g.drawString("SCORE: " + manager.getStats().getScore(), 10, 25);
        g.setColor(Color.GREEN);
        g.drawString("HP: " + manager.getPlayer().getHp() + "/5", 10, 46);
        g.setColor(new Color(100, 200, 255));
        g.drawString("SHIELD: " + manager.getPlayer().getShieldLayers() + "/3", 10, 67);
        g.setColor(Color.YELLOW);
        g.drawString("WAVE: " + manager.getCurrentWave(), 10, 88);
        g.drawString("COMBO: " + manager.getStats().getStreak(), 380, 25);

        manager.getMonsters().stream()
                .filter(Monster::isBoss)
                .filter(BossMonster.class::isInstance)
                .map(BossMonster.class::cast)
                .findFirst()
                .ifPresent(boss -> {
                    g.setColor(Color.MAGENTA);
                    g.drawString("BOSS PHASE: " + boss.getPhase(), 350, 50);
                    g.drawString(String.format("TIMER: %.1fs", boss.getCountdown()), 350, 72);
                    g.drawString("BOSS HP: " + boss.getHp() + "/9", 350, 94);
                });
    }

    private void drawState(Graphics2D g) {
        GameManager.GameState state = manager.getState();
        if (state == GameManager.GameState.PLAYING) {
            return;
        }

        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        String label = state.toString().replace('_', ' ');
        int x = (getWidth() - g.getFontMetrics().stringWidth(label)) / 2;
        g.drawString(label, x, getHeight() / 2);
    }
}
