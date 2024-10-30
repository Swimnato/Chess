package dataaccess;

import chess.ChessGame;
import chess.datastructures.GameData;
import dataaccess.DataAccessException;
import dataaccess.DatabaseDataAccess;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import server.DataStorage;
import server.MemoryDataAccess;

import java.util.ArrayList;

public class DataAccessTest {
    private static DataStorage storage;

    @BeforeAll
    public static void createStorage() throws DataAccessException {
        storage = new DatabaseDataAccess();
    }

    @AfterAll
    public static void closeAll() throws DataAccessException {
        storage.clear();
    }

    @BeforeEach
    public void clearTheSlate() throws DataAccessException {
        storage.clear();
    }

    @Test
    @DisplayName("Create User")
    public void createUser() throws DataAccessException {
        storage.createUser("User", "Password", "e@mail.com");
    }

    @Test
    @DisplayName("Create Users")
    public void createUsers() throws DataAccessException {
        storage.createUser("User", "Password", "e@mail.com");
        storage.createUser("User2", "Password2", "e2@mail.com");
        storage.createUser("User3", "Password3", "e3@mail.com");
        storage.createUser("User4", "Password4", "e4@mail.com");
    }

    @Test
    @DisplayName("Get Users")
    public void getUsers() throws DataAccessException {
        createUsers();

        var result = storage.getUser("User");
        Assertions.assertNotEquals(null, result, "User Could not be retrieved!");

        result = storage.getUser("User2");
        Assertions.assertNotEquals(null, result, "User2 Could not be retrieved!");

        result = storage.getUser("User27");
        Assertions.assertNull(result, "Bad User Retrieved!");
    }

    @Test
    @DisplayName("Create Auth")
    public void makeAuth() throws DataAccessException {
        createUsers();
        storage.createAuth(123456, "User");
        storage.createAuth(654321, "User2");
        storage.createAuth(24601, "User3");
    }

    @Test
    @DisplayName("Get User by Auth")
    public void getAuth() throws DataAccessException {
        makeAuth();

        var result = storage.getUser(123456);
        Assertions.assertNotEquals(null, result, "User Could not be retrieved by auth code!");

        result = storage.getUser(654321);
        Assertions.assertNotEquals(null, result, "User2 Could not be retrieved by auth code!");

        result = storage.getUser(11111111);
        Assertions.assertNull(result, "Bad authToken returned user!");
    }

    @Test
    @DisplayName("Has Auth")
    public void hasAuth() throws DataAccessException {
        makeAuth();

        var result = storage.hasAuth("User");
        Assertions.assertNotEquals(0, result, "User did not have an auth code");

        result = storage.hasAuth("User2");
        Assertions.assertNotEquals(0, result, "User2 did not have an auth code");

        result = storage.hasAuth("User3");
        Assertions.assertNotEquals(0, result, "User3 did not have an auth code");

        result = storage.hasAuth("User4");
        Assertions.assertEquals(0, result, "User4 returned an auth code");
    }

    @Test
    @DisplayName("Delete Auth")
    public void deleteAuth() throws DataAccessException {
        makeAuth();

        storage.deleteAuth(123456);
        storage.deleteAuth(654321);

        var result = storage.hasAuth("User");
        Assertions.assertEquals(0, result, "User returned an auth code");

        result = storage.hasAuth("User2");
        Assertions.assertEquals(0, result, "User2 returned an auth code");

        result = storage.hasAuth("User3");
        Assertions.assertNotEquals(0, result, "User3 had their auth deleted accidentally");

        result = storage.hasAuth("User4");
        Assertions.assertEquals(0, result, "User4 returned an auth code");

        Assertions.assertThrows(DataAccessException.class, () -> storage.deleteAuth(55555),
                "Failed to thow exception for invalid authCode to be deleted");
    }

    @Test
    @DisplayName("Create Game")
    public void createGame() throws DataAccessException {
        var result = storage.createGame(new GameData(new ChessGame(), "Joe's House", 65));
        Assertions.assertNotEquals(0, result);
    }

