import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import View.ParallaxBackground;
import collisiondetection.PlayerCollision;
import collisiondetection.EnemyCollision;
import game2D.*;
import model.*;

import javax.swing.ImageIcon;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. By default GameCore
// will handle the 'Escape' key to quit the game but you should
// override this with your own event handler.

/**
 * @author David Cairns
 *
 */
@SuppressWarnings("serial")

public class Game extends GameCore
{
    // Useful game constants
    private static int screenWidth = 1200;
    private static int screenHeight = 800;

    // Game state vars
    private float gravity = 0.0003f;
    private boolean flap = false;

    Hero player = null;
    // Key used to unlock exit for level.
    Key key = null;
    // Portal used to complete level
    Portal portal = null;
    // sprite that uses ship background animation from
    // Collection of projecties fired by the player
    // LinkedQueue is most suitable for time based animations as it's FIFO
    // ConcurrentLinkedQueue used to avoid ConcurrentModificationExceptions.
    // As the data structure is 'linked' there is no array copying when deleting items - this is good for efficiency
    private Queue<Projectile> fires;
    // collection of enemies - Concurrent Queue taht is linked - as enemies will be added and removed frequently
    private Queue<Enemy> enemies;
    // Collection of sprites building the parallax background
    ParallaxBackground background;
    // Our tile map, note that we load it in init()
    TileMap tmap = new TileMap();
    // Final visible pixel on the map, adjusted for player offsets
    float LAST_PIXEL_ON_MAP;
    private final int PLAYER_SCREEN_OFFSET = 200;
    // how fast the player moves left / right
    private float VELOCITY_FACTOR = 0.1f;
    // how long to wait before switching levels in ms.
    private final int LEVEL_CHANGE_DURATION = 2500;
    // The score will be the total time elapsed since a crash
    long total;


    /**
     * The obligatory main method that creates
     * an instance of our class and starts it running
     *
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false,screenWidth,screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers
     */
    public void init()
    {
        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map.txt");

        // the map right border:
        // map total width / 2 due to effect of offset. If player moves right and screen moves left.
        // Accounts for screen size and where the player is frozen on the screen.
        LAST_PIXEL_ON_MAP = (tmap.getPixelWidth()) - (screenWidth - PLAYER_SCREEN_OFFSET);

        // Create a set of background sprites that we can
        // rearrange to give the illusion of motion

        // Initialise the player with an animation
        player = new Hero();
        key = new Key(tmap);
        portal = new Portal();

        // initialise parallax background
        background = new ParallaxBackground(screenWidth, screenHeight, player);
        background.init();

        initialiseLevel();
        System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it to restart
     * the game.
     */
    public void initialiseLevel()
    {
        total = 0;

        // player.setX(player.getWidth() + (screenWidth/3));
        // player.setY(screenHeight - player.getHeight() - tmap.getTileHeight());
        player.setX(PLAYER_SCREEN_OFFSET);
        player.setY(0);
        player.setVelocityX(0);
        player.setVelocityY(0);
        player.show();

        // ensure key is visible when game starts and after level is completed
        key.show();

        // set portal posotion
        portal.setX(230 * tmap.getTileWidth());
        portal.setY(13 * tmap.getTileHeight());
        portal.show();

        // reset enemies
        enemies = new ConcurrentLinkedQueue<Enemy>();
        // reset projectiles
        fires = new ConcurrentLinkedQueue<Projectile>();
    }

    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {
        // Be careful about the order in which you draw objects - you
        // should draw the background first, then work your way 'forward'

        // First work out how much we need to shift the view
        // in order to see where the player is.
        int xo = (int)((player.getX() - PLAYER_SCREEN_OFFSET)*-1);
        int yo = tmap.getTileHeight() * -1;

        // setup background
        g.setColor(new Color(69, 69, 69));
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw parallax background
        background.draw(g);

        // draw each sprite for projectiles
        for (Projectile fireSprite: fires) {
            fireSprite.setOffsets(xo, yo);
            fireSprite.drawTransformed(g);
        }

        // draw enemies
        for (Enemy e: enemies) {
            e.setOffsets(xo, yo);
            // draw the healthbar
            g.setColor(e.getHealthColour());
            g.draw(e.getHealthRectangle(xo));

            e.drawTransformed(g);
        }

        // Apply offsets to player and draw
        // player.setOffsets(xo, yo);
        player.setOffsets(xo, yo);
        g.setColor(player.getHealthColour());
        g.draw(player.getHealthRectangle(xo));
        player.drawTransformed(g);

        // draw the key on top of the map
        key.setOffsets(xo, yo);
        key.draw(g);

        // draw portal
        portal.setOffsets(xo, yo);
        portal.draw(g);

        // Apply offsets to tile map and draw  it
        // requires a different offset from the player
        tmap.draw(g, xo, 0);

        // Show score and status information
        g.setColor(Color.WHITE);
        String timeMsg = String.format("Game Time %d", total);
        g.drawString(timeMsg, getWidth() - 120, 50);


        // debug
        String msgDbug = "x " + (int)player.getX();
        String msgDbug2 = "y " + (int)player.getY();
        g.drawString(msgDbug, getWidth() - 120, 100);
        g.drawString(msgDbug2, getWidth() - 120, 120);


    }

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed)
    {
        // Make adjustments to the speed of the sprite due to gravity
        player.setVelocityY(player.getVelocityY()+(gravity*elapsed));

        player.setAnimationSpeed(1.0f);

        if (flap)
        {
            player.setAnimationSpeed(1.8f);
            player.setVelocityY(-0.2f);
        }

        // update parallax background
        background.update(elapsed);

        // Update model components
        player.update(elapsed);
        portal.update(elapsed);
        key.update(elapsed);
        // loops for the collections
        for (Sprite s: fires) {
            s.update(elapsed);
            if (s.getAnimation().hasLooped()) {
                s.hide();
                fires.remove(s);
            }
        }
        for (Sprite s: enemies) {
            s.update(elapsed);
        }


        // Then check for any collisions that may have occurred
        handlePlayerCollision(player);
        for (Enemy e : enemies) {
            e.setVelocityY(e.getVelocityY()+(gravity*elapsed));
            handleEnemyCollision(e);

            // check if enemies are hit by player projectiles
            for (Projectile f : fires) {
                // todo -
                // only do this if the sprite is on-screen, we don't need to detect for every single enemy.
                if (boundingBoxCollision(e, f)) {
                    // drain & kill the enemy!
                    e.drainHealth();
                    if (e.getHealth() < 0) {
                        enemies.remove(e);
                    }
                }
            }
        }

        // player has collected key
        if (boundingBoxCollision(player, key)) {
            keyCollected();
        }

        // check if end level sequence is needed.
        endLevel();

        total += elapsed;
    }

