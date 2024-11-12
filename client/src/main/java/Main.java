import commandparser.CommandParser;
import serverfacade.ServerFacade;

import commandparser.*;
import ui.REPLClient;

import java.io.IOException;
import java.net.URISyntaxException;

import static chess.ui.EscapeSequences.*;

public class Main {


    public static void main(String[] args) {
        REPLClient ui = new REPLClient();
        if (!ui.setupPortAndIP(args)) {
            return;
        }
        ui.runREPL(false);
    }


}