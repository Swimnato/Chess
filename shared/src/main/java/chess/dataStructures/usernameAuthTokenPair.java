package chess.dataStructures;

public class usernameAuthTokenPair {
    private final int authToken;
    private final String username;

    public usernameAuthTokenPair(int authToken, String username){
        this.authToken = authToken;
        this.username = username;
    }

    public int getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }
}
