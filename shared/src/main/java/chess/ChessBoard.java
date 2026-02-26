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

    private HashMap<ChessPosition, ChessPiece> board = new HashMap<>();
    public ChessBoard() {
        createBoard();
    }

    public ChessBoard deepCopy() {
        ChessBoard copy = new ChessBoard();
        for (Map.Entry<ChessPosition, ChessPiece> object : board.entrySet()) {
            ChessPosition position = new ChessPosition(object.getKey().getRow(), object.getKey().getColumn());
            ChessPiece piece = object.getValue();
            piece = piece == null ? null : new ChessPiece(piece.getTeamColor(), piece.getPieceType());
            copy.board.put(position, piece);
        }
        return copy;
    }

    private void createBoard() {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                board.put(new ChessPosition(row, col), null);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.equals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(board);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "Board=" + board +
                '}';
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board.put(position, piece);
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board.get(position);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        createBoard();
        // Start by adding the white pieces
        setBoardPosition(ChessGame.TeamColor.WHITE);
        // Add black pieces
        setBoardPosition(ChessGame.TeamColor.BLACK);
    }

    private void setBoardPosition(ChessGame.TeamColor color) {
        int pawnRow = color == ChessGame.TeamColor.BLACK ? 7 : 2;
        int row = color == ChessGame.TeamColor.BLACK ? 8 : 1;

        for (int col = 1; col <= 8; col++) {
            board.put(new ChessPosition(pawnRow, col), new ChessPiece(color, ChessPiece.PieceType.PAWN));
            ChessPiece piece = new ChessPiece(null, null);
            piece = switch (col) {
                case 1, 8 -> new ChessPiece(color, ChessPiece.PieceType.ROOK);
                case 2, 7 -> new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
                case 3, 6 -> new ChessPiece(color, ChessPiece.PieceType.BISHOP);
                case 4 -> new ChessPiece(color, ChessPiece.PieceType.QUEEN);
                case 5 -> new ChessPiece(color, ChessPiece.PieceType.KING);
                default -> piece;
            };
            board.put(new ChessPosition(row, col), piece);
        }
    }
}
