package chess;

import java.util.Collection;

public class ChessRules {
    public static Collection<ChessMove> validMoves(ChessBoard board, ChessPosition startPosition, ChessGame.TeamColor currentTeam) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
        for (ChessMove move : allMoves) {
            ChessBoard copyTestBoard = new ChessBoard();

            if (isInCheck(copyTestBoard, currentTeam)) {
                allMoves.remove(move);
            }
        }
        return allMoves;
    }
    public static void makeMove(ChessBoard board, ChessMove move, ChessGame.TeamColor currentTeam) throws InvalidMoveException {
        ChessPiece movePiece = board.getPiece(move.getStartPosition());
        if (movePiece == null) {
            throw new InvalidMoveException("Piece not present at start positon");
        }
        if (movePiece.getTeamColor() != currentTeam) {
            throw new InvalidMoveException("Wrong turn");
        }
        Collection<ChessMove> validMovesCollection = validMoves(board, move.getStartPosition(), currentTeam);
        if (validMovesCollection != null && validMovesCollection.contains(move)) {
            // make move
            ChessPiece piece = move.getPromotionPiece() == null ? movePiece : new ChessPiece(currentTeam, move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            // switch team turns
            currentTeam = currentTeam == ChessGame.TeamColor.BLACK ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        }
    }
    public static boolean isInCheck(ChessBoard board, ChessGame.TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }


    public static boolean isInCheckmate(ChessBoard board, ChessGame.TeamColor teamColor) {
        if (isInCheck(board, teamColor)) {

        } else {
            return false;
        }
        throw new RuntimeException("Not implemented");
    }

    public static boolean isInStalemate(ChessBoard board, ChessGame.TeamColor teamColor) {
        if (!isInCheck(board, teamColor)) {

        } else {
            return false;
        }
        throw new RuntimeException("Not implemented");
    }
}
