package chess.dataStructures;

import java.util.Objects;

public class UserData {
    private String username;
    private String password;
    private String email;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData userData = (UserData) o;
        return Objects.equals(getUsername(), userData.getUsername()) && Objects.equals(getPassword(), userData.getPassword()) && Objects.equals(getEmail(), userData.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getPassword(), getEmail());
    }
}
