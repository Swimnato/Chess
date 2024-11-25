package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.datastructures.GameData;
import com.google.gson.Gson;
import commandparser.CommandParser;
import commandparser.ErrorResponseException;
import commandparser.InvalidSyntaxException;
import serverfacade.ServerFacade;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.MessageHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Collection;

import static chess.ui.EscapeSequences.*;

public class REPLClient implements MessageHandler.Whole<String> {

    private LoginStatus loggedIn = LoginStatus.LOGGED_OUT;
    private GameStatus gameStatus = GameStatus.NOT_PLAYING;
    private ServerFacade facade;
    private final PrintStream outputToUser;
    private final InputStream inputFromUser;

    private ChessGame currentGame = null;
    private ChessGame.TeamColor playerColor = ChessGame.TeamColor.WHITE;

    public REPLClient() {
        inputFromUser = System.in;
        outputToUser = System.out;
    }

    public boolean setupPortAndIP(String[] args) {
        return setupIPAndPortFromUserInput(args);
    }

    public void runREPL(boolean runSingleTime) {
        outputToUser.println(ERASE_SCREEN + SET_TEXT_BOLD + SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK +
                "Connected to server Successfully!\r\n\r\n♕ 240 Chess Client - Type 'Help' to get started");

        boolean running = true;

        printPrompt();
        do {
            try {
                if (inputFromUser.available() > 0) {
                    char currentCharacter = ' ';
                    StringBuilder command = new StringBuilder();
                    while (currentCharacter != '\n' && currentCharacter != '\r') {
                        currentCharacter = (char) inputFromUser.read();
                        command.append(currentCharacter);
                    }

                    try {
                        running = runCommand(command.toString());
                    } catch (InvalidSyntaxException e) {
                        outputToUser.println(e.isShowRawMessage() ? e.getMessage() :
                                SET_TEXT_COLOR_RED + "Invalid Syntax of " + e.getMessage() +
                                        "! Use \"Help " + e.getMessage() + "\" to see proper syntax.");
                    } catch (ErrorResponseException e) {
                        outputToUser.println(SET_TEXT_COLOR_RED + "Received Error Code From the Server: " + e.getMessage());
                    }
                    if (running) {
                        printPrompt();
                    }
                }
            } catch (Exception e) {
                outputToUser.println(SET_TEXT_COLOR_RED + "ERROR - UNCAUGHT EXCEPTION: " + e.getMessage());
                printPrompt();
            }
        } while (running && !runSingleTime);
    }

    private void printPrompt() {
        if (gameStatus == GameStatus.NOT_PLAYING) {
            outputToUser.print(SET_TEXT_COLOR_WHITE + '[' + loggedIn + ']' + ">> ");
        } else {
            outputToUser.print(SET_TEXT_COLOR_WHITE + '[' + gameStatus + ']' + ">> ");
        }
    }

    private boolean runCommand(String input) throws InvalidSyntaxException, ErrorResponseException {
        CommandParser parser = new CommandParser(input);
        if (parser.isCommand("Help")) {
            HelpForCommands.printHelpForCommand(parser, loggedIn, gameStatus, outputToUser);
        } else if (parser.isCommand("Quit")) {
            if (parser.numOfParameters() == 0) {
                if (gameStatus == GameStatus.PLAYING || gameStatus == GameStatus.OBSERVING) {
                    facade.leaveGame();
                    gameStatus = GameStatus.NOT_PLAYING;
                    outputToUser.println("Left Game");
                }
                if (loggedIn == LoginStatus.LOGGED_IN) {
                    String response = facade.logout();
                    outputToUser.println(response);
                    if (!response.isEmpty()) {
                        loggedIn = LoginStatus.LOGGED_OUT;
                    }
                }
                outputToUser.println("Goodbye! Come back soon! :)");
                return false;
            } else {
                throw new InvalidSyntaxException("Quit");
            }
        } else if (checkAndRunLoggedOutCommand(parser)) {
            return true;
        } else if (checkAndRunLoggedInCommand(parser)) {
            return true;
        } else if (checkAndRunPlayingGameCommand(parser)) {
            return true;
        } else {
            outputToUser.println(SET_TEXT_COLOR_RED + "Unrecognized command! Use \"Help\" to find a list of available commands!");
        }

        return true;
    }

