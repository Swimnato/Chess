package chess.datastructures;

import chess.ChessGame;
import chess.ChessGame.*;

import java.util.Objects;

public class GameData {
    private ChessGame game;
    private String name;
    private String player1;
    private String player2;
    private final int ID;

    public GameData(ChessGame game, String name, int Id) {
        this.game = game;
        this.name = name;
        player1 = null;
        player2 = null;
        this.ID = Id;
    }

    public GameData(ChessGame game, String name, int Id, String player1) {
        this.game = game;
        this.name = name;
        this.player1 = player1;
        this.player2 = null;
        this.ID = Id;
    }

    public GameData(ChessGame game, String name, int Id, String player1, String player2) {
        this.game = game;
        this.name = name;
        this.player1 = player1;
        this.player2 = player2;
        this.ID = Id;
    }

    public GameData(GameData gameToCopy) {
        game = gameToCopy.getGame();
        this.name = gameToCopy.getName();
        player1 = gameToCopy.getPlayer1();
        player2 = gameToCopy.getPlayer2();
        ID = gameToCopy.getID();
    }

    public String getName() {
        return name;
    }

    public boolean hasPlayer(String username) {
        boolean isPlayer1 = (player1 != null && player1.equals(username));
        boolean isPlayer2 = (player2 != null && player2.equals(username));
        return isPlayer1 || isPlayer2;
    }

    public int getID() {
        return ID;
    }

    public void updateBoard(ChessGame gme) {
        game = gme;
    }

    public void setPlayer1(String p1) {
        player1 = p1;
    }

    public void setPlayer2(String p2) {
        player2 = p2;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public ChessGame getGame() {
        return game;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return ((GameData) obj).getGame().equals(game) && ((GameData) obj).getID() == ID && ((GameData) obj).getPlayer2().equals(player2) && ((GameData) obj).getPlayer1().equals(player1);
    }

    @Override
    public String toString() {
        return game.toString() + ' ' + player1 + ' ' + player2 + ' ' + ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGame(), getPlayer1(), getPlayer2(), getID());
    }
}
