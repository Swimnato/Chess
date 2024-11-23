package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private ChessPosition startPosition;
    private ChessPosition endPosition;
    private ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }


    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return new ChessPosition(startPosition);
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return new ChessPosition(endPosition);
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public String toString() {
        return "SP: " + startPosition + " EP: " + endPosition + " PP: " + promotionPiece + "\r\n";
    }

    public String toString(boolean displayForUser) {
        if (displayForUser) {
            return startPosition.toString(true) + " -> " + endPosition.toString(true);
        } else {
            return toString();
        }
    }

    @Override
    public int hashCode() {
        int hash1 = 0;
        int hash2 = 0;
        int hash3 = 0;
        if (startPosition != null) {
            hash1 = startPosition.hashCode();
        }
        if (endPosition != null) {
            hash2 = endPosition.hashCode();
        }
        if (promotionPiece != null) {
            hash3 = promotionPiece.hashCode();
        }
        return hash1 * 71 + hash2 * 73 + hash3 * 5;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else if (obj.toString().equals(this.toString())) {
            return true;
        } else {
            return false;
        }
    }
}
