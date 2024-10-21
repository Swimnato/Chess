package server;

import java.util.UUID;

public class UniqueIDGenerator {
    public int createAuth() {
        //I learned too late that the ID should be generated using UUID, so I already had a system similar to that
        //that is below for the createGameID, where it was an int generated using the username and current time
        //multiplied by prime numbers and kept in the bounds of an int, so I decided to just use the UUID hash code.
        int value = UUID.randomUUID().hashCode();
        return value == 0 ? 1 : value; //Zero is an error value, so I have it return 1 instead.
    }

    public int createGameID(String gameName) {
        int auth = (int) ((gameName.hashCode() * System.currentTimeMillis() * 1000003) % (2147483647)); // take the username hash code, multiply it by the current time and a large prime number, then mod that so that it is in integer bounds.
        return (auth == 0 ? 1 : auth) < 0 ? -auth : auth; // <= 0 is an error value so we can't have that as a valid auth value;
    }
}
