package chess;
import java.util.HashSet;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType pieceTypeValue;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && pieceTypeValue == that.pieceTypeValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, pieceTypeValue);
    }

    @Override
    public String toString() {
        return pieceTypeValue.toString();
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        pieceTypeValue = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceTypeValue;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> movesCollection = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int direction = 1;
        if (pieceColor == ChessGame.TeamColor.BLACK) {
            direction = -1;
        }
        switch (pieceTypeValue) {
            case PAWN:
                pawnMoves(movesCollection, board, myPosition, direction, row, col);
                break;
            case BISHOP:
                bishopMoves(movesCollection, board, myPosition, direction, row, col);
                break;
            case KNIGHT:
                break;
            case ROOK:
                rookMoves(movesCollection, board, myPosition, direction, row, col);
                break;
            case QUEEN:
                break;
            case KING:
                break;
        }
        return movesCollection;
    }
    private void pawnMoves (Collection<ChessMove> movesCollection, ChessBoard board, ChessPosition myPosition, int direction, int row, int col) {
        ChessPosition leftMove = new ChessPosition(row + (1*direction), col - 1);
        if (validPositon(leftMove) && piecePresent(board, leftMove)) {
            movesCollection.add(new ChessMove(myPosition, leftMove, null));
        }
        ChessPosition rightMove = new ChessPosition(row + (1*direction), col - 1);
        if (validPositon(rightMove) && piecePresent(board, rightMove)) {
            movesCollection.add(new ChessMove(myPosition, rightMove, null));
        }
        ChessPosition forwardMove = new ChessPosition(row + (1*direction), col);
        if (validPositon(forwardMove) && !piecePresent(board, forwardMove)) {
            movesCollection.add(new ChessMove(myPosition, forwardMove, null));
        }
        int initialRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        ChessPosition doubleMove = new ChessPosition(row + (2*direction), col);
        if (initialRow == row && !piecePresent(board, forwardMove) && !piecePresent(board, doubleMove)) {
            movesCollection.add(new ChessMove(myPosition, doubleMove, null));
        }
    }
    private void bishopMoves (Collection<ChessMove> movesCollection, ChessBoard board, ChessPosition myPosition, int direction, int row, int col) {
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition nextMove = new ChessPosition(row + offset, col + offset);
            if (invalidMove(movesCollection, board, myPosition, nextMove)) {
                break;
            }
        }
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition nextMove = new ChessPosition(row + offset, col - offset);
            if (invalidMove(movesCollection, board, myPosition, nextMove)) {
                break;
            }
        }
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition nextMove = new ChessPosition(row - offset, col + offset);
            if (invalidMove(movesCollection, board, myPosition, nextMove)) {
                break;
            }
        }
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition nextMove = new ChessPosition(row - offset, col - offset);
            if (invalidMove(movesCollection, board, myPosition, nextMove)) {
                break;
            }
        }
    }
    private void rookMoves (Collection<ChessMove> movesCollection, ChessBoard board, ChessPosition myPosition, int direction, int row, int col) {
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition nextMove = new ChessPosition(row + offset, col);
            if (invalidMove(movesCollection, board, myPosition, nextMove)) {
                break;
            }
        }
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition nextMove = new ChessPosition(row - offset, col);
            if (invalidMove(movesCollection, board, myPosition, nextMove)) {
                break;
            }
        }
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition nextMove = new ChessPosition(row, col + offset);
            if (invalidMove(movesCollection, board, myPosition, nextMove)) {
                break;
            }
        }
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition nextMove = new ChessPosition(row, col - offset);
            if (invalidMove(movesCollection, board, myPosition, nextMove)) {
                break;
            }
        }
    }

    private boolean invalidMove(Collection<ChessMove> movesCollection, ChessBoard board, ChessPosition myPosition, ChessPosition nextMove) {
        if (validPositon(nextMove)) {
            if (piecePresent(board, nextMove)) {
                if (capturablePiece(board, nextMove)) {
                    movesCollection.add(new ChessMove(myPosition, nextMove, null));
                }
                return true;
            } else {
                movesCollection.add(new ChessMove(myPosition, nextMove, null));
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean validPositon(ChessPosition nextPosition) {
        return nextPosition.getColumn() <= 8 && nextPosition.getColumn() >= 1 && nextPosition.getRow() <= 8 && nextPosition.getRow() >= 1;
    }
    private boolean piecePresent(ChessBoard board, ChessPosition nextPosition) {
        return board.getPiece(nextPosition) != null;
    }
    private boolean capturablePiece(ChessBoard board, ChessPosition nextPosition) {
        return board.getPiece(nextPosition).getTeamColor() != pieceColor;
    }
}
