import chess.*;

import static chess.ui.EscapeSequences.*;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        var board = new ChessBoard();
        var game = new ChessGame();
        System.out.println(ERASE_SCREEN + SET_TEXT_BOLD + SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "♕ 240 Chess Client: ");
        System.out.println(piece);
        System.out.println(board);
        board.resetBoard();
        System.out.println(board);
        System.out.println(game);
    }
}