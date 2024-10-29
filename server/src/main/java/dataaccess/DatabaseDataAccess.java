package dataaccess;

import chess.datastructures.GameData;
import chess.datastructures.UserData;
import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;
import org.mindrot.jbcrypt.BCrypt;
import server.DataStorage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class DatabaseDataAccess implements DataStorage {
    private static final String[] CREATEDBCOMMANDS = {
            "CREATE TABLE IF NOT EXISTS userLookup (username VARCHAR(255) NOT NULL, hashedPassword TEXT NOT NULL, email TEXT NOT NULL, PRIMARY KEY(username));",
            "CREATE TABLE IF NOT EXISTS authTokenLookup (token INT NOT NULL, username VARCHAR(255) NOT NULL, PRIMARY KEY(token), INDEX(username));",
            "CREATE TABLE IF NOT EXISTS gameDataLookup (id INT NOT NULL AUTO_INCREMENT, gameJSON TEXT NOT NULL, whitePlayer VARCHAR(255), blackPlayer VARCHAR(255), PRIMARY KEY(id), INDEX(whitePlayer), INDEX(blackPlayer));"
    };
    private static final String[] CLEARDBCOMMANDS = {
            "TRUNCATE TABLE userLookup;",
            "TRUNCATE TABLE authTokenLookup;",
            "TRUNCATE TABLE gameDataLookup;"
    };
    private static final String CREATEUSERCOMMAND = "INSERT INTO userLookup (username, hashedPassword, email) VALUES (?,?,?);";
    private static final String GETUSERBYUSERNAME = "SELECT hashedPassword, email FROM userLookup WHERE username = ?;";
    private static final String GETUSERBYAUTHCODE = "SELECT userLookup.username, userLookup.hashedPassword, userLookup.email FROM authTokenLookup JOIN userLookup ON authTokenLookup.username = userLookup.username FROM token = ?;";
    private static final String MAKENEWCHESSBOARD = "INSERT INTO gameDataLookup (gameJSON, whitePlayer, blackPlayer) VALUES (?,?,?);";
    private static final String MAKECHESSBOARDALT = "INSERT INTO gameDataLookup (gameJSON, whitePlayer, blackPlayer, id) VALUES (?,?,?,?);";
    private static final String CREATEAUTHCOMMAND = "INSERT INTO authTokenLookup (token, username) VALUES (?,?);";
    private static final String DELETEAUTHCOMMAND = "DELETE FROM authTokenLookup WHERE token = ?;";

    public static void main(String[] args) throws DataAccessException {
        DatabaseDataAccess main = new DatabaseDataAccess();
        main.clear();
        main.createUser("admin", "321", "test");
        main.createUser("ad", "32441", "test");
        main.createUser("min", "lolz", "test");
        main.createUser("reerere", "reererere", "test");

        var gotUser = main.getUser("admin");
        System.out.println("Got User == Null? " + (gotUser == null));
        if (gotUser != null) {
            System.out.print(gotUser.getUsername() + " , ");
            System.out.print(gotUser.getPassword() + " , ");
            System.out.println(gotUser.getEmail());
        }
        gotUser = main.getUser("noexist");
        System.out.println("Got User == Null? " + (gotUser == null));

    }

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
            try (var conn = DatabaseManager.getConnection()) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
        }
    }

    @Override
    public void createUser(String username, String password, String email) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(CREATEUSERCOMMAND)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, email);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData toReturn;
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(GETUSERBYUSERNAME)) {
                preparedStatement.setString(1, username);
                try (var rs = preparedStatement.executeQuery()) {
                    rs.next();
                    String password = rs.getNString("hashedPassword");
                    String email = rs.getNString("email");
                    if (password != null && email != null) {
                        toReturn = new UserData(username, password, email);
                    } else {
                        toReturn = null;
                    }
                } catch (SQLException e) {
                    toReturn = null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return toReturn;
    }

    @Override
    public UserData getUser(int authCode) throws DataAccessException {
        UserData toReturn;
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(GETUSERBYAUTHCODE)) {
                preparedStatement.setInt(1, authCode);
                try (var rs = preparedStatement.executeQuery()) {
                    rs.next();
                    String username = rs.getNString("username");
                    String password = rs.getNString("hashedPassword");
                    String email = rs.getNString("email");
                    if (password != null && email != null && username != null) {
                        toReturn = new UserData(username, password, email);
                    } else {
                        toReturn = null;
                    }
                } catch (SQLException e) {
                    toReturn = null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return toReturn;
    }

    @Override
    public int createGame(GameData gameData) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            boolean idGiven = gameData.getId() == 0;
            String statement = (idGiven ? MAKENEWCHESSBOARD : MAKECHESSBOARDALT);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, new Gson().toJson(gameData.getGame()));
                preparedStatement.setString(2, gameData.getPlayer1());
                preparedStatement.setString(3, gameData.getPlayer2());
                if (idGiven) {
                    preparedStatement.setInt(4, gameData.getId());
                }
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return -4; // I need to add code to get the ID back
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
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(CREATEAUTHCOMMAND)) {
                preparedStatement.setInt(1, authCode);
                preparedStatement.setString(2, username);
                preparedStatement.executeUpdate();
            }
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
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(DELETEAUTHCOMMAND)) {
                preparedStatement.setInt(1, authCode);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
