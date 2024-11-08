package chess.datastructures;

public class GamesList {
    GameOverview[] games;

    GamesList(GameOverview[] games) {
        this.games = games;
    }

    public GameOverview[] getGames() {
        return games;
    }

    public void setGames(GameOverview[] games) {
        this.games = games;
    }
}
