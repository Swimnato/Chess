package chess.datastructures;

public class GameOverview {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;

    public GameOverview(GameData input) {
        gameID = input.getId();
        whiteUsername = input.getPlayer1();
        blackUsername = input.getPlayer2();
        gameName = input.getName();
    }

    public GameOverview(int id, String white, String black, String name) {
        gameID = id;
        whiteUsername = white;
        blackUsername = black;
        gameName = name;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public String getGameName() {
        return gameName;
    }

    public int getGameID() {
        return gameID;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }
}
