package server.websocket;

import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SessionManager {
    HashMap<Integer, GameClients> sessions = new HashMap<>();

    public void updateObservers(int gameID, String message) throws IOException, DataAccessException {
        ArrayList<Session> toRemove = new ArrayList<>();

        if (sessions.get(gameID) == null) {
            throw new DataAccessException("Game Not Found!");
        }

        var observers = sessions.get(gameID).getObservers();

        for (var session : observers) {
            if (session.isOpen()) {
                session.getRemote().sendString(message);
            } else {
                toRemove.add(session);
            }
        }

        for (var session : toRemove) {
            observers.remove(session);
        }

        if (!toRemove.isEmpty()) {
            sessions.get(gameID).setObservers(observers);
        }
    }

    public void updateAllPlayers(int gameID, String message) throws IOException, DataAccessException {
        updateBlackPlayer(gameID, message);
        updateWhitePlayer(gameID, message);
        updateObservers(gameID, message);
    }

    public void updateAllButWhite(int gameID, String message) throws IOException, DataAccessException {
        updateBlackPlayer(gameID, message);
        updateObservers(gameID, message);
    }

    public void updateAllButBlack(int gameID, String message) throws IOException, DataAccessException {
        updateWhitePlayer(gameID, message);
        updateObservers(gameID, message);
    }

    public void updateWhitePlayer(int gameID, String message) throws IOException, DataAccessException {
        if (sessions.get(gameID) == null) {
            throw new DataAccessException("Game Not Found!");
        }
        Session whitePlayer = sessions.get(gameID).getWhitePlayer();
        if (whitePlayer == null) {
            return;
        }
        if (!whitePlayer.isOpen()) {
            sessions.get(gameID).setWhitePlayer(null);
        }
        whitePlayer.getRemote().sendString(message);
    }

    public void updateBlackPlayer(int gameID, String message) throws IOException, DataAccessException {
        if (sessions.get(gameID) == null) {
            throw new DataAccessException("Game Not Found!");
        }
        Session blackPlayer = sessions.get(gameID).getBlackPlayer();
        if (blackPlayer == null) {
            return;
        }
        if (!blackPlayer.isOpen()) {
            sessions.get(gameID).setBlackPlayer(null);
        }
        blackPlayer.getRemote().sendString(message);
    }

    public void setWhitePlayer(int gameID, Session session) {
        sessions.computeIfAbsent(gameID, k -> new GameClients());
        sessions.get(gameID).setWhitePlayer(session);
    }

    public void setBlackPlayer(int gameID, Session session) {
        sessions.computeIfAbsent(gameID, k -> new GameClients());
        sessions.get(gameID).setBlackPlayer(session);
    }

    public void addObserver(int gameID, Session session) {
        sessions.computeIfAbsent(gameID, k -> new GameClients());
        sessions.get(gameID).addObserver(session);
    }
}
