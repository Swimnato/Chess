package serverfacade;

import chess.ChessGame;
import chess.datastructures.*;
import com.google.gson.Gson;
import commandparser.*;

import javax.websocket.MessageHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Map;

import static chess.ui.EscapeSequences.*;

public class ServerFacade {
    private String ip;
    private int port;
    private String linkAndPort;
    private int authToken;
    private GameOverview[] gamesList;
    private static final String BAD_REQUEST = SET_TEXT_COLOR_RED + "Bad Request!";
    private static final String BAD_SESSION = SET_TEXT_COLOR_RED + "Bad Session!";
    private WebSocketFacade webSocketFacade;

    public ServerFacade(int port, String ip, MessageHandler.Whole handler) throws URISyntaxException {
        this.ip = ip;
        this.port = port;
        linkAndPort = "http://" + ip + ':' + port;
        authToken = 0;
        gamesList = null;
        webSocketFacade = new WebSocketFacade(linkAndPort, handler);
    }

    public ServerFacade(MessageHandler.Whole handler) throws URISyntaxException {
        ip = "127.0.0.1";
        port = 8080;
        linkAndPort = "http://" + ip + ':' + port;
        authToken = 0;
        gamesList = null;
        webSocketFacade = new WebSocketFacade(linkAndPort, handler);
    }

    public void clearServer() throws InvalidSyntaxException, ErrorResponseException {
        try {
            makeRequest("/db", "DELETE", null, null);
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        authToken = 0;
        gamesList = null;
    }

    private String makeRequest(String path, String type, String body,
                               Map<String, String> extraHeaders)
            throws IOException, URISyntaxException, ErrorResponseException {
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
            if (conn.getResponseCode() == 500) {
                throw new ErrorResponseException("Internal Server Error!");
            }
            return "" + conn.getResponseCode();
        }
        var inputStream = conn.getInputStream();
        StringBuilder output = new StringBuilder();
        while (inputStream.available() > 0) {
            output.append((char) inputStream.read());
        }
        return output.toString();
    }

    public String register(String username, String password, String email) throws InvalidSyntaxException, ErrorResponseException {
        RegisterInfo info = new RegisterInfo(username, password, email);
        String output;
        try {
            output = makeRequest("/user", "POST", new Gson().toJson(info), null);
            if (output.equals("403")) {
                return SET_TEXT_COLOR_RED + "Username Already Taken!";
            } else if (output.equals("400")) {
                return BAD_REQUEST;
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
                return SET_TEXT_COLOR_RED + "Bad Username/Password!";
            } else if (output.equals("400")) {
                return BAD_REQUEST;
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
                return BAD_SESSION;
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
                return BAD_SESSION;
            } else if (response.equals("400")) {
                return BAD_REQUEST;
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
                return BAD_SESSION;
            } else if (output.equals("400")) {
                return BAD_REQUEST;
            }
            GameID gameID = new Gson().fromJson(output, GameID.class);
            if (gameID.getGameID() <= 0) {
                return SET_TEXT_COLOR_RED + "Error Creating Game!";
            }
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        return "Created Game Successfully!";
    }

    public String joinGame(String desiredGame, String desiredColor) throws InvalidSyntaxException, ErrorResponseException {
        int indexOnList = getGameIndex(desiredGame);
        return joinGame(indexOnList, desiredColor);
    }


    public String joinGame(int desiredGame, String desiredColor) throws InvalidSyntaxException, ErrorResponseException {
        if (gamesList == null) {
            return "Please run " + SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_WHITE + " to show available games first";
        }
        if (desiredGame > gamesList.length || desiredGame <= 0) {
            throw new InvalidSyntaxException(SET_TEXT_COLOR_RED + "Invalid Game ID! Use " + SET_TEXT_COLOR_BLUE +
                    "List Games" + SET_TEXT_COLOR_RED + " to show available games with their IDs", true);
        }
        try {
            return webSocketFacade.joinGame(desiredColor, gamesList[desiredGame - 1].getGameID(), authToken);
        } catch (IOException e) {
            throw new InvalidSyntaxException("Play Game");
        }
    }

    private int getGameIndex(String desiredGame) {
        int indexOnList = 1;
        for (var item : gamesList) {
            if (item.getGameName().equals(desiredGame)) {
                break;
            }
            indexOnList++;
        }
        return indexOnList;
    }

    public String getChessGameFromServer(String desiredGame) throws InvalidSyntaxException, ErrorResponseException {
        int indexOnList = getGameIndex(desiredGame);
        return getChessGameFromServer(indexOnList);
    }

    public String getChessGameFromServer(int desiredGame) throws InvalidSyntaxException, ErrorResponseException {
        GameData game;
        if (desiredGame > gamesList.length || desiredGame <= 0) {
            throw new InvalidSyntaxException(SET_TEXT_COLOR_RED + "Invalid Game #! Use" + SET_TEXT_COLOR_BLUE +
                    "List Games" + SET_TEXT_COLOR_RED + " to show available games with their #s", true);
        }
        String response;
        try {
            response = makeRequest("/chessGame", "GET", null, Map.of("gameID",
                    Integer.toString(gamesList[desiredGame - 1].getGameID())));
            if (response.equals("400")) {
                return BAD_REQUEST;
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
            output.append(SET_TEXT_COLOR_LIGHT_GREY).append("\t#" + SET_TEXT_COLOR_MAGENTA +
                    "\tGame Name" + SET_TEXT_COLOR_WHITE + "\tWhite Player" +
                    SET_TEXT_COLOR_DARK_GREY + "\tBlack Player\r\n").append(SET_TEXT_COLOR_WHITE);
            int index = 0;
            for (GameOverview game : gamesList) {
                index++;
                output.append(SET_TEXT_COLOR_WHITE).append("\t").append(index);
                output.append(index >= 1000 ? "" : (index >= 100 ? " " : (index >= 10 ? "  " : "   ")));
                output.append(game.toString(false)).append("\r\n");
            }
            return output.toString();
        }
        return "No Games on the server! Use " + SET_TEXT_COLOR_BLUE + "Create Game" + SET_TEXT_COLOR_WHITE + " to create one!";
    }

    public boolean testServer() throws IOException, URISyntaxException, ErrorResponseException {
        return !makeRequest("", "GET", null, null).isEmpty();
    }

    public String redrawChessBoard() {
        return webSocketFacade.redrawChessBoard();
    }
}
