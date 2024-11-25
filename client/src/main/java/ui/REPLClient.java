package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
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
            printHelpForCommand(parser);
        } else if (parser.isCommand("Quit")) {
            if (parser.numOfParameters() == 0) {
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
        } else if (parser.isCommand("Register")) {
            if (parser.numOfParameters() == 3 && loggedIn == LoginStatus.LOGGED_OUT) {
                String response = facade.register(parser.getParameter(0), parser.getParameter(1), parser.getParameter(2));
                outputToUser.println(response);
                if (response.equals("Registered Successfully!")) {
                    loggedIn = LoginStatus.LOGGED_IN;
                }
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
            } else {
                throw new InvalidSyntaxException("Login");
            }
        } else if (parser.isCommand("Logout")) {
            if (parser.numOfParameters() == 0 && loggedIn == LoginStatus.LOGGED_IN) {
                String response = facade.logout();
                outputToUser.println(response);
                if (response.equals("Logged Out Successfully!")) {
                    loggedIn = LoginStatus.LOGGED_OUT;
                }
            } else {
                throw new InvalidSyntaxException("Logout");
            }
        } else if (parser.isCommand("List") && parser.isParameterEqual(0, "Games")) {
            if (parser.numOfParameters() == 1 && loggedIn == LoginStatus.LOGGED_IN && gameStatus == GameStatus.NOT_PLAYING) {
                String response = facade.listGames();
                outputToUser.println(response);
            } else {
                throw new InvalidSyntaxException("List Games");
            }
        } else if (parser.isCommand("Create") && parser.isParameterEqual(0, "Game")) {
            if (parser.numOfParameters() == 2 && loggedIn == LoginStatus.LOGGED_IN && gameStatus == GameStatus.NOT_PLAYING) {
                String response = facade.createGame(parser.getParameter(1));
                outputToUser.println(response);
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
                gameStatus = GameStatus.PLAYING;
            } else {
                throw new InvalidSyntaxException("Play Game");
            }
        } else if (parser.isCommand("Redraw") && parser.isParameterEqual(0, "Board")) {
            if (parser.numOfParameters() == 1 && loggedIn == LoginStatus.LOGGED_IN) {
                outputToUser.println(drawBoard());
            } else {
                throw new InvalidSyntaxException("Redraw Board");
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
            } else {
                throw new InvalidSyntaxException("Observe Game");
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
            } else {
                throw new InvalidSyntaxException("Highlight Legal Moves");
            }
        } else if (parser.isCommand("Leave")) {
            if ((parser.numOfParameters() == 0) && loggedIn == LoginStatus.LOGGED_IN
                    && gameStatus == GameStatus.PLAYING) {
                outputToUser.println(facade.leaveGame());
                gameStatus = GameStatus.NOT_PLAYING;
            } else {
                throw new InvalidSyntaxException("Leave");
            }
        } else if (parser.isCommand("Resign")) {
            if ((parser.numOfParameters() == 0) && loggedIn == LoginStatus.LOGGED_IN
                    && gameStatus == GameStatus.PLAYING) {
                outputToUser.println(facade.resignGame());
                gameStatus = GameStatus.NOT_PLAYING;
            } else {
                throw new InvalidSyntaxException("Resign");
            }
        } else {
            outputToUser.println(SET_TEXT_COLOR_RED + "Unrecognized command! Use \"Help\" to find a list of available commands!");
        }

        return true;
    }


    private void printAvailableCommands() {
        if (loggedIn == LoginStatus.LOGGED_OUT) {
            outputToUser.println("List Of Available Commands:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tRegister " + SET_TEXT_COLOR_GREEN +
                    "<username> <password> <email>" + SET_TEXT_COLOR_LIGHT_GREY + " - To create an account on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tLogin " + SET_TEXT_COLOR_GREEN +
                    "<username> <password>" + SET_TEXT_COLOR_LIGHT_GREY + " - To play chess");
        } else {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tCreate Game " + SET_TEXT_COLOR_GREEN +
                    "<name>" + SET_TEXT_COLOR_LIGHT_GREY + " - Create a game on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tList Games " + SET_TEXT_COLOR_LIGHT_GREY + " - List the games on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tPlay Game " + SET_TEXT_COLOR_GREEN + "<#> <color>" + SET_TEXT_COLOR_LIGHT_GREY +
                    " - List the games on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tObserve Game " + SET_TEXT_COLOR_GREEN + "<#>" + SET_TEXT_COLOR_LIGHT_GREY +
                    " - List the games on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tlogout " + SET_TEXT_COLOR_LIGHT_GREY + " - To logout of the server");
        }
        outputToUser.println(SET_TEXT_COLOR_BLUE + "\tQuit " + SET_TEXT_COLOR_LIGHT_GREY + " - To close the client");
        outputToUser.println(SET_TEXT_COLOR_BLUE + "\tHelp " + SET_TEXT_COLOR_LIGHT_GREY + " - Display available commands");
    }

    private void printHelpForCommand(CommandParser input) throws InvalidSyntaxException {
        if (input.numOfParameters() == 0) {
            printAvailableCommands();
            return;
        }
        if (input.numOfParameters() > 2) {
            throw new InvalidSyntaxException("Help");
        }
        if (input.isParameterEqual(0, "Register")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Register" + SET_TEXT_COLOR_LIGHT_GREY +
                    ": This is a command to create an account on the created server. This can only be used when logged out.");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tRegister " + SET_TEXT_COLOR_GREEN + "<username> <password> <email>");
            outputToUser.println("Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<username> " + SET_TEXT_COLOR_LIGHT_GREY +
                    "This is your chosen alias on the server, or what you wish to be called.");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<password> " + SET_TEXT_COLOR_LIGHT_GREY +
                    "A password, to keep your account safe, something you will remember.");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<email> " + SET_TEXT_COLOR_LIGHT_GREY + "How you can be reached by the server operator.");
        } else if (input.isParameterEqual(0, "Login")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Login" + SET_TEXT_COLOR_LIGHT_GREY +
                    ": This is a command to login into a server. This can only be used when logged out. " +
                    "If you need to create an account, use " + SET_TEXT_COLOR_BLUE + "\tRegister ");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tLogin " + SET_TEXT_COLOR_GREEN + "<username> <password>");
            outputToUser.println("Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<username> " + SET_TEXT_COLOR_LIGHT_GREY + "Your Username on the server.");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<password> " + SET_TEXT_COLOR_LIGHT_GREY + "Your Password.");
        } else if (input.isParameterEqual(0, "Logout")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Logout" + SET_TEXT_COLOR_LIGHT_GREY +
                    ": This is a command to logout of a server. Must be logged in to use.");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tLogout");
            outputToUser.println("Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\tNone");
        } else if (input.isParameterEqual(0, "Create") && input.isParameterEqual(1, "Game")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Create Game" + SET_TEXT_COLOR_LIGHT_GREY +
                    ": This is a command to create a game on the server.");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tCreate Game " + SET_TEXT_COLOR_GREEN + "<name>");
            outputToUser.println("Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<name> " + SET_TEXT_COLOR_LIGHT_GREY + "The name of the game.");
        } else if (input.isParameterEqual(0, "Play") && input.isParameterEqual(1, "Game")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Play Game" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to join a game on the server.");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tPlay Game " + SET_TEXT_COLOR_GREEN + "<#> <color>");
            outputToUser.println("Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<#> " + SET_TEXT_COLOR_LIGHT_GREY +
                    "The number it appeared using the " + SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_LIGHT_GREY + " Command");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<color> " + SET_TEXT_COLOR_LIGHT_GREY +
                    "The desired chess color [" + SET_TEXT_COLOR_WHITE + "WHITE" + SET_TEXT_COLOR_LIGHT_GREY +
                    "/" + SET_TEXT_COLOR_DARK_GREY + "BLACK" + SET_TEXT_COLOR_LIGHT_GREY + "].");
        } else if (input.isParameterEqual(0, "Observe") &&
                input.isParameterEqual(1, "Game")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Observe Game" + SET_TEXT_COLOR_LIGHT_GREY +
                    ": This is a command to observe a game on the server.");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tPlay Game " + SET_TEXT_COLOR_GREEN + "<#>");
            outputToUser.println("Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<#> " + SET_TEXT_COLOR_LIGHT_GREY +
                    "The number it appeared using the " + SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_LIGHT_GREY + " Command");
        } else if (input.isParameterEqual(0, "List") && input.isParameterEqual(1, "Games")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_LIGHT_GREY +
                    ": This is a command to show all games on the server.");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tList Games");
            outputToUser.println("Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\tNone");
        } else if (input.isParameterEqual(0, "Quit")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Quit" + SET_TEXT_COLOR_LIGHT_GREY + " Stops the client.");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tQuit");
            outputToUser.println(SET_TEXT_COLOR_WHITE + "Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\tNone");
        } else if (input.isParameterEqual(0, "Help")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Help" + SET_TEXT_COLOR_LIGHT_GREY
                    + " Shows available commands, alternativly can be used to learn more " +
                    "about a given command.");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tHelp");
            outputToUser.println(SET_TEXT_COLOR_WHITE + "Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\t<command> " + SET_TEXT_COLOR_LIGHT_GREY +
                    "(Optional) - The desired command that will be explained. " +
                    "If not given, the system will display all available commands.");
        } else {
            throw new InvalidSyntaxException("Help");
        }
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
            return "\r\n" + currentGame.toString(playerColor) + SET_TEXT_COLOR_BLUE + "Game is over, " + currentGame.getWinner() + " Won!\r\n";
        } else {
            return "\r\n" + currentGame.toString(playerColor);
        }
    }

    public void onMessage(String message) {
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
        printPrompt();
    }

    private enum LoginStatus {
        LOGGED_IN,
        LOGGED_OUT
    }

    private enum GameStatus {
        NOT_PLAYING,
        PLAYING,
        OBSERVING
    }
}