    public boolean checkAndRunLoggedInCommand(CommandParser parser) throws InvalidSyntaxException, ErrorResponseException {
        if (parser.isCommand("Logout")) {
            if (parser.numOfParameters() == 0 && loggedIn == LoginStatus.LOGGED_IN) {
                String response = facade.logout();
                outputToUser.println(response);
                if (response.equals("Logged Out Successfully!")) {
                    loggedIn = LoginStatus.LOGGED_OUT;
                }
                return true;
            } else {
                throw new InvalidSyntaxException("Logout");
            }
        } else if (parser.isCommand("List") && parser.isParameterEqual(0, "Games")) {
            if (parser.numOfParameters() == 1 && loggedIn == LoginStatus.LOGGED_IN && gameStatus == GameStatus.NOT_PLAYING) {
                String response = facade.listGames();
                outputToUser.println(response);
                return true;
            } else {
                throw new InvalidSyntaxException("List Games");
            }
        } else if (parser.isCommand("Create") && parser.isParameterEqual(0, "Game")) {
            if (parser.numOfParameters() == 2 && loggedIn == LoginStatus.LOGGED_IN && gameStatus == GameStatus.NOT_PLAYING) {
                String response = facade.createGame(parser.getParameter(1));
                outputToUser.println(response);
                return true;
            } else {
                throw new InvalidSyntaxException("Create Game");
            }
        } else if (parser.isCommand("Play") && parser.isParameterEqual(0, "Game")) {
            if (parser.numOfParameters() == 3 && loggedIn == LoginStatus.LOGGED_IN && gameStatus == GameStatus.NOT_PLAYING
                    && (parser.isParameterEqual(2, "White") ||
                    parser.isParameterEqual(2, "Black"))) {
                String response;
                try {
                    int gameNum = Integer.parseInt(parser.getParameter(1));
                    response = facade.joinGame(gameNum, parser.getParameter(2).toUpperCase());
                    playerColor = (parser.isParameterEqual(2, "White") ?
                            ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK);
                } catch (NumberFormatException e) {
                    response = facade.joinGame(parser.getParameter(1), parser.getParameter(2).toUpperCase());
                }
                outputToUser.println(response);
                if (response.isEmpty()) {
                    gameStatus = GameStatus.PLAYING;
                }
                return true;
            } else {
                throw new InvalidSyntaxException("Play Game");
            }
        } else if (parser.isCommand("Observe") && parser.isParameterEqual(0, "Game")) {
            if (parser.numOfParameters() == 2 && loggedIn == LoginStatus.LOGGED_IN) {
                String response;
                try {
                    int gameNum = Integer.parseInt(parser.getParameter(1));
                    response = facade.observeGame(gameNum);
                } catch (NumberFormatException e) {
                    response = facade.observeGame(parser.getParameter(1));
                }
                outputToUser.println(response);

                playerColor = ChessGame.TeamColor.WHITE;
                gameStatus = GameStatus.OBSERVING;
                return true;
            } else {
                throw new InvalidSyntaxException("Observe Game");
            }
        }
        return false;
    }

    public boolean checkAndRunLoggedOutCommand(CommandParser parser) throws InvalidSyntaxException, ErrorResponseException {
        if (parser.isCommand("Register")) {
            if (parser.numOfParameters() == 3 && loggedIn == LoginStatus.LOGGED_OUT) {
                String response = facade.register(parser.getParameter(0), parser.getParameter(1), parser.getParameter(2));
                outputToUser.println(response);
                if (response.equals("Registered Successfully!")) {
                    loggedIn = LoginStatus.LOGGED_IN;
                }
                return true;
            } else {
                throw new InvalidSyntaxException("Register");
            }
        } else if (parser.isCommand("Login")) {
            if (parser.numOfParameters() == 2 && loggedIn == LoginStatus.LOGGED_OUT) {
                String response = facade.login(parser.getParameter(0), parser.getParameter(1));
                outputToUser.println(response);
                if (response.equals("Logged In Successfully!")) {
                    loggedIn = LoginStatus.LOGGED_IN;
                }
                return true;
            } else {
                throw new InvalidSyntaxException("Login");
            }
        }
        return false;
    }

