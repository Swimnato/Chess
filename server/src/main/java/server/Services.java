package server;

import chess.ChessGame;
import chess.dataStructures.*;
import chess.dataStructures.CreateGameInfo;
import chess.dataStructures.GameData;
import chess.dataStructures.GameID;
import chess.dataStructures.GameOverview;
import chess.dataStructures.JoinGameInfo;
import chess.dataStructures.LoginInfo;
import chess.dataStructures.RegisterInfo;
import chess.dataStructures.UserData;
import chess.dataStructures.UsernameAuthTokenPair;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Services {
    private DataStorage dataAccess;
    private static final UniqueIDGenerator IDGenerator = new UniqueIDGenerator();

    public Services(DataStorage desiredPersistance) {
        dataAccess = desiredPersistance;
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
        while (!result) { // this makes sure that should our game ID number repeat, that it will generate a new one
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
            if (desiredGame.getPlayer1() != null) { //make sure white is free
                return "{ \"message\": \"Error: already taken\" }";
            }
            desiredGame.setPlayer1(user.getUsername());
        } else {
            if (desiredGame.getPlayer2() != null) { //make sure black is free
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

    public String register(String username, String password, String email) throws DataAccessException {
        UserData test = dataAccess.getUser(username);

        if (test == null) {
            dataAccess.createUser(username, password, email);
            return login(username, password);
        }

        return "{ \"message\": \"Error: already taken\" }";
    }

    public String login(String username, String password) throws DataAccessException {
        UserData user = dataAccess.getUser(username);
        if (user != null && user.getPassword().equals(password)) {
            int authToken = dataAccess.hasAuth(username);

            /* These next 3 lines are to assure that you never get more than one auth token for each user
            the TA tests require the use of outdated tokens though sadly so I had to remove this to pass
            the passoff. */

            /*if(authToken != 0) {
                dataAccess.deleteAuth(authToken);
            }*/

            authToken = IDGenerator.createAuth();
            dataAccess.createAuth(authToken, username);

            return new Gson().toJson(new UsernameAuthTokenPair(authToken, username), UsernameAuthTokenPair.class);
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
