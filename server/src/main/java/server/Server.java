package server;

import server.websocket.WebSocketHandler;
import spark.*;
import com.google.gson.Gson;
import chess.datastructures.*;
import dataaccess.*;

public class Server {

    private Services service;

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        DataStorage dataPersistence;
        try {
            dataPersistence = new DatabaseDataAccess();
            service = new Services(dataPersistence);
        } catch (DataAccessException e) {
            System.err.print(e.getMessage());
            stop();
            return 0;
        }

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        WebSocketHandler webSocketHandler = new WebSocketHandler(dataPersistence);
        Spark.webSocket("/ws", webSocketHandler);
        Spark.get("/game", this::listGames);
        Spark.get("/chessGame", this::getGame);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearApplication);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.exception(DataAccessException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(DataAccessException ex, Request req, Response res) {
        System.out.println(ex.getMessage());
        res.status(500);
        res.body("{ \"message\": \"Error: " + ex.getMessage() + " \"}");
    }

    private Object listGames(Request req, Response res) throws DataAccessException {
        int authToken = 0;
        try {
            String auth = req.headers("Authorization");
            authToken = Integer.parseInt(auth);
        } catch (Exception e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String result = service.listGames(authToken);
        if (result == "{ \"message\": \"Error: unauthorized\" }") {
            res.status(401);
        }
        return result;
    }

    private Object getGame(Request req, Response res) throws DataAccessException {
        int authToken = 0;
        int gameID;
        try {
            gameID = Integer.parseInt(req.headers("gameID"));
        } catch (Exception e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        GameData result = service.getGame(gameID);
        if (result == null) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        return (new Gson().toJson(result));
    }

    private Object createGame(Request req, Response res) throws DataAccessException {
        CreateGameInfo info;
        int authToken = 0;
        try {
            info = new Gson().fromJson(req.body(), CreateGameInfo.class);
            String auth = req.headers("Authorization");
            authToken = Integer.parseInt(auth);
        } catch (Exception e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String response = service.createGame(authToken, info.getGameName());
        if (response.equals("{ \"message\": \"Error: unauthorized\" }")) {
            res.status(401);
        }
        return response;
    }

    private Object joinGame(Request req, Response res) throws DataAccessException {
        JoinGameInfo inputs;
        int authToken = 0;
        try {
            inputs = new Gson().fromJson(req.body(), JoinGameInfo.class);
        } catch (Exception e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        try {
            String auth = req.headers("Authorization");
            authToken = Integer.parseInt(auth);
        } catch (Exception e) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        if (inputs.getplayerColor() == null || (!inputs.getplayerColor().equals("WHITE") && !inputs.getplayerColor().equals("BLACK"))) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String result = service.joinGame(inputs.getGameID(), inputs.getplayerColor(), authToken);
        if (result.equals("{ \"message\": \"Error: bad request\" }")) {
            res.status(400);
        }
        if (result.equals("{ \"message\": \"Error: unauthorized\" }")) {
            res.status(401);
        }
        if (result.equals("{ \"message\": \"Error: already taken\" }")) {
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
        } catch (Exception e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String username = inputs.getUsername();
        String password = inputs.getPassword();
        String email = inputs.getEmail();
        if (username == null || password == null || email == null) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String output = service.register(username, password, email);
        if (output.equals("{ \"message\": \"Error: already taken\" }")) {
            res.status(403);
        }
        return output;
    }

    private Object login(Request req, Response res) throws DataAccessException {
        LoginInfo inputs;
        try {
            inputs = new Gson().fromJson(req.body(), LoginInfo.class);
        } catch (Exception e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        String username = inputs.getUsername();
        String password = inputs.getPassword();
        String output = service.login(username, password);
        if (output.equals("{ \"message\": \"Error: unauthorized\" }")) {
            res.status(401);
        }
        return output;
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        String returnVal;
        try {
            String request = req.headers("Authorization");
            returnVal = service.logout(Integer.parseInt(request));
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(1);
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        if (returnVal.equals("Auth Does Not Exist!")) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }
        return returnVal;
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
