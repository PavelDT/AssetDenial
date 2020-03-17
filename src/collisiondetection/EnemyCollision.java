package collisiondetection;

import game2D.Sprite;
import game2D.TileMap;

public class EnemyCollision {

    public final char LEFT;
    public final char RIGHT;
    public final char TOP;
    public final char BOTTOM;
    Sprite sprite;

    public EnemyCollision(Sprite s, TileMap tmap, float x, float y) {
        this.sprite = s;

        int relativeX = (int)x;
        int relativeY = (int)y;

        //       tmap.getTileChar((spriteX + widthOffset/2) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());
        BOTTOM = tmap.getTileChar((relativeX + s.getWidth()/2) / tmap.getTileWidth(), (relativeY + s.getHeight()/2) / tmap.getTileHeight());
        TOP = tmap.getTileChar((relativeX + s.getWidth()/2) / tmap.getTileWidth(), (relativeY - s.getHeight()) / tmap.getTileHeight());
        LEFT = tmap.getTileChar((relativeX)/ tmap.getTileWidth(), (relativeY - s.getHeight()/2) / tmap.getTileHeight());
        RIGHT = tmap.getTileChar((relativeX + s.getWidth())/ tmap.getTileWidth(), (relativeY - s.getHeight()/2) / tmap.getTileHeight());
    }

    public boolean[] detectCollision() {
        // assume no collision for all 4 corners of sprite
        // order is TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        boolean [] collisionStatus = new boolean[]{false, false, false, false};
        // for each corner, detect collision
        char[] corners = new char[]{BOTTOM, TOP, LEFT, RIGHT};
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

    public void applyCollisionRules() {

        boolean[] collisionStatus2 = detectCollision();
        final int BOTTOM = 0;
        final int TOP = 1;
        final int LEFT = 2;
        final int RIGHT = 3;

        if (collisionStatus2[BOTTOM]) {
            sprite.setVelocityY(0.02f);
            float currentY = sprite.getY();
            sprite.setY(currentY - 2f);
        }
        if (collisionStatus2[TOP]) {
            sprite.setVelocityY(-0.02f);
            float currentY = sprite.getY();
            sprite.setY(currentY + 10f);
        }

        // check sprite Top Right
        if (collisionStatus2[RIGHT]) {
            sprite.setVelocityX(0.01f);
            float currentX = sprite.getX();
            sprite.setX(currentX - 2f);
        }
        if (collisionStatus2[LEFT]) {
            sprite.setVelocityX(-0.01f);
            float currentX = sprite.getX();
            sprite.setX(currentX + 2f);
        }
    }
}
