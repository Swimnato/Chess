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
    private HashMap<Integer, GameData> gameDataLookup;

    public MemoryDataAccess(){
        userLookup = new HashMap<>();
        authTokenLookup = new HashMap<>();
        gameDataLookup = new HashMap<>();
    }

    @Override
    public void clear() {
        userLookup.clear();
        authTokenLookup.clear();
        gameDataLookup.clear();
    }

    @Override
    public void createUser(String username, String password, String email) {
        var data = new UserData(username, password, email);
        userLookup.put(username, data);
    }

    @Override
    public UserData getUser(String _username) throws DataAccessException {
        if(!userLookup.containsKey(_username)){
            throw new DataAccessException("{ \"message\": \"Error: bad request\" }");
        }
        return userLookup.get(_username);
    }

    public UserData getUser(int authCode) throws DataAccessException {
        return getUser(getAuth(authCode));
    }

    @Override
    public void createGame(GameData _gd) {
        gameDataLookup.put(_gd.getID(), _gd);
    }

    @Override
    public Collection<GameData> listGames() {
        return gameDataLookup.values();
    }

    public Collection<GameData> listGames(String _username){
        ArrayList<GameData> games = new ArrayList<>(gameDataLookup.values());
        var output = new ArrayList<GameData>();
        for(var game : games){
            if(game.hasPlayer(_username)){
                output.add(new GameData(game));
            }
        }
        return output;
    }

    @Override
    public void updateGame(GameData _gd) throws DataAccessException {
        if(!gameDataLookup.containsKey(_gd.getID())){
            throw new DataAccessException("{ \"message\": \"Error: bad request\" }");
        }
        gameDataLookup.remove(_gd.getID());
        gameDataLookup.put(_gd.getID(), _gd);
    }

    @Override
    public void createAuth(int authCode, String username) {
        authTokenLookup.put(authCode, username);
    }

    @Override
    public String getAuth(int authCode) throws DataAccessException {
        if(!authTokenLookup.containsKey(authCode)){
            throw new DataAccessException("{ \"message\": \"Error: bad request\" }");
        }
        return authTokenLookup.get(authCode);
    }

    @Override
    public void deleteAuth(int authCode) throws DataAccessException {
        if(!authTokenLookup.containsKey(authCode)){
            throw new DataAccessException("{ \"message\": \"Error: bad request\" }");
        }
        authTokenLookup.remove(authCode);
    }
}
