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
                    final boolean whiteEquals = authData.username().equals(gameData.whiteUsername());
                    final boolean blackEquals = authData.username().equals(gameData.blackUsername());
                    if (whiteEquals
                            && teamColor != ChessGame.TeamColor.WHITE) {
                        throw new InvalidMoveException("Invalid Turn");
                    } else if (blackEquals
                            && teamColor != ChessGame.TeamColor.BLACK) {
                        throw new InvalidMoveException("Invalid Turn");
                    } else if (!whiteEquals
                            && !blackEquals) {
                        throw new InvalidMoveException("Invalid Player");
                    }
                    game.makeMove(command.getMove());
                    String state;
                    ChessGame.TeamColor nextColor = game.getTeamTurn();
                    if (game.isInStalemate(nextColor)) {
                        state = "stalemate";
                    } else if (game.isInCheckmate(nextColor)) {
                        state = "checkmate";
                    } else if (game.isInCheck(nextColor)) {
                        state = "check";
                    } else {
                        state =  null;
                    }
                    if (!gameService.updateGame(gameData)) {
                        throw new DataAccessException("Update Error");
                    }
                    connectionManager.sendGame(new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game), command.getGameID());
                    String moveNotif = teamColor.toString() + " made a move: " + command.getMove().toString();
                    connectionManager.broadcastToGame(session,
                            new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveNotif), command.getGameID());
                    if (Objects.nonNull(state)) {
                        String stateNotif = nextColor + " is in " + state;
                        connectionManager.broadcastToGame(null,
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
}
