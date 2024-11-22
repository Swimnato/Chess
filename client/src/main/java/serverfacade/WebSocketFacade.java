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

@ClientEndpoint
public class WebSocketFacade {

    ChessGame currentGame = null;
    ChessGame.TeamColor playerColor = TeamColor.WHITE;
    Session session = null;
    URI uri;
    MessageHandler.Whole handler;

    WebSocketFacade(String URL, MessageHandler.Whole handler) throws URISyntaxException {
        uri = new URI(URL.replace("http", "ws") + "/ws");
        this.handler = handler;
    }

    public void connectToWebsocket() throws IOException, InvalidSyntaxException {
        if (session == null || !session.isOpen()) {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            try {
                this.session = container.connectToServer(this, uri);

                this.session.addMessageHandler(handler);

            } catch (DeploymentException e) {
                throw new InvalidSyntaxException(SET_TEXT_COLOR_RED + "Could not connect to server, please reboot client and try again", true);
            }
        }
    }

    public String joinGame(String desiredColor, int gameID, int authToken) throws IOException, InvalidSyntaxException {
        connectToWebsocket();
        UserGameCommand joinGameCommand =
                new UserGameCommand(UserGameCommand.CommandType.CONNECT, Integer.toString(authToken), gameID);
        session.getBasicRemote().sendText(new Gson().toJson(joinGameCommand));

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
