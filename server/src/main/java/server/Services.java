package server;

import chess.dataStructures.UserData;
import chess.dataStructures.usernameAuthTokenPair;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;

public class Services {
    private DataStorage dataAccess;

    public Services(DataStorage _d){
        dataAccess = _d;
    }

    public String listGames() throws DataAccessException {
        var games = dataAccess.listGames();
        return new Gson().toJson(games);
    }

    public String createGame() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    public String joinGame() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    public void clearApplication() throws DataAccessException {
        dataAccess.clear();
    }

    public String register(String _un, String _pwd, String _eml) throws DataAccessException {
        UserData _test = dataAccess.getUser(_un);
        if(_test == null) {
            dataAccess.createUser(_un, _pwd, _eml);
            return login(_un, _pwd);
        }
        return "{ \"message\": \"Error: already taken\" }";
    }

    public String login(String _un, String _pwd) throws DataAccessException {
        UserData user = dataAccess.getUser(_un);
        if(user != null && user.getPassword().equals(_pwd)){
            int authToken = createAuth(_un);
            dataAccess.createAuth(authToken, _un);
            return new Gson().toJson(new usernameAuthTokenPair(authToken, _un), usernameAuthTokenPair.class);
        }
        else{
            return "{ \"message\": \"Error: unauthorized\" }";
        }
    }

    public String logout() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    private int createAuth(String _Username){
        return (int)((_Username.hashCode() * System.currentTimeMillis() * 1000003) % (2147483647)); // take the username hash code, multiply it by the current time and a large prime number, then mod that so that it is in integer bounds.
    }

}
