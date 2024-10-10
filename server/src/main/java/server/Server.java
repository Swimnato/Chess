package server;

import spark.*;
import dataaccess.DataAccessException;

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
        res.status(400);
        res.body(ex.getMessage());
    }

    private Object listGames(Request req, Response res) throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    private Object createGame(Request req, Response res) throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    private Object joinGame(Request req, Response res) throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    private Object clearApplication(Request req, Response res) throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    private Object register(Request req, Response res) throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    private Object login(Request req, Response res) throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        throw new DataAccessException("Not Implemmented!");
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
