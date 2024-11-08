package chess.datastructures;

import chess.ChessGame;

public class JoinGameInfo {
    private String playerColor;
    private int gameID;

    public JoinGameInfo(String color, int id) {
        playerColor = color;
        gameID = id;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void setplayerColor(String desiredColor) {
        this.playerColor = desiredColor;
    }

    public int getGameID() {
        return gameID;
    }

    public String getplayerColor() {
        return playerColor;
    }
}