    public boolean checkAndRunPlayingGameCommand(CommandParser parser) throws InvalidSyntaxException, ErrorResponseException {
        if (parser.isCommand("Redraw") && parser.isParameterEqual(0, "Board")) {
            if (parser.numOfParameters() == 1 && loggedIn == LoginStatus.LOGGED_IN) {
                outputToUser.println(drawBoard());
                return true;
            } else {
                throw new InvalidSyntaxException("Redraw Board");
            }
        } else if (parser.isCommand("Highlight") && parser.isParameterEqual(0, "Legal")
                && parser.isParameterEqual(1, "Moves")) {
            if ((parser.numOfParameters() == 3 || parser.numOfParameters() == 4) && loggedIn == LoginStatus.LOGGED_IN
                    && gameStatus == GameStatus.PLAYING) {
                if (currentGame.isGameOver()) {
                    outputToUser.println("Game Is Over, No More Moves! (You should play another though ;) )");
                    return true;
                }
                String coords;
                if (parser.numOfParameters() == 3) {
                    coords = parser.getParameter(2).toLowerCase();
                } else {
                    coords = parser.getParameter(2);
                }
                ChessPosition piece;
                if (coords.length() != 2) {
                    throw new InvalidSyntaxException("Highlight Legal Moves");
                }
                try {
                    piece = new ChessPosition(coords);
                } catch (IOException e) {
                    throw new InvalidSyntaxException("Highlight Legal Moves");
                }
                if (!piece.isValid(currentGame.getBoard())) {
                    outputToUser.println("Invalid Chess Coordinates! \r\n They should be structured <col char> <row num> \r\n" +
                            "also use" + SET_TEXT_COLOR_BLUE + " \"Help Highlight Legal Moves\" " + SET_TEXT_COLOR_WHITE + " to get more help");
                } else {
                    outputToUser.println(currentGame.toString(playerColor, piece));
                }
                return true;
            } else {
                throw new InvalidSyntaxException("Highlight Legal Moves");
            }
        } else if (parser.isCommand("Leave")) {
            if ((parser.numOfParameters() == 0) && loggedIn == LoginStatus.LOGGED_IN
                    && (gameStatus == GameStatus.PLAYING || gameStatus == GameStatus.OBSERVING)) {
                outputToUser.println(facade.leaveGame());
                gameStatus = GameStatus.NOT_PLAYING;
                return true;
            } else {
                throw new InvalidSyntaxException("Leave");
            }
        } else if (parser.isCommand("Resign")) {
            if ((parser.numOfParameters() == 0) && loggedIn == LoginStatus.LOGGED_IN
                    && gameStatus == GameStatus.PLAYING) {
                outputToUser.println(facade.resignGame());
                gameStatus = GameStatus.NOT_PLAYING;
                return true;
            } else {
                throw new InvalidSyntaxException("Resign");
            }
        } else if (parser.isCommand("Make") && parser.isParameterEqual(0, "Move")) {
            if (((parser.numOfParameters() == 3 && parser.getParameter(1).length() == 2 && parser.getParameter(2).length() == 2)
                    || (parser.numOfParameters() == 2 && parser.getParameter(1).length() == 4)) && loggedIn == LoginStatus.LOGGED_IN
                    && gameStatus == GameStatus.PLAYING) {
                String startPosStr;
                String endPosStr;
                if (parser.numOfParameters() == 2) {
                    startPosStr = parser.getParameter(1).substring(0, 2);
                    endPosStr = parser.getParameter(1).substring(2);
                } else {
                    startPosStr = parser.getParameter(1);
                    endPosStr = parser.getParameter(2);
                }
                ChessMove move;
                try {
                    ChessPosition startPos = new ChessPosition(startPosStr);
                    ChessPosition endPos = new ChessPosition(endPosStr);
                    move = currentGame.getMoveForStartAndEndPositions(startPos, endPos);
                } catch (IOException e) {
                    throw new InvalidSyntaxException("Make Move");
                }

                if (move == null) {
                    throw new InvalidSyntaxException("Invalid Move! use " + SET_TEXT_COLOR_BLUE + "Highlight Legal Moves" +
                            SET_TEXT_COLOR_WHITE + " To see valid Moves", true);
                }
                try {
                    facade.makeMove(move);
                } catch (Exception e) {
                    throw new InvalidSyntaxException("There Was A Problem Parsing Your Command! " + e.getMessage(), true);
                }
                return true;
            } else {
                throw new InvalidSyntaxException("Make Move");
            }
        }
        return false;
    }

