package chess;

import java.util.Collection;
import java.util.HashSet;

public class ChessRules {
    public static Collection<ChessMove> validMoves(ChessBoard board, ChessPosition startPosition, ChessGame.TeamColor currentTeam) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> actualMoves = new HashSet<>();
        for (ChessMove move : allMoves) {
            ChessBoard copyTestBoard = board.deepCopy();
            moveMaker(copyTestBoard, piece, move, currentTeam);
            if (!isInCheck(copyTestBoard, teamColor)) {
                actualMoves.add(move);
            }
        }
        return actualMoves;
    }
    public static void moveMaker(ChessBoard board, ChessPiece movePiece, ChessMove move, ChessGame.TeamColor currentTeam) {
        ChessPiece piece = move.getPromotionPiece() == null ? movePiece : new ChessPiece(currentTeam, move.getPromotionPiece());
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
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
        if (validMovesCollection == null) {
            throw new InvalidMoveException("No valid Moves");
        }
        if (validMovesCollection.contains(move)) {
            // make move
            moveMaker(board, movePiece, move, currentTeam);
        } else {
            throw  new InvalidMoveException("Invalid Move");
        }
    }
    public static boolean isInCheck(ChessBoard board, ChessGame.TeamColor teamColor) {
        ChessGame.TeamColor otherTeam = teamColor == ChessGame.TeamColor.BLACK ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        Collection<ChessMove> moves = calculateMoves(board, otherTeam);
        ChessPosition kingPosition = findKing(board, teamColor);
        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isInCheckmate(ChessBoard board, ChessGame.TeamColor teamColor) {
        if (isInCheck(board, teamColor)) {
            Collection<ChessPosition> pieces = findPieces(board, teamColor);
            for (ChessPosition piece : pieces) {
                if (!validMoves(board, piece, teamColor).isEmpty()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInStalemate(ChessBoard board, ChessGame.TeamColor teamColor) {
        if (!isInCheck(board, teamColor)) {
            Collection<ChessPosition> pieces = findPieces(board, teamColor);
            for (ChessPosition piece : pieces) {
                if (!validMoves(board, piece, teamColor).isEmpty()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    public static Collection<ChessMove> calculateMoves(ChessBoard board, ChessGame.TeamColor teamColor) {
        Collection<ChessMove> moves = new HashSet<>();
        Collection<ChessPosition> pieces = findPieces(board, teamColor);
        for (ChessPosition piece : pieces) {
            moves.addAll(board.getPiece(piece).pieceMoves(board, piece));
        }
        return moves;
    }
    public static Collection<ChessPosition> findPieces(ChessBoard board, ChessGame.TeamColor teamColor) {
        Collection<ChessPosition> pieces = new HashSet<>();
        for (int col = 1; col <= 8; col++) {
            for (int row = 1; row <= 8; row++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    pieces.add(position);
                }
            }
        }
        return pieces;
    }
    public static ChessPosition findKing(ChessBoard board, ChessGame.TeamColor teamColor) {
        for (int col = 1; col <= 8; col++) {
            for (int row = 1; row <= 8; row++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null;
    }
}
