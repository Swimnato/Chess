import chess.*;

import static chess.ChessGame.TeamColor.*;
import static chess.ui.EscapeSequences.*;

public class Main {

    static LoginStatus loggedIn = LoginStatus.LOGGED_OUT;

    public static void main(String[] args) {
        System.out.println(ERASE_SCREEN + SET_TEXT_BOLD + SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "♕ 240 Chess Client - Type 'Help' to get started");

        boolean running = true;

        System.out.print(loggedIn + ">> ");
        while (running) {
            try {
                if (System.in.available() > 0) {
                    char currentCharacter = ' ';
                    StringBuilder command = new StringBuilder();
                    while (currentCharacter != '\n') {
                        currentCharacter = (char) System.in.read();
                        command.append(currentCharacter);
                    }
                    running = runCommand(command.toString());
                    if (running) {
                        System.out.print(SET_TEXT_COLOR_WHITE + loggedIn + ">> ");
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR - UNCAUGHT EXCEPTION: " + e.getMessage());
            }
        }
    }

    private static boolean runCommand(String input) {
        if (input.toLowerCase().contains("help")) {
            if (removeWhitespace(input).length() == 4) {
                if (loggedIn == LoginStatus.LOGGED_OUT) {
                    System.out.println("List Of Available Commands:");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tRegister " + SET_TEXT_COLOR_GREEN + "<username> <password> <email>" + SET_TEXT_COLOR_LIGHT_GREY + " - To create an account on the server");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tLogin " + SET_TEXT_COLOR_GREEN + "<username> <password>" + SET_TEXT_COLOR_LIGHT_GREY + " - To play chess");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tQuit " + SET_TEXT_COLOR_LIGHT_GREY + " - To close the client");
                    System.out.println(SET_TEXT_COLOR_BLUE + "\tHelp " + SET_TEXT_COLOR_LIGHT_GREY + " - Display available commands");
                }
            } else {
                printHelpForCommand(input);
            }
        } else if (input.toLowerCase().contains("quit")) {
            System.out.println("Goodbye! Come back soon! :)");
            return false;
        } else if (input.toLowerCase().contains("register")) {

        } else if (input.toLowerCase().contains("login")) {

        } else {

        }
        return true;
    }

    private static void printHelpForCommand(String input) {
        input = input.substring(4).toLowerCase();
        if (loggedIn == LoginStatus.LOGGED_OUT && input.contains("register")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Register" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to create an account on the created server.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tRegister " + SET_TEXT_COLOR_GREEN + "<username> <password> <email>");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<username> " + SET_TEXT_COLOR_LIGHT_GREY + "This is your chosen alias on the server, or what you wish to be called.");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<password> " + SET_TEXT_COLOR_LIGHT_GREY + "A password, to keep your account safe, something you will remember.");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<email> " + SET_TEXT_COLOR_LIGHT_GREY + "How you can be reached by the server operator.");
        } else if (loggedIn == LoginStatus.LOGGED_OUT && input.contains("login")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Login" + SET_TEXT_COLOR_LIGHT_GREY + ": This is a command to login into a server. If you need to create an account, use " + SET_TEXT_COLOR_BLUE + "\tRegister ");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tLogin " + SET_TEXT_COLOR_GREEN + "<username> <password>");
            System.out.println("Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<username> " + SET_TEXT_COLOR_LIGHT_GREY + "Your Username on the server.");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<password> " + SET_TEXT_COLOR_LIGHT_GREY + "Your Password.");
        } else if (input.contains("quit")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Quit" + SET_TEXT_COLOR_LIGHT_GREY + " Stops the client.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tQuit");
            System.out.println(SET_TEXT_COLOR_WHITE + "Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\tNone");
        } else if (input.contains("help")) {
            System.out.println(SET_TEXT_COLOR_BLUE + "Help" + SET_TEXT_COLOR_LIGHT_GREY + " Shows available commands, alternativly can be used to learn more about a given command.");
            System.out.println("Syntax:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tHelp");
            System.out.println(SET_TEXT_COLOR_WHITE + "Parameters:");
            System.out.println(SET_TEXT_COLOR_GREEN + "\t<command>" + SET_TEXT_COLOR_LIGHT_GREY + "(Optional) - The desired command that will be explained. If not given, the system will display all available commands.");
        } else {
            System.out.println(SET_TEXT_COLOR_RED + "Invalid Syntax of Help! use \"Help Help\" to see proper syntax.");
        }
    }

    private static String removeWhitespace(String input) {
        String output = input.replace(" ", "");
        output = output.replace("\r", "");
        output = output.replace("\n", "");
        output = output.replace(" ", "");
        output = output.replace(" ", "");

        return output;
    }

    enum LoginStatus {
        LOGGED_IN,
        LOGGED_OUT
    }
}