    @Test
    @DisplayName("Create Games")
    public void createGames() throws DataAccessException {
        var result = storage.createGame(new GameData(new ChessGame(), "Joe's House", 65));
        Assertions.assertNotEquals(0, result, "Failed to create a game");
        result = storage.createGame(new GameData(new ChessGame(), "Happy Dayz", 584));
        Assertions.assertNotEquals(0, result, "Failed to create a second game");
        result = storage.createGame(new GameData(new ChessGame(), "Joe's House", 39485)); // game with same name, but different ID
        Assertions.assertNotEquals(0, result, "Failed to create a game that has a distinct ID, but same name as other game");
        result = storage.createGame(new GameData(new ChessGame(), "Duplicate ID", 65));
        Assertions.assertEquals(0, result, "Created a game with a duplicate ID!");
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws DataAccessException {
        var games = storage.listGames();
        Assertions.assertEquals(0, games.size(), "Couldn't read length of zero games");

        createGame();

        games = storage.listGames();
        Assertions.assertEquals(1, games.size(), "Couldn't read length of one game");

        storage.clear();
        createGames();

        games = storage.listGames();
        Assertions.assertEquals(3, games.size(), "Couldn't read length of three game");
    }

    @Test
    @DisplayName("Get Game")
    public void getGames() throws DataAccessException {
        createGames();

        var result = storage.getGame(65);
        Assertions.assertNotNull(result, "Valid game returned as null");

        result = storage.getGame(584);
        Assertions.assertNotNull(result, "Valid game returned as null");

        result = storage.getGame(39485);
        Assertions.assertNotNull(result, "Valid game returned as null");

        result = storage.getGame(24601);
        Assertions.assertNull(result, "Invalid game returned a game");
    }

    @Test
    @DisplayName("Get Games For User")
    public void getGamesForUser() throws DataAccessException {
        createUsers();

        {
            var result = storage.createGame(new GameData(new ChessGame(), "Joe's House", 65, "User"));
            Assertions.assertNotEquals(0, result, "Game with one user failed to create");
            result = storage.createGame(new GameData(new ChessGame(), "Happy Dayz", 584, "User2", "User"));
            Assertions.assertNotEquals(0, result, "Game with two users failed to create");
            result = storage.createGame(new GameData(new ChessGame(), "Joe's House", 39485,
                    "User3", "User2")); // game with same name, but different ID
            Assertions.assertNotEquals(0, result, "Game with two users failed to create");
        }

        System.out.println(storage.listGames());

        var result = storage.listGames("User");
        Assertions.assertEquals(2, result.size(), "User was listed in the incorrect number of games!");

        result = storage.listGames("User2");
        Assertions.assertEquals(2, result.size(), "User2 was listed in the incorrect number of games!");

        result = storage.listGames("User3");
        Assertions.assertEquals(1, result.size(), "User3 was listed in the incorrect number of games!");
        Assertions.assertEquals(39485, (new ArrayList<GameData>(result)).get(0).getId(), "Returned Wrong Game");

        result = storage.listGames("User4");
        Assertions.assertEquals(0, result.size(), "User4 was listed in a game!");
    }

    @Test
    @DisplayName("Update Game")
    public void updateGames() throws DataAccessException {
        createGames();

        storage.updateGame(new GameData(new GameData(new ChessGame(), "New Name", 65)));
        var result = storage.getGame(65);
        Assertions.assertEquals("New Name", result.getName(), "Name Update didn't work!");
        result = storage.getGame(584);
        Assertions.assertEquals("Happy Dayz", result.getName(), "Name Update edited other game!");
        result = storage.getGame(39485);
        Assertions.assertEquals("Joe's House", result.getName(), "Name Update edited other game!");

        storage.updateGame(new GameData(new GameData(new ChessGame(), "Newer Name", 584)));
        result = storage.getGame(65);
        Assertions.assertEquals("New Name", result.getName(), "Name Update edited other game!");
        result = storage.getGame(584);
        Assertions.assertEquals("Newer Name", result.getName(), "Name Update didn't work!");
        result = storage.getGame(39485);
        Assertions.assertEquals("Joe's House", result.getName(), "Name Update edited other game!");

        Assertions.assertThrows(DataAccessException.class, () -> storage.updateGame(new GameData(new ChessGame(), "", 55555)));
    }

}
