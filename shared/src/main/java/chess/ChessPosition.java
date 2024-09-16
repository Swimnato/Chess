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

    @Override
    public boolean equals(Object _other) {
        if (this == _other) {
            return true;
        } else if (_other == null || _other.getClass() != this.getClass()) {
            return false;
        } else {
            return (row == ((ChessPosition) _other).getRow() && col == ((ChessPosition) _other).getRow());
        }
    }

    public boolean isValid() {
        return row > 0 && col > 0 && row < 9 && col < 9;
    }

    @Override
    public int hashCode() {
        return (row * 71 + col * 73);
    }

    @Override
    public String toString() {
        return "(" + row + ',' + col + ')';
    }
}
