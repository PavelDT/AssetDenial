package collisiondetection;

import game2D.Sprite;
import game2D.TileMap;

public class PlayerCollision {

    private Sprite sprite;
    private TileMap tmap;
    private int xOff;
    private int yOff;

    public PlayerCollision(Sprite s, TileMap tmap, int xOff, int yOff) {
        this.sprite = s;
        this.tmap = tmap;
        this.xOff = xOff;
        this.yOff = yOff;
    }

    private boolean[] detectCollision() {

        int spriteX = (int)((sprite.getX() - xOff) / 1.5);
        int spriteY = ((int)sprite.getY() - yOff) + tmap.getTileHeight();

        // lots of casting, but otherwise the scale can't be accounted for without
        // modifying core game library
        int widthOffset = (int)((double)sprite.getWidth());
        int heightOffset = (int)((double)sprite.getHeight());

        // top center of sprite
        char TOP = tmap.getTileChar((spriteX + widthOffset/2) / tmap.getTileWidth(), (spriteY) / tmap.getTileHeight());
        // bottom center of sprite
        char BOTTOM = tmap.getTileChar((spriteX + widthOffset/2) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());
        // A sprite's position is represented by the top left corner
        char TOP_LEFT = tmap.getTileChar((spriteX)/ tmap.getTileWidth(), (spriteY) / tmap.getTileHeight());
        // account for image width by adding the sprite's width to the X position
        char TOP_RIGHT = tmap.getTileChar((spriteX + widthOffset) / tmap.getTileWidth(), (spriteY) / tmap.getTileHeight());
        // account for sprite's height by adding sprite's height to the Y position
        char BOTTOM_LEFT = tmap.getTileChar((spriteX) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());
        // account for both sprite height and width by adding both to X and Y respectively
        char BOTTOM_RIGHT = tmap.getTileChar((spriteX + widthOffset) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());
        // account for left side mid point, ensures 1/2 the sprite cant make it into a block
        char LEFT = tmap.getTileChar((spriteX) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());
        // account for right side mid point, ensures 1/2 the sprite cant make it into a block
        char RIGHT = tmap.getTileChar((spriteX + widthOffset) / tmap.getTileWidth(), (spriteY + heightOffset) / tmap.getTileHeight());

        // assume no collision for all 4 corners of sprite
        // order is BOTTOM, TOP, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        boolean [] collisionStatus = new boolean[]{false, false, false, false, false, false, false, false};
        // for each corner, detect collision
        char[] corners = new char[]{BOTTOM, TOP, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, RIGHT};
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
                case '5':
                case '6':
                    collisionStatus[i] = true;
                    break;
            }
        }

        return collisionStatus;
    }

    public void applyCollisionRules() {
        final int BOTTOM = 0;
        final int TOP = 1;
        // player sprite specific - 8 point precession
        final int TOP_LEFT = 2;
        final int TOP_RIGHT = 3;
        final int BOTTOM_LEFT = 4;
        final int BOTTOM_RIGHT = 5;
        final int LEFT = 6;
        final int RIGHT = 7;

        // check if player is colliding with map objects.
        boolean[] collisionStatus = detectCollision();

        if (collisionStatus[BOTTOM]) {
            sprite.setVelocityY(0.02f);
            float currentY = sprite.getY();
            sprite.setY(currentY - 2f);
        }
        if (collisionStatus[TOP]) {
            sprite.setVelocityY(-0.02f);
            float currentY = sprite.getY();
            sprite.setY(currentY + 15f);
        }
        // check sprite Top left
        if (collisionStatus[TOP_LEFT]) {
            float currentX = sprite.getX();
            sprite.setX(currentX + 4f);
        }
        // check sprite Top Right
        if (collisionStatus[TOP_RIGHT]) {
            float currentX = sprite.getX();
            sprite.setX(currentX - 4f);
        }
        // check sprite Bottom Left
        if (collisionStatus[BOTTOM_LEFT] && !collisionStatus[BOTTOM]) {
            float currentX = sprite.getX();
            sprite.setX(currentX + 4f);
        }
        // check sprite Bottom Right
        if (collisionStatus[BOTTOM_RIGHT] && !collisionStatus[BOTTOM]) {
            float currentX = sprite.getX();
            sprite.setX(currentX - 4f);
        }
        // check sprite Left
        if (collisionStatus[LEFT] && !collisionStatus[BOTTOM]) {
            float currentX = sprite.getX();
            sprite.setX(currentX + 4f);
        }
        // check sprite Right
        if (collisionStatus[RIGHT] && !collisionStatus[BOTTOM]) {
            float currentX = sprite.getX();
            sprite.setX(currentX - 4f);
        }
    }
}
