package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class KingRules extends Rules {
    private static final int[][] moveSet = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final boolean repeatable = false;

    public KingRules() {
        super(repeatable, moveSet);
    }

    @Override
    public Collection<ChessMove> getMoves(ChessBoard _board, ChessPosition _StartingPosition) {
        HashSet<ChessPosition> _allPossibleMovesForOtherPieces = new HashSet<ChessPosition>();
        for (int row = 1; row < 9; row++) { // Check for all other pieces moves;
            for (int col = 1; col < 9; col++) {
                var curPiece = _board.getPiece(new ChessPosition(row, col));
                if (curPiece != null && curPiece.getPieceType() != ChessPiece.PieceType.PAWN && curPiece.getTeamColor() != _board.getPiece(_StartingPosition).getTeamColor()) {
                    var enemyMoves = curPiece.pieceMoves(_board, new ChessPosition(row, col));
                    for (var move : enemyMoves) {
                        _allPossibleMovesForOtherPieces.add(move.getEndPosition());
                        System.out.println("EnemyMove: " + move);
                    }
                }
            }
        }
        ArrayList<ChessMove> myMoves = new ArrayList<ChessMove>(super.getMoves(_board, _StartingPosition)); // Get default moves;
        for (int i = 0; i < myMoves.size(); i++) { // delete moves;
            if (_allPossibleMovesForOtherPieces.contains(myMoves.get(i).getEndPosition())) {
                System.out.println("Deleted: " + myMoves.get(i));
                myMoves.remove(i);
            }
        }
        for (int i = 0; i < myMoves.size(); i++) { // Check for pawns
            for (int j = -1; j < 2; j += 2) {
                ChessPosition positionToCheck = new ChessPosition(myMoves.get(i).getEndPosition());
                var piece = _board.getPiece(new ChessPosition(positionToCheck.getRow() + j, positionToCheck.getColumn() + j));
                if (piece != null && piece.getTeamColor() != _board.getPiece(_StartingPosition).getTeamColor() && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                    myMoves.remove(i);
                    break;
                }
            }
        }

        return myMoves;
    }
}