    /**
     * Checks if player is colliding with any tiles on the map
     * @param s Sprite representing player
     */
    private void handlePlayerCollision(Sprite s) {
        PlayerCollision p = new PlayerCollision(s, tmap, (int)((( s.getX() * -1)/2)), tmap.getTileHeight() * 2);
        p.applyCollisionRules();
        handleTileMapCollisions(s);
    }

    /**
     * Checks if enemy sprite is colliding with any tiles on the map
     * @param s Sprite representing player
     */
    private void handleEnemyCollision(Sprite s) {
        EnemyCollision e = new EnemyCollision(s, tmap, s.getX(), s.getY());
        e.applyCollisionRules();
        handleTileMapCollisions(s);
    }

    /**
     * Checks and handles collisions with the tile map for the
     * given sprite 's'. Initial functionality is limited...
     *
     * @param s			The Sprite to check collisions for
     */
    private void handleTileMapCollisions(Sprite s)
    {
        // map events
        // find the key
        if ((player.getX() / 32 == 143) && (player.getY() / 32 == 13)) {
            keyCollected();
        }

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

    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     *
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e)
    {
        int key = e.getKeyCode();

        // todo -- rm this.
        if (key == KeyEvent.VK_SHIFT) {
            initialiseLevel();
        }

        // todo -- rm this.
        if (key == KeyEvent.VK_R) {
            for (Sprite en : enemies) {
                if (en.getVelocityX() < 0)
                    en.setVelocityX(0.1f);
                else if (en.getVelocityX() >= 0)
                    en.setVelocityX(-0.1f);
            }
        }

        // todo - also remove this.
        if (key == KeyEvent.VK_T) {
            if (VELOCITY_FACTOR == 0.1f) {
                VELOCITY_FACTOR = 1f;
            }
            else {
                VELOCITY_FACTOR = 0.1f;
            }
        }

        if (key == KeyEvent.VK_E) addEnemy();

        if (key == KeyEvent.VK_ESCAPE) stop();

        if (key == KeyEvent.VK_UP) flap = true;

        if (key == KeyEvent.VK_RIGHT) {
            player.setRunning();
            player.setVelocityX(VELOCITY_FACTOR);
            player.setFlipX(false);
        }

        if (key == KeyEvent.VK_LEFT) {
            player.setRunning();
            player.setVelocityX(VELOCITY_FACTOR * -1);
            player.setFlipX(true);
        }

        if (key == KeyEvent.VK_S)
        {
            // Example of playing a sound as a thread
            Sound s = new Sound("sounds/caw.wav");
            s.start();
        }
        if (key == KeyEvent.VK_SPACE)
        {
            Projectile fireSprite = new Projectile(player);
            // fire from middle of player sprite
            fireSprite.setY(player.getY() + player.getHeight()/4);
            // inherit position of fire from player
            fireSprite.show();
            fireSprite.playSound();
            // keeps tracking of current projectiles
            fires.add(fireSprite);
        }

    }

    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
        if (s1.getX() < s2.getX() + s2.getWidth() &&   // check s1's left boundary is on the left of s2's right boundary
                s1.getX() + s1.getWidth() > s2.getX() &&   // check s1's right boundary is on the right of s2's left boundary
                s1.getY() < s2.getY() + s2.getHeight() &&  // check s1's upper boundary  is below s2's upper boundary
                s1.getY() + s1.getHeight() > s2.getY())    // check s1's lower boundary is above s2's lower boundary
        {
            return true;
        }

