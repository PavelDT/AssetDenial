package state;

import sprite.Boss;

public class StateManager {

    public static int STATE_NEW = 1;
    public static int STATE_PAUSE = 2;
    public static int STATE_GAMEOVER = 3;
    public static int STATE_GAMEWON = 4;

    private boolean WIN = false;
    private boolean PAUSE = true;
    private boolean NEW_GAME = true;

    public int decideState(int playerHealth, int level, boolean paused, Boss boss) {
        if (playerHealth < 1) {
            return STATE_GAMEOVER;
        } else if (NEW_GAME) {
            return STATE_NEW;
        } else if (paused) {
            return STATE_PAUSE;
        } else if (level == 3 & boss != null & boss.getHealth() < 1) {
            return STATE_GAMEWON;
        }

        return STATE_NEW;
    }

    public boolean getWin() {
        return WIN;
    }

    public void setWin(boolean win) {
        WIN = win;
    }

    public boolean getPause() {
        return PAUSE;
    }

    public void setPause(boolean pause) {
        PAUSE = pause;
    }

    public boolean getNewGame() {
        return NEW_GAME;
    }

    public void setNewGame(boolean newGame) {
        NEW_GAME = newGame;
    }
}
