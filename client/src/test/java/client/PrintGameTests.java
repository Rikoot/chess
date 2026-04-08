package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import ui.PrintGame;

import java.util.Collection;
import java.util.HashSet;

public class PrintGameTests {

    public static void main() {
        ChessGame game = new ChessGame();
        System.out.print(PrintGame.print(game, ChessGame.TeamColor.WHITE, new HashSet<>(), null));
        ChessPosition position = new ChessPosition(1, 2);
        Collection<ChessPosition> positions = new HashSet<>();
        for (ChessMove move : game.validMoves(position)) {
            positions.add(move.getEndPosition());
        }
        System.out.print(PrintGame.print(game, ChessGame.TeamColor.WHITE, positions, position));
    }

}
