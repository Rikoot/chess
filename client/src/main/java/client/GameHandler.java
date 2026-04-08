package client;

import chess.*;
import model.GameData;
import ui.PrintGame;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;

public class GameHandler {
    public static void playGame(GameData gameData, ChessGame.TeamColor teamColor) {
        System.out.println(PrintGame.print(gameData.game(), teamColor, new HashSet<>(), null));
        boolean leaveStatus = true;
        Scanner scanner = new Scanner(System.in);
        while (leaveStatus) {
            System.out.print("[In game] >>>> ");
            String userInput = scanner.nextLine();
            String[] userArgs = userInput.split(" ");
            switch (userArgs[0]) {
                case "draw" ->
                        System.out.println(PrintGame.print(gameData.game(), teamColor, new HashSet<>(), null));
                case "leave" -> {
                    handleLeave();
                    leaveStatus = false;
                }
                case "resign" ->
                        handleResign();
                case "legal" ->
                        handleLegal(userArgs, gameData, teamColor);
                case "move" ->
                        handleMove(userArgs);
                case "help" ->
                        printGameHelp();
                default ->
                        System.out.println("Invalid command option, enter help for more information");
            }
        }
    }

    private static void printGameHelp() {
        System.out.println("""
                                draw  - redraw the chess board
                                leave - leave the current game
                                move [START] [END] [PROMOTION] - move a piece (example: move a4 a5)
                                    PROMOTION Options: Q, R, N, B (example: move a7 a8 Q)
                                resign - resign from the current game
                                legal [POSITION] - shows legal moves for a piece (example: legal a4)
                                help - print available commands
                                """);
    }

    private static void handleLeave() {

    }

    private static void handleResign() {

    }

    private static void handleLegal(String[] userArgs, GameData gameData, ChessGame.TeamColor teamColor) {
        if (userArgs.length == 2) {
            if (userArgs[1].length() == 2) {
                ChessPosition position = convertNotation(userArgs[1]);
                ChessBoard board = gameData.game().getBoard();
                ChessPiece piece = board.getPiece(position);
                Collection<ChessPosition> positions = new HashSet<>();
                if (Objects.nonNull(piece)) {
                    for (ChessMove move : piece.pieceMoves(board, position)) {
                        positions.add(move.getEndPosition());
                    }
                }
                System.out.print(PrintGame.print(gameData.game(), teamColor, positions, position));
                return;
            }
        }
        System.out.println("Invalid command arguments, enter help for more information");
    }
    private static void handleMove(String[] userArgs) {
        if (userArgs.length <= 4 && userArgs.length >= 3) {
            if (userArgs[1].length() == 2 && userArgs[2].length() == 2 && userArgs[3].length() <= 1) {
                ChessPosition startPosition = convertNotation(userArgs[1]);
                ChessPosition endPosition = convertNotation(userArgs[2]);
                ChessPiece.PieceType pieceType= switch (userArgs[3].toUpperCase()) {
                    case "Q" -> ChessPiece.PieceType.QUEEN;
                    case "R" -> ChessPiece.PieceType.ROOK;
                    case "N" -> ChessPiece.PieceType.KNIGHT;
                    case "B" -> ChessPiece.PieceType.BISHOP;
                    default -> null;
                };
                ChessMove move = new ChessMove(startPosition, endPosition, pieceType);

            }
        } else {
            System.out.println("Invalid command arguments, enter help for more information");
        }
    }
    private static ChessPosition convertNotation(String positionString) {
        int col = positionString.toLowerCase().charAt(0) - 'a' + 1;
        int row = positionString.charAt(1) - '0';
        return new ChessPosition(row, col);
    }
}
