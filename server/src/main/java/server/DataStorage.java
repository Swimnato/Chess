package server;

import chess.dataStructures.*;
import org.eclipse.jetty.server.Authentication;

import java.util.Collection;

public interface DataStorage {
    public void clear();
    public void createUser(String username, String password, String email);
    public UserData getUser(String username);
    public UserData getUser(int authCode);
    public void createGame(GameData _gd);
    public Collection<GameData> listGames();
    public Collection<GameData> listGames(String _username);
    public void updateGame(GameData _gd);
    public void createAuth(int authCode, String username);
    public String getAuth(int authCode);
    public void deleteAuth(int authCode);
}
