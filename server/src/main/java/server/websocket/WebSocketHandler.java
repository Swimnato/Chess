package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.DataStorage;
import server.Services;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

import static websocket.commands.UserGameCommand.CommandType.*;

@WebSocket
public class WebSocketHandler {
    private DataStorage mainDB;
    Services services;
    SessionManager sessionManager = new SessionManager();

    public WebSocketHandler(DataStorage desiredStorage) {
        mainDB = desiredStorage;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        try {
            var user = mainDB.getUser(userGameCommand.getAuthToken());
            if (user == null) {
                session.getRemote().sendString(new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Session")));
                return;
            }

            var game = mainDB.getGame(userGameCommand.getGameID());
            if (game == null) {
                session.getRemote().sendString(new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Game ID")));
                return;
            }

            if (!game.hasPlayer(user.getUsername())) {
                sessionManager.addObserver(game.getId(), session);
            } else if (game.getPlayer1().equals(user.getUsername())) {
                sessionManager.setWhitePlayer(game.getId(), session);
            } else if (game.getPlayer2().equals(user.getUsername())) {
                sessionManager.setBlackPlayer(game.getId(), session);
            }
        } catch (DataAccessException e) {
            session.getRemote().sendString(new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal Server Error")));
            return;
        }

        String response = switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect();
            case MAKE_MOVE -> makeMove();
            case RESIGN -> resign();
            case LEAVE -> leave();
            default -> "ERROR, UNKNOWN COMMAND";
        };
        session.getRemote().sendString(response);
    }

    private String connect() {
        return "";
    }

    private String makeMove() {
        return "";

    }

    private String resign() {
        return "";

    }

    private String leave() {
        return "";

    }
}
