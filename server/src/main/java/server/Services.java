package server;

import dataaccess.DataAccessException;
import com.google.gson.Gson;

public class Services {
    private DataStorage dataAccess;

    public Services(DataStorage _d){
        dataAccess = _d;
    }

    public String listGames() throws DataAccessException {
        var output = dataAccess.listGames();

    }

    public String createGame() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    public String joinGame() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    public String clearApplication() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    public String register() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    public String login() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    public String logout() throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

}
