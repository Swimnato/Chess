package server;

import chess.dataStructures.*;
import dataaccess.DataAccessException;
import org.eclipse.jetty.server.Authentication;

import java.util.Collection;

public interface DataStorage {
    public void clear();
    public void createUser(String username, String password, String email);
    public UserData getUser(String username) throws DataAccessException ;
    public UserData getUser(int authCode) throws DataAccessException ;
    public void createGame(GameData _gd);
    public Collection<GameData> listGames();
    public Collection<GameData> listGames(String _username);
    public void updateGame(GameData _gd) throws DataAccessException ;
    public void createAuth(int authCode, String username);
    public String getAuth(int authCode) throws DataAccessException ;
    public void deleteAuth(int authCode) throws DataAccessException ;
}
