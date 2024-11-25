package ui;

import commandparser.CommandParser;
import commandparser.InvalidSyntaxException;

import java.io.PrintStream;

import static chess.ui.EscapeSequences.*;
import static chess.ui.EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;

public class HelpForCommands {
    public static void printAvailableCommands(REPLClient.LoginStatus loggedIn, REPLClient.GameStatus gameStatus, PrintStream outputToUser) {
        if (loggedIn == REPLClient.LoginStatus.LOGGED_OUT) {
            outputToUser.println("List Of Available Commands:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tRegister " + SET_TEXT_COLOR_GREEN +
                    "<username> <password> <email>" + SET_TEXT_COLOR_LIGHT_GREY + " - To create an account on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tLogin " + SET_TEXT_COLOR_GREEN +
                    "<username> <password>" + SET_TEXT_COLOR_LIGHT_GREY + " - To play chess");
        } else if (gameStatus == REPLClient.GameStatus.NOT_PLAYING) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tCreate Game " + SET_TEXT_COLOR_GREEN +
                    "<name>" + SET_TEXT_COLOR_LIGHT_GREY + " - Create a game on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tList Games " + SET_TEXT_COLOR_LIGHT_GREY + " - List the games on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tPlay Game " + SET_TEXT_COLOR_GREEN + "<#> <color>" + SET_TEXT_COLOR_LIGHT_GREY +
                    " - List the games on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tObserve Game " + SET_TEXT_COLOR_GREEN + "<#>" + SET_TEXT_COLOR_LIGHT_GREY +
                    " - List the games on the server");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tlogout " + SET_TEXT_COLOR_LIGHT_GREY + " - To logout of the server");
        } else if (gameStatus == REPLClient.GameStatus.PLAYING) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tHighlight Legal Moves " + SET_TEXT_COLOR_GREEN + "<position>" + SET_TEXT_COLOR_LIGHT_GREY +
                    " Shows available moves for a piece.");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tMake Move " + SET_TEXT_COLOR_GREEN + "<start position> <end position>" + SET_TEXT_COLOR_LIGHT_GREY +
                    " Moves a Piece on the board.");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tLeave " + SET_TEXT_COLOR_LIGHT_GREY + " Leave current game");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tResign " + SET_TEXT_COLOR_LIGHT_GREY + " Resigns the current game to your opponent");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tRedraw Board " + SET_TEXT_COLOR_LIGHT_GREY + " Redraws the chess board");
        } else if (gameStatus == REPLClient.GameStatus.OBSERVING) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tLeave " + SET_TEXT_COLOR_LIGHT_GREY + " Leave current game");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tRedraw Board " + SET_TEXT_COLOR_LIGHT_GREY + " Redraws the chess board");
        }
        outputToUser.println(SET_TEXT_COLOR_BLUE + "\tQuit " + SET_TEXT_COLOR_LIGHT_GREY + " - To close the client");
        outputToUser.println(SET_TEXT_COLOR_BLUE + "\tHelp " + SET_TEXT_COLOR_LIGHT_GREY + " - Display available commands");
    }

    public static void printHelpForCommand(CommandParser input, REPLClient.LoginStatus loggedIn,
                                           REPLClient.GameStatus gameStatus, PrintStream outputToUser) throws InvalidSyntaxException {
        if (input.numOfParameters() == 0) {
            printAvailableCommands(loggedIn, gameStatus, outputToUser);
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
        } else if (!isOtherCommand(input, loggedIn, gameStatus, outputToUser)) {
            throw new InvalidSyntaxException("Help");
        }
    }

    private static boolean isOtherCommand(CommandParser input, REPLClient.LoginStatus loggedIn,
                                          REPLClient.GameStatus gameStatus, PrintStream outputToUser) {
        if (input.isParameterEqual(0, "Leave")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Leave" + SET_TEXT_COLOR_LIGHT_GREY
                    + " Leaves the current game you are playing or observing");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tLeave");
            outputToUser.println(SET_TEXT_COLOR_WHITE + "Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\tNone");
        } else if (input.isParameterEqual(0, "Resign")) {
            outputToUser.println(SET_TEXT_COLOR_BLUE + "Leave" + SET_TEXT_COLOR_LIGHT_GREY
                    + " Resigns the current game to the other player, observers cannot resign for players");
            outputToUser.println("Syntax:");
            outputToUser.println(SET_TEXT_COLOR_BLUE + "\tResign");
            outputToUser.println(SET_TEXT_COLOR_WHITE + "Parameters:");
            outputToUser.println(SET_TEXT_COLOR_GREEN + "\tNone");
        }
        return false;
    }
}
