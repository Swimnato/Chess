package chess.datastructures;

public class CreateGameInfo {
    String gameName;

    CreateGameInfo(String gameName) {
        this.gameName = gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }
}
