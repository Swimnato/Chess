package chess;

import chess.rules.BishopRules;
import chess.rules.KingRules;
import chess.rules.Rules;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
    }

    public ChessPiece(ChessPiece _pieceToCopy) {
        pieceType = _pieceToCopy.getPieceType();
        pieceColor = _pieceToCopy.getTeamColor();
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    @Override
    public String toString() {
        if (pieceColor == ChessGame.TeamColor.WHITE) {
            if (pieceType == PieceType.KING) {
                return ("K");
            } else if (pieceType == PieceType.QUEEN) {
                return ("Q");
            } else if (pieceType == PieceType.BISHOP) {
                return ("B");
            } else if (pieceType == PieceType.KNIGHT) {
                return ("N");
            } else if (pieceType == PieceType.ROOK) {
                return ("R");
            } else {
                return ("P");
            }
        } else {
            if (pieceType == PieceType.KING) {
                return ("k");
            } else if (pieceType == PieceType.QUEEN) {
                return ("q");
            } else if (pieceType == PieceType.BISHOP) {
                return ("b");
            } else if (pieceType == PieceType.KNIGHT) {
                return ("n");
            } else if (pieceType == PieceType.ROOK) {
                return ("r");
            } else {
                return ("p");
            }
        }
    }

    @Override
    public int hashCode() {
        return pieceColor.hashCode() * 17 + pieceType.hashCode() * 13;
    }

    @Override
    public boolean equals(Object _other) {
        if (this == _other) {
            return true;
        } else if (_other == null || _other.getClass() != this.getClass()) {
            return false;
        } else {
            return (this.toString().equals(_other.toString()));
        }
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Rules output = switch (this.pieceType) {
            case BISHOP -> new BishopRules();
            case KING -> new KingRules();
            default -> null;
        };
        return output.getMoves(board, myPosition);
    }

    private Collection<ChessMove> KingMove(ChessBoard board, ChessPosition myPosition) {
        for (byte i = 0; i < 3; i++) {
            for (byte j = 0; j < 3; j++) {
                int x = myPosition.getRow() + (i - 1);
                int y = myPosition.getColumn() + (j - 1);

            }
        }
        return null;
    }

    private Collection<ChessMove> BishopMove(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> _output = new HashSet<>();
        for (byte direction = 0; direction < 4; direction++) { //we will test all 4 directions till the edge
            for (byte i = 1; i < 8; i++) {
                int x = 0;
                int y = 0;
                if (direction < 2) {
                    if (direction == 0) { //First Quadrant
                        y = myPosition.getColumn() + i;
                        x = myPosition.getRow() + i;
                    } else { // Fourth Quadrant
                        y = myPosition.getColumn() + i;
                        x = myPosition.getRow() - i;
                    }
                } else {
                    if (direction == 2) { // Third Quadrant
                        y = myPosition.getColumn() - i;
                        x = myPosition.getRow() - i;
                    } else { // Second Quadrant
                        y = myPosition.getColumn() - i;
                        x = myPosition.getRow() + i;
                    }
                }
                if (x < 9 && x > 0 && y < 9 && y > 0) {
                    ChessPosition newPos = new ChessPosition(x, y);
                    if (board.getPiece(newPos) == null || board.getPiece(newPos).getTeamColor() != this.getTeamColor()) {
                        _output.add(new ChessMove(myPosition, newPos, null));
                        if (board.getPiece(newPos) != null && board.getPiece(newPos).getTeamColor() != this.getTeamColor()) {
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        System.out.println(_output);
        return _output;
    }
}
