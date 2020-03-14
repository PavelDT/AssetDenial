import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import View.ParallaxBackground;
import collisiondetection.Position;
import collisiondetection.Position2;
import game2D.*;

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
	static int screenWidth = 800;
	static int screenHeight = 576;

    float 	lift = 0.08f;
    float	gravity = 0.0003f;
    
    // Game state flags
    boolean flap = false;

    // Game resources
    Animation landing;
    Animation idle;
    private final int fireDuration = 2500;
    
    Sprite	player = null;
    // sprite that uses ship background animation from
    // Collection of projecties fired by the player
    // LinkedQueue is most suitable for time based animations as it's FIFO
    // ConcurrentLinkedQueue used to avoid ConcurrentModificationExceptions.
    // As the data structure is 'linked' there is no array copying when deleting items - this is good for efficiency
    private Queue<Sprite> fires = new ConcurrentLinkedQueue<Sprite>();
    // collection of enemies - Concurrent Queue taht is linked - as enemies will be added and removed frequently
    private Queue<Sprite> enemies = new ConcurrentLinkedQueue<Sprite>();
    // Collection of sprites building the parallax background
    ParallaxBackground background;
    // Our tile map, note that we load it in init()
    TileMap tmap = new TileMap();
    // Final visible pixel on the map, adjusted for player offsets
    float LAST_PIXEL_ON_MAP;
    private final int PLAYER_SCREEN_OFFSET = 200;
    // The score will be the total time elapsed since a crash
    long total;
    Random rng = new SecureRandom();


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
        Sprite s;	// Temporary reference to a sprite

        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map.txt");

        // the map right border:
        // map total width / 2 due to effect of offset. If player moves right and screen moves left.
        // Accounts for screen size and where the player is frozen on the screen.
        LAST_PIXEL_ON_MAP = (tmap.getPixelWidth()/2) - (screenWidth - PLAYER_SCREEN_OFFSET);

        // Create a set of background sprites that we can 
        // rearrange to give the illusion of motion
        
        landing = new Animation();
        landing.loadAnimationFromSheet("images/character_run.png", 9, 1, 120);
        idle = new Animation();
        idle.loadAnimationFromSheet("images/character_idle.png", 9, 1, 120);

        // Initialise the player with an animation
        player = new Sprite(idle);

        // initialise parallax background
        background = new ParallaxBackground(screenWidth, screenHeight, player);
        background.init();

        initialiseGame();
        System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it to restart
     * the game.
     */
    public void initialiseGame()
    {
    	total = 0;

        // player.setX(player.getWidth() + (screenWidth/3));
        // player.setY(screenHeight - player.getHeight() - tmap.getTileHeight());
        player.setX(PLAYER_SCREEN_OFFSET);
        player.setY(0);
        player.setVelocityX(0);
        player.setVelocityY(0);
        player.show();
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
        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, getWidth(), getHeight());

        


        // draw parallax background
        background.draw(g);

        // draw each sprite for projectiles
        for (Sprite fireSprite: fires) {
            fireSprite.setOffsets(xo, yo);
            fireSprite.drawTransformed(g);
        }

        // draw enemies
        for (Sprite s: enemies) {
            s.setOffsets(xo, yo);
            s.drawTransformed(g);
        }

        // Apply offsets to player and draw
        // player.setOffsets(xo, yo);
        player.setOffsets(xo, yo);
        player.drawTransformed(g);

        // Apply offsets to tile map and draw  it
        // requires a different offset from the player
        // tmap.draw(g, (int)player.getX(), 0);
        tmap.draw(g, xo, 0);

        // Show score and status information
        g.setColor(Color.darkGray);
        String timeMsg = String.format("Game Time %d", total);
        g.drawString(timeMsg, getWidth() - 120, 50);


        // debug
        String msgDbug = "x " + (int)player.getX();
        String msgDbug2 = "y " + (int)player.getY();
        g.drawString(msgDbug, getWidth() - 120, 100);
        g.drawString(msgDbug2, getWidth() - 120, 120);

        // xoff and yoff are the offsets applied to tmap when drawn.
        // PLAYER_SCREEN_OFFSET to compensate for the player offset used to position the sprite on the screen
        // Position p = new Position(player, tmap, ((int)player.getX()*-1) - PLAYER_SCREEN_OFFSET, tmap.getTileHeight());
        Position p = new Position(player, tmap, ((int) player.getX() * -1)/2, tmap.getTileHeight());
        g.drawString(p.debugPosition(), getWidth() - 80, 140);
        g.drawString(p.debugPosition2(), getWidth() - 80, 160);
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

       	for (Sprite s: fires) {
       	    s.update(elapsed);
            if (s.getAnimation().hasLooped()) {
                s.hide();
                fires.remove(s);
            }
        }
       	// update enemies
        for (Sprite s: enemies)
            s.update(elapsed);
       	
        // Now update the sprites animation and position
        player.update(elapsed);
       
        // Then check for any collisions that may have occurred
        handleTileMapCollisions(player, elapsed);
        for (Sprite s : enemies) {
            s.setVelocityY(s.getVelocityY()+(gravity*elapsed));
            handleTileMapCollisions(s, elapsed);
        }
        total += elapsed;
    }
    
    
    /**
     * Checks and handles collisions with the tile map for the
     * given sprite 's'. Initial functionality is limited...
     * 
     * @param s			The Sprite to check collisions for
     * @param elapsed	How time has gone by
     */
    public void handleTileMapCollisions(Sprite s, long elapsed)
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


        if (s == player) {
            // for readability.
            final int TOP_LEFT = 0;
            final int TOP_RIGHT = 1;
            final int BOTTOM_LEFT = 2;
            final int BOTTOM_RIGHT = 3;
            final int TOP = 4;
            final int BOTTOM = 5;

            // check if player is colliding with map objects.
            Position p = new Position(player, tmap, (int)((( s.getX() * -1)/2)), tmap.getTileHeight() * 2);
            boolean[] collisionStatus = p.detectCollision();


            // check sprite Top left
            if (collisionStatus[TOP_LEFT]) {
                float currentX = s.getX();
                s.setX(currentX + 2f);
            }
            // check sprite Top Right
            if (collisionStatus[TOP_RIGHT]) {
                float currentX = s.getX();
                s.setX(currentX - 2f);
            }
            // check sprite Bottom Left
            if (collisionStatus[BOTTOM_LEFT]) {
                float currentX = s.getX();
                s.setX(currentX + 2f);
            }
            // check sprite Bottom Right
            if (collisionStatus[BOTTOM_RIGHT]) {
                float currentX = s.getX();
                s.setX(currentX - 2f);
            }
            if (collisionStatus[BOTTOM]) {
                s.setVelocityY(0.02f);
                float currentY = s.getY();
                s.setY(currentY - 1f);
            }
            if (collisionStatus[TOP]) {
                s.setVelocityY(-0.02f);
                float currentY = s.getY();
                s.setY(currentY + 5f);
            }
        } else {

            final int LEFT = 0;
            final int RIGHT = 1;
            final int TOP = 2;
            final int BOTTOM = 3;
            Position2 p2 = new Position2(player, tmap, s.getX(), s.getY());
            boolean[] collisionStatus2 = p2.detectCollision();


            // check sprite Top Right
            if (collisionStatus2[RIGHT]) {
                s.setVelocityX(0.01f);
                System.out.println("RIGHT");
                float currentX = s.getX();
                s.setX(currentX - 2f);
            }
            if (collisionStatus2[LEFT]) {
                s.setVelocityX(-0.01f);
                System.out.println("LEFT");
                float currentX = s.getX();
                s.setX(currentX + 2f);
            }


            if (collisionStatus2[BOTTOM]) {
                s.setVelocityY(0.02f);
                float currentY = s.getY();
                s.setY(currentY - 2f);
            }
            if (collisionStatus2[TOP]) {
                s.setVelocityY(-0.02f);
                float currentY = s.getY();
                s.setY(currentY + 5f);
            }
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
        if (key == KeyEvent.VK_R) {
            for (Sprite en : enemies) {
                if (en.getVelocityX() < 0)
                    en.setVelocityX(0.1f);
                else if (en.getVelocityX() >= 0)
                    en.setVelocityX(-0.1f);
            }
        }

    	if (key == KeyEvent.VK_E) addEnemy();
    	
    	if (key == KeyEvent.VK_ESCAPE) stop();
    	
    	if (key == KeyEvent.VK_UP) flap = true;

    	if (key == KeyEvent.VK_RIGHT) {
    	    player.setAnimation(landing);
    	    player.setVelocityX(0.1f);
    	    player.setFlipX(false);
        }

        if (key == KeyEvent.VK_LEFT) {
            player.setAnimation(landing);
            player.setVelocityX(-0.1f);
            player.setFlipX(true);
         }
    	   	
    	if (key == KeyEvent.VK_S)
    	{
    		// Example of playing a sound as a thread
    		Sound s = new Sound("sounds/caw.wav");
    		s.start();
    	}
        if (key == KeyEvent.VK_D)
        {
            // Example of playing a sound as a thread
            Sound s = new Sound("sounds/Dic Logo.wav");
            s.start();
        }
        if (key == KeyEvent.VK_SPACE)
        {
            Sound s = new Sound("sounds/fire.wav");
            s.start();

            Animation fire = new Animation();
            fire.addFrame(loadImage("images/fire.png"), fireDuration);
            Sprite fireSprite = new Sprite(fire);

            // place on right side of player
            if (player.getFlipX()) {
                fireSprite.setX(player.getX() - fireSprite.getWidth());
            } else {
                fireSprite.setX(player.getX() + player.getWidth());
            }
            fireSprite.setY(player.getY());
            // inherit position of fire from player
            fireSprite.setFlipX(player.getFlipX());
            fireSprite.show();

            fires.add(fireSprite);
        }

    }

    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
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
                    player.setAnimation(idle);
                }
                break;
            case KeyEvent.VK_RIGHT   :
                if (player.getVelocityX() > 0) {
                    player.setVelocityX(0);
                    player.setAnimation(idle);
                }
                break;
            default :  break;
		}
	}

	private void addEnemy() {
        Animation enemy = new Animation();
        enemy.loadAnimationFromSheet("images/enemy2.png", 4, 1, 60);
        enemy.play();
        Sprite e = new Sprite (enemy);

        // todo - decide positioning
        //e.setX(player.getX() + 200);
        // e.setY(200);
        //e.setY(tmap.getPixelHeight() - e.getHeight() - tmap.getTileHeight());
        // remove the image's height and 1 layer of the map (the floor)
        // System.out.println();


        decideAIPosition(e);

        // display the enemy
        e.show();
        enemies.add(e);
    }

    private void decideAIPosition(Sprite e) {
        int xOFf = (((int)player.getX())*-1) + PLAYER_SCREEN_OFFSET;

        // pick random X position near the player
        float x = player.getX() + rng.nextInt(screenWidth - PLAYER_SCREEN_OFFSET);
        // find the top player for the current X coordinate
        // start from the floor
        float y = tmap.getPixelHeight() - e.getHeight() - tmap.getTileHeight() + 5;
        while (true) {
            // Position p = new Position(e, tmap, xOFf, 0);
            int kekX = (int)x/tmap.getTileWidth();
            int kekY = (int)y/tmap.getTileHeight();
            char EEE = tmap.getTileChar(kekX + 200/32, kekY);
            if (EEE == '.') {
                break;
            } else {
                y = y - tmap.getTileHeight();
            }
        }

        e.setX(player.getX() + 200);
        e.setY(200);

//        e.setX(x);
//        e.setY(y);
        // System.out.println(x + "," + y);
    }
}
