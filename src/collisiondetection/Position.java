package collisiondetection;

import game2D.Sprite;
import game2D.TileMap;

import java.util.Arrays;

public class Position {

    public final char BOTTOM_LEFT;
    public final char BOTTOM_RIGHT;
    public final char TOP_LEFT;
    public final char TOP_RIGHT;

    public Position(Sprite s, TileMap tmap) {
        int spriteX = (int)s.getX();
        int spriteY = (int)s.getY();


        // lots of casting, but otherwise the scale can't be accounted for without
        // modifying core game library
        int widthOffset = (int)((double)s.getWidth());
        int heightOffset = (int)((double)s.getHeight());

        // A sprite's position is represented by the bottom left corner
        BOTTOM_LEFT = tmap.getTileChar(spriteX / tmap.getTileWidth(), spriteY / tmap.getTileHeight());
        // account for image width by adding the sprite's width to the X position
        BOTTOM_RIGHT = tmap.getTileChar((spriteX + widthOffset) / tmap.getTileWidth(), spriteY / tmap.getTileHeight());
        // account for sprite's height by adding sprite's height to the Y position
        TOP_LEFT = tmap.getTileChar(spriteX / tmap.getTileWidth(), (spriteY + heightOffset)/ tmap.getTileHeight());
        // account for both sprite height and width by adding both to X and Y respectively
        TOP_RIGHT = tmap.getTileChar((spriteX + widthOffset) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());


    }

    public void debugPosition() {
        System.out.println(TOP_LEFT + " " + TOP_RIGHT);
        System.out.println(BOTTOM_LEFT + " " + BOTTOM_RIGHT);

        // for each corner of the sprite check if there's collision.
        for (char c : Arrays.asList(TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT)) {

        }
    }

    public boolean detectCollision(char c) {
        // assume no collision
        boolean collisionStatus = false;
        switch (c) {
            case '?':
                // out of bounds
                System.out.println("out of bounds");
                break;
            case '.':
                System.out.println("no collision");
                break;
            case 't':
                System.out.println("collision with top of pipe");
                collisionStatus = true;
                break;
            case 'p':
                System.out.println("collision with pipe body");
                collisionStatus = true;
                break;
            case 'b':
                System.out.println("collision with bottom of pipe");
                collisionStatus = true;
                break;
            default:
                System.out.println(" Should never hit this. This is an exception");
                throw new RuntimeException("Unrecognised collision status");
        }

        return collisionStatus;
    }
}