        return false;
    }


    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        // Switch statement instead of lots of ifs...
        // Need to use break to prevent fall through.
        switch (key)
        {
            case KeyEvent.VK_ESCAPE : stop(); break;
            case KeyEvent.VK_UP     : flap = false; break;
            case KeyEvent.VK_LEFT   :
                if (player.getVelocityX() < 0) {
                    player.setVelocityX(0);
                    player.setIdle();
                }
                break;
            case KeyEvent.VK_RIGHT   :
                if (player.getVelocityX() > 0) {
                    player.setVelocityX(0);
                    player.setIdle();
                }
                break;
            default :  break;
        }
    }

    private void addEnemy() {


        // todo - decide positioning
        //e.setX(player.getX() + 200);
        // e.setY(200);
        //e.setY(tmap.getPixelHeight() - e.getHeight() - tmap.getTileHeight());
        // remove the image's height and 1 layer of the map (the floor)
        // System.out.println();

        Enemy e = new Enemy();
        decideAIPosition(e);

        // display the enemy
        e.show();
        enemies.add(e);
    }

    private void decideAIPosition(Sprite e) {
        int xOFf = (((int)player.getX())*-1) + PLAYER_SCREEN_OFFSET;

        // pick random X position near the player
        float x = player.getX() + PLAYER_SCREEN_OFFSET;
        // find the top player for the current X coordinate
        // start from the floor
        float y = tmap.getPixelHeight() - e.getHeight() - tmap.getTileHeight() + 5;
        while (true) {
            // PlayerCollision p = new PlayerCollision(e, tmap, xOFf, 0);
            int kekX = (int)x/tmap.getTileWidth();
            int kekY = (int)y/tmap.getTileHeight();
            char EEE = tmap.getTileChar(kekX + 200/100, kekY);
            if (EEE == '.') {
                break;
            } else {
                y = y - tmap.getTileHeight();
            }
        }

        e.setX(player.getX() + 200);
        e.setY(200);
    }

    private void keyCollected() {
        // hide the key
        key.hide();

        // open end of game gate.
        tmap.setTileChar('.', 218, 4);
        tmap.setTileChar('.', 219, 4);
        tmap.setTileChar('.', 220, 4);
    }

    /**
     * Initiates the end of level sequence if necessary conditions are met
     */
    private void endLevel() {
        // key has to be invisible as it was collected AND
        // player has to be past the portal
        if (player.getX() > portal.getX() && !key.isVisible()) {
            // this is a trick to prevent the end level trigger from happenning many times
            // while waiting for the level to complete
            // this can be done by a boolean, but the key visibility can serve as the boolean instead.
            key.show();

            // play portal sound to highlight level end
            player.setVelocityX(0);
            portal.playSound();
            // mute and hide any projectiles
            for (Projectile fire : fires) {
                fire.hide();
                fire.stopSound();
            }

            // add a delay using a timer without pausing the current execution thread
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    initialiseLevel();
                }
            }, LEVEL_CHANGE_DURATION);
        }
    }
}
