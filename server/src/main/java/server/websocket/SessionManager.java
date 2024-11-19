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
        Session blackPlayer = sessions.get(gameID).getWhitePlayer();
        if (blackPlayer == null) {
            return;
        }
        if (!blackPlayer.isOpen()) {
            sessions.get(gameID).setWhitePlayer(null);
        }
        blackPlayer.getRemote().sendString(message);
    }
    
}
