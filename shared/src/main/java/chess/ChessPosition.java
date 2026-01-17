package chess;

import javax.swing.text.Position;
import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int PositionRow;
    private final int PositionColumn;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return PositionRow == that.PositionRow && PositionColumn == that.PositionColumn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(PositionRow, PositionColumn);
    }

    public ChessPosition(int row, int col) {
        PositionRow = row;
        PositionColumn = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return PositionRow;
//        throw new RuntimeException("Not implemented");
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return PositionColumn;
//        throw new RuntimeException("Not implemented");
    }
}
