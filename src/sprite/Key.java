package sprite;

import game2D.Animation;
import game2D.Sprite;
import game2D.TileMap;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.Random;

public class Key extends Sprite {
    private Animation keyAnimation = new Animation();;
    private final String imagePath = "images/key.png";
    private Random rng = new SecureRandom();

    public Key() {
        super();

        keyAnimation.addFrame(new ImageIcon(imagePath).getImage(), 60);
        super.setAnimation(keyAnimation);
    }

    /**
     * Randomly generates the position of the key somewhere on the map.
     * Map dependant. Assumes map has atleast 155 x tiles and 14 y tiles
     */
    public void positionKey(TileMap tmap, int level) {

        // there is no key for the third level.
        if (level == 3) {
            return;
        }

        // ensure map is large enough.
        if (tmap.getPixelWidth() / tmap.getTileWidth() < 155 || tmap.getPixelHeight() / tmap.getTileHeight() < 14) {
            throw new RuntimeException("Map isn't big enough");
        }

        // use RNG to pick key position
        int x = rng.nextInt(13) + 142;
        int y = rng.nextInt(5) + 8;
        // loop until an empty spot is found in the given range
        while (tmap.getTileChar(x, y) != '.') {
            x = rng.nextInt(13) + 142;
            y = rng.nextInt(5) + 8;
        }
        // set key position
        setX(x * tmap.getTileWidth());
        setY(y * tmap.getTileHeight() + this.getHeight());
    }

    public void keyCollected(TileMap tmap, int level) {
        // hide the key
        hide();

        // open end of game gate.

        switch (level) {
            case 1:
                tmap.setTileChar('.', 218, 4);
                tmap.setTileChar('.', 219, 4);
                tmap.setTileChar('.', 220, 4);
                break;
            case 2:
                tmap.setTileChar('.', 214, 1);
                tmap.setTileChar('.', 214, 2);
                tmap.setTileChar('.', 214, 3);
                break;
            default:
                break;
        }
    }
}
