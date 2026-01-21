package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int positionRow;
    private final int positionColumn;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return positionRow == that.positionRow && positionColumn == that.positionColumn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionRow, positionColumn);
    }

    public ChessPosition(int row, int col) {
        positionRow = row;
        positionColumn = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return positionRow;
//        throw new RuntimeException("Not implemented");
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return positionColumn;
//        throw new RuntimeException("Not implemented");
    }
}
