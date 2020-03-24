package collisiondetection;

import game2D.Sprite;
import game2D.TileMap;
import sprite.Hero;
import sprite.Key;

public class MapCollision {

    private Key key;
    private Hero player;
    private TileMap tmap;
    private final int PLAYER_SCREEN_OFFSET;
    private final int LAST_PIXEL_ON_MAP;

    public MapCollision(int screenWidth, TileMap tmap, Hero player, Key key, int playerOffset) {

        PLAYER_SCREEN_OFFSET = playerOffset;
        // the map right border:
        // map total width / 2 due to effect of offset. If player moves right and screen moves left.
        // Accounts for screen size and where the player is frozen on the screen.
        LAST_PIXEL_ON_MAP = (tmap.getPixelWidth()) - (screenWidth - PLAYER_SCREEN_OFFSET);

        this.tmap = tmap;
        this.player = player;
        this.key = key;
    }

    /**
     * Checks and handles collisions with the tile map for the
     * given sprite 's'. Initial functionality is limited...
     *
     * @param s			The Sprite to check collisions for
     */
    public void handleTileMapCollisions(Sprite s)
    {
        // These statements need to be individual IFs. We can be hitting 2 borders at once.
        // BOTTOM - Apply bottom border rules
        // prevents sprite from falling off of map
        if (s.getY() + s.getHeight() > tmap.getPixelHeight())
        {
            // Put the player back on the map
            // -1 ensures collision detection detects player being above surface.
            s.setY(tmap.getPixelHeight() - s.getHeight() - 1);
        }

        // LEFT - Apply left border rules.
        if (s.getX() < PLAYER_SCREEN_OFFSET) {
            // ensure collision detection detects player inside of map.
            s.setX(PLAYER_SCREEN_OFFSET);
        }

        // RIGHT - Apply right border rules.
        if (s.getX() > LAST_PIXEL_ON_MAP) {
            s.setX(LAST_PIXEL_ON_MAP);
        }

        // TOP - Apply ceiling
        if (s.getY() < tmap.getTileHeight() * 2) {
            s.setY(tmap.getTileHeight() * 2);
            // prevent the animation looking like it's glued to the ceiling
            s.setVelocityY(0);
        }
    }
}
