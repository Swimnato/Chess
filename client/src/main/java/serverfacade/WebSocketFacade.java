package serverfacade;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.ChessMove;
import chess.datastructures.GameID;
import chess.datastructures.JoinGameInfo;
import com.google.gson.Gson;
import commandparser.InvalidSyntaxException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import javax.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static chess.ui.EscapeSequences.*;

@ClientEndpoint
public class WebSocketFacade {

    Session session = null;
    URI uri;
    MessageHandler.Whole handler;

    WebSocketFacade(String url, MessageHandler.Whole handler) throws URISyntaxException {
        uri = new URI(url.replace("http", "ws") + "/ws");
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

    public String subscribeToGame(int gameID, int authToken) throws IOException, InvalidSyntaxException {
        connectToWebsocket();
        UserGameCommand joinGameCommand =
                new UserGameCommand(UserGameCommand.CommandType.CONNECT, Integer.toString(authToken), gameID);
        session.getBasicRemote().sendText(new Gson().toJson(joinGameCommand));

        return "Successfully!\r\n";
    }

    public String leaveGame(int gameID, int authToken) throws IOException {
        UserGameCommand leaveGameCommand =
                new UserGameCommand(UserGameCommand.CommandType.LEAVE, Integer.toString(authToken), gameID);
        session.getBasicRemote().sendText(new Gson().toJson(leaveGameCommand));
        return "";
    }

    public String resignGame(int gameID, int authToken) throws IOException {
        UserGameCommand resignGameCommand =
                new UserGameCommand(UserGameCommand.CommandType.RESIGN, Integer.toString(authToken), gameID);
        session.getBasicRemote().sendText(new Gson().toJson(resignGameCommand));
        return "";
    }

    public String makeMove(ChessMove move, int gameID, int authToken) throws IOException {
        MakeMoveCommand makeMoveCommand = new MakeMoveCommand(Integer.toString(authToken), gameID, move);
        session.getBasicRemote().sendText(new Gson().toJson(makeMoveCommand));
        return "";
    }
}
