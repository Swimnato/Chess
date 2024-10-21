package server;

import chess.dataStructures.GameData;
import chess.dataStructures.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import dataaccess.DataAccessException;

public class MemoryDataAccess implements DataStorage {
    private HashMap<String, UserData> userLookup;
    private HashMap<Integer, String> authTokenLookup;
    private HashMap<String, Integer> authTokenReverseLookup;
    private HashMap<Integer, GameData> gameDataLookup;

    public MemoryDataAccess() {
        userLookup = new HashMap<>();
        authTokenLookup = new HashMap<>();
        gameDataLookup = new HashMap<>();
        authTokenReverseLookup = new HashMap<>();
    }

    @Override
    public void clear() {
        userLookup.clear();
        authTokenLookup.clear();
        gameDataLookup.clear();
        authTokenReverseLookup.clear();
    }

    @Override
    public void createUser(String username, String password, String email) {
        var data = new UserData(username, password, email);
        userLookup.put(username, data);
    }

    @Override
    public UserData getUser(String username) {
        if (!userLookup.containsKey(username)) {
            return null;
        }
        return userLookup.get(username);
    }

    @Override
    public UserData getUser(int authCode) throws DataAccessException {
        String username;
        try {
            username = getAuth(authCode);
        } catch (DataAccessException e) {
            return null;
        }
        return getUser(username);
    }

    @Override
    public boolean createGame(GameData gameData) {
        if (gameDataLookup.containsKey(gameData.getID())) {
            return false;
        }
        gameDataLookup.put(gameData.getID(), gameData);
        return true;
    }

    @Override
    public GameData getGame(int gameID) {
        if (gameDataLookup.containsKey(gameID)) {
            return gameDataLookup.get(gameID);
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return gameDataLookup.values();
    }

    @Override
    public Collection<GameData> listGames(String username) {
        ArrayList<GameData> games = new ArrayList<>(gameDataLookup.values());
        var output = new ArrayList<GameData>();
        for (var game : games) {
            if (game.hasPlayer(username)) {
                output.add(new GameData(game));
            }
        }
        return output;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        if (!gameDataLookup.containsKey(gameData.getID())) {
            throw new DataAccessException("Game Does Not Exist!");
        }
        gameDataLookup.remove(gameData.getID());
        gameDataLookup.put(gameData.getID(), gameData);
    }

    @Override
    public int hasAuth(String username) {
        if (authTokenReverseLookup.containsKey(username)) {
            return authTokenReverseLookup.get(username);
        } else {
            return 0;
        }
    }

    @Override
    public void createAuth(int authCode, String username) {
        authTokenLookup.put(authCode, username);
        authTokenReverseLookup.put(username, authCode);
    }

    @Override
    public String getAuth(int authCode) throws DataAccessException {
        if (!authTokenLookup.containsKey(authCode)) {
            throw new DataAccessException("Auth Does Not Exist!");
        }
        return authTokenLookup.get(authCode);
    }

    @Override
    public void deleteAuth(int authCode) throws DataAccessException {
        if (!authTokenLookup.containsKey(authCode)) {
            throw new DataAccessException("Auth Does Not Exist!");
        }
        authTokenReverseLookup.remove(authTokenLookup.get(authCode));
        authTokenLookup.remove(authCode);
    }
}
