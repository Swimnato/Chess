package chess;

import chess.rules.*;

import java.util.Collection;

import static chess.ui.EscapeSequences.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor myColor;
    private PieceType myType;
    private boolean firstMove;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean firstMove) {
        myColor = pieceColor;
        myType = type;
        this.firstMove = firstMove;
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        myColor = pieceColor;
        myType = type;
        firstMove = false;
    }

    public ChessPiece(ChessPiece toCopy) {
        myColor = toCopy.getTeamColor();
        myType = toCopy.getPieceType();
        firstMove = toCopy.isFirstMove();
    }

    public ChessPiece(ChessPiece toCopy, boolean firstMove) {
        myColor = toCopy.getTeamColor();
        myType = toCopy.getPieceType();
        this.firstMove = firstMove;
    }

    public boolean isFirstMove() {
        return firstMove;
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
        return myColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return myType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */

    //I know this name goes against java naming conventions, but I am not allowed to change this :(
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Rules ruleset = switch (myType) {
            case BISHOP -> new BishopRules();
            case ROOK -> new RookRules();
            case QUEEN -> new QueenRules();
            case KING -> new KingRules();
            case PAWN -> new PawnRules();
            case KNIGHT -> new KnightRules();
        };
        return ruleset.generateMoves(board, myPosition);
    }

    @Override
    public String toString() {
        var output = "";
        if (myColor == ChessGame.TeamColor.WHITE) {
            output = switch (myType) {
                case PAWN -> BLACK_PAWN;
                case ROOK -> BLACK_ROOK;
                case KING -> BLACK_KING;
                case KNIGHT -> BLACK_KNIGHT;
                case BISHOP -> BLACK_BISHOP;
                case QUEEN -> BLACK_QUEEN;
            };
        } else {
            output = switch (myType) {
                case PAWN -> BLACK_PAWN;
                case ROOK -> BLACK_ROOK;
                case KING -> BLACK_KING;
                case KNIGHT -> BLACK_KNIGHT;
                case BISHOP -> BLACK_BISHOP;
                case QUEEN -> BLACK_QUEEN;
            };
        }
        return output;
    }

    @Override
    public int hashCode() {
        return myType.hashCode() * 11 + myColor.hashCode() * 13 + (firstMove ? 3 : 17);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else if (this.myColor == ((ChessPiece) obj).myColor && this.myType == ((ChessPiece) obj).myType &&
                this.isFirstMove() == ((ChessPiece) obj).isFirstMove()) {
            return true;
        } else {
            return false;
        }
    }
}
