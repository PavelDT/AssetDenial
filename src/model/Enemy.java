package model;

import game2D.Animation;
import game2D.Sprite;

import java.awt.*;

public class Enemy extends Sprite {

    private Animation animation = new Animation();
    private final String imagePath = "images/enemy.png";
    private int health;

    public Enemy() {
        super();

        animation.loadAnimationFromSheet(imagePath, 4, 1, 60);
        super.setAnimation(animation);
        health = 140;
    }

    public void drainHealth() {
        health -= 2;
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
