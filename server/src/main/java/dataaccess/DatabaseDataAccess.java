package dataaccess;

import chess.datastructures.GameData;
import chess.datastructures.UserData;
import org.mindrot.jbcrypt.BCrypt;
import server.DataStorage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class DatabaseDataAccess implements DataStorage {
    private static final String[] CREATEDBCOMMANDS = {
            "CREATE TABLE IF NOT EXISTS userLookup (username VARCHAR(255) NOT NULL, hashedpassword TEXT NOT NULL, email TEXT NOT NULL, PRIMARY KEY(username));",
            "CREATE TABLE IF NOT EXISTS authTokenLookup (token INT NOT NULL, username VARCHAR(255) NOT NULL, PRIMARY KEY(token), INDEX(username));",
            "CREATE TABLE IF NOT EXISTS gameDataLookup (id INT NOT NULL, gamejson TEXT NOT NULL, whiteplayer VARCHAR(255), blackplayer VARCHAR(255), PRIMARY KEY(id), INDEX(whiteplayer), INDEX(blackplayer));"
    };
    private static final String[] CLEARDBCOMMANDS = {
            "TRUNCATE TABLE userLookup;",
            "TRUNCATE TABLE authTokenLookup;",
            "TRUNCATE TABLE gameDataLookup;"
    };
    private static final String CREATEUSERCOMMAND = "INSERT INTO userLookup (username, hashedpassword, email) VALUES (?,?,?)";
    private static final String CREATEAUTHCOMMAND = "INSERT INTO authTokenLookup (token, username) VALUES (?,?)";


    private Connection conn;

    public DatabaseDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : CREATEDBCOMMANDS) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        for (var statement : CLEARDBCOMMANDS) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
        }
    }

    @Override
    public void createUser(String username, String password, String email) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement(CREATEUSERCOMMAND)) {
            preparedStatement.setString(0, username);
            preparedStatement.setString(1, generateHash(password));
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public UserData getUser(int authCode) throws DataAccessException {
        return null;
    }

    @Override
    public int createGame(GameData gameData) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public Collection<GameData> listGames(String username) throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {

    }

    @Override
    public int hasAuth(String username) throws DataAccessException {
        return 0;
    }

    @Override
    public void createAuth(int authCode, String username) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement(CREATEAUTHCOMMAND)) {
            preparedStatement.setInt(0, authCode);
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public String getAuth(int authCode) throws DataAccessException {
        return "";
    }

    @Override
    public void deleteAuth(int authCode) throws DataAccessException {

    }

    private String generateHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean checkHash(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
