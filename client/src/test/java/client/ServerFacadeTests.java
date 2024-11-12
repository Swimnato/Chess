package client;

import commandparser.InvalidSyntaxException;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;
import ui.REPLClient;

import java.io.InputStream;
import java.io.PrintStream;

import static chess.ui.EscapeSequences.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static final String ip = "127.0.0.1 ";


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        System.out.println("AttemptingToRunClient");

        facade = new ServerFacade(port, "127.0.0.1");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("ClearServer")
    public void clearServer() {
        Assertions.assertDoesNotThrow(() -> facade.clearServer(), "FailedToClearServer");
    }

    @Test
    @DisplayName("Clear Fail")
    public void clearFail() {
        ServerFacade failFacade = new ServerFacade(0, "0.0.0.0");
        Assertions.assertThrows(Exception.class, () -> failFacade.clearServer(), "Cleared False Server!");
    }

    @Test
    @DisplayName("Register User")
    public void registerUser() throws Exception {
        clearServer();
        String output = facade.register("userName", "PWD", "EML");
        Assertions.assertEquals("Registered Successfully!", output, "Failed to register user");
    }

    @Test
    @DisplayName("Register Users")
    public void registerUsers() throws Exception {
        registerUser();
        String output = facade.register("user2", "PWD", "EML");
        Assertions.assertEquals("Registered Successfully!", output, "Failed to register user");
    }

    @Test
    @DisplayName("Register Fail")
    public void registerFail() throws Exception {
        registerUsers();
        String output = facade.register("userName", "PWD", "EML");
        Assertions.assertNotEquals("Registered Successfully!", output, "Duplicate User Registered Twice!");
    }

    @Test
    @DisplayName("Login")
    public void loginUser() throws Exception {
        registerUsers();
        String output = facade.login("userName", "PWD");
        Assertions.assertEquals("Logged In Successfully!", output, "Failed to login user");
        output = facade.login("user2", "PWD");
        Assertions.assertEquals("Logged In Successfully!", output, "Failed to login user");

    }

    @Test
    @DisplayName("Login Fail")
    public void loginFail() throws Exception {
        clearServer();
        String output = facade.login("notFound", "PWD");
        Assertions.assertNotEquals("Logged In Successfully!", output, "Logged In Fake User!");
    }

    @Test
    @DisplayName("Fail Create Game")
    public void createBadGame() throws Exception {
        clearServer();
        String output = facade.createGame("uhOh");
        Assertions.assertNotEquals("Created Game Successfully!", output, "Created Game When Not Logged In!");

    }

    @Test
    @DisplayName("Create Games")
    public void createGames() throws Exception {
        registerUser();

        String output = facade.createGame("CheeseAndRice");
        Assertions.assertEquals("Created Game Successfully!", output, "Failed to create game");

        output = facade.createGame("HamNCheese");
        Assertions.assertEquals("Created Game Successfully!", output, "Failed to create game");

        output = facade.createGame("ChickenAlfredo");
        Assertions.assertEquals("Created Game Successfully!", output, "Failed to create game");
    }

    @Test
    @DisplayName("List Fail")
    public void listGamesFail() throws Exception {
        clearServer();
        String output = facade.listGames();
        Assertions.assertEquals(SET_TEXT_COLOR_RED + "Bad Session!", output, "Listed Games when user wasn't logged in!");
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws Exception {
        registerUser();
        String output = facade.listGames();
        Assertions.assertEquals("No Games on the server! Use " + SET_TEXT_COLOR_BLUE +
                        "Create Game" + SET_TEXT_COLOR_WHITE + " to create one!", output,
                "Listed Games when user wasn't logged in!");

        clearServer();
        createGames();
        output = facade.listGames();
        Assertions.assertNotEquals(SET_TEXT_COLOR_RED + "Bad Session!", output, "Valid Session was rejected!");
        Assertions.assertNotEquals("No Games on the server! Use " + SET_TEXT_COLOR_BLUE +
                        "Create Game" + SET_TEXT_COLOR_WHITE + " to create one!", output,
                "No Games Returned!");
    }

    @Test
    @DisplayName("Join When Not Logged In")
    public void JoinWhenNotLoggedIn() throws Exception {
        clearServer();

        String output = facade.joinGame(0, "WHITE");
        Assertions.assertEquals("Please run " + SET_TEXT_COLOR_BLUE + "List Games" +
                SET_TEXT_COLOR_WHITE + " to show available games first", output, "Joined Game before listing them!");

    }

    @Test
    @DisplayName("Join Games")
    public void joinGames() throws Exception {
        listGames();

        String output = facade.joinGame(1, "WHITE");
        if (!output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Unable to Join Game!");
        }
        output = facade.joinGame(3, "BLACK");
        if (!output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Unable to Join Game!");
        }
        output = facade.joinGame(1, "WHITE");
        if (!output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Unable to Join Game as Same Color!");
        }
        output = facade.joinGame("ChickenAlfredo", "BLACK");
        if (!output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Unable to Join Game as Same Color by name!");
        }
        Assertions.assertThrows(InvalidSyntaxException.class, () -> facade.joinGame(9, "WHITE"), "Joined Invalid Game!");

        output = facade.register("user2", "PWD", "EML");
        Assertions.assertEquals("Registered Successfully!", output, "Failed to register user");


        output = facade.joinGame(3, "WHITE");
        if (!output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Unable to Join Game with second user!");
        }
    }

    @Test
    @DisplayName("Other User Take Occupied Slot")
    public void takeOccupiedSlot() throws Exception {
        joinGames(); // this ends with us signed in as the second user
        String output = facade.joinGame(1, "WHITE");
        if (output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Joined Taken Slot In Game!");
        }
    }

    @Test
    @DisplayName("Logout")
    public void logout() throws Exception {
        registerUser();
        String output = facade.createGame("CheeseAndRice");
        Assertions.assertEquals("Created Game Successfully!", output, "Failed to create game");

        facade.logout();

        output = facade.createGame("uhOh");
        Assertions.assertNotEquals("Created Game Successfully!", output, "Logout Didn't Work!");
    }

    @Test
    @DisplayName("Logout Fail")
    public void logoutFail() throws Exception {
        clearServer();
        String output = facade.logout();
        Assertions.assertEquals(SET_TEXT_COLOR_RED + "Bad Session!", output, "Logout on bad session worked!");
    }

}
