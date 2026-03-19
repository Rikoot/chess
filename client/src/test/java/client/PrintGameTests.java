package client;

import chess.ChessGame;
import ui.PrintGame;

public class PrintGameTests {

    public static void main() {
        ChessGame game = new ChessGame();
        System.out.print(PrintGame.print(game, ChessGame.TeamColor.WHITE));
    }

}
