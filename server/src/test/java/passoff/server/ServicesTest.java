package passoff.server;

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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServicesTest {
    private static Services service;
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
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
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
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
        }
        Assertions.assertNotEquals("{ \"message\": \"Error: already taken\" }", response,
                "User wasn't created successfully! Got Response: " + response);
        try {
            response = service.register("User2", "NewPswd", "hehehehe@website.org");
        }
        catch(DataAccessException e){
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
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
        register();
        registerTwice();
        try{
            response[0] = service.login("User", "Securepswd");
            response[1] = service.login("User2","EvenMoreSecurepswd");
        } catch (DataAccessException e) {
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
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
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
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
        }
        catch(DataAccessException e){
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
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
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
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
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
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
            System.err.println("Exception Thrown: " + e.getMessage());
            return;
        }
    }



}
