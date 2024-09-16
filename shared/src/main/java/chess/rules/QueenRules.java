package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class QueenRules extends Rules {
    private static final int[][] moveSet = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final boolean repeatable = true;

    public QueenRules() {
        super(repeatable, moveSet);
    }

    @Override
    public Collection<ChessMove> getMoves(ChessBoard _board, ChessPosition _StartingPosition) {
        return super.getMoves(_board, _StartingPosition);
    }
}
