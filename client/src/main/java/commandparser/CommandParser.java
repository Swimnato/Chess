package commandparser;

import java.util.ArrayList;

public class CommandParser {
    private String rawCommand;
    private String command;
    private ArrayList<String> parameters;

    public CommandParser(String commandTyped) {
        rawCommand = removeFrontWhitespace(commandTyped);
        command = "";
        parameters = new ArrayList<>();
        parseCommand();
    }

    public String getCommand() {
        return command;
    }

    public boolean isCommand(String comparedCommand) {
        return command.equalsIgnoreCase(comparedCommand);
    }

    public int numOfParameters() {
        return parameters.size();
    }

    public String getParameter(int index) {
        return parameters.get(index);
    }

    public boolean isParameterEqual(int parameterNum, String valueToCompare) {
        return parameters.get(parameterNum).equalsIgnoreCase(valueToCompare);
    }

    private void parseCommand() {
        boolean onString = false;
        char currentChar;
        int indexStart = 0;
        if (rawCommand.isEmpty()) {
            return; // nothing to parse
        }
        for (int index = 0; index < rawCommand.length(); index++) {
            currentChar = rawCommand.charAt(index);
            if (!onString && !isWhitespace(currentChar)) {
                indexStart = index;
                onString = true;
            } else if (onString && (isWhitespace(currentChar))) {
                onString = false;
                if (command.isEmpty()) {
                    command = rawCommand.substring(indexStart, index);
                } else {
                    parameters.add(rawCommand.substring(indexStart, index));
                }
            }
        }
        if (onString) {
            if (command.isEmpty()) {
                command = rawCommand.substring(indexStart);
            } else {
                parameters.add(rawCommand.substring(indexStart));
            }
        }
    }

    private boolean isWhitespace(char input) {
        return input == '\r' || input == '\n' || input == ' ' || input == ' ' || input == ' ' || input == '\t';
    }


    private String removeWhitespace(String input) {
        String output = input.replace(" ", "");
        output = output.replace("\r", "");
        output = output.replace("\n", "");
        output = output.replace(" ", "");
        output = output.replace(" ", "");

        return output;
    }

    private String removeFrontWhitespace(String input) {
        int index = 0;
        if (input.isEmpty()) {
            return "";
        }
        char currentChar = input.charAt(index);

        while (isWhitespace(currentChar)) {
            index++;

            if (index >= input.length()) {
                break;
            }

            currentChar = input.charAt(index);
        }

        return input.substring(index);
    }
}
