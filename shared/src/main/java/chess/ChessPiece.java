package chess;

import chess.rules.*;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    ChessGame.TeamColor myColor;
    PieceType myType;
    boolean firstMove;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        myColor = pieceColor;
        myType = type;
        firstMove = false;
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean firstMove) {
        myColor = pieceColor;
        myType = type;
        this.firstMove = firstMove;
    }

    public boolean isFirstMove(){
        return firstMove;
    }

    public ChessPiece(ChessPiece _toCopy){
        myColor = _toCopy.getTeamColor();
        myType = _toCopy.getPieceType();
    }

    public ChessPiece(ChessPiece _toCopy, boolean firstMove){
        myColor = _toCopy.getTeamColor();
        myType = _toCopy.getPieceType();
        this.firstMove = firstMove;
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
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Rules ruleset =  switch(myType){
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
        if(myColor == ChessGame.TeamColor.WHITE){
            return switch(myType){
                case PAWN -> "P";
                case ROOK -> "R";
                case KING -> "K";
                case KNIGHT -> "N";
                case BISHOP -> "B";
                case QUEEN -> "Q";
            };
        }
        else{
            return switch(myType){
                case PAWN -> "p";
                case ROOK -> "r";
                case KING -> "k";
                case KNIGHT -> "n";
                case BISHOP -> "b";
                case QUEEN -> "q";
            };
        }
    }

    @Override
    public int hashCode() {
        return myType.hashCode() * 11 + myColor.hashCode() * 13;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        else if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        else if(this.myColor == ((ChessPiece) obj).myColor && this.myType == ((ChessPiece) obj).myType){
            return true;
        }
        else{
            return false;
        }
    }
}
