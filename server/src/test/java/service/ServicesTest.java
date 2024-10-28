package service;

import chess.datastructures.GameID;
import chess.datastructures.GameOverview;
import chess.datastructures.RegisterInfo;
import chess.datastructures.UsernameAuthTokenPair;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;
import passoff.model.TestCreateRequest;
import passoff.model.TestUser;
import server.MemoryDataAccess;
import server.Server;
import server.Services;
import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServicesTest {
    private static Services service;

    private class Games {
        ArrayList<GameOverview> games;

        Games(ArrayList<GameOverview> input) {
            games = input;
        }

        public void setGames(ArrayList<GameOverview> games) {
            this.games = games;
        }

        public ArrayList<GameOverview> getGames() {
            return games;
        }
    }

    @AfterAll
    static void stopServer() throws DataAccessException {
        service.clearApplication();
    }

    @BeforeEach
    public void clearServer() throws DataAccessException {
        service.clearApplication();
    }

    ;

    @BeforeAll
    public static void init() throws DataAccessException {
        service = new Services(new MemoryDataAccess());
    }

    @Test
    @DisplayName("Register Test")
    public void register() throws DataAccessException {
        String response = "";
        response = service.register("User", "Securepswd", "Your@facebook.com");
        Assertions.assertNotEquals("{ \"message\": \"Error: already taken\" }", response,
                "User wasn't created successfully! Got Response: " + response);
        System.out.println("Passed!");
    }

    @Test
    @DisplayName("Register Twice Test")
    public void registerTwice() throws DataAccessException {
        String response = "";
        register();
        response = service.register("User2", "EvenMoreSecurepswd", "Your@facebook.com");
        Assertions.assertNotEquals("{ \"message\": \"Error: already taken\" }", response,
                "User wasn't created successfully! Got Response: " + response);
        response = service.register("User2", "NewPswd", "hehehehe@website.org");
        Assertions.assertEquals("{ \"message\": \"Error: already taken\" }", response,
                "User wasn't created successfully! Got Response: " + response);
        System.out.println("Passed!");
    }

    @Test
    @DisplayName("Login Test")
    public void loginTest() throws DataAccessException {
        String[] response = {"", ""};
        registerTwice();
        response[0] = service.login("User", "Securepswd");
        response[1] = service.login("User2", "EvenMoreSecurepswd");
        Assertions.assertNotEquals("{ \"message\": \"Error: unauthorized\" }", response[0],
                "User wasn't created successfully! Got Response: " + response[0]);
        Assertions.assertNotEquals("{ \"message\": \"Error: unauthorized\" }", response[1],
                "User wasn't created successfully! Got Response: " + response[1]);
    }

    @Test
    @DisplayName("Login Bad Credentials Test")
    public void loginBadTest() throws DataAccessException {
        String response = service.login("FakeUser", "Nopswd");
        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response,
                "User wasn't created successfully! Got Response: " + response);
    }

    @Test
    @DisplayName("Login with Bad Password")
    public void loginBadPassword() throws DataAccessException {
        register();
        var response = service.login("User", "Hacker4Dayz");
        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response, "It didn't reject the password!");
    }

    @Test
    @DisplayName("Logout User")
    public void logoutUser() throws DataAccessException {
        String response = "";
        response = service.register("ToBeDeleted", "notImportant", "nada@gddm.com");
        var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        Assertions.assertNotEquals("{ \"message\": \"Error: already taken\" }", response,
                "User wasn't created successfully! Got Response: " + response);
        response = service.logout(info.getAuthToken());
        Assertions.assertEquals("{}", response,
                "User wasn't created successfully! Got Response: " + response);
        response = service.listGames(info.getAuthToken());
        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response,
                "Token wasn't deleted!");

    }

    @Test
    @DisplayName("Clear users test")
    public void clearUsersTest() throws DataAccessException {
        String[] response = {"", ""};
        service.register("User3", "Securepswd", "Your@facebook.com");
        service.register("User4", "EvenMoreSecurepswd", "Your@facebook.com");
        service.clearApplication();
        response[0] = service.login("User3", "Securepswd");
        response[1] = service.login("User4", "EvenMoreSecurepswd");
        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response[0],
                "User wasn't created successfully! Got Response: " + response[0]);
        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response[1],
                "User wasn't created successfully! Got Response: " + response[1]);
    }

    @Test
    @DisplayName("Create Game Test")
    public void createGame() throws DataAccessException {
        String response = service.register("ToBeDeleted", "notImportant", "nada@gddm.com");
        var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        response = service.createGame(info.getAuthToken(), "Big Cheeses' giant house");
        Assertions.assertNotEquals("{ \"message\": \"Error: unauthorized\" }", response,
                "Game wasn't created successfully! Got Response: " + response);
    }

    @Test
    @DisplayName("Create Game Test Unauthorized")
    public void createGameNoAuth() throws DataAccessException {
        String response = service.createGame(127001, "Big Cheeses' giant house");
        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response,
                "Game wasn't created successfully! Got Response: " + response);
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws DataAccessException {
        String response = service.register("gameLister", "noNeed", "hmmmm@yes.com");
        var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        response = service.listGames(info.getAuthToken());
        var games = new Gson().fromJson(response, Games.class);
        var listOfGames = games.getGames();
        Assertions.assertEquals(0, listOfGames.size(), "Games not empty!");

        service.createGame(info.getAuthToken(), "1");
        service.createGame(info.getAuthToken(), "2");
        service.createGame(info.getAuthToken(), "3");

        response = service.listGames(info.getAuthToken());
        games = new Gson().fromJson(response, Games.class);
        listOfGames = games.getGames();
        Assertions.assertEquals(3, listOfGames.size(), "Games not populating!");

    }

    @Test
    @DisplayName("Join Game")
    public void joinGameTest() throws DataAccessException {
        int authTokens[] = {0, 0};
        String response = service.register("User1", "1", "y@w.io");
        var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        authTokens[0] = info.getAuthToken();
        response = service.register("User2", "2", "y@w.io");
        info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        authTokens[1] = info.getAuthToken();

        response = service.createGame(authTokens[0], "Game1");
        var gameInfo = new Gson().fromJson(response, GameID.class);

        service.joinGame(gameInfo.getGameID(), "WHITE", authTokens[0]);
        service.joinGame(gameInfo.getGameID(), "BLACK", authTokens[1]);

        response = service.listGames(authTokens[0]);
        var games = new Gson().fromJson(response, Games.class);
        var game = games.games.get(0);

        Assertions.assertEquals("User1", game.getWhiteUsername(), "White Username Incorrect!");
        Assertions.assertEquals("User2", game.getBlackUsername(), "Black Username Incorrect!");

    }

    @Test
    @DisplayName("Join Game Same Color")
    public void joinGameSameColor() throws DataAccessException {
        int authTokens[] = {0, 0};
        String response = service.register("User1", "1", "y@w.io");
        var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        authTokens[0] = info.getAuthToken();
        response = service.register("User2", "2", "y@w.io");
        info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        authTokens[1] = info.getAuthToken();

        response = service.createGame(authTokens[0], "Game1");
        var gameInfo = new Gson().fromJson(response, GameID.class);

        service.joinGame(gameInfo.getGameID(), "WHITE", authTokens[0]);
        response = service.joinGame(gameInfo.getGameID(), "WHITE", authTokens[1]);

        Assertions.assertEquals("{ \"message\": \"Error: already taken\" }", response, "Error not thrown!");

    }


    @Test
    @DisplayName("Join Game Same Color as P2")
    public void joinGameSameColorP2() throws DataAccessException {
        int authTokens[] = {0, 0};
        String response = service.register("User1", "1", "y@w.io");
        var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        authTokens[0] = info.getAuthToken();
        response = service.register("User2", "2", "y@w.io");
        info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        authTokens[1] = info.getAuthToken();

        response = service.createGame(authTokens[0], "Game1");
        var gameInfo = new Gson().fromJson(response, GameID.class);

        service.joinGame(gameInfo.getGameID(), "BLACK", authTokens[0]);
        response = service.joinGame(gameInfo.getGameID(), "BLACK", authTokens[1]);

        Assertions.assertEquals("{ \"message\": \"Error: already taken\" }", response, "Error not thrown!");

    }

    @Test
    @DisplayName("Join W/O auth")
    public void joinGameWOAuth() throws DataAccessException {
        int[] authTokens = {0, 0};
        String response = service.register("User1", "1", "y@w.io");
        var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        authTokens[0] = info.getAuthToken();

        response = service.createGame(authTokens[0], "Game1");
        var gameInfo = new Gson().fromJson(response, GameID.class);

        service.joinGame(gameInfo.getGameID(), "WHITE", authTokens[0]);
        response = service.joinGame(gameInfo.getGameID(), "Black", authTokens[0] + 1);

        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response, "Error not thrown!");
    }

    @Test
    @DisplayName("Join game with null ID")
    public void joinInvalidGame() throws DataAccessException {
        String response = service.register("User1", "1", "y@w.io");
        var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
        var result = service.joinGame(395, "WHITE", info.getAuthToken());
        Assertions.assertEquals("{ \"message\": \"Error: bad request\" }", result, "The Error was not thrown!");
    }

    @Test
    @DisplayName("Invalid Logout")
    public void logoutInvalid() throws DataAccessException {
        var result = service.logout(8764423);
        Assertions.assertEquals("Auth Does Not Exist!", result, "Did not error out!");

    }


}
