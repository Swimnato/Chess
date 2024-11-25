package websocket.messages;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String game = null;
    String errorMessage = null;
    String message = null;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String payload) {
        this.serverMessageType = type;
        switch (serverMessageType) {
            case ERROR:
                errorMessage = payload;
                break;
            case LOAD_GAME:
                game = payload;
                break;
            case NOTIFICATION:
                message = payload;
                break;
        }
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public String getPayload() {
        return switch (serverMessageType) {
            case ERROR -> errorMessage;
            case LOAD_GAME -> game;
            case NOTIFICATION -> message;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
