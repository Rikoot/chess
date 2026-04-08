package client.websocket;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.websocket.*;
import model.GameData;
import ui.PrintGame;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private final Gson gson;
    private GameData gameData;
    private String authToken;
    private ChessGame.TeamColor teamColor;

    public WebSocketFacade(URI uri, Collection<GameData> gameDataCollection) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.registerTypeAdapter(NotificationMessage.class, new ServerMessageDeserializer());
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGameDeserializer());
        gson = gsonBuilder.create();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            session = container.connectToServer(this, uri);
        } catch (DeploymentException | IOException e) {
            return;
        }

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                ServerMessage msg = gson.fromJson(message, ServerMessage.class);
                switch (msg.getServerMessageType()) {
                    case NOTIFICATION -> {
                        NotificationMessage notificationMessage = gson.fromJson(message, NotificationMessage.class);
                        System.out.println();
                        System.out.println(notificationMessage.getMessage());
                    }
                    case ERROR -> {
                        ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
                        System.out.println();
                        System.out.println(errorMessage.getErrorMessage());
                    }
                    case LOAD_GAME -> {
                        LoadGameMessage loadGameMessage = gson.fromJson(message, LoadGameMessage.class);
                        System.out.println();
                        gameData = gameData.setGame(loadGameMessage.getGame());
                        handleDraw();
                    }
                }
                System.out.print("[In game] >>>> ");
            }
        });
    }

    public void send(MakeMoveCommand message) throws IOException {
        session.getBasicRemote().sendText(gson.toJson(message));
    }

    @Override
    // This method must be overridden, but we don't have to do anything with it
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
    public void playGame(GameData gameData, ChessGame.TeamColor teamColor, String authToken) {
        this.gameData = gameData;
        this.teamColor = teamColor;
        this.authToken = authToken;
        boolean leaveStatus = true;
        Scanner scanner = new Scanner(System.in);
        try {
            send(new MakeMoveCommand(UserGameCommand.CommandType.CONNECT, authToken, gameData.gameID(), null));
        } catch (IOException e) {
            System.out.println("An error occurred, try again.");
            return;
        }
        while (leaveStatus) {
            System.out.print("[In game] >>>> ");
            String userInput = scanner.nextLine();
            String[] userArgs = userInput.split(" ");
            try {
                switch (userArgs[0]) {
                    case "draw" -> handleDraw();
                    case "leave" -> {
                        handleLeave();
                        leaveStatus = false;
                    }
                    case "resign" -> handleResign();
                    case "legal" -> handleLegal(userArgs);
                    case "move" -> handleMove(userArgs);
                    case "help" -> printGameHelp();
                    default -> System.out.println("Invalid command option, enter help for more information");
                }
            } catch (IOException e) {
                System.out.println("An error occurred, try again.");
            }
        }
    }

    private void printGameHelp() {
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

    private void handleDraw() {
        System.out.println(PrintGame.print(gameData.game(), teamColor, new HashSet<>(), null));
    }

    private void handleLeave() throws IOException {
        send(new MakeMoveCommand(UserGameCommand.CommandType.LEAVE,
                authToken, gameData.gameID(), null));
    }

    private void handleResign() throws IOException {
        send(new MakeMoveCommand(UserGameCommand.CommandType.RESIGN,
                authToken, gameData.gameID(), null));
    }

    private void handleLegal(String[] userArgs) {
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
    private void handleMove(String[] userArgs) throws IOException {
        ChessPiece.PieceType pieceType = null;
        switch (userArgs.length) {
            case 4:
                if (userArgs[3].length() != 1) {
                    break;
                }
                pieceType = switch (userArgs[3].toUpperCase()) {
                    case "Q" -> ChessPiece.PieceType.QUEEN;
                    case "R" -> ChessPiece.PieceType.ROOK;
                    case "N" -> ChessPiece.PieceType.KNIGHT;
                    case "B" -> ChessPiece.PieceType.BISHOP;
                    default -> null;
                };
            case 3:
                if (userArgs[1].length() != 2 || userArgs[2].length() != 2) {
                    break;
                }
                ChessPosition startPosition = convertNotation(userArgs[1]);
                ChessPosition endPosition = convertNotation(userArgs[2]);
                ChessMove move = new ChessMove(startPosition, endPosition, pieceType);
                send(new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE,
                        authToken, gameData.gameID(), move));
                return;
            default:
                return;
        }
        System.out.println("Invalid command arguments, enter help for more information");
    }
    private ChessPosition convertNotation(String positionString) {
        int col = positionString.toLowerCase().charAt(0) - 'a' + 1;
        int row = positionString.charAt(1) - '0';
        return new ChessPosition(row, col);
    }
}