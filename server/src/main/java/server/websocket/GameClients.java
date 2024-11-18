package server.websocket;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;

public class GameClients {
    Session whitePlayer;
    Session blackPlayer;
    ArrayList<Session> observers;

    public ArrayList<Session> getObservers() {
        return observers;
    }

    public Session getBlackPlayer() {
        return blackPlayer;
    }

    public Session getWhitePlayer() {
        return whitePlayer;
    }
}
