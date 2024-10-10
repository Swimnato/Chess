package chess.dataStructures;

public class LoginInfo {
    private String username;
    private String password;
    public LoginInfo(String _username, String _password){
        username = _username;
        password = _password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
