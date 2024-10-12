package server;

import chess.ChessGame;
import chess.dataStructures.GameData;
import chess.dataStructures.GameID;
import chess.dataStructures.UserData;
import chess.dataStructures.UsernameAuthTokenPair;
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

    public String createGame(int AuthToken, String GameName) throws DataAccessException {
        var user = dataAccess.getUser(AuthToken);
        if(user == null){
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        boolean result = false;
        int gameID = 0;
        while(!result) {
            gameID = createAuth(GameName);
            GameData game = new GameData(new ChessGame(), GameName, gameID, user.getUsername());
            result = dataAccess.createGame(game);
        }
        return new Gson().toJson(new GameID(gameID));
    }

    public String joinGame(int gameID, ChessGame.TeamColor Color, int AuthToken) throws DataAccessException {
        var user = dataAccess.getUser(AuthToken);
        if(user == null){
            return "{ \"message\": \"Error: unauthorized\" }";
        }

        GameData desiredGame = dataAccess.getGame(gameID);
        if(desiredGame == null){
            return "{ \"message\": \"Error: bad request\" }";
        }
        if(desiredGame.getPlayer2() != null){
            return "{ \"message\": \"Error: already taken\" }";
        }
        if(Color == ChessGame.TeamColor.BLACK){
            desiredGame.setPlayer2(user.getUsername());
        }
        else{
            desiredGame.setPlayer2(desiredGame.getPlayer1());
            desiredGame.setPlayer1(user.getUsername());
        }
        dataAccess.updateGame(desiredGame);

        return new Gson().toJson(new GameID(gameID));
    }

    public void clearApplication(){
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
            int authToken = dataAccess.hasAuth(_un);
            if(authToken != 0) {
                dataAccess.deleteAuth(authToken);
            }
            authToken = createAuth(_un);
            dataAccess.createAuth(authToken, _un);
            return new Gson().toJson(new UsernameAuthTokenPair(authToken, _un), UsernameAuthTokenPair.class);
        }
        else{
            return "{ \"message\": \"Error: unauthorized\" }";
        }
    }

    public String logout(int AuthToken) throws DataAccessException {
        try {
            dataAccess.deleteAuth(AuthToken);
        }
        catch(DataAccessException e){
            return "Auth Does Not Exist!";
        }
        return "{}";
    }

    private int createAuth(String input){
        int auth = (int)((input.hashCode() * System.currentTimeMillis() * 1000003) % (2147483647)); // take the username hash code, multiply it by the current time and a large prime number, then mod that so that it is in integer bounds.
        return (auth == 0 ? 1 : auth); //0 is an error value so we can't have that as a valid auth value;
    }

}
