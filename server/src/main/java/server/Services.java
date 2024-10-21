package server;

import chess.ChessGame;
import chess.dataStructures.*;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Services {
    private DataStorage dataAccess;
    private static final UniqueIDGenerator IDGenerator = new UniqueIDGenerator();

    public Services(DataStorage _d) {
        dataAccess = _d;
    }

    public String listGames(int authToken) throws DataAccessException {
        var user = dataAccess.getUser(authToken);
        if (user == null) {
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        var games = dataAccess.listGames();
        ArrayList<GameOverview> output = new ArrayList<>();
        for (var game : games) {
            output.add(new GameOverview(game));
        }
        return "{ \"games\": " + new Gson().toJson(output) + "}";
    }

    public String createGame(int AuthToken, String GameName) throws DataAccessException {
        var user = dataAccess.getUser(AuthToken);
        if (user == null) {
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        boolean result = false;
        int gameID = 0;
        while (!result) {
            gameID = IDGenerator.createGameID(GameName);
            GameData game = new GameData(new ChessGame(), GameName, gameID);
            result = dataAccess.createGame(game);
        }
        return new Gson().toJson(new GameID(gameID));
    }

    public String joinGame(Integer gameID, String Color, int AuthToken) throws DataAccessException {
        var user = dataAccess.getUser(AuthToken);
        if (user == null) {
            return "{ \"message\": \"Error: unauthorized\" }";
        }

        GameData desiredGame = dataAccess.getGame(gameID);
        if (desiredGame == null) {
            return "{ \"message\": \"Error: bad request\" }";
        }
        if (Color.equals("WHITE")) {
            if (desiredGame.getPlayer1() != null) {
                return "{ \"message\": \"Error: already taken\" }";
            }
            desiredGame.setPlayer1(user.getUsername());
        } else {
            if (desiredGame.getPlayer2() != null) {
                return "{ \"message\": \"Error: already taken\" }";
            }
            desiredGame.setPlayer2(user.getUsername());
        }

        dataAccess.updateGame(desiredGame);

        return new Gson().toJson(new GameID(gameID));
    }

    public void clearApplication() {
        dataAccess.clear();
    }

    public String register(String _un, String _pwd, String _eml) throws DataAccessException {
        UserData _test = dataAccess.getUser(_un);
        if (_test == null) {
            dataAccess.createUser(_un, _pwd, _eml);
            return login(_un, _pwd);
        }
        return "{ \"message\": \"Error: already taken\" }";
    }

    public String login(String _un, String _pwd) throws DataAccessException {
        UserData user = dataAccess.getUser(_un);
        if (user != null && user.getPassword().equals(_pwd)) {
            int authToken = dataAccess.hasAuth(_un);
            /*if(authToken != 0) {
                dataAccess.deleteAuth(authToken);
            }*/
            authToken = IDGenerator.createAuth();
            dataAccess.createAuth(authToken, _un);
            return new Gson().toJson(new UsernameAuthTokenPair(authToken, _un), UsernameAuthTokenPair.class);
        } else {
            return "{ \"message\": \"Error: unauthorized\" }";
        }
    }

    public String logout(int AuthToken) throws DataAccessException {
        try {
            dataAccess.deleteAuth(AuthToken);
        } catch (DataAccessException e) {
            return "Auth Does Not Exist!";
        }
        return "{}";
    }

}
