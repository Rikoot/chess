package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class PrintGame {

    public static String print(ChessGame game, ChessGame.TeamColor teamColor) {
        StringBuilder output = new StringBuilder();
        int rowStart = 8;
        int rowEnd = 0;
        int colStart = 1;
        int colEnd = 9;
        int direction = -1;
        String backgroundColor = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        output.append(backgroundColor);
        String columns = (teamColor == ChessGame.TeamColor.WHITE)
                ? "   a   b   c   d   e   f   g   h   " : "   h   g   f   e   d   c   b   a   ";
        output.append(columns);
        output.append(EscapeSequences.RESET_BG_COLOR);
        output.append("\n");
        if (teamColor == ChessGame.TeamColor.BLACK) {
            rowStart = 1;
            rowEnd = 9;
            colStart = 8;
            colEnd = 0;
            direction = 1;
        }
        String blackTile = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String whiteTile = EscapeSequences.SET_BG_COLOR_RED;
        String tileBackgroundColor = whiteTile;
        for (int row = rowStart; row != rowEnd; row += direction) {
            output.append(backgroundColor);
            output.append(" ");
            output.append(row);
            output.append(" ");
            for (int col = colStart; col != colEnd; col -= direction) {
                output.append(tileBackgroundColor);
                if (col != colEnd + direction) {
                    tileBackgroundColor = (tileBackgroundColor.equals(blackTile))
                            ? whiteTile : blackTile;
                }
                ChessPiece piece = game.getBoard().getPiece(new ChessPosition(row, col));
                output.append(convertPiece(piece));
            }
            output.append(backgroundColor);
            output.append(" ");
            output.append(row);
            output.append(" ");
            output.append(EscapeSequences.RESET_BG_COLOR);
            output.append("\n");
        }
        output.append(backgroundColor);
        output.append(columns);
        output.append(EscapeSequences.RESET_BG_COLOR);
        output.append("\n");
        return output.toString();
    }

    private static String convertPiece(ChessPiece piece) {
        if (piece == null) { return EscapeSequences.EMPTY;}
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
                        ? (EscapeSequences.BLACK_KING) : EscapeSequences.WHITE_KING;
            }
            case null, default -> {
                return EscapeSequences.EMPTY;
            }
        }
    }
}
