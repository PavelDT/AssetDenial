package model;

import game2D.Animation;
import game2D.Sprite;

import java.awt.*;

public class Hero extends Sprite {

    private Animation idle = new Animation();
    private final String imagePathIdle = "images/character_idle.png";
    private Animation running = new Animation();
    private final String imagePathRunning = "images/character_run.png";
    private int health = 100;


    public Hero() {
        super();
        idle.loadAnimationFromSheet(imagePathIdle, 9, 1, 120);
        running.loadAnimationFromSheet(imagePathRunning, 5, 1, 120);
        setIdle();
    }

    /**
     * Switch to running animation
     */
    public void setRunning() {
        setAnimation(running);
    }

    /**
     * Switch to idle animation
     */
    public void setIdle() {
        setAnimation(idle);
    }

    public int getHealth() {
        return health;
    }

    public Rectangle getHealthRectangle(int xo) {
        Rectangle r = new Rectangle();
        r.x = (int)(getX() + xo);
        r.y = (int)((getY()) - getHeight()/2 - 10);
        r.width = getHealth();
        r.height = 3;
        return r;
    }

    /**
     * Returns a colour representing the colour of the healthbar
     * @return Color - representing how healthy the enemy is
     */
    public Color getHealthColour() {
        if (health > 80) {
            return Color.GREEN;
        } else if (health > 50) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }
}
