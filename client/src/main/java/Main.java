import CommandParser.CommandParser;
import chess.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static chess.ChessGame.TeamColor.*;
import static chess.ui.EscapeSequences.*;

public class Main {

    static LoginStatus loggedIn = LoginStatus.LOGGED_OUT;
    static ServerFacade facade;

    public static void main(String[] args) {

        if (!setupIPAndPortFromUserInput(args)) {
            return;
        }


        System.out.println(ERASE_SCREEN + SET_TEXT_BOLD + SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "Connected to server Successfully!\r\n\r\n♕ 240 Chess Client - Type 'Help' to get started");

        boolean running = true;

        printPrompt();
        while (running) {
            try {
                if (System.in.available() > 0) {
                    char currentCharacter = ' ';
                    StringBuilder command = new StringBuilder();
                    while (currentCharacter != '\n') {
                        currentCharacter = (char) System.in.read();
                        command.append(currentCharacter);
                    }

                    try {
                        running = runCommand(command.toString());
                    } catch (InvalidSyntaxException e) {
                        System.out.println(e.isShowRawMessage() ? e.getMessage() :
                                SET_TEXT_COLOR_RED + "Invalid Syntax of " + e.getMessage() +
                                        "! Use \"Help " + e.getMessage() + "\" to see proper syntax.");
                    } catch (ErrorResponseException e) {
                        System.out.println(SET_TEXT_COLOR_RED + "Recieved Error Code From the Server: " + e.getMessage());
                    }
                    if (running) {
                        printPrompt();
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR - UNCAUGHT EXCEPTION: " + e.getMessage());
                printPrompt();
            }
        }
    }

    private static void printPrompt() {
        System.out.print(SET_TEXT_COLOR_WHITE + '[' + loggedIn + ']' + ">> ");
    }

    private static boolean runCommand(String input) throws InvalidSyntaxException, ErrorResponseException {
        CommandParser parser = new CommandParser(input);
        if (parser.isCommand("Help")) {
            if (parser.numOfParameters() == 0) {
                if (loggedIn == LoginStatus.LOGGED_OUT) {
                    System.out.println("List Of Available Commands:");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tRegister " + SET_TEXT_COLOR_GREEN + "<username> <password> <email>" + SET_TEXT_COLOR_LIGHT_GREY + " - To create an account on the server");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tLogin " + SET_TEXT_COLOR_GREEN + "<username> <password>" + SET_TEXT_COLOR_LIGHT_GREY + " - To play chess");
                } else {
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tCreate Game " + SET_TEXT_COLOR_GREEN + "<name>" + SET_TEXT_COLOR_LIGHT_GREY + " - Create a game on the server");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tList Games " + SET_TEXT_COLOR_LIGHT_GREY + " - List the games on the server");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tPlay Game " + SET_TEXT_COLOR_GREEN + "<id> <color>" + SET_TEXT_COLOR_LIGHT_GREY + " - List the games on the server");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tObserve Game " + SET_TEXT_COLOR_GREEN + "<id>" + SET_TEXT_COLOR_LIGHT_GREY + " - List the games on the server");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tlogout " + SET_TEXT_COLOR_LIGHT_GREY + " - To logout of the server");
                }
                System.out.println(SET_TEXT_COLOR_BLUE + "\tQuit " + SET_TEXT_COLOR_LIGHT_GREY + " - To close the client");
                System.out.println(SET_TEXT_COLOR_BLUE + "\tHelp " + SET_TEXT_COLOR_LIGHT_GREY + " - Display available commands");
            } else {
                printHelpForCommand(parser);
            }
        } else if (parser.isCommand("Quit")) {
            if (parser.numOfParameters() == 0) {
                if (loggedIn == LoginStatus.LOGGED_IN) {
                    String response = facade.logout();
                    System.out.println(response);
                    if (!response.isEmpty()) {
                        loggedIn = LoginStatus.LOGGED_OUT;
                    }
                }
                System.out.println("Goodbye! Come back soon! :)");
                return false;
            } else {
                throw new InvalidSyntaxException("Quit");
            }
        } else if (parser.isCommand("Register")) {
            if (parser.numOfParameters() == 3 && loggedIn == LoginStatus.LOGGED_OUT) {
                String response = facade.register(parser.getParameter(0), parser.getParameter(1), parser.getParameter(2));
                System.out.println(response);
                if (response.equals("Registered Successfully!")) {
                    loggedIn = LoginStatus.LOGGED_IN;
                }
            } else {
                throw new InvalidSyntaxException("Register");
            }
        } else if (parser.isCommand("Login")) {
            if (parser.numOfParameters() == 2 && loggedIn == LoginStatus.LOGGED_OUT) {
                String response = facade.login(parser.getParameter(0), parser.getParameter(1));
                System.out.println(response);
                if (response.equals("Logged In Successfully!")) {
                    loggedIn = LoginStatus.LOGGED_IN;
                }
            } else {
                throw new InvalidSyntaxException("Login");
            }
        } else if (parser.isCommand("Logout")) {
            if (parser.numOfParameters() == 0 && loggedIn == LoginStatus.LOGGED_IN) {
                String response = facade.logout();
                System.out.println(response);
                if (!response.equals("Logged Out Successfully!")) {
                    loggedIn = LoginStatus.LOGGED_OUT;
                }
            } else {
                throw new InvalidSyntaxException("Logout");
            }
        } else if (parser.isCommand("List") && parser.isParameterEqual(0, "Games")) {
            if (parser.numOfParameters() == 1 && loggedIn == LoginStatus.LOGGED_IN) {
                String response = facade.listGames();
                System.out.println(response);
            } else {
                throw new InvalidSyntaxException("List Games");
            }
        } else if (parser.isCommand("Create") && parser.isParameterEqual(0, "Game")) {
            if (parser.numOfParameters() == 2 && loggedIn == LoginStatus.LOGGED_IN) {
                String response = facade.createGame(parser.getParameter(1));
                System.out.println(response);
            } else {
                throw new InvalidSyntaxException("Create Game");
            }
        } else if (parser.isCommand("Play") && parser.isParameterEqual(0, "Game")) {
            if (parser.numOfParameters() == 3 && loggedIn == LoginStatus.LOGGED_IN) {
                String response = facade.joinGame(Integer.parseInt(parser.getParameter(1)), parser.getParameter(2));
                System.out.println(response);
            } else {
                throw new InvalidSyntaxException("Play Game");
            }
        } else if (parser.isCommand("Observe") && parser.isParameterEqual(0, "Game")) {
            if (parser.numOfParameters() == 2 && loggedIn == LoginStatus.LOGGED_IN) {
                String response = facade.getChessGameFromServer(Integer.parseInt(parser.getParameter(1)));
                System.out.println("Observing game " + parser.getParameter(1) + ",\r\n\r\n" + response);
            } else {
                throw new InvalidSyntaxException("Observe Game");
            }
        } else {
            System.out.println(SET_TEXT_COLOR_RED + "Unrecognized command! Use \"Help\" to find a list of available commands!");
        }

