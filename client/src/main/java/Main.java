import chess.*;

import static chess.ChessGame.TeamColor.*;
import static chess.ui.EscapeSequences.*;

public class Main {
    public static void main(String[] args) {
        System.out.println(ERASE_SCREEN + SET_TEXT_BOLD + SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "♕ 240 Chess Client - Type 'Help' to get started");

        boolean running = true;
        LoginStatus loggedIn = LoginStatus.LOGGED_OUT;

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
                    runCommand(command.toString());
                    System.out.print(SET_TEXT_COLOR_WHITE + loggedIn + ">> ");
                }
            } catch (Exception e) {
                System.err.println("ERROR - UNCAUGHT EXCEPTION: " + e.getMessage());
            }
        }
    }

    private static void runCommand(String input) {
        if (input.substring(0, 4).equalsIgnoreCase("Help")) {
            System.out.println("List Of Available Commands:");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tRegister " + SET_TEXT_COLOR_GREEN + "<username> <password> <email>" + SET_TEXT_COLOR_LIGHT_GREY + " - To create an account on the server");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tLogin " + SET_TEXT_COLOR_GREEN + "<username> <password>" + SET_TEXT_COLOR_LIGHT_GREY + " - To play chess");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tQuit " + SET_TEXT_COLOR_LIGHT_GREY + " - To close the client");
            System.out.println(SET_TEXT_COLOR_BLUE + "\tHelp " + SET_TEXT_COLOR_LIGHT_GREY + " - Display available commands");
        }
    }

    enum LoginStatus {
        LOGGED_IN,
        LOGGED_OUT
    }
}