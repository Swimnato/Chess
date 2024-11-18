package server.websocket;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SessionManager {
    HashMap<int, GameClients> sessions = new HashMap<>();

    public void sendUniversalMessage(String message) throws IOException {
        ArrayList<String> toRemove = new ArrayList<>();

        for (var session : sessions.keySet()) {
            if (sessions.get(session).isOpen()) {
                sessions.get(session).getRemote().sendString(message);
            } else {
                toRemove.add(session);
            }
        }

        for (var session : toRemove) {
            sessions.remove(session);
        }
    }

    public void updateObservers(int gameID) {
        
    }
}
