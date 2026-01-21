package chess;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    HashMap<ChessPosition, ChessPiece> Board = new HashMap<>();
    public ChessBoard() {
        createBoard();
    }
    private void createBoard() {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                Board.put(new ChessPosition(row, col), null);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.equals(Board, that.Board);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Board);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "Board=" + Board +
                '}';
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        Board.put(position, piece);
//        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return Board.get(position);
//        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // Start by adding the white pieces
        createBoard();
        setBoardPosition(ChessGame.TeamColor.WHITE);
        // Add black pieces
        setBoardPosition(ChessGame.TeamColor.BLACK);
        //throw new RuntimeException("Not implemented");
    }
    private void setBoardPosition(ChessGame.TeamColor color) {
        int row;
        int pawnRow;
        if (color == ChessGame.TeamColor.WHITE) {
            row = 1;
            pawnRow = 2;
        } else {
            row = 8;
            pawnRow = 7;
        }
        for (int col = 1; col <= 8; col++) {
            Board.put(new ChessPosition(pawnRow, col), new ChessPiece(color, ChessPiece.PieceType.PAWN));
        }
        for (int col = 1; col <= 8; col++) {
            ChessPiece.PieceType type = switch (col) {
                case 1, 8 -> ChessPiece.PieceType.ROOK;
                case 2, 7 -> ChessPiece.PieceType.KNIGHT;
                case 3, 6 -> ChessPiece.PieceType.BISHOP;
                case 4 -> ChessPiece.PieceType.QUEEN;
                case 5 -> ChessPiece.PieceType.KING;
                default -> ChessPiece.PieceType.PAWN;
            };
            Board.put(new ChessPosition(row, col), new ChessPiece(color, type));
        }
    }
}
