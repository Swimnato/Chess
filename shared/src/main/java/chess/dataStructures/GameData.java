package chess.dataStructures;

import chess.ChessGame;
import chess.ChessGame.*;

import java.util.Objects;

public class GameData {
    private ChessGame game;
    private String player1;
    private String player2;
    private final int ID;
    public GameData(ChessGame _gme, int _ID){
        game = _gme;
        player1 = null;
        player2 = null;
        ID = _ID;
    }
    public GameData(ChessGame _gme, int _ID , String _p1){
        game = _gme;
        player1 = _p1;
        player2 = null;
        ID = _ID;
    }
    public GameData(ChessGame _gme, int _ID , String _p1, String _p2){
        game = _gme;
        player1 = _p1;
        player2 = _p2;
        ID = _ID;
    }
    public GameData(GameData _game){
        game = _game.getGame();
        player1 = _game.getPlayer1();
        player2 = _game.getPlayer2();
        ID = _game.getID();
    }

    public boolean hasPlayer(String _username){
        return (player1.equals(_username)) || (player2.equals(_username));
    }

    public int getID() {
        return ID;
    }

    public void updateBoard(ChessGame _gme){
        game = _gme;
    }

    public void setPlayer1(String _p1){
        player1 = _p1;
    }

    public void setPlayer2(String _p2){
        player2 = _p2;
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
        if(this == obj){
            return true;
        }
        if(obj == null || obj.getClass() != this.getClass()){
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