        return true;
    }

    private static void printHelpForCommand(CommandParser input) throws InvalidSyntaxException {
        if (input.numOfParameters() > 2) {
            throw new InvalidSyntaxException("Help");
        }
        if (input.isParameterEqual(0, "Register")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Register" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to create an account on the created server. This can only be used when logged out.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tRegister " + SET_TEXT_COLOR_GREEN + "<username> <password> <email>");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<username> " + SET_TEXT_COLOR_LIGHT_GREY + "This is your chosen alias on the server, or what you wish to be called.");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<password> " + SET_TEXT_COLOR_LIGHT_GREY + "A password, to keep your account safe, something you will remember.");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<email> " + SET_TEXT_COLOR_LIGHT_GREY + "How you can be reached by the server operator.");
        } else if (input.isParameterEqual(0, "Login")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Login" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to login into a server. This can only be used when logged out. If you need to create an account, use " + SET_TEXT_COLOR_BLUE + "\tRegister ");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tLogin " + SET_TEXT_COLOR_GREEN + "<username> <password>");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<username> " + SET_TEXT_COLOR_LIGHT_GREY + "Your Username on the server.");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<password> " + SET_TEXT_COLOR_LIGHT_GREY + "Your Password.");
        } else if (input.isParameterEqual(0, "Logout")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Logout" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to logout of a server. Must be logged in to use.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tLogout");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\tNone");
        } else if (input.isParameterEqual(0, "Create") && input.isParameterEqual(1, "Game")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Create Game" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to create a game on the server.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tCreate Game " + SET_TEXT_COLOR_GREEN + "<name>");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<name> " + SET_TEXT_COLOR_LIGHT_GREY + "The name of the game.");
        } else if (input.isParameterEqual(0, "Play") && input.isParameterEqual(1, "Game")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Play Game" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to join a game on the server.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tPlay Game " + SET_TEXT_COLOR_GREEN + "<id> <color>");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<id> " + SET_TEXT_COLOR_LIGHT_GREY + "The id or number it appeared using the " + SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_LIGHT_GREY + " Command");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<color> " + SET_TEXT_COLOR_LIGHT_GREY + "The desired chess color [" + SET_TEXT_COLOR_WHITE + "WHITE" + SET_TEXT_COLOR_LIGHT_GREY + "/" + SET_TEXT_COLOR_DARK_GREY + "BLACK" + SET_TEXT_COLOR_LIGHT_GREY + "].");
        } else if (input.isParameterEqual(0, "Observe") && input.isParameterEqual(1, "Game")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Observe Game" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to observe a game on the server.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tPlay Game " + SET_TEXT_COLOR_GREEN + "<id>");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<id> " + SET_TEXT_COLOR_LIGHT_GREY + "The id or number it appeared using the " + SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_LIGHT_GREY + " Command");
        } else if (input.isParameterEqual(0, "List") && input.isParameterEqual(1, "Games")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "List Games" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to show all games on the server.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tList Games");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\tNone");
        } else if (input.isParameterEqual(0, "Quit")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Quit" + SET_TEXT_COLOR_LIGHT_GREY + " Stops the client.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tQuit");
            System.out.println(SET_TEXT_COLOR_WHITE + "Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\tNone");
        } else if (input.isParameterEqual(0, "Help")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Help" + SET_TEXT_COLOR_LIGHT_GREY + " Shows available commands, alternativly can be used to learn more about a given command.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tHelp");
            System.out.println(SET_TEXT_COLOR_WHITE + "Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<command> " + SET_TEXT_COLOR_LIGHT_GREY + "(Optional) - The desired command that will be explained. If not given, the system will display all available commands.");
        } else {
            throw new InvalidSyntaxException("Help");
        }
    }

    private static boolean setupIPAndPortFromUserInput(String[] args) {
        if (args.length == 2) {
            int port = Integer.parseInt(args[0]);
            String ip = args[1];
            facade = new ServerFacade(port, ip);

            boolean serverAvailable;
            try {
                serverAvailable = facade.testServer();
            } catch (URISyntaxException e) {
                System.out.println(SET_TEXT_COLOR_RED + "Invalid arguments! URL was malformed! Correct Syntax: " + SET_TEXT_COLOR_BLUE + "<ChessClient.exe/jar> " + SET_TEXT_COLOR_GREEN + "<port> <ip>" +
                        SET_TEXT_COLOR_RED + " Port and IP are optional, though the loopback IP is the default value, and port 8080 will be used. You must include both or neither");
                return false;
            } catch (IOException e) {
                System.out.println(SET_TEXT_COLOR_RED + e.getMessage() + " Please try another Port/IP");
                return false;
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + "Unknown Exception! " + e.getMessage());
                return false;
            }

            if (!serverAvailable) {
                System.out.println(SET_TEXT_COLOR_RED + "Server Unreachable/Incompatable! Check Port and IP, it could also be that the server is down.");
                return false;
            }

        } else if (args.length == 0) {
            boolean inputsNotValid = true;
            while (inputsNotValid) {
                try {
                    System.out.print(SET_TEXT_COLOR_WHITE + "Enter Server Port and IP, or use [enter] to accept default values of port: 8080 and IP: 127.0.0.1\r\nServer port: ");
                    while (System.in.available() == 0) {
                    }
                    char currentCharacter = (char) System.in.read();
                    StringBuilder input = new StringBuilder();
                    while (currentCharacter != '\n') {
                        input.append(currentCharacter);
                        currentCharacter = (char) System.in.read();
                    }
                    if (input.toString().equalsIgnoreCase("Quit")) {
                        return false;
                    }
                    if (input.toString().isEmpty()) {
                        facade = new ServerFacade();
                        inputsNotValid = !facade.testServer();
                        continue;
                    }
                    int port = Integer.parseInt(input.toString());

                    System.out.print("Server IP: ");

                    currentCharacter = (char) System.in.read();
                    input = new StringBuilder();
                    while (currentCharacter != '\n') {
                        input.append(currentCharacter);
                        currentCharacter = (char) System.in.read();
                    }
                    if (input.toString().equalsIgnoreCase("Quit")) {
                        return false;
                    }
                    String ip;
                    if (input.toString().isEmpty()) {
                        ip = "127.0.0.1";
                    } else {
                        ip = input.toString();
                    }

                    facade = new ServerFacade(port, ip);

                    inputsNotValid = !facade.testServer();
                } catch (Exception e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid IP/Port or server is unreachable! Try Again or type quit to quit.");
                }
            }
        } else {
            System.out.println(SET_TEXT_COLOR_RED + "Invalid arguments! Correct Syntax: " + SET_TEXT_COLOR_BLUE + "<ChessClient.exe/jar> " + SET_TEXT_COLOR_GREEN + "<port> <ip>" +
                    SET_TEXT_COLOR_RED + " Port and IP are optional, though you will be prompted for them.");
            return false;
        }
        return true;
    }

    enum LoginStatus {
        LOGGED_IN,
        LOGGED_OUT
    }
}