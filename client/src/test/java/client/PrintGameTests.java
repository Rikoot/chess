package client;

import chess.ChessGame;
import chess.ChessPosition;
import ui.PrintGame;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class PrintGameTests {

    public static void main() {
        ChessGame game = new ChessGame();
        System.out.print(PrintGame.print(game, ChessGame.TeamColor.WHITE, new HashSet<>()));
        Collection<ChessPosition> positions = List.of(
                new ChessPosition(3, 1),
                new ChessPosition(4, 1),
                new ChessPosition(3, 2)
        );
        System.out.print(PrintGame.print(game, ChessGame.TeamColor.WHITE, positions));
    }

}
