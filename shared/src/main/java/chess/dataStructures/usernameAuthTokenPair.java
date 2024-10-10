package chess.dataStructures;

public class UsernameAuthTokenPair {
    private final int authToken;
    private final String username;

    public UsernameAuthTokenPair(int authToken, String username){
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
