package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class Rules {
    private final int[][] moveset;
    private final boolean repeats;

    public Rules(int[][] moves, boolean repeats) {
        moveset = moves.clone();
        this.repeats = repeats;
    }

    public Collection<ChessMove> generateMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> output = new HashSet<ChessMove>();
        //make a move for each position in the moveset
        for (var move : moveset) {
            int row = myPosition.getRow() + move[0];
            int col = myPosition.getColumn() + move[1];
            ChessPiece myself = board.getPiece(myPosition);
            while (true) { // repeat the move until the edge
                ChessPosition newPos = new ChessPosition(row, col);
                if (newPos.isValid(board)) {
                    ChessPiece pieceAtPos = board.getPiece(newPos);
                    if (pieceAtPos == null) { // empty space
                        output.add(new ChessMove(myPosition, newPos, null));
                        row += move[0];
                        col += move[1];
                    } else if (pieceAtPos.getTeamColor() != myself.getTeamColor()) { // enemy at position, we can capture but cannot move past
                        output.add(new ChessMove(myPosition, newPos, null));
                        break;
                    } else { // is same color of piece, we cannot move past it
                        break;
                    }
                } else { // is not a valid position
                    break;
                }
                if (!repeats) { // if we aren't supposed to repeat this move break out of loop
                    break;
                }
            }
        }
        return output;
    }
}
