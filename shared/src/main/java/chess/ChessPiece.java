package chess;

import chess.rules.*;

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
            case KNIGHT -> new KnightRules();
            case QUEEN -> new QueenRules();
            default -> null;
        };
        return output.getMoves(board, myPosition);
    }
}
