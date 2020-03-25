package sprite;

import game2D.Animation;
import game2D.Sprite;

import java.awt.*;

public class Hero extends Sprite {

    private Animation idle = new Animation();
    private final String imagePathIdle = "images/character_idle.png";
    private Animation fire = new Animation();
    private final String imagePathfire = "images/fireanim.png";
    private Animation jump = new Animation();
    private final String imagePathjump = "images/character_jump.png";
    private Animation running = new Animation();
    private final String imagePathRunning = "images/character_run.png";
    private int health = 1000;


    public Hero() {
        super();
        idle.loadAnimationFromSheet(imagePathIdle, 9, 1, 120);
        running.loadAnimationFromSheet(imagePathRunning, 5, 1, 120);
        fire.loadAnimationFromSheet(imagePathfire,5,1,60);
        jump.loadAnimationFromSheet(imagePathjump, 5,1,60);
        setIdle();
    }

    /**
     * Switch to running animation
     */
    public void setRunning() {
        setAnimation(running);
    }

    /**
     * Switch to fire animation
     */
    public void setFire() {
        setAnimation(fire);
    }

    /**
     * Switch to jump animation
     */
    public void setJump() {
        setAnimation(jump);
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
        r.width = getHealth()/10;
        r.height = 3;
        return r;
    }

    /**
     * Returns a colour representing the colour of the healthbar
     * @return Color - representing how healthy the enemy is
     */
    public Color getHealthColour() {
        if (health > 800) {
            return Color.GREEN;
        } else if (health > 500) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }

    /**
     * Drains health of player
     */
    public void drainHealth() {
        health -= 2;
    }

    /**
     * Drains health of player
     */
    public void resetHealth() {
        health = 1000;
    }
}
