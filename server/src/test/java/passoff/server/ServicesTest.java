package passoff.server;

import chess.dataStructures.GameID;
import chess.dataStructures.GameOverview;
import chess.dataStructures.RegisterInfo;
import chess.dataStructures.UsernameAuthTokenPair;
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
        Games(ArrayList<GameOverview> input){
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
    static void stopServer() {
        service.clearApplication();
    }

    @BeforeAll
    public static void init() {
        service = new Services(new MemoryDataAccess());
    }

    @Test
    @Order(1)
    @DisplayName("Register Test")
    public void register(){
        String response = "";
        try {
            response = service.register("User", "Securepswd", "Your@facebook.com");
        }
        catch(DataAccessException e){
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
        Assertions.assertNotEquals("{ \"message\": \"Error: already taken\" }", response,
                "User wasn't created successfully! Got Response: " + response);
        System.out.println("Passed!");
    }

    @Test
    @Order(2)
    @DisplayName("Register Twice Test")
    public void registerTwice(){
        String response = "";
        try {
            response = service.register("User2", "EvenMoreSecurepswd", "Your@facebook.com");
        }
        catch(DataAccessException e){
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
        Assertions.assertNotEquals("{ \"message\": \"Error: already taken\" }", response,
                "User wasn't created successfully! Got Response: " + response);
        try {
            response = service.register("User2", "NewPswd", "hehehehe@website.org");
        }
        catch(DataAccessException e){
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
        Assertions.assertEquals("{ \"message\": \"Error: already taken\" }", response,
                "User wasn't created successfully! Got Response: " + response);
        System.out.println("Passed!");
    }

    @Test
    @Order(3)
    @DisplayName("Login Test")
    public void loginTest() {
        String[] response = {"",""};
        service.clearApplication();
        register();
        registerTwice();
        try{
            response[0] = service.login("User", "Securepswd");
            response[1] = service.login("User2","EvenMoreSecurepswd");
        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
        Assertions.assertNotEquals("{ \"message\": \"Error: unauthorized\" }", response[0],
                "User wasn't created successfully! Got Response: " + response[0]);
        Assertions.assertNotEquals("{ \"message\": \"Error: unauthorized\" }", response[1],
                "User wasn't created successfully! Got Response: " + response[1]);
    }

    @Test
    @Order(4)
    @DisplayName("Login Bad Credentials Test")
    public void loginBadTest(){
        try{
            String response = service.login("FakeUser", "Nopswd");
            Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response,
                    "User wasn't created successfully! Got Response: " + response);
        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Logout User")
    public void logoutUser(){
        String response = "";
        try{
            response = service.register("ToBeDeleted","notImportant", "nada@gddm.com");
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
        catch(DataAccessException e){
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }

    }

    @Test
    @Order(6)
    @DisplayName("Clear users test")
    public void clearUsersTest() {
        String[] response = {"",""};
        try{
            service.register("User3", "Securepswd", "Your@facebook.com");
            service.register("User4", "EvenMoreSecurepswd", "Your@facebook.com");
            service.clearApplication();
            response[0] = service.login("User3", "Securepswd");
            response[1] = service.login("User4","EvenMoreSecurepswd");
        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response[0],
                "User wasn't created successfully! Got Response: " + response[0]);
        Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response[1],
                "User wasn't created successfully! Got Response: " + response[1]);
    }

    @Test
    @Order(7)
    @DisplayName("Create Game Test")
    public void createGame(){
        try{
            String response = service.register("ToBeDeleted","notImportant", "nada@gddm.com");
            var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
            response = service.createGame(info.getAuthToken(), "Big Cheeses' giant house");
            Assertions.assertNotEquals("{ \"message\": \"Error: unauthorized\" }", response,
                    "Game wasn't created successfully! Got Response: " + response);
        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    @DisplayName("Create Game Test Unauthorized")
    public void createGameNoAuth(){
        try{
            String response = service.createGame(127001, "Big Cheeses' giant house");
            Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response,
                    "Game wasn't created successfully! Got Response: " + response);
        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(9)
    @DisplayName("List Games")
    public void listGames(){
        try{
            service.clearApplication();
            String response = service.register("gameLister", "noNeed", "hmmmm@yes.com");
            var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
            response = service.listGames(info.getAuthToken());
            var games = new Gson().fromJson(response, Games.class);
            var listOfGames = games.getGames();
            Assertions.assertEquals(0, listOfGames.size(), "Games not empty!");

            service.createGame(info.getAuthToken(),"1");
            service.createGame(info.getAuthToken(),"2");
            service.createGame(info.getAuthToken(),"3");

            response = service.listGames(info.getAuthToken());
            games = new Gson().fromJson(response, Games.class);
            listOfGames = games.getGames();
            Assertions.assertEquals(3, listOfGames.size(), "Games not populating!");
        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    @DisplayName("Join Game")
    public void joinGameTest(){
        service.clearApplication();
        try {
            int AuthTokens[] = {0,0};
            String response = service.register("User1", "1", "y@w.io");
            var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
            AuthTokens[0] = info.getAuthToken();
            response = service.register("User2", "2", "y@w.io");
            info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
            AuthTokens[1] = info.getAuthToken();

            response = service.createGame(AuthTokens[0], "Game1");
            var gameInfo = new Gson().fromJson(response, GameID.class);

            service.joinGame(gameInfo.getGameID(), "WHITE",AuthTokens[0]);
            service.joinGame(gameInfo.getGameID(), "BLACK",AuthTokens[1]);

            response = service.listGames(AuthTokens[0]);
            var games = new Gson().fromJson(response, Games.class);
            var game = games.games.get(0);

            Assertions.assertEquals("User1", game.getWhiteUsername(), "White Username Incorrect!");
            Assertions.assertEquals("User2", game.getBlackUsername(), "Black Username Incorrect!");

        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(11)
    @DisplayName("Join Game Same Color")
    public void joinGameSameColor(){
        service.clearApplication();
        try {
            int AuthTokens[] = {0,0};
            String response = service.register("User1", "1", "y@w.io");
            var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
            AuthTokens[0] = info.getAuthToken();
            response = service.register("User2", "2", "y@w.io");
            info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
            AuthTokens[1] = info.getAuthToken();

            response = service.createGame(AuthTokens[0], "Game1");
            var gameInfo = new Gson().fromJson(response, GameID.class);

            service.joinGame(gameInfo.getGameID(), "WHITE",AuthTokens[0]);
            response = service.joinGame(gameInfo.getGameID(), "WHITE",AuthTokens[1]);

            Assertions.assertEquals("{ \"message\": \"Error: already taken\" }", response, "Error not thrown!");

        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(12)
    @DisplayName("Join W/O auth")
    public void joinGameWOAuth(){
        service.clearApplication();
        try {
            int AuthTokens[] = {0,0};
            String response = service.register("User1", "1", "y@w.io");
            var info = new Gson().fromJson(response, UsernameAuthTokenPair.class);
            AuthTokens[0] = info.getAuthToken();

            response = service.createGame(AuthTokens[0], "Game1");
            var gameInfo = new Gson().fromJson(response, GameID.class);

            service.joinGame(gameInfo.getGameID(), "WHITE",AuthTokens[0]);
            response = service.joinGame(gameInfo.getGameID(), "Black",AuthTokens[0]+1);

            Assertions.assertEquals("{ \"message\": \"Error: unauthorized\" }", response, "Error not thrown!");

        } catch (DataAccessException e) {
            Assertions.assertEquals(0,1, "Exception Thrown: " + e.getMessage());
        }
    }



}
