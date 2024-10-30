package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;
import chess.datastructures.GameData;
import chess.datastructures.UserData;
import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;
import org.mindrot.jbcrypt.BCrypt;
import server.DataStorage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DatabaseDataAccess implements DataStorage {
    private static final String[] CREATEDBCOMMANDS = {
            "CREATE TABLE IF NOT EXISTS userLookup (username VARCHAR(255) NOT NULL, hashedPassword TEXT NOT NULL, email TEXT NOT NULL, " +
                    "PRIMARY KEY(username));",
            "CREATE TABLE IF NOT EXISTS authTokenLookup (token INT NOT NULL, username VARCHAR(255) NOT NULL, PRIMARY KEY(token), INDEX(username));",
            "CREATE TABLE IF NOT EXISTS gameDataLookup (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(255) NOT NULL, gameJSON TEXT NOT NULL, " +
                    "whitePlayer VARCHAR(255), blackPlayer VARCHAR(255), PRIMARY KEY(id), INDEX(whitePlayer), INDEX(blackPlayer), INDEX(name));"
    };
    private static final String[] CLEARDBCOMMANDS = {
            "TRUNCATE TABLE userLookup;",
            "TRUNCATE TABLE authTokenLookup;",
            "TRUNCATE TABLE gameDataLookup;"
    };
    private static final String CREATEUSERCOMMAND = "INSERT INTO userLookup (username, hashedPassword, email) VALUES (?,?,?);";
    private static final String GETUSERBYUSERNAME = "SELECT hashedPassword, email FROM userLookup WHERE username = ?;";
    private static final String GETUSERBYAUTHCODE = "SELECT userLookup.username, userLookup.hashedPassword, userLookup.email FROM authTokenLookup " +
            "JOIN userLookup ON authTokenLookup.username = userLookup.username WHERE token = ?;";
    private static final String MAKENEWCHESSBOARD = "INSERT INTO gameDataLookup (gameJSON, name, whitePlayer, blackPlayer) VALUES (?,?,?,?);";
    private static final String MAKECHESSBOARDALT = "INSERT INTO gameDataLookup (gameJSON, name, whitePlayer, blackPlayer, id) VALUES (?,?,?,?,?);";
    private static final String GETACHESSGAMEBYID = "SELECT * FROM gameDataLookup WHERE id = ?;";
    private static final String GETAGAMEBYITSNAME = "SELECT * FROM gameDataLookup WHERE name = ?;";
    private static final String LISTALLACTIVEGAME = "SELECT * FROM gameDataLookup;";
    private static final String LISTALLGAMEBYUSER = "SELECT * FROM gameDataLookup WHERE whitePlayer = ? OR blackPlayer = ?;";
    private static final String UPDATEGAMEINTHEDB = "UPDATE gameDataLookup SET gameJSON = ?, name = ?, whitePlayer = ?," +
            " blackPlayer = ? WHERE id = ?;";
    private static final String GETUSERNAMEBYAUTH = "SELECT username FROM authTokenLookup WHERE token = ?;";
    private static final String CREATEAUTHCOMMAND = "INSERT INTO authTokenLookup (token, username) VALUES (?,?);";
    private static final String AUTHENTICATE4AUTH = "SELECT token FROM authTokenLookup WHERE username = ?;";
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
        var user = getUser(username);
        if (user != null) {
            throw new DataAccessException("User Already Exists!");
        }
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
        int toReturn = 0;
        if (gameData.getName() == null || gameData.getGame() == null || gameData.getId() <= 0) {
            return 0;
        }
        try (var conn = DatabaseManager.getConnection()) {
            boolean idGiven = gameData.getId() != 0;
            String statement = (idGiven ? MAKECHESSBOARDALT : MAKENEWCHESSBOARD);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, new Gson().toJson(gameData.getGame()));
                preparedStatement.setString(2, gameData.getName());
                preparedStatement.setString(3, gameData.getPlayer1());
                preparedStatement.setString(4, gameData.getPlayer2());
                if (idGiven) {
                    preparedStatement.setInt(5, gameData.getId());
                    toReturn = gameData.getId();
                }
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        if (toReturn == 0) {
            toReturn = getGame(gameData.getName()).getId();
        }
        return toReturn;
    }

    private GameData getGame(String gameName) throws DataAccessException {
        GameData toReturn = null;
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(GETAGAMEBYITSNAME)) {
                preparedStatement.setString(1, gameName);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        toReturn = parseGameData(rs);
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
    public GameData getGame(int gameID) throws DataAccessException {
        GameData toReturn = null;
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(GETACHESSGAMEBYID)) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        toReturn = parseGameData(rs);
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

    private GameData parseGameData(java.sql.ResultSet rs) throws SQLException {
        GameData output;
        var id = rs.getInt("id");
        var name = rs.getNString("name");
        var json = rs.getNString("gameJSON");
        var whitePlayer = rs.getNString("whitePlayer");
        var blackPlayer = rs.getNString("blackPlayer");
        output = new GameData(new Gson().fromJson(json, ChessGame.class), name, id, whitePlayer, blackPlayer);
        return output;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> toReturn = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(LISTALLACTIVEGAME)) {
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        toReturn.add(parseGameData(rs));
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
    public Collection<GameData> listGames(String username) throws DataAccessException {
        ArrayList<GameData> toReturn = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(LISTALLGAMEBYUSER)) {
                preparedStatement.setNString(1, username);
                preparedStatement.setNString(2, username);
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        toReturn.add(parseGameData(rs));
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
    public void updateGame(GameData gameData) throws DataAccessException {
        GameData game = getGame(gameData.getId());
        if (game == null) {
            throw new DataAccessException("Game Does Not Exist!");
        }
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(UPDATEGAMEINTHEDB)) {
                preparedStatement.setString(1, new Gson().toJson(gameData.getGame()));
                preparedStatement.setString(2, gameData.getName());
                preparedStatement.setString(3, gameData.getPlayer1());
                preparedStatement.setString(4, gameData.getPlayer2());
                preparedStatement.setInt(5, gameData.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public int hasAuth(String username) throws DataAccessException {
        int toReturn = 0;
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(AUTHENTICATE4AUTH)) {
                preparedStatement.setNString(1, username);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        toReturn = rs.getInt("token");
                    }
                } catch (SQLException e) {
                    toReturn = 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return toReturn;
    }

    @Override
    public void createAuth(int authCode, String username) throws DataAccessException {
        if (authCode == 0 || username == null) {
            throw new DataAccessException("Invalid Auth Creation!");
        }
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
        String toReturn = null;
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(GETUSERNAMEBYAUTH)) {
                preparedStatement.setInt(1, authCode);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        toReturn = rs.getNString("username");
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
    public void deleteAuth(int authCode) throws DataAccessException {
        UserData user = getUser(authCode);
        if (user == null) {
            throw new DataAccessException("Auth Does Not Exist!");
        }
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
