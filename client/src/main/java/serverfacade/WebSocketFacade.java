package serverfacade;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.datastructures.GameID;
import chess.datastructures.JoinGameInfo;
import com.google.gson.Gson;
import commandparser.InvalidSyntaxException;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URISyntaxException;

import static chess.ui.EscapeSequences.*;

public class WebSocketFacade {

    int authToken;
    ChessGame currentGame = null;
    ChessGame.TeamColor playerColor = TeamColor.WHITE;

    WebSocketFacade() {

    }

    public void setAuthToken(int newToken) {
        authToken = newToken;
    }

    public void connectToWebsocket(int gameID) {
        UserGameCommand joinGameCommand =
                new UserGameCommand(UserGameCommand.CommandType.CONNECT, Integer.toString(authToken), gameID);
    }

    public String joinGame(String desiredColor, int gameID) {
        connectToWebsocket(gameID);
    }

    public String leaveGame() {
        return "";
    }

    public String resignGame() {
        return "";
    }

    public String makeMove() {
        return "";
    }

    public String highlightMoves() {
        return "";
    }

    public String redrawChessBoard() {
        if (currentGame != null) {
            return currentGame.toString(playerColor);
        } else {
            return "No game is loaded, please use " + SET_TEXT_COLOR_BLUE + "Join Game " + SET_TEXT_COLOR_WHITE + "to join one";
        }
    }
}
