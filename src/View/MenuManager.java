package view;

import state.StateManager;

import java.awt.*;

public class MenuManager {

    private int screenHeight;
    private int screenWidth;
    public short menuItem = 1;

    public MenuManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     *
     * @param g
     * @param gameState - State of the game 1 - just started, 2 - paused, 3 - gameover
     */
    public void draw(Graphics2D g, int gameState) {
        int menuItemWidth = 300;
        int menuItemHeight = 80;
        int menuItemOffset = screenHeight / 6;

        g.setColor(new Color(50, 50, 50, 200));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // add border to selected item
        g.setColor(Color.RED);
        g.fillRect(screenWidth/2 - (menuItemWidth+20)/2, (menuItemOffset * menuItem) - 10, menuItemWidth + 20, menuItemHeight+20);


        int xMenu = screenWidth/2 - menuItemWidth/2;
        g.setColor(new Color(250, 250, 250));
        // un-pause
        g.fillRect(xMenu, menuItemOffset, menuItemWidth, menuItemHeight);
        // restart level
        g.fillRect(screenWidth/2 - menuItemWidth/2, menuItemOffset * 2, menuItemWidth, menuItemHeight);
        // debug mode
        g.fillRect(screenWidth/2 - menuItemWidth/2, menuItemOffset * 3, menuItemWidth, menuItemHeight);
        // exit
        g.fillRect(screenWidth/2 - menuItemWidth/2, menuItemOffset * 4, menuItemWidth, menuItemHeight);


        g.setColor(Color.BLACK);
        Font font = new Font("Courier", Font.BOLD,24);
        g.setFont(font);
        short charWidth = 7;

        String menuItem1Msg = "";
        if (gameState == StateManager.STATE_NEW || gameState == StateManager.STATE_GAMEOVER) {
            menuItem1Msg = "New Game";
        } else if (gameState == StateManager.STATE_PAUSE) {
            menuItem1Msg = "Un-Pause";
        }


        g.drawString(menuItem1Msg, xMenu + menuItemWidth/2 - (7 * charWidth) , menuItemOffset + menuItemHeight/2);
        g.drawString("Restart Level", xMenu + menuItemWidth/2 - (13 * charWidth), menuItemOffset * 2 + menuItemHeight/2);
        g.drawString("Debug Mode", xMenu + menuItemWidth/2 - (10 * charWidth), menuItemOffset * 3 + menuItemHeight/2);
        g.drawString("Exit", xMenu + menuItemWidth/2 - (4 * charWidth), menuItemOffset * 4 + menuItemHeight/2);
    }
}
