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

    private static final ChessPiece.PieceType[] firstRow = {ChessPiece.PieceType.ROOK,
                                                            ChessPiece.PieceType.KNIGHT,
                                                            ChessPiece.PieceType.BISHOP,
                                                            ChessPiece.PieceType.QUEEN,
                                                            ChessPiece.PieceType.KING,
                                                            ChessPiece.PieceType.BISHOP,
                                                            ChessPiece.PieceType.KNIGHT,
                                                            ChessPiece.PieceType.ROOK };

    public ChessBoard() {
        board = new ChessPiece[rows][cols];
    }

    public int getRows(){
        return rows;
    }

    public int getCols(){
        return cols;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = new ChessPiece(piece);
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
        if(requestedPiece != null){
            return new ChessPiece(requestedPiece);
        }
        return null;
    }

    public int movePiece(ChessMove move){
        if(!(move.getEndPosition().isValid(this)) && !(move.getStartPosition().isValid(this))){
            return 1;
        }
        if(getPiece(move.getStartPosition()) == null){
            return 2;
        }

        addPiece(move.getEndPosition(), getPiece(move.getStartPosition()));
        addPiece(move.getStartPosition(), null);

        return 0;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[rows][cols];
        for(byte color = 0; color < 2; color++){
            for(byte row = 0; row < 2; row++){
                for(byte col = 0; col < cols; col++){
                    int _currRow = (color == 0 ? row : (rows - 1 + (row == 0 ? 0 : -1 )));
                    int _currCol = col;
                    ChessPiece.PieceType _currPiece = row == 0 ? firstRow[col] : ChessPiece.PieceType.PAWN;
                    ChessGame.TeamColor _currColor = color == 0 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                    board[_currRow][_currCol] = new ChessPiece(_currColor, _currPiece);
                }
            }
        }
    }

    @Override
    public String toString() {
        String output = "|";
        for(int row = rows - 1; row >= 0; row--){
            for(int col = 0; col < cols; col++){
                ChessPiece _curr = board[row][col];
                if(_curr != null){
                    output  = output + _curr + '|';
                }
                else{
                    output = output + " |";
                }
            }
            output = output + "\r\n";
        }
        return output;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        else if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        else if(this.toString().equals(obj.toString())){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode() * 101;
    }
}
