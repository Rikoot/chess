package ui;

import chess.ChessGame;
import chess.ChessPiece;

public class PrintGame {

    public static String print(ChessGame game, ChessGame.TeamColor teamColor) {
        StringBuilder output = new StringBuilder();
        int start = 0;
        int end = 0;
        int direction = 0;

        output.append(EscapeSequences.SET_BG_COLOR_BLUE);
        output.append(EscapeSequences.EMPTY);
        if (teamColor == ChessGame.TeamColor.BLACK) {
            start = 8;
            end = 8;
        }

        output.append("");
        return output.toString();
    }

    private static String getChessPieceString(ChessPiece piece) {
        switch (piece.getPieceType()){
            case QUEEN -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? EscapeSequences.BLACK_QUEEN : EscapeSequences.WHITE_QUEEN;
            }
            case BISHOP -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? EscapeSequences.BLACK_BISHOP : EscapeSequences.WHITE_BISHOP;
            }
            case ROOK -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? EscapeSequences.BLACK_ROOK : EscapeSequences.WHITE_ROOK;
            }
            case KNIGHT -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? EscapeSequences.BLACK_KNIGHT : EscapeSequences.WHITE_KNIGHT;
            }
            case PAWN -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? EscapeSequences.BLACK_PAWN : EscapeSequences.WHITE_PAWN;
            }
            case KING -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? EscapeSequences.BLACK_KING : EscapeSequences.WHITE_KING;
            }
            case null, default -> {
                return EscapeSequences.EMPTY;
            }
        }
    }
}
