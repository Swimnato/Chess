import chess.*;
import dataaccess.DataAccessException;
import dataaccess.DatabaseDataAccess;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var server = new Server();

        var port = server.run(8080);

        System.out.println("♕ 240 Chess server started on port : " + port);
    }
}