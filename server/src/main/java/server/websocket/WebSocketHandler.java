package server.websocket;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import service.AuthService;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connectionManager = new ConnectionManager();
    private final Gson gson;
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;

    public WebSocketHandler(UserService userService, AuthService authService, GameService gameService) {
        this.userService = userService;
        this.authService = authService;
        this.gameService = gameService;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGameDeserializer());
        gson = gsonBuilder.create();
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected with session: " + ctx.session);
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            MakeMoveCommand command = gson.fromJson(ctx.message(), MakeMoveCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command, ctx.session);
                case LEAVE -> leave(command, ctx.session);
                case RESIGN -> resign(command, ctx.session);
                case MAKE_MOVE -> makeMove(command, ctx.session);

            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed with session: " + ctx.session);
    }

    private void connect(MakeMoveCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        if (Objects.nonNull(authData)) {
            int gameID = command.getGameID();
            GameData game = null;
            try {
                game = gameService.getGame(gameID);
            } catch (DataAccessException e) {
                // continue to error below
            }
            if (Objects.nonNull(game)) {
                connectionManager.addSessionToGame(session, gameID);
                String username = authData.username();
                String role = "Observer";
                if (Objects.equals(game.blackUsername(), username)) {
                    role = "Black";
                } else if (Objects.equals(game.whiteUsername(), username)) {
                    role = "White";
                }
                NotificationMessage msg = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        "ℹ️: A new user just joined: " + username + " as: " + role);
                connectionManager.broadcastToGame(session, msg, gameID);
                sendGame(session, gameID);
                return;
            }
        }
        sendError(session, "⚠️:An error occurred joining.");
    }

    private void leave(MakeMoveCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        try {
            if (Objects.nonNull(authData)) {
                GameData gameData = gameService.getGame(command.getGameID());
                if (authData.username().equals(gameData.whiteUsername())) {
                    gameData = gameData.setWhiteUsername(null);
                } else if (authData.username().equals(gameData.blackUsername())) {
                    gameData.setBlackUsername(null);
                }
                gameService.updateGame(gameData);
                connectionManager.removeSessionFromGame(session, command.getGameID());
                NotificationMessage msg = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        authData.username() + " just left!");
                connectionManager.broadcastToGame(session, msg, command.getGameID());
                return;
            }
        } catch (DataAccessException e) {
            // continue
        }
        sendError(session,"⚠️: An error occurred leaving.");
    }

    private void resign(MakeMoveCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        try {
            if (Objects.nonNull(authData)) {
                GameData gameData = gameService.getGame(command.getGameID());
                if (!gameData.game().playable) {
                    throw new InvalidMoveException("Unplayable game");
                }
                if (!authData.username().equals(gameData.whiteUsername())
                        && !authData.username().equals(gameData.blackUsername())) {
                    throw new InvalidMoveException("Invalid Player");
                }
                gameData.game().resign();
                gameService.updateGame(gameData);
                NotificationMessage msg = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        authData.username() + " just resigned!");
                connectionManager.broadcastToGame(null, msg, command.getGameID());
                connectionManager.removeSessionFromGame(session, command.getGameID());
                return;
            }
        } catch (InvalidMoveException | DataAccessException e) {
            // continue to error
        }
        sendError(session,"⚠️: An error occurred resigning.");
    }

    private void makeMove(MakeMoveCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        if (Objects.nonNull(authData)) {
            try {
                GameData gameData = gameService.getGame(command.getGameID());
                ChessGame game = gameData.game();
                try {
                    if (!game.playable) {
                        throw new InvalidMoveException("Unplayable game");
                    }
                    ChessGame.TeamColor teamColor = game.getTeamTurn();
                    if (authData.username().equals(gameData.whiteUsername())) {
                        if (teamColor != ChessGame.TeamColor.WHITE) {
                            throw new InvalidMoveException("Invalid Turn");
                        }
                    } else if (authData.username().equals(gameData.blackUsername())) {
                        if (teamColor != ChessGame.TeamColor.BLACK) {
                            throw new InvalidMoveException("Invalid Turn");
                        }
                    } else {
                        throw new InvalidMoveException("Invalid Player");
                    }
                    game.makeMove(command.getMove());
                    String state;
                    if (game.isInCheck(teamColor)) {
                        state = "check";
                    } else if (game.isInCheckmate(teamColor)) {
                        state = "checkmate";
                    } else if (game.isInStalemate(teamColor)) {
                        state = "stalemate";
                    } else {
                        state =  null;
                    }
                    if (!gameService.updateGame(gameData)) {
                        throw new DataAccessException("Update Error");
                    }
                    System.out.println(print(game, teamColor, new HashSet<>(), null));
                    connectionManager.sendGame(new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game), command.getGameID());
                    String moveNotif = teamColor.toString() + " made a move: " + command.getMove().toString();
                    connectionManager.broadcastToGame(session,
                            new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveNotif), command.getGameID());
                    if (Objects.nonNull(state)) {
                        String stateNotif = teamColor + " is in " + state;
                        connectionManager.broadcastToGame(session,
                                new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, stateNotif), command.getGameID());
                    }
                    return;
                } catch (InvalidMoveException e) {
                    // continue to error below
                }
            } catch (DataAccessException e) {
                // continue to error below
            }
        }
        sendError(session,"⚠️: An error occurred making a move.");
    }


    private void sendError(Session session, String message) throws IOException {
        ErrorMessage mgs = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
        session.getRemote().sendString(gson.toJson(mgs));
    }

    private void sendGame(Session session, int gameID) throws IOException {
        ChessGame game = null;
        try {
            game = gameService.getGame(gameID).game();
        } catch (DataAccessException e) {
            // continue to error below
        }
        if (Objects.nonNull(game)) {
            LoadGameMessage gameMsg = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    game);
            session.getRemote().sendString(gson.toJson(gameMsg));
            return;
        }
        sendError(session, "⚠️: An error occurred sending a game");
    }
    public static String print(ChessGame game, ChessGame.TeamColor teamColor, Collection<ChessPosition> positions, ChessPosition startPosition) {
        StringBuilder output = new StringBuilder();
        int rowStart = 8;
        int rowEnd = 0;
        int colStart = 1;
        int colEnd = 9;
        int direction = -1;
        String backgroundColor = SET_BG_COLOR_DARK_GREY;
        output.append(backgroundColor);
        String columns = (teamColor == ChessGame.TeamColor.WHITE)
                ? "   a   b   c   d   e   f   g   h   " : "   h   g   f   e   d   c   b   a   ";
        output.append(columns);
        output.append(RESET_BG_COLOR);
        output.append("\n");
        if (teamColor == ChessGame.TeamColor.BLACK) {
            rowStart = 1;
            rowEnd = 9;
            colStart = 8;
            colEnd = 0;
            direction = 1;
        }
        String blackTile = SET_BG_COLOR_LIGHT_GREY;
        String whiteTile = SET_BG_COLOR_RED;
        boolean tileColor = false;
        String tileBackgroundColor = whiteTile;
        for (int row = rowStart; row != rowEnd; row += direction) {
            output.append(backgroundColor);
            output.append(" ");
            output.append(row);
            output.append(" ");
            for (int col = colStart; col != colEnd; col -= direction) {
                ChessPosition position = new ChessPosition(row, col);
                if (positions.contains(position)) {
                    tileBackgroundColor = (tileColor) ? SET_BG_COLOR_LIGHT_GREY_GREEN_TINT : SET_BG_COLOR_RED_GREEN_TINT;
                } else if (position.equals(startPosition)) {
                    tileBackgroundColor = SET_BG_COLOR_YELLOW;
                }
                output.append(tileBackgroundColor);
                if (col != colEnd + direction) {
                    tileBackgroundColor = (tileColor)
                            ? whiteTile : blackTile;
                    tileColor = !tileColor;
                }
                ChessPiece piece = game.getBoard().getPiece(position);
                output.append(convertPiece(piece));
            }
            output.append(backgroundColor);
            output.append(" ");
            output.append(row);
            output.append(" ");
            output.append(RESET_BG_COLOR);
            output.append("\n");
        }
        output.append(backgroundColor);
        output.append(columns);
        output.append(RESET_BG_COLOR);
        output.append("\n");
        return output.toString();
    }

    private static String convertPiece(ChessPiece piece) {
        if (piece == null) { return EMPTY;}
        switch (piece.getPieceType()){
            case QUEEN -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? BLACK_QUEEN : WHITE_QUEEN;
            }
            case BISHOP -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? BLACK_BISHOP : WHITE_BISHOP;
            }
            case ROOK -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? BLACK_ROOK : WHITE_ROOK;
            }
            case KNIGHT -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? BLACK_KNIGHT : WHITE_KNIGHT;
            }
            case PAWN -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? BLACK_PAWN : WHITE_PAWN;
            }
            case KING -> {
                return (piece.getTeamColor() == ChessGame.TeamColor.BLACK)
                        ? (BLACK_KING) : WHITE_KING;
            }
            case null, default -> {
                return EMPTY;
            }
        }
    }
    private static final String UNICODE_ESCAPE = "\u001b";
    private static final String ANSI_ESCAPE = "\033";

    public static final String ERASE_SCREEN = UNICODE_ESCAPE + "[H" + UNICODE_ESCAPE + "[2J";
    public static final String ERASE_LINE = UNICODE_ESCAPE + "[2K";

    public static final String SET_TEXT_BOLD = UNICODE_ESCAPE + "[1m";
    public static final String SET_TEXT_FAINT = UNICODE_ESCAPE + "[2m";
    public static final String RESET_TEXT_BOLD_FAINT = UNICODE_ESCAPE + "[22m";
    public static final String SET_TEXT_ITALIC = UNICODE_ESCAPE + "[3m";
    public static final String RESET_TEXT_ITALIC = UNICODE_ESCAPE + "[23m";
    public static final String SET_TEXT_UNDERLINE = UNICODE_ESCAPE + "[4m";
    public static final String RESET_TEXT_UNDERLINE = UNICODE_ESCAPE + "[24m";
    public static final String SET_TEXT_BLINKING = UNICODE_ESCAPE + "[5m";
    public static final String RESET_TEXT_BLINKING = UNICODE_ESCAPE + "[25m";

    private static final String SET_TEXT_COLOR = UNICODE_ESCAPE + "[38;5;";
    private static final String SET_BG_COLOR = UNICODE_ESCAPE + "[48;5;";

    public static final String SET_TEXT_COLOR_BLACK = SET_TEXT_COLOR + "0m";
    public static final String SET_TEXT_COLOR_LIGHT_GREY = SET_TEXT_COLOR + "242m";
    public static final String SET_TEXT_COLOR_DARK_GREY = SET_TEXT_COLOR + "235m";
    public static final String SET_TEXT_COLOR_RED = SET_TEXT_COLOR + "160m";
    public static final String SET_TEXT_COLOR_GREEN = SET_TEXT_COLOR + "46m";
    public static final String SET_TEXT_COLOR_YELLOW = SET_TEXT_COLOR + "226m";
    public static final String SET_TEXT_COLOR_BLUE = SET_TEXT_COLOR + "12m";
    public static final String SET_TEXT_COLOR_MAGENTA = SET_TEXT_COLOR + "5m";
    public static final String SET_TEXT_COLOR_WHITE = SET_TEXT_COLOR + "15m";
    public static final String RESET_TEXT_COLOR = UNICODE_ESCAPE + "[39m";

    public static final String SET_BG_COLOR_BLACK = SET_BG_COLOR + "0m";
    public static final String SET_BG_COLOR_LIGHT_GREY = SET_BG_COLOR + "242m";
    public static final String SET_BG_COLOR_DARK_GREY = SET_BG_COLOR + "235m";
    public static final String SET_BG_COLOR_RED = SET_BG_COLOR + "160m";
    public static final String SET_BG_COLOR_GREEN = SET_BG_COLOR + "46m";
    public static final String SET_BG_COLOR_DARK_GREEN = SET_BG_COLOR + "22m";
    public static final String SET_BG_COLOR_YELLOW = SET_BG_COLOR + "226m";
    public static final String SET_BG_COLOR_BLUE = SET_BG_COLOR + "12m";
    public static final String SET_BG_COLOR_MAGENTA = SET_BG_COLOR + "5m";
    public static final String SET_BG_COLOR_WHITE = SET_BG_COLOR + "15m";
    public static final String RESET_BG_COLOR = UNICODE_ESCAPE + "[49m";
    public static final String SET_BG_COLOR_LIGHT_GREY_GREEN_TINT = UNICODE_ESCAPE + "[48;2;100;120;100m";
    public static final String SET_BG_COLOR_RED_GREEN_TINT = UNICODE_ESCAPE + "[48;2;160;110;80m";

    public static final String WHITE_KING = " ♚ ";
    public static final String WHITE_QUEEN = " ♛ ";
    public static final String WHITE_BISHOP = " ♝ ";
    public static final String WHITE_KNIGHT = " ♞ ";
    public static final String WHITE_ROOK = " ♜ ";
    public static final String WHITE_PAWN = " ♟ ";
    public static final String BLACK_KING = SET_TEXT_COLOR_BLACK +" ♚ "+ RESET_TEXT_COLOR;
    public static final String BLACK_QUEEN = SET_TEXT_COLOR_BLACK +" ♛ "+ RESET_TEXT_COLOR;
    public static final String BLACK_BISHOP = SET_TEXT_COLOR_BLACK +" ♝ "+ RESET_TEXT_COLOR;
    public static final String BLACK_KNIGHT = SET_TEXT_COLOR_BLACK + " ♞ "+ RESET_TEXT_COLOR;
    public static final String BLACK_ROOK = SET_TEXT_COLOR_BLACK + " ♜ "+ RESET_TEXT_COLOR;
    public static final String BLACK_PAWN = SET_TEXT_COLOR_BLACK + " ♟ " + RESET_TEXT_COLOR;
    public static final String EMPTY = " \u2003 ";

    public static String moveCursorToLocation(int x, int y) { return UNICODE_ESCAPE + "[" + y + ";" + x + "H"; }
}
