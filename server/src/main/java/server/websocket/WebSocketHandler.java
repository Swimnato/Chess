package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.DataStorage;
import server.Services;
import websocket.commands.ConnectGameCommand;
import websocket.commands.MakeMoveCommand;
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

        String response = switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(userGameCommand, session);
            case MAKE_MOVE -> makeMove(new Gson().fromJson(message, MakeMoveCommand.class));
            case RESIGN -> resign(userGameCommand);
            case LEAVE -> leave(userGameCommand);
            default -> new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "ERROR, UNKNOWN COMMAND"));
        };
        session.getRemote().sendString(response);
    }

    private String connect(UserGameCommand command, Session session) {
        try {
            var user = mainDB.getUser(command.getAuthToken());
            if (user == null) {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Session")));
            }

            var game = mainDB.getGame(command.getGameID());
            if (game == null) {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Game ID")));
            }

            if (!game.hasPlayer(user.getUsername())) {
                sessionManager.addObserver(game.getId(), session);
            } else if (game.getPlayer1().equals(user.getUsername())) {
                sessionManager.setWhitePlayer(game.getId(), session);
            } else if (game.getPlayer2().equals(user.getUsername())) {
                sessionManager.setBlackPlayer(game.getId(), session);
            }
        } catch (DataAccessException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal Server Error")));
        }
        return new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Connected Successfully!"));
    }

    private String makeMove(MakeMoveCommand command) {
        return "";

    }

    private String resign(UserGameCommand command) {
        return "";

    }

    private String leave(UserGameCommand command) {
        return "";
    }
}
