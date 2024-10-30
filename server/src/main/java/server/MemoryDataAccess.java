package server;

import chess.ChessGame;
import chess.datastructures.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import dataaccess.DataAccessException;

public class MemoryDataAccess implements DataStorage {
    private HashMap<String, UserData> userLookup;
    private HashMap<Integer, String> authTokenLookup;
    private HashMap<String, Integer> authTokenReverseLookup;
    private HashMap<Integer, GameData> gameDataLookup;
    private static final UniqueIDGenerator IDGENERATOR = new UniqueIDGenerator();

    public MemoryDataAccess() {
        userLookup = new HashMap<>();
        authTokenLookup = new HashMap<>();
        gameDataLookup = new HashMap<>();
        authTokenReverseLookup = new HashMap<>();
    }

    @Override
    public void clear() throws DataAccessException {
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
    public int createGame(GameData gameData) {
        GameData game;
        int gameID = gameData.getId();
        boolean containsKey = gameDataLookup.containsKey(gameID);
        if (gameID != 0 && containsKey) {
            return 0;
        }
        while (containsKey || gameID == 0) { // this makes sure that should our game ID number repeat,
            // that it will generate a new one it uses the time as a seed so it should change every time it is called
            gameID = IDGENERATOR.createGameID(gameData.getName());
            containsKey = gameDataLookup.containsKey(gameID);
        }
        game = new GameData(gameData.getGame(), gameData.getName(), gameID, gameData.getPlayer1(), gameData.getPlayer2());
        gameDataLookup.put(game.getId(), game);
        return game.getId();
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
        if (!gameDataLookup.containsKey(gameData.getId())) {
            throw new DataAccessException("Game Does Not Exist!");
        }
        gameDataLookup.remove(gameData.getId());
        gameDataLookup.put(gameData.getId(), gameData);
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
