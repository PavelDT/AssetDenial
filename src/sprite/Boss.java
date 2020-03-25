package sprite;

import game2D.Animation;
import game2D.Sprite;

import java.awt.*;
import java.security.SecureRandom;

public class Boss extends Sprite {

    private Animation animation = new Animation();
    private Animation attackAnimationTelegraph = new Animation();
    private Sprite attackTelegraph;
    private Animation attackAnimation = new Animation();
    private Sprite attack;
    private final String imagePath = "images/enemy.png";
    private int health;
    private long attackTimer;
    private final short INIT_TIMER_VAL = 12000;
    private SecureRandom rng = new SecureRandom();

    public Boss() {
        super();

        animation.loadAnimationFromSheet(imagePath, 4, 1, 60);
        super.setAnimation(animation);
        attackAnimationTelegraph.loadAnimationFromSheet("images/telegraph_attack.png", 1, 1, 60);
        attackTelegraph = new Sprite(attackAnimationTelegraph);
        attackAnimation.loadAnimationFromSheet("images/boss_attack.png", 1, 1, 60);
        attack = new Sprite(attackAnimation);

        health = 1400;
        attackTimer = INIT_TIMER_VAL;
        attackTelegraph.setY(100);
        attack.setY(100);
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
        if (attack.isVisible())
            return attack;
        else if (attackTelegraph.isVisible())
            return attackTelegraph;
        else
            return null;
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
        r.width = getHealth()/3;
        r.height = 3;
        return r;
    }

    /**
     * Returns a colour representing the colour of the healthbar
     * @return Color - representing how healthy the enemy is
     */
    private Color getHealthColour() {
        if (health > 800) {
            return Color.GREEN;
        } else if (health > 500) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }

    /**
     * Updates boss's fields
     * @param elapsed - time since last frame
     * @param playerX - position of player
     */
    public void update(long elapsed, float playerX) {
        super.update(elapsed);
        attackTelegraph.update(elapsed);
        attack.update(elapsed);

        attackPlayer(elapsed);

        // hide attacks once they are out of bounds
        if(attackTelegraph.getX() < 0) {
            attackTelegraph.hide();
            attackTelegraph.setVelocityX(0);
        }
        if(attack.getX() < 0) {
            attack.hide();
            attack.setVelocityX(0);
        }

        // once the attack timer hits 0, reset the attack
        if (attackTimer < 7100 && attackTimer > 7000) {
            attackTelegraph.setX(1000);
            attackTelegraph.show();
        }
        if (attackTimer < 50) {
            attack.setX(1000);
            attack.show();
        }
    }

    /**
     * Overload of the drawTransformed method. Draws both this enemy sprite and the
     * attack sprite.
     * @param g Graphics2D object used for drawing sprites
     * @param xo x offset
     * @param yo y offset
     */
    public void drawTransformed(Graphics2D g, int xo, int yo) {

        this.setScale(3);
        this.setOffsets(xo, yo);
        super.drawTransformed(g);

        attackTelegraph.setOffsets(xo, yo);
        attackTelegraph.drawTransformed(g);

        attack.setOffsets(xo, yo);
        attack.drawTransformed(g);

        // draw the healthbar
        g.setColor(getHealthColour());
        g.draw(getHealthRectangle(xo));
    }

    /**
     * Attacks player
     */
    private void attackPlayer(long elapsed) {

        // decrement the timer continuously
        attackTimer -= elapsed;

        if (attackTimer < 9000 && attackTimer > 8950) {
            // use rng to randomly position the attack
            // positions should be 150, 350, 550,
            // generate 0, 1 or 2
            int y = (rng.nextInt(3) * 200) + 150;
            attackTelegraph.setY(y + 50);
            attack.setY(y - 50);
        }

        // at 7 sec - issue telegraph attack
        if (attackTimer < 7000 && attackTimer > 3000) {
            // throw out attack 1 to telegraph attack 2
            attackTelegraph.show();
            attackTelegraph.setVelocityX(-0.2f);
        }
        // at 3 seconds fire main attack
        if (attackTimer < 3000) {
            attack.show();
            attack.setVelocityX(-0.5f);
        }
        // reset the timer
        if (attackTimer < 1) {
            attackTimer = INIT_TIMER_VAL;
        }
    }

    public void hideAll() {
        hide();
        attack.hide();
        attackTelegraph.hide();
    }
}
