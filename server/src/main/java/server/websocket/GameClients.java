package server.websocket;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;

public class GameClients {
    Session whitePlayer;
    Session blackPlayer;
    ArrayList<Session> observers = new ArrayList<>();

    public ArrayList<Session> getObservers() {
        return observers;
    }

    public Session getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(Session blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public Session getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(Session whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public void setObservers(ArrayList<Session> observers) {
        this.observers = observers;
    }

    public void addObserver(Session observer) {
        this.observers.add(observer);
    }

    @Override
    public String toString() {
        return "White Player:" + whitePlayer + " Black Player: " + blackPlayer + " Observers: " + observers;
    }
}
