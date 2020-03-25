import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import collisiondetection.MapCollision;
import collisiondetection.SpriteCollision;
import state.StateManager;
import view.MenuManager;
import view.ParallaxBackground;
import collisiondetection.PlayerCollision;
import collisiondetection.EnemyCollision;
import game2D.*;
import sprite.*;


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
    private boolean jump = false;

    private Hero player = null;
    // Key used to unlock exit for level.
    private Key key = null;
    // Portal used to complete level
    private Portal portal = null;
    // menu manager
    private MenuManager menu;
    // sprite that uses ship background animation from
    // Collection of projecties fired by the player
    // LinkedQueue is most suitable for time based animations as it's FIFO
    // ConcurrentLinkedQueue used to avoid ConcurrentModificationExceptions.
    // As the data structure is 'linked' there is no array copying when deleting items - this is good for efficiency
    private Queue<Projectile> fires;
    // collection of enemies - Concurrent Queue taht is linked - as enemies will be added and removed frequently
    private Queue<Enemy> enemies;
    // the boss of level 3
    private Boss boss;
    // Collection of sprites building the parallax background
    private ParallaxBackground background;
    // Our tile map, note that we load it in init()
    private TileMap tmap = new TileMap();
    // Handlers for collision
    private MapCollision mapCollision;
    // Background sound
    Sound backgroundSound;
    // position of where the player sprite is frozen onto the screen.
    private final int PLAYER_SCREEN_OFFSET = 200;
    // how fast the player moves left / right
    private float VELOCITY_FACTOR = 0.1f;
    // how long to wait before switching levels in ms.
    private final int LEVEL_CHANGE_DURATION = 2500;
    // Represents total time since game elapsed
    private long total;
    // tracks player progress
    private int LEVEL = 1;
    // used for random number generation
    private SecureRandom rng;
    // tracks game state
    private StateManager gameState;


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
        gct.run(false, screenWidth, screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers
     */
    public void init()
    {
        // Create a set of background sprites that we can
        // rearrange to give the illusion of motion

        // initialise game state
        gameState = new StateManager();

        // Initialise the player with an animation
        player = new Hero();
        key = new Key();
        portal = new Portal();

        // initialise parallax background
        background = new ParallaxBackground(screenWidth, screenHeight, player);
        background.init();

        rng = new SecureRandom();

        menu = new MenuManager(screenWidth, screenHeight);

        backgroundSound = new Sound("sounds/retribution.wav", Sound.NO_EFFECT, true);
        backgroundSound.start();

        initialiseLevel();
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it to restart
     * the game.
     */
    public void initialiseLevel()
    {
        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map" + LEVEL + ".txt");
        System.out.println(tmap);

        // ensure sound is at normal speed
        backgroundSound.startLevel();

        // restore player health
        player.resetHealth();

        // initialise collision handler for the map
        mapCollision = new MapCollision(screenWidth, tmap, player, key, PLAYER_SCREEN_OFFSET);

        total = 0;

        // player.setX(player.getWidth() + (screenWidth/3));
        // player.setY(screenHeight - player.getHeight() - tmap.getTileHeight());
        player.setX(PLAYER_SCREEN_OFFSET);
        player.setY(0);
        player.setVelocityX(0);
        player.setVelocityY(0);
        player.show();

        // pick key position
        key.positionKey(tmap, LEVEL);
        // ensure key is visible when game starts and after level is completed
        key.show();

        // set portal posotion
        portal.setPosition(tmap.getTileWidth(), tmap.getTileHeight());
        portal.initSound();
        if (LEVEL != 3)
            portal.show();

        // reset enemies
        enemies = new ConcurrentLinkedQueue<Enemy>();
        addEnemies(LEVEL);

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

        // set up font to be used
        g.setFont(new Font("Arial", Font.PLAIN,12));

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

        // Apply offsets to player and draw
        // player.setOffsets(xo, yo);
        player.setOffsets(xo, yo);
        g.setColor(player.getHealthColour());
        g.draw(player.getHealthRectangle(xo));
        player.drawTransformed(g);

        // draw enemies
        for (Enemy e: enemies) {
            e.setOffsets(xo, yo);
            e.drawTransformed(g, xo, yo, player.getX());
        }

        if (boss != null) {
            boss.drawTransformed(g, xo, yo, player.getX());
        }

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
        String msgDbug = "Enemies Remaining " + enemies.size();
        String msgDbug2 = "Level " + LEVEL;
        g.drawString(msgDbug, getWidth() - 200, 100);
        g.drawString(msgDbug2, getWidth() - 200, 120);

        if (gameState.getPause() && ! gameState.getWin()) {
            int state = gameState.decideState(player.getHealth(), LEVEL, gameState.getPause(), boss);
            menu.draw(g, state);
        } else if (gameState.getWin()) {
            g.setColor(new Color(50, 50, 50, 200));
            g.fillRect(0,0, screenWidth, screenHeight);
            g.setColor(new Color(50, 255, 255));
            g.setFont(new Font("Courier", Font.BOLD,44));
            g.drawString("GAME WON!!!", screenWidth/2 - 200, screenHeight/2);
            g.setFont(new Font("Courier", Font.BOLD,24));
            g.drawString("Press Q to continue", screenWidth/2 - 140, screenHeight/2 + 50);
        }
    }

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed)
    {
        // dont update anything if the game is paused
        if (gameState.getPause()) {
            return;
        }

        // todo -- display "you died" message etc.
        if (player.getHealth() < 1) {
            gameState.setPause(true);
        }

        // Make adjustments to the speed of the sprite due to gravity
        player.setVelocityY(player.getVelocityY()+(gravity*elapsed));

        player.setAnimationSpeed(1.0f);

        if (jump)
        {
            player.setJump();
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
        for (Projectile s: fires) {
            s.update(elapsed);
            if (s.getAnimation().hasLooped()) {
                s.hide();
                fires.remove(s);
                if (player.getVelocityX() > 0) {
                    player.setRunning();
                } else {
                    player.setIdle();
                }
            }
        }

        // Then check for any collisions that may have occurred
        handlePlayerCollision(player);
        for (Enemy e : enemies) {
            e.update(elapsed);
            e.setVelocityY(e.getVelocityY()+(gravity * elapsed));
            handleEnemyCollision(e);

            // check if enemies are hit by player projectiles
            for (Projectile f : fires) {
                // only do this if the sprite is on-screen, we don't need to detect for every single enemy.
                if (SpriteCollision.boundingBoxCollision(e, f)) {
                    // drain & kill the enemy!
                    e.drainHealth();
                    if (e.getHealth() < 0) {
                        enemies.remove(e);
                    }
                }
            }

            // check if player is hit by enemy attacks
            if (SpriteCollision.boundingBoxCollision(player, e.getAttack())) {
                player.drainHealth();
                // speed music up if playr is low hp.
                if (player.getHealth() < 500) {
                    backgroundSound.switchEffect(Sound.FAST_EFFECT);
                }
            }
        }

        if (boss != null) {
            boss.update(elapsed);
            handleEnemyCollision(boss);
            // check if enemies are hit by player projectiles
            for (Projectile f : fires) {
                // todo -
                // only do this if the sprite is on-screen, we don't need to detect for every single enemy.
                if (SpriteCollision.boundingBoxCollision(boss, f)) {
                    // drain & kill the enemy!
                    boss.drainHealth();
                    if (boss.getHealth() < 0 && boss.isVisible()) {
                        gg();
                    }
                }
            }

            // check if player is hit by boss attacks -- todo
//            if (SpriteCollision.boundingBoxCollision(player, e.getAttack())) {
//                player.drainHealth();
//                // speed music up if playr is low hp.
//                if (player.getHealth() < 500) {
//                    backgroundSound.switchEffect(Sound.FAST_EFFECT);
//                }
//            }
        }

        // player has collected key
        if (SpriteCollision.boundingBoxCollision(player, key)) {
            key.keyCollected(tmap, LEVEL);
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
        mapCollision.handleTileMapCollisions(s);

        // find the key
        if ((player.getX() / 32 == 143) && (player.getY() / 32 == 13)) {
            key.keyCollected(tmap, LEVEL);
        }
    }

    /**
     * Checks if enemy sprite is colliding with any tiles on the map
     * @param s Sprite representing player
     */
    private void handleEnemyCollision(Sprite s) {
        EnemyCollision e = new EnemyCollision(s, tmap, s.getX(), s.getY());
        e.applyCollisionRules();
        mapCollision.handleTileMapCollisions(s);
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

        // check if player wants to unpause
        if (key == KeyEvent.VK_ESCAPE) togglePause();

        if (key == KeyEvent.VK_Q) {
            // the game has been won and the user pressed "Q"
            // reset the game and display the menu
            if (gameState.getWin()) {
                gameState.setPause(true);
                gameState.setWin(false);
                initialiseLevel();
            }
        }

        if (gameState.getPause()) {
            if (key == KeyEvent.VK_UP && menu.menuItem > 1) {
                menu.menuItem--;
            }
            if (key == KeyEvent.VK_DOWN && menu.menuItem < 4) {
                menu.menuItem++;
            }

            if (key == KeyEvent.VK_ENTER) {
                managePause(menu.menuItem);
            }
            // we don't care about remaining key events when the game is paused.
            return;
        }

        if (key == KeyEvent.VK_UP){
            jump = true;
        } else {
            jump = false;
        }

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
            Sound s = new Sound("sounds/caw.wav", Sound.NO_EFFECT, false);
            s.start();
        }
        if (key == KeyEvent.VK_SPACE)
        {
            player.setFire();
            Projectile fireSprite = new Projectile(player);
            // fire from middle of player sprite
            fireSprite.setY(player.getY() + player.getHeight()/4f);
            // inherit position of fire from player
            fireSprite.show();
            fireSprite.playSound();
            // keeps tracking of current projectiles
            fires.add(fireSprite);
        }
    }

    /**
     * Override of the keyReleased event defined in GameCore to catch our
     * own events when a key is released
     *
     *  @param e The event that has been generated
     */
    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        // Switch statement instead of lots of ifs...
        // Need to use break to prevent fall through.
        switch (key)
        {
            case KeyEvent.VK_UP     : jump = false; break;
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

    /**
     * Adds enemies to interact with the player
     * @param level - Current level of player, increases number of enemies spawned
     */
    private void addEnemies (int level) {
        // decide what enemies to add based on level
        switch (level) {
            // for first two levels spawn lots of enemies
            case 1:
            case 2:
                int enemyCount = 5 * level;
                int rangeWidth = (tmap.getPixelWidth() - screenWidth) / enemyCount;
                for (int i = 0; i < enemyCount; i++) {

                    int range = i * rangeWidth;
                    // reduce the range by 300, ensures enemies don't spawn on player.
                    if (i == 0) {
                        range += 300;
                    }
                    Enemy e = new Enemy();
                    e.setX(rng.nextInt(rangeWidth) + range);
                    e.setAttackInitialX();
                    e.show();
                    enemies.add(e);
                }
                break;
            case 3: // level 3 is a boss battle
                boss = new Boss();
                boss.setX(300);
                boss.show();
                break;
            default:
                // do nothing if a level isn't specified
                break;
        }
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

            LEVEL = LEVEL + 1;

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


    /**
     * Pauses and Unpauses the game
     */
    private void togglePause() {
        // once the game has started and the initial menu is displayed
        // it is now treated as a paused game
        if (gameState.getNewGame()) {
            gameState.setNewGame(false);
        }

        // toggle pausing
        if (!gameState.getPause()) {
            // if the game isn't paused, then pause it
            gameState.setPause(true);
            player.setIdle();
            player.pauseAnimation();
            portal.pauseAnimation();
            for (Enemy e : enemies) {
                e.pauseAnimation();
                e.getAttack().pauseAnimation();
            }
            for (Projectile f : fires) {
                f.pauseAnimation();
            }
        } else {
            // if the game is paused, un-pause it
            gameState.setPause(false);
            player.playAnimation();
            portal.playAnimation();
            for (Enemy e : enemies) {
                e.playAnimation();
                e.getAttack().playAnimation();
            }
            for (Projectile f : fires) {
                f.playAnimation();
            }
        }
    }

    /**
     * Handles the un-pause event by running the selected menu option
     * @param menuIndex - menu item that was selected during pause
     */
    private void managePause(int menuIndex) {
        switch (menuIndex) {
            case 1:
                // unpause
                togglePause();
                break;
            case 2:
                // restart level
                initialiseLevel();
                togglePause();
                break;
            case 3:
                // debug mode - go very fast ignoring collision
                if (VELOCITY_FACTOR == 0.1f) {
                    VELOCITY_FACTOR = 1f;
                }
                else {
                    VELOCITY_FACTOR = 0.1f;
                }
                togglePause();
                break;
            case 4:
                stop();
                break;
            default:
                System.out.println("Issue with selection, defaulting to un-pausing");
                togglePause();
                break;
        }
    }


    /**
     * Initiates end-of-game sequence
     */
    private void gg() {
        // hide the boss and set it to null
        boss.hide();
        // pause the game and display the new game menu
        gameState.setNewGame(true);
        gameState.setPause(true);
        gameState.setWin(true);
        // reset levels
        LEVEL = 1;

    }
}
