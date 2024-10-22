package chess.datastructures;

import chess.ChessGame;

public class JoinGameInfo {
    private String playerColor;
    private int gameID;

    JoinGameInfo(String color, int Id) {
        playerColor = color;
        gameID = Id;
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
