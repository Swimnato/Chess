package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private int row;
    private int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public ChessPosition(ChessPosition toCopy) {
        this.row = toCopy.getRow();
        this.col = toCopy.getColumn();
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    public boolean isValid(ChessBoard board) {
        if (row > 0 && col > 0 && row <= board.getRows() && col <= board.getCols()) { // is in board bounds
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + row + ',' + col + ')';
    }

    public String toString(boolean inChessCoords) {
        if (inChessCoords) {
            char colChar = switch (col) {
                case 1 -> 'A';
                case 2 -> 'B';
                case 3 -> 'C';
                case 4 -> 'D';
                case 5 -> 'E';
                case 6 -> 'F';
                case 7 -> 'G';
                case 8 -> 'H';
                default -> '!';
            };
            return "" + colChar + row;
        } else {
            return toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        if (col == ((ChessPosition) obj).getColumn() && row == ((ChessPosition) obj).getRow()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return row * 7 + col * 11;
    }
}
