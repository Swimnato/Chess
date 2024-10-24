package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final int rows = 8;
    private final int cols = 8;
    private ChessPiece[][] board;

    private static final ChessPiece.PieceType[] FIRSTROW = {ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.KING,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.ROOK};

    public ChessBoard() {
        board = new ChessPiece[rows][cols];
    }

    public ChessBoard(ChessBoard toClone) {
        board = new ChessPiece[rows][cols];
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                ChessPosition currPos = new ChessPosition(i, j);
                ChessPiece currPiece = toClone.getPiece(currPos);
                if (currPiece != null) {
                    this.setPiece(currPos, new ChessPiece(currPiece));
                }
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    private void setPiece(ChessPosition position, ChessPiece piece) {
        if (piece != null) {
            board[position.getRow() - 1][position.getColumn() - 1] = new ChessPiece(piece);
        } else {
            board[position.getRow() - 1][position.getColumn() - 1] = null;
        }
    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (piece != null) {
            board[position.getRow() - 1][position.getColumn() - 1] = new ChessPiece(piece, true);
        } else {
            board[position.getRow() - 1][position.getColumn() - 1] = null;
        }
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        ChessPiece requestedPiece = board[position.getRow() - 1][position.getColumn() - 1];
        if (requestedPiece != null) {
            return new ChessPiece(requestedPiece);
        }
        return null;
    }

    public int movePiece(ChessMove move) {
        if (!(move.getEndPosition().isValid(this)) && !(move.getStartPosition().isValid(this))) {
            return 1; // error code invalid position.
        }
        if (getPiece(move.getStartPosition()) == null) {
            return 2; // error code no start position
        }
        if (getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.PAWN &&
                move.getStartPosition().getColumn() != move.getEndPosition().getColumn() &&
                getPiece(move.getEndPosition()) == null) {
            addPiece(new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn()), null);
        }
        int diffInCols = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
        if (getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.KING && (diffInCols > 1 || diffInCols < -1)) {
            board[move.getEndPosition().getRow() - 1][move.getEndPosition().getColumn() - 1] = getPiece(move.getStartPosition());
            ChessPosition desiredRook = new ChessPosition(move.getStartPosition().getRow(), (diffInCols == -2 ? 1 : 8));
            movePiece(new ChessMove(desiredRook, new ChessPosition(move.getStartPosition().getRow(),
                    move.getStartPosition().getColumn() + diffInCols / 2), null));
        }
        if (move.getPromotionPiece() == null) {
            if (getPiece(move.getStartPosition()).isFirstMove()) {
                board[move.getEndPosition().getRow() - 1][move.getEndPosition().getColumn() - 1] =
                        new ChessPiece(getPiece(move.getStartPosition()), false);
            } else {
                board[move.getEndPosition().getRow() - 1][move.getEndPosition().getColumn() - 1] = getPiece(move.getStartPosition());
            }
        } else {
            board[move.getEndPosition().getRow() - 1][move.getEndPosition().getColumn() - 1] =
                    new ChessPiece(getPiece(move.getStartPosition()).getTeamColor(), move.getPromotionPiece(), false);
        }


        addPiece(move.getStartPosition(), null);

        return 0;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[rows][cols];
        for (byte color = 0; color < 2; color++) {
            for (byte row = 0; row < 2; row++) {
                for (byte col = 0; col < cols; col++) {
                    int currRow = (color == 0 ? row : (rows - 1 + (row == 0 ? 0 : -1)));
                    int currCol = col;
                    ChessPiece.PieceType currPiece = row == 0 ? FIRSTROW[col] : ChessPiece.PieceType.PAWN;
                    ChessGame.TeamColor currColor = color == 0 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                    board[currRow][currCol] = new ChessPiece(currColor, currPiece, true);
                }
            }
        }
    }

    @Override
    public String toString() {
        String output = "";
        for (int row = rows - 1; row >= 0; row--) {
            output = output + "|";
            for (int col = 0; col < cols; col++) {
                ChessPiece curr = board[row][col];
                if (curr != null) {
                    output = output + curr + '|';
                } else {
                    output = output + " |";
                }
            }
            output = output + "\r\n";
        }
        return output;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else if (this.toString().equals(obj.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode() * 101;
    }
}
