package client;

import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;
import ui.REPLClient;

import java.io.InputStream;
import java.io.PrintStream;


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
    @DisplayName("Register Users")
    public void registerUsers() throws Exception {
        clearServer();
        String output = facade.register("userName", "PWD", "EML");
        Assertions.assertEquals("Registered Successfully!", output, "Failed to register user");
        output = facade.register("user2", "PWD", "EML");
        Assertions.assertEquals("Registered Successfully!", output, "Failed to register user");
        output = facade.register("userName", "PWD", "EML");
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
        output = facade.login("notFound", "PWD");
        Assertions.assertNotEquals("Logged In Successfully!", output, "Logged In Fake User!");
    }

    @Test
    @DisplayName("Create Game")
    public void createGames() throws Exception {
        clearServer();
        String output = facade.register("userName", "PWD", "EML");
        Assertions.assertEquals("Registered Successfully!", output, "Failed to register user");

        output = facade.createGame("CheeseAndRice");
        Assertions.assertEquals("Created Game Successfully!", output, "Failed to create game");

        output = facade.createGame("HamNCheese");
        Assertions.assertEquals("Created Game Successfully!", output, "Failed to create game");

        output = facade.createGame("ChickenAlfredo");
        Assertions.assertEquals("Created Game Successfully!", output, "Failed to create game");
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws Exception {
        createGames();
    }

    @Test
    @DisplayName("Join Games")
    public void joinGames() throws Exception {
        listGames();

        String output = facade.joinGame(1, "WHITE");
        if (!output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Unable to Join Game!");
        }
        output = facade.joinGame(3, "WHITE");
        if (!output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Unable to Join Game!");
        }
        output = facade.joinGame(1, "WHITE");
        if (!output.contains("Joined Game Successfully!")) {
            Assertions.assertEquals(0, 1, "Unable to Join Game!");
        }
    }

}
