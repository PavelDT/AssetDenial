package collisiondetection;

import game2D.Sprite;
import game2D.TileMap;

import java.util.Arrays;

public class Position {

    public final char BOTTOM_LEFT;
    public final char BOTTOM_RIGHT;
    public final char TOP_LEFT;
    public final char TOP_RIGHT;
    public final char TOP;
    public final char BOTTOM;

    public Position(Sprite s, TileMap tmap, int xOff, int yOff) {
        int spriteX = (int)((s.getX() - xOff) / 1.5);
        int spriteY = ((int)s.getY() - yOff) + tmap.getTileHeight();

        // System.out.println(spriteX + " " + spriteY);
        // xOff = 0;// 200 / tmap.getTileWidth();
        // yOff = yOff / tmap.getTileHeight();

        // lots of casting, but otherwise the scale can't be accounted for without
        // modifying core game library
        int widthOffset = (int)((double)s.getWidth());
        int heightOffset = (int)((double)s.getHeight());

        // A sprite's position is represented by the top left corner
        TOP_LEFT = tmap.getTileChar((spriteX)/ tmap.getTileWidth(), (spriteY) / tmap.getTileHeight());
        // account for image width by adding the sprite's width to the X position
        TOP_RIGHT = tmap.getTileChar((spriteX + widthOffset) / tmap.getTileWidth(), (spriteY) / tmap.getTileHeight());
        // account for sprite's height by adding sprite's height to the Y position
        BOTTOM_LEFT = tmap.getTileChar((spriteX) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());
        // account for both sprite height and width by adding both to X and Y respectively
        BOTTOM_RIGHT = tmap.getTileChar((spriteX + widthOffset) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());
        // top center of sprite
        TOP = tmap.getTileChar((spriteX + widthOffset/2) / tmap.getTileWidth(), (spriteY) / tmap.getTileHeight());
        // bottom center of sprite
        BOTTOM = tmap.getTileChar((spriteX + widthOffset/2) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());
    }

    public String debugPosition() {
        return TOP_LEFT + " " + TOP_RIGHT;
    }

    public String debugPosition2() {
        return BOTTOM_LEFT + " " + BOTTOM_RIGHT;
    }

    public boolean[] detectCollision() {
        // assume no collision for all 4 corners of sprite
        // order is TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        boolean [] collisionStatus = new boolean[]{false, false, false, false, false, false};
        // for each corner, detect collision
        char[] corners = new char[]{TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP, BOTTOM};
        for (int i = 0; i < corners.length; i++) {
            // access the character
            switch (corners[i]) {
                case '?':
                    // out of bounds
                    // throw new RuntimeException("Sprite out of bounds, should've been prevented by map collision detection");
                case '.':
                    // no collision detected, no action needed.
                    break;
                case 't':
                case 'p':
                case 'b':
                case 'g':
                case '1':
                case '2':
                case '3':
                case '4':
                    collisionStatus[i] = true;
                    break;
            }
        }

        return collisionStatus;
    }
}
