package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import service.AuthService;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connectionManager = new ConnectionManager();
    UserService userService;
    AuthService authService;
    GameService gameService;

    public WebSocketHandler(UserService userService, AuthService authService, GameService gameService) {
        this.userService = userService;
        this.authService = authService;
        this.gameService = gameService;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
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

    private void connect(UserGameCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        if (Objects.nonNull(authData)) {
               connectionManager.addSessionToGame(session, command.getGameID());
            NotificationMessage msg = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    authData.username() + " just joined!");
            connectionManager.broadcastToGame(session, msg, command.getGameID());
        }
        
    }

    private void leave(UserGameCommand command, Session session) throws IOException {
        AuthData authData = authService.validateSession(command.getAuthToken());
        if (Objects.nonNull(authData)) {
            connectionManager.removeSessionFromGame(session, command.getGameID());
            NotificationMessage msg = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    authData.username() + " just left!");
            connectionManager.broadcastToGame(session, msg, command.getGameID());
        }
    }

    private void resign(UserGameCommand command, Session session) {

    }

    private void makeMove(UserGameCommand command, Session session) {

    }

}