    private boolean setupIPAndPortFromUserInput(String[] args) {
        if (args.length == 2) {
            int port = Integer.parseInt(args[0]);
            String ip = args[1];

            boolean serverAvailable;
            try {
                facade = new ServerFacade(port, ip, this);
                serverAvailable = facade.testServer();
            } catch (URISyntaxException e) {
                outputToUser.println(SET_TEXT_COLOR_RED + "Invalid arguments! URL" +
                        " was malformed! Correct Syntax: " + SET_TEXT_COLOR_BLUE +
                        "<ChessClient.exe/jar> " + SET_TEXT_COLOR_GREEN + "<port> <ip>" +
                        SET_TEXT_COLOR_RED + " Port and IP are optional, though" +
                        " the loopback IP is the default value, and port 8080 will" +
                        " be used. You must include both or neither");
                return false;
            } catch (IOException e) {
                outputToUser.println(SET_TEXT_COLOR_RED + e.getMessage() + " Please try another Port/IP");
                return false;
            } catch (Exception e) {
                outputToUser.println(SET_TEXT_COLOR_RED + "Unknown Exception! " + e.getMessage());
                return false;
            }

            if (!serverAvailable) {
                outputToUser.println(SET_TEXT_COLOR_RED +
                        "Server Unreachable/Incompatable! Check Port and IP, " +
                        "it could also be that the server is down.");
                return false;
            }

        } else if (args.length == 0) {
            boolean inputsNotValid = true;
            while (inputsNotValid) {
                try {
                    outputToUser.print(SET_TEXT_COLOR_WHITE +
                            "Enter Server Port and IP, or use [enter] to accept default " +
                            "values of port: 8080 and IP: 127.0.0.1\r\nServer port: ");
                    while (inputFromUser.available() == 0) {
                    }
                    String input = getLineInputFromUser();
                    if (input.isEmpty()) {
                        facade = new ServerFacade(this);
                        inputsNotValid = !facade.testServer();
                        continue;
                    }
                    int port = Integer.parseInt(input);

                    outputToUser.print("Server IP: ");

                    input = getLineInputFromUser();

                    if (input.equalsIgnoreCase("Quit")) {
                        return false;
                    }
                    String ip;
                    if (input.isEmpty()) {
                        ip = "127.0.0.1";
                    } else {
                        ip = input;
                    }

                    facade = new ServerFacade(port, ip, this);

                    inputsNotValid = !facade.testServer();
                } catch (Exception e) {
                    outputToUser.println(SET_TEXT_COLOR_RED + "Invalid IP/Port or server is unreachable! Try Again or type quit to quit.");
                }
            }
        } else {
            outputToUser.println(SET_TEXT_COLOR_RED + "Invalid arguments! Correct Syntax: "
                    + SET_TEXT_COLOR_BLUE + "<ChessClient.exe/jar> " + SET_TEXT_COLOR_GREEN + "<port> <ip>" +
                    SET_TEXT_COLOR_RED + " Port and IP are optional, though you will be prompted for them.");
            return false;
        }
        return true;
    }

    private String getLineInputFromUser() throws Exception {
        char currentCharacter = (char) inputFromUser.read();
        StringBuilder input = new StringBuilder();
        while (currentCharacter != '\n' && currentCharacter != '\r') {
            input.append(currentCharacter);
            currentCharacter = (char) inputFromUser.read();
        }
        if (currentCharacter == '\r') {
            inputFromUser.read();
        }
        return input.toString();
    }

    private String drawBoard() {
        if (currentGame.isGameOver()) {
            if (currentGame.getWinner() != null) {
                return "\r\n" + currentGame.toString(playerColor) + SET_TEXT_COLOR_BLUE + "Game is over, " + currentGame.getWinner() + " Won!\r\n";
            } else {
                return "\r\n" + currentGame.toString(playerColor) + SET_TEXT_COLOR_BLUE + "Game ended in a stalemate, nobody wins!\r\n";
            }
        } else {
            return "\r\n" + currentGame.toString(playerColor);
        }
    }

    public void onMessage(String message) {
        try {
            ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                currentGame = new Gson().fromJson(serverMessage.getPayload(), ChessGame.class);
            }
            outputToUser.println(
                    switch (serverMessage.getServerMessageType()) {
                        case NOTIFICATION -> serverMessage.getPayload();
                        case ERROR -> SET_TEXT_COLOR_RED + serverMessage.getPayload();
                        case LOAD_GAME -> drawBoard();
                    }
            );
        } catch (Exception e) {
            outputToUser.println(SET_TEXT_COLOR_RED + "ERROR - UNCAUGHT EXCEPTION: " + e.getMessage());
        }
        printPrompt();
    }

    public enum LoginStatus {
        LOGGED_IN,
        LOGGED_OUT
    }

    public enum GameStatus {
        NOT_PLAYING,
        PLAYING,
        OBSERVING
    }
}
