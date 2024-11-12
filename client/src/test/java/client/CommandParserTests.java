package client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import commandparser.CommandParser;

public class CommandParserTests {
    @Test
    @DisplayName("Parse Single Command")
    public void parseSingleCommand() {
        CommandParser parser = new CommandParser("test");
        Assertions.assertEquals("test", parser.getCommand(), "Couldn't get regular command!");
        parser = new CommandParser("        test       ");
        Assertions.assertEquals("test", parser.getCommand(), "Couldn't get command with whitespace!");
        parser = new CommandParser("");
        Assertions.assertEquals("", parser.getCommand(), "Returned a Command!");
    }

    @Test
    @DisplayName("Parse Command With parameters")
    public void parseParameters() {
        CommandParser parser = new CommandParser("test bannanas        applePie          \t\t\t crackers");
        Assertions.assertEquals("test", parser.getCommand(), "Couldn't get regular command!");
        Assertions.assertEquals(3, parser.numOfParameters(), "Couldn't get correct number of parameters!");
        Assertions.assertEquals("bannanas", parser.getParameter(0), "Couldn't get parameter!");
        Assertions.assertEquals("applePie", parser.getParameter(1), "Couldn't get parameter!");
        Assertions.assertEquals("crackers", parser.getParameter(2), "Couldn't get parameter!");
    }
}
