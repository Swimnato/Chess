package client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import CommandParser.CommandParser;

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
}
