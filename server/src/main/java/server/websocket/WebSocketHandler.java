package server.websocket;

import chess.ChessGame;
import chess.ChessGameDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import model.requests.ListRequest;
import org.eclipse.jetty.websocket.api.Session;
import service.AuthService;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;
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
               connectionManager.addSessionToGame(session, command.getGameID());
            NotificationMessage msg = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    "ℹ️: A new user just joined: "+ authData.username());
            connectionManager.broadcastToGame(session, msg, command.getGameID());
            sendGame(session, command.getGameID());
        } else {
            sendMessage(session, "⚠️:An error occurred joining.");
        }
    }

    private void leave(MakeMoveCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        if (Objects.nonNull(authData)) {
            connectionManager.removeSessionFromGame(session, command.getGameID());
            NotificationMessage msg = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    authData.username() + " just left!");
            connectionManager.broadcastToGame(session, msg, command.getGameID());
        } else {
            sendMessage(session,"⚠️: An error occurred leaving.");
        }
    }

    private void resign(MakeMoveCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        if (Objects.nonNull(authData)) {
            connectionManager.removeSessionFromGame(session, command.getGameID());
            NotificationMessage msg = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    authData.username() + " just resigned!");
            connectionManager.broadcastToGame(session, msg, command.getGameID());
        } else {
            sendMessage(session,"⚠️: An error occurred resigning.");
        }
    }

    private void makeMove(MakeMoveCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        if (Objects.nonNull(authData)) { 
            
        } else {
            sendMessage(session,"⚠️: An error occurred making a move.");
        }
    }

    private void sendMessage(Session session, String message) throws IOException {
        NotificationMessage mgs = new NotificationMessage(ServerMessage.ServerMessageType.ERROR, message);
        session.getRemote().sendString(gson.toJson(mgs));
    }

    private void sendGame(Session session, int gameID) throws IOException {
        ChessGame game = null;
        try {
            Collection<GameData> games = gameService.listGames(new ListRequest("jab-aorok")).games();
            for (GameData gameData : games) {
                if (gameData.gameID() == gameID) {
                    game = gameData.game();
                }
            }
        } catch (DataAccessException e) {
            // continue to error below
        }
        if (Objects.nonNull(game)) {
            NotificationMessage gameMsg = new NotificationMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    gson.toJson(game));
            session.getRemote().sendString(gson.toJson(gameMsg));
        } else {
            sendMessage(session, "⚠️: An error occurred sending a game");
        }
    }
}
