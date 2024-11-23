package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.DataStorage;
import server.Services;
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
            case MAKE_MOVE -> makeMove(new Gson().fromJson(message, MakeMoveCommand.class), session);
            case RESIGN -> resign(userGameCommand, session);
            case LEAVE -> leave(userGameCommand, session);
            default -> new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "ERROR, UNKNOWN COMMAND"));
        };
        session.getRemote().sendString(response);
    }

    private String connect(UserGameCommand command, Session session) {
        try {
            var user = mainDB.getUser(Integer.parseInt(command.getAuthToken()));
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

            return new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, new Gson().toJson(game.getGame())));
        } catch (DataAccessException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal Server Error")));
        }
    }

    private String makeMove(MakeMoveCommand command, Session session) throws IOException {
        try {
            var user = mainDB.getUser(Integer.parseInt(command.getAuthToken()));
            if (user == null) {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Session")));
            }

            var game = mainDB.getGame(command.getGameID());
            if (game == null) {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Game ID")));
            }

            ChessGame.TeamColor colorOfPlayer;

            if (!game.hasPlayer(user.getUsername())) {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Observers cannot make moves!")));
            } else if (game.getPlayer1().equals(user.getUsername())) {
                if (game.getGame().getTeamTurn() != ChessGame.TeamColor.WHITE) {
                    return (new Gson().toJson(
                            new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You cannot play out of turn!")));
                }
                colorOfPlayer = ChessGame.TeamColor.WHITE;
            } else if (game.getPlayer2().equals(user.getUsername())) {
                if (game.getGame().getTeamTurn() != ChessGame.TeamColor.BLACK) {
                    return (new Gson().toJson(
                            new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You cannot play out of turn!")));
                }
                colorOfPlayer = ChessGame.TeamColor.BLACK;
            } else {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You have not subscribed to this game!")));
            }

            game.getGame().makeMove(command.getMove());

            mainDB.updateGame(game);

            sessionManager.updateAllPlayers(game.getId(), new Gson().toJson(game.getGame()));

            var updateMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    user.getUsername() + "(" + colorOfPlayer + ") made move: " + command.getMove().toString());

            if (colorOfPlayer == ChessGame.TeamColor.WHITE) {
                sessionManager.updateAllButWhite(game.getId(), new Gson().toJson(updateMessage));
            } else {
                sessionManager.updateAllButBlack(game.getId(), new Gson().toJson(updateMessage));
            }


        } catch (DataAccessException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal Server Error")));
        } catch (InvalidMoveException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Move: " + e.getMessage())));
        }

    }

    private String resign(UserGameCommand command, Session session) {
        return "";

    }

    private String leave(UserGameCommand command, Session session) {
        return "";
    }
}
