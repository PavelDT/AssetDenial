package model;

import game2D.Animation;
import game2D.Sound;
import game2D.Sprite;

public class Portal extends Sprite {

    private Animation portalAnimation = new Animation();
    private final String imagePath = "images/portal.png";
    private Sound portalSound;
    private final String soundPath = "sounds/portal_sound.wav";

    public Portal() {
        super();
        portalAnimation.loadAnimationFromSheet(imagePath, 5, 1, 120);
        super.setAnimation(portalAnimation);
        portalSound = new Sound(soundPath);
    }

    public void playSound() {
        portalSound.start();
    }
}
