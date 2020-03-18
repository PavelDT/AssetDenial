package collisiondetection;

import game2D.Sprite;

public class SpriteCollision {

    /**
     * Checks if two sprites are colliding.
     * @param s1 First Sprite to check
     * @param s2 Second sprite to check
     * @return Boolean representing if two sprites are overlapping
     */
    public static boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
        if (s1.getX() < s2.getX() + s2.getWidth() &&       // check s1's left boundary is on the left of s2's right boundary
                s1.getX() + s1.getWidth() > s2.getX() &&   // check s1's right boundary is on the right of s2's left boundary
                s1.getY() < s2.getY() + s2.getHeight() &&  // check s1's upper boundary  is below s2's upper boundary
                s1.getY() + s1.getHeight() > s2.getY())    // check s1's lower boundary is above s2's lower boundary
        {
            return true;
        }

        return false;
    }
}
