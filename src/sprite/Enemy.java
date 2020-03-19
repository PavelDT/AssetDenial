package sprite;

import game2D.Animation;
import game2D.Sprite;

import java.awt.*;

public class Enemy extends Sprite {

    private Animation animation = new Animation();
    private Animation attackAnimation = new Animation();
    private Sprite attack;
    private final String imagePath = "images/enemy.png";
    private int health;

    public Enemy() {
        super();

        animation.loadAnimationFromSheet(imagePath, 4, 1, 60);
        super.setAnimation(animation);
        attackAnimation.loadAnimationFromSheet("images/lightning.png", 8, 1, 60);
        attack = new Sprite(attackAnimation);
        attack.show();

        health = 140;
    }

    /**
     * Inflicts damage to this enemy
     */
    public void drainHealth() {
        health -= 2;
    }

    // getter for health
    public int getHealth() {
        return health;
    }

    // getter for attack
    public Sprite getAttack() {
        return attack;
    }

    /**
     * Not a mutator - used to set initial placement of the attack
     */
    public void setAttackInitialX() {
        attack.setX(this.getX());
    }


    /**
     * Draws a rectangle outline representing enemy healthbar
     * @param xo x offset to be applied to rectangle
     * @return
     */
    private Rectangle getHealthRectangle(int xo) {
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
    private Color getHealthColour() {
        if (health > 80) {
            return Color.GREEN;
        } else if (health > 50) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }

    @Override
    public void update(long elapsed) {
        super.update(elapsed);
        attack.update(elapsed);
    }

    /**
     * Overload of the drawTransformed method. Draws both this enemy sprite and the
     * attack sprite.
     * @param g Graphics2D object used for drawing sprites
     * @param xo x offset
     * @param yo y offset
     * @param playerX x position of player sprite
     */
    public void drawTransformed(Graphics2D g, int xo, int yo, float playerX) {

        this.setOffsets(xo, yo);
        super.drawTransformed(g);

        attack.setOffsets(xo, yo);
        attackPlayer(playerX);
        attack.setY(this.getY() + this.getHeight() - attack.getHeight());
        attack.drawTransformed(g);

        // draw the healthbar
        g.setColor(getHealthColour());
        g.draw(getHealthRectangle(xo));
    }

    /**
     * Attacks player by moving attack towards them once player is close enough
     * @param playerX - Position of the player to attack
     */
    private void attackPlayer(float playerX) {

        // decide whether to move left or right
        // 1 = left, -1 = right
        short direction = 1;
        if (attack.getX() < playerX) {
            direction = -1;
        }

        // move towards player when close enough
        if ((attack.getX() - playerX) * direction < 500) {
            // the multiplication of direction needs to be reversed
            // enables following left or right
            attack.setVelocityX(0.04f * (0 - direction));
        } else {
            attack.setVelocityX(0);
        }
    }
}
