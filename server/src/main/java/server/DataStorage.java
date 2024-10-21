package server;

import chess.dataStructures.*;
import dataaccess.DataAccessException;
import org.eclipse.jetty.server.Authentication;

import java.util.Collection;

public interface DataStorage {
    public void clear();

    public void createUser(String username, String password, String email);

    public UserData getUser(String username);

    public UserData getUser(int authCode) throws DataAccessException;

    public boolean createGame(GameData gameData);

    public GameData getGame(int gameID);

    public Collection<GameData> listGames();

    public Collection<GameData> listGames(String _username);

    public void updateGame(GameData gameData) throws DataAccessException;

    public int hasAuth(String username);

    public void createAuth(int authCode, String username);

    public String getAuth(int authCode) throws DataAccessException;

    public void deleteAuth(int authCode) throws DataAccessException;
}
