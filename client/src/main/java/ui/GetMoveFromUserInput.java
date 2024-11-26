package ui;

import commandparser.CommandParser;

public class GetMoveFromUserInput {
    private String startPosStr;
    private String endPosStr;
    private String promotionPieceStr = null;

    public GetMoveFromUserInput(CommandParser parser) {
        if (parser.numOfParameters() == 4) { // a7 a9 knight
            startPosStr = parser.getParameter(1);
            endPosStr = parser.getParameter(2);
            promotionPieceStr = parser.getParameter(3);
        } else if (parser.numOfParameters() == 3) {
            if (parser.getParameter(2).length() != 2) { // a7a9 knight
                startPosStr = parser.getParameter(1).substring(0, 2);
                endPosStr = parser.getParameter(1).substring(2);
                promotionPieceStr = parser.getParameter(2);
            } else { // a2 a4
                startPosStr = parser.getParameter(1);
                endPosStr = parser.getParameter(2);
            }
        } else { // a2a4
            startPosStr = parser.getParameter(1).substring(0, 2);
            endPosStr = parser.getParameter(1).substring(2);
        }
    }

    public String getEndPosStr() {
        return endPosStr;
    }

    public String getPromotionPieceStr() {
        return promotionPieceStr;
    }

    public String getStartPosStr() {
        return startPosStr;
    }
}