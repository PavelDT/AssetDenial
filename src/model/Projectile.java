package model;

import game2D.Animation;
import game2D.Sound;
import game2D.Sprite;

import javax.swing.*;

public class Projectile extends Sprite {

    private Animation fire;
    private Sound fireSound;
    private final String imagePath = "images/fire_preliminary.png";
    private Sprite player;

    public Projectile(Sprite player) {
        super();
        fire = new Animation();
        fire.loadAnimationFromSheet(imagePath, 1, 7, 60);
        fireSound = new Sound("sounds/fire.wav");
        super.setAnimation(fire);
        super.setFlipX(player.getFlipX());
        this.player = player;
        configureDirection();
    }

    /**
     * Configures direction of projectile, either left or right
     */
    private void configureDirection() {
        // place on right side of player
        if (player.getFlipX()) {
            // trick to get animation width, when loading animations from sheets, the width is set to -1
            setX(player.getX() - getWidth());
        } else {
            setX(player.getX() + player.getWidth());
        }
    }

    /**
     * Starts sound playing
     */
    public void playSound() {
        fireSound.start();
    }

    /**
     * Interupts thread playing sound
     */
    public void stopSound() {
        fireSound.interrupt();
    }

    @Override
    /**
     * Defaul
     * @return int - represents width of animation
     */
    public int getWidth() {
        return new ImageIcon(imagePath).getIconWidth();
    }
}
