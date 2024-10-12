package server;

import spark.*;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import chess.dataStructures.*;

import java.util.ArrayList;

public class Server {

    private Services service;

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearApplication);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.exception(DataAccessException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        //we can do some other stuff before we just are waiting for the other thread to finish initialization
        service = new Services(new MemoryDataAccess());

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(DataAccessException ex, Request req, Response res) {
        System.out.println(ex.getMessage());
        res.status(500);
        res.body("{ \"message\": \"Error: " + ex.getMessage() + " \"}");
    }

    private Object listGames(Request req, Response res) throws DataAccessException {
        return service.listGames();
    }

    private Object createGame(Request req, Response res) throws DataAccessException {
        CreateGameInfo info;
        int AuthToken = 0;
        try{
            info = new Gson().fromJson(req.body(), CreateGameInfo.class);
            String auth = req.headers("Authorization");
            AuthToken = Integer.parseInt(auth);
        }
        catch(Exception e){
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String response = service.createGame(AuthToken, info.getGameName());
        if(response.equals("{ \"message\": \"Error: unauthorized\" }")){
            res.status(401);
        }
        return response;
    }

    private Object joinGame(Request req, Response res) throws DataAccessException {
        JoinGameInfo inputs;
        int AuthToken = 0;
        try {
            inputs = new Gson().fromJson(req.body(), JoinGameInfo.class);
        }
        catch (Exception e){
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        try {
            String auth = req.headers("Authorization");
            AuthToken = Integer.parseInt(auth);
        }
        catch (Exception e){
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        String result = service.joinGame(inputs.getGameID(), inputs.getDesiredColor(), AuthToken);
        if(result.equals("{ \"message\": \"Error: bad request\" }")){
            res.status(400);
        }
        if(result.equals("{ \"message\": \"Error: unauthorized\" }")){
            res.status(401);
        }
        if(result.equals("{ \"message\": \"Error: already taken\" }")){
            res.status(403);
        }

        return result;
    }

    private Object clearApplication(Request req, Response res) throws DataAccessException {
        service.clearApplication();
        return "{}";
    }

    private Object register(Request req, Response res) throws DataAccessException {
        RegisterInfo inputs;
        try {
            inputs = new Gson().fromJson(req.body(), RegisterInfo.class);
        }
        catch (Exception e){
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String username = inputs.getUsername();
        String password = inputs.getPassword();
        String email = inputs.getEmail();
        if(username == null || password == null || email == null){
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String output = service.register(username, password, email);
        if(output.equals("{ \"message\": \"Error: already taken\" }")){
            res.status(403);
        }
        return output;
    }

    private Object login(Request req, Response res) throws DataAccessException {
        LoginInfo inputs;
        try {
            inputs = new Gson().fromJson(req.body(), LoginInfo.class);
        }
        catch (Exception e){
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String username = inputs.getUsername();
        String password = inputs.getPassword();
        String output = service.login(username, password);
        if(output.equals("{ \"message\": \"Error: unauthorized\" }")){
            res.status(401);
        }
        return output;
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        String returnVal;
        try {
            String request = req.headers("Authorization");
            returnVal  = service.logout(Integer.parseInt(request));
        }
        catch (DataAccessException e) {
            throw e;
        }
        catch (Exception e) {
            System.out.println(1);
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        if(returnVal.equals("Auth Does Not Exist!")){
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        return returnVal;
    }

    public void stop() {
        service.clearApplication();
        Spark.stop();
        Spark.awaitStop();
    }
}
