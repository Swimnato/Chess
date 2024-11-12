import chess.ChessGame;
import chess.datastructures.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    private String makeRequest(String path, String type, String body, Map<String, String> extraHeaders) throws IOException, URISyntaxException, ErrorResponseException {
        URL url = (new URI(linkAndPort + path)).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", String.valueOf(this.authToken));
        if (extraHeaders != null) {
            for (var headerVal : extraHeaders.entrySet()) {
                conn.setRequestProperty(headerVal.getKey(), headerVal.getValue());
            }
        }
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
            return "" + conn.getResponseCode();
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
            output = makeRequest("/user", "POST", new Gson().toJson(info), null);
            if (output.equals("403")) {
                return "Username Already Taken!";
            } else if (output.equals("400")) {
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
            output = makeRequest("/session", "POST", new Gson().toJson(info), null);
            if (output.equals("401")) {
                return "Bad Username/Password!";
            } else if (output.equals("400")) {
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
            output = makeRequest("/session", "DELETE", null, null);
            if (output.equals("401")) {
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
            response = makeRequest("/game", "GET", null, null);
            if (response.equals("401")) {
                return "Bad Session!";
            } else if (response.equals("400")) {
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
            output = makeRequest("/game", "POST", new Gson().toJson(info), null);
            if (output.equals("401")) {
                return "Bad Username/Password!";
            } else if (output.equals("400")) {
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

    public String joinGame(String desiredGame, String desiredColor) throws InvalidSyntaxException, ErrorResponseException {
        int indexOnList = 0;
        for (var item : gamesList) {
            if (item.getGameName().equals(desiredGame)) {
                break;
            }
            indexOnList++;
        }
        return joinGame(indexOnList, desiredColor);
    }


    public String joinGame(int desiredGame, String desiredColor) throws InvalidSyntaxException, ErrorResponseException {
        if (gamesList == null) {
            return "Please run " + SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_WHITE + " to show available games first";
        }
        if (desiredGame > gamesList.length) {
            throw new InvalidSyntaxException(SET_TEXT_COLOR_RED + "Invalid Game ID! Use" + SET_TEXT_COLOR_BLUE +
                    "List Games" + SET_TEXT_COLOR_RED + " to show available games with their IDs", true);
        }
        try {
            JoinGameInfo joinGameInfo = new JoinGameInfo(desiredColor, desiredGame);
            String output = makeRequest("/game", "PUT", new Gson().toJson(joinGameInfo), null);
            if (output.equals("401")) {
                return "Bad Username/Password!";
            } else if (output.equals("400")) {
                return "Bad Request!";
            } else if (output.equals("403")) {
                return "Color Already Taken In Desired Game! Use " + SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_WHITE + " to show updated games";
            }
            GameID gameID = new Gson().fromJson(output, GameID.class);
            if (gameID.getGameID() <= 0) {
                return "Error Joining Game!";
            }
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        return "Joined Game Successfully!\r\n" + getChessGameFromServer(desiredGame);
    }

    public String getChessGameFromServer(String desiredGame) throws InvalidSyntaxException, ErrorResponseException {
        int indexOnList = 0;
        for (var item : gamesList) {
            if (item.getGameName().equals(desiredGame)) {
                break;
            }
            indexOnList++;
        }
        return getChessGameFromServer(indexOnList);
    }

    public String getChessGameFromServer(int desiredGame) throws InvalidSyntaxException, ErrorResponseException {
        GameData game;
        if (desiredGame > gamesList.length) {
            throw new InvalidSyntaxException(SET_TEXT_COLOR_RED + "Invalid Game #! Use" + SET_TEXT_COLOR_BLUE +
                    "List Games" + SET_TEXT_COLOR_RED + " to show available games with their #s", true);
        }
        String response;
        try {
            response = makeRequest("/chessGame", "GET", null, Map.of("gameID", Integer.toString(desiredGame)));
            if (response.equals("400")) {
                return "Bad Request!";
            }
            game = new Gson().fromJson(response, GameData.class);
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        return game.getGame().toString(ChessGame.TeamColor.WHITE) + "\r\n\r\n" + game.getGame().toString(ChessGame.TeamColor.BLACK);
    }

    private String printGamePrompts() {
        if (gamesList.length != 0) {
            StringBuilder output = new StringBuilder();
            output.append(SET_TEXT_COLOR_LIGHT_GREY).append("\tList Order Num\tGame Name\tWhite Player\tBlack Player\r\n").append(SET_TEXT_COLOR_WHITE);
            int index = 1;
            for (GameOverview game : gamesList) {
                output.append("\t").append(index++).append("     \t");
                output.append(game.toString(false)).append("\r\n");
            }
            return output.toString();
        }
        return "No Games on the server! Use " + SET_TEXT_COLOR_BLUE + "Create Game" + SET_TEXT_COLOR_WHITE + " to create one!";
    }


    public boolean testServer() throws IOException, URISyntaxException, ErrorResponseException {
        return !makeRequest("", "GET", null, null).isEmpty();
    }
}
