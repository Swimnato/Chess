package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class Rules {
    private boolean repeatable;
    private int[][] rules;

    public Rules(boolean _repeatable, int[][] _rules) {
        repeatable = _repeatable;
        rules = _rules.clone();
    }

    public Collection<ChessMove> getMoves(ChessBoard _board, ChessPosition _StartingPosition) {
        HashSet<ChessMove> _output = new HashSet<>();
        ChessPiece thisPiece = _board.getPiece(_StartingPosition);
        for (int[] rule : rules) {
            int x = _StartingPosition.getRow();
            int y = _StartingPosition.getColumn();
            x += rule[0];
            y += rule[1];
            while (x < 9 && x > 0 && y < 9 && y > 0) {
                ChessPosition newPos = new ChessPosition(x, y);
                ChessPiece pieceAtPos = _board.getPiece(newPos);
                if (pieceAtPos == null || pieceAtPos.getTeamColor() != thisPiece.getTeamColor())
                    _output.add(new ChessMove(_StartingPosition, newPos, null));
                if (!repeatable || pieceAtPos != null) break;
                x += rule[0];
                y += rule[1];
            }
        }
        return _output;
    }

}
