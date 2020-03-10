import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import collisiondetection.Position;
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
	static int screenHeight = 550;

    float 	lift = 0.08f;
    float	gravity = 0.0003f;
    
    // Game state flags
    boolean flap = false;

    // Game resources
    Animation landing;
    Animation fire;
    private final int fireDuration = 2500;
    
    Sprite	player = null;
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    // requires concurrent collection as projectiles are added
    // as others are being removed. The concurrent collection allows for
    // safeguarding against ConcurrentModificationException.
    Map<Sprite, Long> fires = new ConcurrentHashMap();

    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
    
    long total;         			// The score will be the total time elapsed since a crash


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

        // Create a set of background sprites that we can 
        // rearrange to give the illusion of motion
        
        landing = new Animation();
        landing.loadAnimationFromSheet("images/landbird2.png", 8, 1, 60);
        fire = new Animation();
        // fire.addFrame(loadImage("images/fire.png"), 1000);
        fire.loadAnimationFromSheet("images/fire.png",1, 1, fireDuration);

        // Initialise the player with an animation
        player = new Sprite(landing);
        
        // Load a single cloud animation
        Animation ca = new Animation();
        ca.addFrame(loadImage("images/cloud.png"), 1000);

        Animation fr = new Animation();
        fr.addFrame(loadImage("images/fire.png"),1000);

        // Create 3 clouds at random positions off the screen
        // to the right
        for (int c=0; c<3; c++)
        {
        	s = new Sprite(ca);
        	s.setX(screenWidth + (int)(Math.random()*200.0f));
        	s.setY(30 + (int)(Math.random()*150.0f));
        	s.setVelocityX(-0.02f);
        	s.show();
        	clouds.add(s);
        }


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
    	      
        player.setX(64);
        player.setY(280);
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
        int xo = ((int)player.getX())*-1;
        int yo = ((int)player.getY())*-1;

        // If relative, adjust the offset so that
        // it is relative to the player

        // ...?
//        int xo = ((int)player.getX())*-1;
//        int yo = ((int)player.getY())*-1;
        
        g.setColor(Color.blue);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Apply offsets to sprites then draw them
        for (Sprite s: clouds)
        {
        	s.setOffsets(xo+400,yo+280);
        	s.draw(g);
        }
        // draw each sprite for projectiles
        for (Map.Entry<Sprite, Long> fireSprite: fires.entrySet()) {
            fireSprite.getKey().setOffsets(xo+400,yo+280);
            fireSprite.getKey().draw(g);
        }

        // Apply offsets to player and draw 
        // player.setOffsets(xo, yo);
        player.setOffsets(xo+400,yo+280);
        player.draw(g);
                
        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo+400,yo+280);

        
        // Show score and status information
        String msg = String.format("Score: %d", total/100);
        g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 80, 50);
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
                
       	for (Sprite s: clouds)
       		s.update(elapsed);
       	// update projectile elapsed time and check if any animations are completed
        for (Map.Entry<Sprite, Long> fireSprite: fires.entrySet()) {
            // check if animation has completed, if sh hide the sprite and remove it
            if (System.currentTimeMillis() > (fireSprite.getValue() + fireDuration)) {
                fireSprite.getKey().hide();
                fires.remove(fireSprite.getKey());
            } else {
                fireSprite.getKey().update(elapsed);
            }
        }
       	
        // Now update the sprites animation and position
        player.update(elapsed);
       
        // Then check for any collisions that may have occurred
        handleTileMapCollisions(player,elapsed);
         	
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
    	// This method should check actual tile map collisions. For
    	// now it just checks if the player has gone off the bottom
    	// of the tile map.
    	
        if (s.getY() + s.getHeight() > tmap.getPixelHeight())
        {
        	// Put the player back on the map
        	player.setY(tmap.getPixelHeight() - player.getHeight());
        	
        	// and make them bounce
        	player.setVelocityY(-player.getVelocityY() * (0.0f * elapsed));
        }


        Position p = new Position(s, tmap);
         p.debugPosition();
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
    	
    	if (key == KeyEvent.VK_ESCAPE) stop();
    	
    	if (key == KeyEvent.VK_UP) flap = true;

    	if (key == KeyEvent.VK_RIGHT) {
    	    player.setVelocityX(0.1f);
        }//else {
           // player.setVelocityX(player.getVelocityX() - 0.02f);
        //}

        if (key == KeyEvent.VK_LEFT) {
            player.setVelocityX(-0.1f);
         }//else {
//            player.setVelocityX(player.getVelocityX() + 0.02f);
//        }
    	   	
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


            fire.play();
            fire.start();
            Sprite fireSprite = new Sprite(fire);
            // place on right side of player
            fireSprite.setX(player.getX() + player.getWidth());
            fireSprite.setY(player.getY());
            fireSprite.show();

            fires.put(fireSprite, System.currentTimeMillis());
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
                if (player.getVelocityX() < 0)
                    player.setVelocityX(0);
                break;
            case KeyEvent.VK_RIGHT   :
                if (player.getVelocityX() > 0)
                    player.setVelocityX(0);
                break;
            default :  break;
		}
	}
}
