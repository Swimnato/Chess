package server;

import java.util.UUID;

public class UniqueIDGenerator {
    public int createAuth() {
        return UUID.randomUUID().hashCode();
    }

    public int createGameID(String input) {
        int auth = (int) ((input.hashCode() * System.currentTimeMillis() * 1000003) % (2147483647)); // take the username hash code, multiply it by the current time and a large prime number, then mod that so that it is in integer bounds.
        return (auth == 0 ? 1 : auth) < 0 ? -auth : auth; // <= 0 is an error value so we can't have that as a valid auth value;
    }
}
