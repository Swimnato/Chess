package chess.dataStructures;

public class GameOverview {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;

    public GameOverview(GameData input) {
        gameID = input.getID();
        whiteUsername = input.getPlayer1();
        blackUsername = input.getPlayer2();
        gameName = input.getName();
    }

    public GameOverview(int ID, String White, String Black, String Name) {
        gameID = ID;
        whiteUsername = White;
        blackUsername = Black;
        gameName = Name;
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
