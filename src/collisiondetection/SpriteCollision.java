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
        float s1width = s1.getWidth() * (float)s1.getScale();
        float s1height = s1.getHeight() * (float)s1.getScale();

        float s2width = s2.getWidth() * (float)s2.getScale();
        float s2height = s2.getHeight() * (float)s2.getScale();

        if (s1.getX() < s2.getX() + s2width &&       // check s1's left boundary is on the left of s2's right boundary
                s1.getX() + s1width > s2.getX() &&   // check s1's right boundary is on the right of s2's left boundary
                s1.getY() < s2.getY() + s2height &&  // check s1's upper boundary  is below s2's upper boundary
                s1.getY() + s1height > s2.getY())    // check s1's lower boundary is above s2's lower boundary
        {
            return true;
        }

        return false;
    }
}
