package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece board[][];
    private final ChessPiece.PieceType OrderOfEdges[] = {ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.KING,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.ROOK};

    public ChessBoard() {
        board = new ChessPiece[8][8];
        resetBoard();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        //if (board[position.getRow() - 1][position.getColumn() - 1].getPieceType() == ChessPiece.PieceType.BLANK) {
        board[position.getRow() - 1][position.getColumn() - 1] = new ChessPiece(piece);
        //} else {
        //throw new RuntimeException("Piece already Exists in that location in the board!");
        //}
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (byte row = 2; row < 6; row++) {
            for (byte col = 0; col < 8; col++) {
                board[row][col] = new ChessPiece();
            }
        }
        for (byte col = 0; col < 8; col++) {
            board[1][col] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            board[6][col] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            board[0][col] = new ChessPiece(ChessGame.TeamColor.WHITE, OrderOfEdges[col]);
            board[7][7 - col] = new ChessPiece(ChessGame.TeamColor.BLACK, OrderOfEdges[col]);
        }
    }

    public String printSelf() {
        String output = new String("Printing Matrix:\r\n");
        for (var row : board) {
            for (var piece : row) {
                output = output + '|' + piece.printSelf();
            }
            output += "\r\n|";
        }
        return (output);
    }
}
