package chess.dataStructures;

public class UserData {
    String username;
    String password;
    String email;

    public UserData(String _un, String _pwd, String _eml){
        username = _un;
        password = _pwd;
        email = _eml;
    }

    public void setPassword(String _pwd){
        password = _pwd;
    }

    public void setUsername(String _un){
        username = _un;
    }

    public void setEmail(String _eml){
        email = _eml;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getEmail(){
        return password;
    }

}
