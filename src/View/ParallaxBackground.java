package View;

import game2D.Animation;
import game2D.Sprite;


import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class ParallaxBackground {

    Sprite player;
    final int screenHeight;
    List<Sprite> parallax = new ArrayList<>();

    public ParallaxBackground(int screenHeight, Sprite player) {
        this.screenHeight = screenHeight;
        this.player = player;
    }

    public void init() {
        // parallax background setup
        Animation background = new Animation();
        background.addFrame(new ImageIcon("maps/parallax/background.png").getImage(), 60);
        Sprite backgroundSprite = new Sprite(background);
        backgroundSprite.setY(screenHeight - backgroundSprite.getHeight());
        backgroundSprite.show();
        parallax.add(backgroundSprite);
        int BACKGROUND_VISIBLE = 130;
        // background can repeat.
        // parallax.add(backgroundSprite);
        // first building
        Animation building1 = new Animation();
        building1.addFrame(new ImageIcon("maps/parallax/building1.png").getImage(), 60);
        Sprite buildingSprite1 = new Sprite(building1);
        buildingSprite1.setY(screenHeight - buildingSprite1.getHeight() - BACKGROUND_VISIBLE);
        buildingSprite1.setX(300);
        buildingSprite1.show();
        parallax.add(buildingSprite1);
        // 2nd building
        Animation building2 = new Animation();
        building2.addFrame(new ImageIcon("maps/parallax/building2.png").getImage(), 60);
        Sprite buildingSprite2 = new Sprite(building2);
        buildingSprite2.setY(screenHeight - buildingSprite2.getHeight() - BACKGROUND_VISIBLE);
        buildingSprite2.setX(800);
        buildingSprite2.show();
        parallax.add(buildingSprite2);
        // 3th building
        Animation building3 = new Animation();
        building3.addFrame(new ImageIcon("maps/parallax/building3.png").getImage(), 60);
        Sprite buildingSprite3 = new Sprite(building3);
        buildingSprite3.setY(screenHeight - buildingSprite3.getHeight() - BACKGROUND_VISIBLE);
        buildingSprite3.setX(500);
        buildingSprite3.show();
        parallax.add(buildingSprite3);
    }

    public void draw(Graphics2D g) {
        // move mountains only horizontally, creates an affect of them being far away
        parallax.get(3).setOffsets((int)(player.getX() * 0.1f) * -1, 0);
        parallax.get(3).drawTransformed(g);
        parallax.get(2).setOffsets((int)(player.getX() * 0.3f) * -1, 0);
        parallax.get(2).drawTransformed(g);
        parallax.get(1).setOffsets((int)(player.getX() * 0.2f) * -1, 0);
        parallax.get(1).drawTransformed(g);
        // background is on top of buildings
        parallax.get(0).setOffsets((int)(player.getX() * 0.15f) * -1, 0);
        parallax.get(0).drawTransformed(g);
    }

    public void update(long elapsed) {
        // parallax update
        for (Sprite component: parallax) {
            component.update(elapsed);
        }
    }
}
