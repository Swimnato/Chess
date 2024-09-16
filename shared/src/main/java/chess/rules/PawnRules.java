package chess.rules;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class PawnRules extends Rules {
    private static final int[][] moveSet = {};
    private static final boolean repeatable = false;

    public PawnRules() {
        super(repeatable, moveSet);
    }

    @Override
    public Collection<ChessMove> getMoves(ChessBoard _board, ChessPosition _StartingPosition) {
        var myColor = _board.getPiece(_StartingPosition).getTeamColor();
        int direction = myColor == ChessGame.TeamColor.WHITE ? 1 : -1;
        HashSet<ChessMove> _output = new HashSet<>();

        ChessPiece.PieceType promotionPiece = _StartingPosition.getRow() + direction == (myColor == ChessGame.TeamColor.WHITE ? 8 : 1) ? ChessPiece.PieceType.QUEEN : null;

        if (_StartingPosition.getRow() == (myColor == ChessGame.TeamColor.WHITE ? 2 : 7)) {
            _output.add(new ChessMove(_StartingPosition, new ChessPosition(_StartingPosition.getRow() + direction * 2, _StartingPosition.getColumn()), promotionPiece));
        }
        ChessPosition newPos = new ChessPosition(_StartingPosition.getRow() + direction, _StartingPosition.getColumn());
        if (newPos.isValid() && _board.getPiece(newPos) == null) {
            _output.add(new ChessMove(_StartingPosition, newPos, promotionPiece));
        }
        for (int i = -1; i < 2; i += 2) {
            newPos = new ChessPosition(_StartingPosition.getRow() + direction, _StartingPosition.getColumn());
            if (newPos.isValid() && _board.getPiece(newPos) != null) {
                _output.add(new ChessMove(_StartingPosition, newPos, promotionPiece));
            }
        }

        return _output;
    }
}