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

import static chess.ui.EscapeSequences.*;

import java.io.IOException;

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
            case RESIGN -> resign(userGameCommand, session);
            case LEAVE -> leave(userGameCommand, session);
            default -> new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "ERROR, UNKNOWN COMMAND"));
        };

        if (!response.isEmpty()) {
            session.getRemote().sendString(response);
        }
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
            } else if (user.getUsername().equals(game.getPlayer1())) {
                sessionManager.setWhitePlayer(game.getId(), session);
            } else if (user.getUsername().equals(game.getPlayer2())) {
                sessionManager.setBlackPlayer(game.getId(), session);
            }

            return new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, new Gson().toJson(game.getGame())));
        } catch (DataAccessException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal Server Error")));
        }
    }

    private String makeMove(MakeMoveCommand command) throws IOException {
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

            if (game.getGame().isGameOver()) {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Game Has Ended!" + SET_TEXT_COLOR_WHITE + " ... you should play another though...")));
            }

            ChessGame.TeamColor colorOfPlayer;

            if (!game.hasPlayer(user.getUsername())) {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Observers cannot make moves!")));
            } else if (user.getUsername().equals(game.getPlayer1())) {
                colorOfPlayer = ChessGame.TeamColor.WHITE;
            } else if (user.getUsername().equals(game.getPlayer2())) {
                colorOfPlayer = ChessGame.TeamColor.BLACK;
            } else {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You have not subscribed to this game!")));
            }

            if (game.getGame().getTeamTurn() != colorOfPlayer) {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You cannot play out of turn!")));
            }


            game.getGame().makeMove(command.getMove());

            mainDB.updateGame(game);

            sessionManager.updateAllPlayers(game.getId(), new Gson().toJson(game.getGame()));

            var updateMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    user.getUsername() + " (" + colorOfPlayer + ") made move: " + command.getMove().toString(true));

            if (colorOfPlayer == ChessGame.TeamColor.WHITE) {
                sessionManager.updateAllButWhite(game.getId(), new Gson().toJson(updateMessage));
            } else {
                sessionManager.updateAllButBlack(game.getId(), new Gson().toJson(updateMessage));
            }

            if (game.getGame().isGameOver()) {
                sessionManager.updateAllPlayers(game.getId(), new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        user.getUsername() + " (" + colorOfPlayer + ") has won!")));
            }


        } catch (DataAccessException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal Server Error")));
        } catch (InvalidMoveException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Move: " + e.getMessage())));
        }
        return "";
    }

    private String resign(UserGameCommand command, Session session) throws IOException {
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
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Only Players can resign the game")));
            } else if (user.getUsername().equals(game.getPlayer1())) {
                game.getGame().declareWinner(ChessGame.TeamColor.BLACK);
                colorOfPlayer = ChessGame.TeamColor.WHITE;
            } else if (user.getUsername().equals(game.getPlayer2())) {
                game.getGame().declareWinner(ChessGame.TeamColor.WHITE);
                colorOfPlayer = ChessGame.TeamColor.BLACK;
            } else {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You have not subscribed to this game!")));
            }

            mainDB.updateGame(game);

            sessionManager.updateAllPlayers(game.getId(), new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    user.getUsername() + " (" + colorOfPlayer + ") has resigned the game")));

        } catch (DataAccessException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal Server Error")));
        }
        return (new Gson().toJson(
                new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "You have resigned the game successfully")));
    }

    private String leave(UserGameCommand command, Session session) throws IOException {
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
                sessionManager.removeObserver(game.getId(), session);
                return "";
            } else if (user.getUsername().equals(game.getPlayer1())) {
                sessionManager.setWhitePlayer(game.getId(), null);
                colorOfPlayer = ChessGame.TeamColor.WHITE;
                game.setPlayer1(null);
            } else if (user.getUsername().equals(game.getPlayer2())) {
                sessionManager.setBlackPlayer(game.getId(), null);
                colorOfPlayer = ChessGame.TeamColor.BLACK;
                game.setPlayer2(null);
            } else {
                return (new Gson().toJson(
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You have not subscribed to this game!")));
            }

            mainDB.updateGame(game);

            sessionManager.updateAllPlayers(game.getId(), new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    user.getUsername() + " (" + colorOfPlayer + ") has left the game")));

        } catch (DataAccessException e) {
            return (new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal Server Error")));
        }
        return (new Gson().toJson(
                new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "You have left the game successfully")));
    }
}
