package sprite;

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
    }

    public void initSound() {
        portalSound = new Sound(soundPath, Sound.NO_EFFECT, false);
    }

    public void playSound() {
        portalSound.start();
    }
}
