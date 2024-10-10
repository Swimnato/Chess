package chess.dataStructures;

public class RegisterInfo {
    private String username;
    private String password;
    private String email;
    public RegisterInfo(String _username, String _password, String _email){
        username = _username;
        password = _password;
        email = _email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getEmail() {
        return email;
    }
}
