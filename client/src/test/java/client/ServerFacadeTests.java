package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.REPLClient;

import java.io.InputStream;
import java.io.PrintStream;


public class ServerFacadeTests {

    private static Server server;
    private static REPLClient client;
    private static final String ip = "127.0.0.1 ";


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        System.out.println("AttemptingToRunClient");

        client = new REPLClient();

        String[] args = {Integer.toString(port), ip};
        client.setupPortAndIP(args);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @DisplayName("RunBlankRepl")
    public void runBlankRepl() {
        client.runREPL(true);
    }

    @Test
    @DisplayName("Getting Help")
    public void getHelp() {
        Assertions.assertDoesNotThrow(Exception.class, () -> client.);
    }

}
