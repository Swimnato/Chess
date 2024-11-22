package serverfacade;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.datastructures.GameID;
import chess.datastructures.JoinGameInfo;
import com.google.gson.Gson;
import commandparser.InvalidSyntaxException;
import websocket.commands.UserGameCommand;

import javax.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static chess.ui.EscapeSequences.*;

public class WebSocketFacade {

    int authToken;
    ChessGame currentGame = null;
    ChessGame.TeamColor playerColor = TeamColor.WHITE;
    Session session = null;
    URI uri;
    ServerMessageHandler handler;

    WebSocketFacade(String URL, ServerMessageHandler handler) throws URISyntaxException {
        uri = new URI(URL);
        this.handler = handler;
    }

    WebSocketFacade(URI uri, ServerMessageHandler handler) {
        this.uri = uri;
        this.handler = handler;
    }

    public void setAuthToken(int newToken) {
        authToken = newToken;
    }

    public void connectToWebsocket() throws IOException, InvalidSyntaxException {
        if (session == null || !session.isOpen()) {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            try {
                this.session = container.connectToServer(this, uri);

            } catch (DeploymentException e) {
                throw new InvalidSyntaxException(SET_TEXT_COLOR_RED + "Could not connect to server, please reboot client and try again", true);
            }
        }
    }

    public String joinGame(String desiredColor, int gameID) throws IOException, InvalidSyntaxException {
        connectToWebsocket();
        UserGameCommand joinGameCommand =
                new UserGameCommand(UserGameCommand.CommandType.CONNECT, Integer.toString(authToken), gameID);
        return "";
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
