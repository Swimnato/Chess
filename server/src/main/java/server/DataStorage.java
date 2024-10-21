package server;

import chess.dataStructures.*;
import dataaccess.DataAccessException;
import org.eclipse.jetty.server.Authentication;

import java.util.Collection;

public interface DataStorage {
    public void clear();

    public void createUser(String username, String password, String email) throws DataAccessException;

    public UserData getUser(String username) throws DataAccessException;

    public UserData getUser(int authCode) throws DataAccessException;

    public boolean createGame(GameData gameData) throws DataAccessException;

    public GameData getGame(int gameID) throws DataAccessException;

    public Collection<GameData> listGames() throws DataAccessException;

    public Collection<GameData> listGames(String _username) throws DataAccessException;

    public void updateGame(GameData gameData) throws DataAccessException;

    public int hasAuth(String username) throws DataAccessException;

    public void createAuth(int authCode, String username) throws DataAccessException;

    public String getAuth(int authCode) throws DataAccessException;

    public void deleteAuth(int authCode) throws DataAccessException;
}
