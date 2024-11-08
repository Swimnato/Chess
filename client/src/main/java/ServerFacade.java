import chess.datastructures.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;

import static chess.ui.EscapeSequences.*;

public class ServerFacade {
    String ip;
    int port;
    String linkAndPort;
    int authToken;
    GameOverview[] gamesList;

    public ServerFacade(int port, String ip) {
        this.ip = ip;
        this.port = port;
        linkAndPort = "http://" + ip + ':' + port;
        authToken = 0;
        gamesList = null;
    }

    public ServerFacade() {
        ip = "127.0.0.1";
        port = 8080;
        linkAndPort = "http://" + ip + ':' + port;
        authToken = 0;
        gamesList = null;
    }

    private String makeRequest(String path, String type, String body) throws IOException, URISyntaxException, ErrorResponseException {
        URL url = (new URI(linkAndPort + path)).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", String.valueOf(this.authToken));
        conn.setRequestMethod(type);
        conn.setDoOutput(true);
        conn.addRequestProperty("Accept", "application/json");
        if (body != null) {
            try (OutputStream reqBody = conn.getOutputStream()) {
                reqBody.write(body.getBytes());
            }
        }
        conn.connect();
        if (conn.getResponseCode() != 200) {
            return conn.getResponseMessage();
        }
        var inputStream = conn.getInputStream();
        StringBuilder output = new StringBuilder();
        while (inputStream.available() > 0)
            output.append((char) inputStream.read());
        return output.toString();
    }

    public String register(String username, String password, String email) throws InvalidSyntaxException, ErrorResponseException {
        RegisterInfo info = new RegisterInfo(username, password, email);
        String output;
        try {
            output = makeRequest("/user", "POST", new Gson().toJson(info));
            if (output.equals("Forbidden")) {
                return "Username Already Taken!";
            } else if (output.equals("Bad Request")) {
                return "Bad Request!";
            }
            UsernameAuthTokenPair usernameAuthTokenPair = new Gson().fromJson(output, UsernameAuthTokenPair.class);
            authToken = usernameAuthTokenPair.getAuthToken();
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        return "Registered Successfully!";
    }

    public String login(String username, String password) throws InvalidSyntaxException, ErrorResponseException {
        LoginInfo info = new LoginInfo(username, password);
        String output;
        try {
            output = makeRequest("/session", "POST", new Gson().toJson(info));
            if (output.equals("Unauthorized")) {
                return "Bad Username/Password!";
            } else if (output.equals("Bad Request")) {
                return "Bad Request!";
            }
            UsernameAuthTokenPair usernameAuthTokenPair = new Gson().fromJson(output, UsernameAuthTokenPair.class);
            authToken = usernameAuthTokenPair.getAuthToken();
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        return "Logged In Successfully!";
    }

    public String logout() throws InvalidSyntaxException, ErrorResponseException {
        String output;
        try {
            output = makeRequest("/session", "DELETE", null);
            if (output.equals("Unauthorized")) {
                return "Bad Session!";
            }
            UsernameAuthTokenPair usernameAuthTokenPair = new Gson().fromJson(output, UsernameAuthTokenPair.class);
            authToken = usernameAuthTokenPair.getAuthToken();
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }

        return "Logged Out Successfully!";
    }

    public String listGames() throws InvalidSyntaxException, ErrorResponseException {
        String response;
        try {
            response = makeRequest("/game", "GET", null);
            if (response.equals("Unauthorized")) {
                return "Bad Session!";
            } else if (response.equals("Bad Request")) {
                return "Bad Request!";
            }
            gamesList = new Gson().fromJson(response, GamesList.class).getGames();
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        return printGamePrompts();
    }

    public String createGame(String name) throws InvalidSyntaxException, ErrorResponseException {
        CreateGameInfo info = new CreateGameInfo(name);
        String output;
        try {
            output = makeRequest("/game", "POST", new Gson().toJson(info));
            if (output.equals("Unauthorized")) {
                return "Bad Username/Password!";
            } else if (output.equals("Bad Request")) {
                return "Bad Request!";
            }
            GameID gameID = new Gson().fromJson(output, GameID.class);
            if (gameID.getGameID() <= 0) {
                return "Error Creating Game!";
            }
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        return "Created Game Successfully!";
    }

    private String printGamePrompts() {
        if (gamesList.length != 0) {
            StringBuilder output = new StringBuilder();
            output.append(SET_TEXT_COLOR_LIGHT_GREY).append("\tGameID\tGame Name\tWhite Player\tBlack Player\r\n").append(SET_TEXT_COLOR_WHITE);
            int index = 0;
            for (GameOverview game : gamesList) {
                output.append("\t").append(index++).append("     \t");
                output.append(game.toString(false)).append("\r\n");
            }
            return output.toString();
        }
        return "No Games on the server! Use " + SET_TEXT_COLOR_BLUE + "Create Game" + SET_TEXT_COLOR_WHITE + " to create one!";
    }


    public boolean testServer() throws IOException, URISyntaxException, ErrorResponseException {
        return !makeRequest("", "GET", null).isEmpty();
    }
}
