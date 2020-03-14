package collisiondetection;

import game2D.Sprite;
import game2D.TileMap;

public class Position2 {

    public final char LEFT;
    public final char RIGHT;
    public final char TOP;
    public final char BOTTOM;

    public Position2(Sprite s, TileMap tmap, float x, float y) {

        int relativeX = (int)x;
        int relativeY = (int)y;
        LEFT = tmap.getTileChar((relativeX)/ tmap.getTileWidth(), (relativeY - s.getHeight()/2) / tmap.getTileHeight());
        RIGHT = tmap.getTileChar((relativeX + s.getWidth() + tmap.getTileWidth())/ tmap.getTileWidth(), (relativeY - s.getHeight()/2) / tmap.getTileHeight());
        TOP = tmap.getTileChar((relativeX + s.getWidth()/2) / tmap.getTileWidth(), (relativeY - s.getHeight()) / tmap.getTileHeight());
        BOTTOM = tmap.getTileChar((relativeX + s.getWidth()/2) / tmap.getTileWidth(), (relativeY + s.getHeight()/2) / tmap.getTileHeight());
    }

    public boolean[] detectCollision() {
        // assume no collision for all 4 corners of sprite
        // order is TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        boolean [] collisionStatus = new boolean[]{false, false, false, false};
        // for each corner, detect collision
        char[] corners = new char[]{LEFT, RIGHT, TOP, BOTTOM};
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
