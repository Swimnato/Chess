package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
    }

    public ChessPiece() {
        this.pieceColor = ChessGame.TeamColor.WHITE;
        this.pieceType = PieceType.BLANK;
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
        PAWN,
        BLANK
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

    public char printSelf() {
        if (pieceType == PieceType.BLANK) {
            return (' ');
        } else if (pieceColor == ChessGame.TeamColor.WHITE) {
            if (pieceType == PieceType.KING) {
                return ('K');
            } else if (pieceType == PieceType.QUEEN) {
                return ('Q');
            } else if (pieceType == PieceType.BISHOP) {
                return ('B');
            } else if (pieceType == PieceType.KNIGHT) {
                return ('K');
            } else if (pieceType == PieceType.ROOK) {
                return ('R');
            } else {
                return ('P');
            }
        } else {
            if (pieceType == PieceType.KING) {
                return ('k');
            } else if (pieceType == PieceType.QUEEN) {
                return ('q');
            } else if (pieceType == PieceType.BISHOP) {
                return ('b');
            } else if (pieceType == PieceType.KNIGHT) {
                return ('k');
            } else if (pieceType == PieceType.ROOK) {
                return ('r');
            } else {
                return ('p');
            }
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
        throw new RuntimeException("Not implemented");
    }
}
