package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    ChessGame.TeamColor pieceColor;
    ChessPiece.PieceType type;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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
        return type;
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

        switch(type) {
            case BISHOP -> bishopMoves(board, myPosition, movesCollection);
            case KING -> kingMoves(board, myPosition, movesCollection);
            case KNIGHT -> knightMoves(board, myPosition, movesCollection);
            case PAWN -> promotionMoves(board, myPosition, movesCollection);
            case QUEEN -> queenMoves(board, myPosition, movesCollection);
            case ROOK -> rookMoves(board, myPosition, movesCollection);
        }
        return movesCollection;
    }

    private void bishopMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> movesCollection) {
        // up right
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int distance = 1; distance <=7; distance++) {
            ChessPosition move = new ChessPosition(row + distance, col + distance);
            if (!validMove(board, move, myPosition, movesCollection)) {
                break;
            }
        }
        // up left
        for (int distance = 1; distance <=7; distance++) {
            ChessPosition move = new ChessPosition(row + distance, col - distance);
            if (!validMove(board, move, myPosition, movesCollection)) {
                break;
            }
        }
        // down right
        for (int distance = 1; distance <=7; distance++) {
            ChessPosition move = new ChessPosition(row - distance, col + distance);
            if (!validMove(board, move, myPosition, movesCollection)) {
                break;
            }
        }
        // down left
        for (int distance = 1; distance <=7; distance++) {
            ChessPosition move = new ChessPosition(row - distance, col - distance);
            if (!validMove(board, move, myPosition, movesCollection)) {
                break;
            }
        }
    }
    private void kingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> movesCollection) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[] directions = {-1, 1};
        //
        for (int direction : directions) {
            ChessPosition move = new ChessPosition(row + direction, col + direction);
            validMove(board, move, myPosition, movesCollection);
        }
        //
        for (int direction : directions) {
            ChessPosition move = new ChessPosition(row - direction, col + direction);
            validMove(board, move, myPosition, movesCollection);
        }
        // up and down
        for (int direction : directions) {
            ChessPosition move = new ChessPosition(row, col + direction);
            validMove(board, move, myPosition, movesCollection);
        }
        // right and left
        for (int direction : directions) {
            ChessPosition move = new ChessPosition(row + direction, col);
            validMove(board, move, myPosition, movesCollection);
        }
    }
    private void knightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> movesCollection) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[] directions = {-1, 1};
        // right
        for (int direction : directions) {
            ChessPosition move = new ChessPosition(row + 2, col + direction);
            validMove(board, move, myPosition, movesCollection);
        }
        // left
        for (int direction : directions) {
            ChessPosition move = new ChessPosition(row - 2, col + direction);
            validMove(board, move, myPosition, movesCollection);
        }
        // up
        for (int direction : directions) {
            ChessPosition move = new ChessPosition(row + direction, col + 2);
            validMove(board, move, myPosition, movesCollection);
        }
        // down
        for (int direction : directions) {
            ChessPosition move = new ChessPosition(row + direction, col - 2);
            validMove(board, move, myPosition, movesCollection);
        }
    }
    private void promotionMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> movesCollection) {
        if (pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2
                || pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7) {
            ChessPiece.PieceType[] promotionTypes = {PieceType.BISHOP, PieceType.KNIGHT, PieceType.QUEEN, PieceType.ROOK};
            for (ChessPiece.PieceType promotionType : promotionTypes) {
                pawnMoves(board, myPosition, movesCollection, promotionType);
            }
        } else {
            pawnMoves(board, myPosition, movesCollection, null);
        }
    }
    private void pawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> movesCollection, ChessPiece.PieceType promotionType) {
        int direction = pieceColor == ChessGame.TeamColor.BLACK ? -1 : 1;
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        // left move
        ChessPosition leftMove = new ChessPosition(row + direction, col - 1);
        if (validPosition(leftMove) && piecePresent(board, leftMove) && capturablePiece(board, leftMove)) {
            movesCollection.add(new ChessMove(myPosition, leftMove, promotionType));
        }
        // right move
        ChessPosition rightMove = new ChessPosition(row + direction, col + 1);
        if (validPosition(rightMove) && piecePresent(board, rightMove) && capturablePiece(board, rightMove)) {
            movesCollection.add(new ChessMove(myPosition, rightMove, promotionType));
        }
        // front move
        ChessPosition frontMove = new ChessPosition(row + direction, col);
        if (validPosition(frontMove) && !piecePresent(board, frontMove)) {
            movesCollection.add(new ChessMove(myPosition, frontMove, promotionType));
        }
        // double move
        int initialRow = pieceColor == ChessGame.TeamColor.BLACK ? 7 : 2;
        ChessPosition doubleMove = new ChessPosition(row + (2*direction), col);
        if ((pieceColor == ChessGame.TeamColor.BLACK && initialRow == row)
                || (pieceColor == ChessGame.TeamColor.WHITE && initialRow == row)){
            if (!piecePresent(board, frontMove)
                    && !piecePresent(board, doubleMove)) {
                movesCollection.add(new ChessMove(myPosition, doubleMove, promotionType));
            }
        }

    }
    private void queenMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> movesCollection) {
        bishopMoves(board, myPosition, movesCollection);
        rookMoves(board, myPosition, movesCollection);
    }
    private void rookMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> movesCollection) {
        // up
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int distance = 1; distance <=7; distance++) {
            ChessPosition move = new ChessPosition(row + distance, col);
            if (!validMove(board, move, myPosition, movesCollection)) {
                break;
            }
        }
        // down
        for (int distance = 1; distance <=7; distance++) {
            ChessPosition move = new ChessPosition(row - distance, col);
            if (!validMove(board, move, myPosition, movesCollection)) {
                break;
            }
        }
        // right
        for (int distance = 1; distance <=7; distance++) {
            ChessPosition move = new ChessPosition(row, col + distance);
            if (!validMove(board, move, myPosition, movesCollection)) {
                break;
            }
        }
        // left
        for (int distance = 1; distance <=7; distance++) {
            ChessPosition move = new ChessPosition(row, col - distance);
            if (!validMove(board, move, myPosition, movesCollection)) {
                break;
            }
        }
    }
    private boolean validMove(ChessBoard board, ChessPosition nextPosition, ChessPosition myPosition, Collection<ChessMove> movesCollection) {
        if (validPosition(nextPosition)) {
            if (piecePresent(board, nextPosition)) {
                if (capturablePiece(board, nextPosition)) {
                    movesCollection.add(new ChessMove(myPosition, nextPosition, null));
                    return false;
                }
            } else {
                movesCollection.add(new ChessMove(myPosition, nextPosition, null));
                return true;
            }
        }
        return false;
    }

    private boolean piecePresent(ChessBoard board, ChessPosition nextPosition) {
        return board.getPiece(nextPosition) != null;
    }
    private boolean validPosition(ChessPosition nextPosition) {
        int row = nextPosition.getRow();
        int col = nextPosition.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
    private boolean capturablePiece(ChessBoard board, ChessPosition nextPosition) {
        return board.getPiece(nextPosition).getTeamColor() != pieceColor;
    }
}
