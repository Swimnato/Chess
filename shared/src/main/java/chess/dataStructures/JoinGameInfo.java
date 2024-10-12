package chess.dataStructures;
import chess.ChessGame;

public class JoinGameInfo {
    private ChessGame.TeamColor desiredColor;
    private int gameID;
    JoinGameInfo(ChessGame.TeamColor color, int ID){
        desiredColor = color;
        gameID = ID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void setDesiredColor(ChessGame.TeamColor desiredColor) {
        this.desiredColor = desiredColor;
    }

    public int getGameID() {
        return gameID;
    }

    public ChessGame.TeamColor getDesiredColor() {
        return desiredColor;
    }
}
