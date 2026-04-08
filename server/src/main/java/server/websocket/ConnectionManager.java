package server.websocket;

import chess.ChessGame;
import chess.ChessGameDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, List<Session>> gameConnections = new ConcurrentHashMap<>();
    private final Gson gson;

    public ConnectionManager() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGameDeserializer());
        gson = gsonBuilder.create();
    }
    public void addSessionToGame(Session session, int gameID) {
        gameConnections.computeIfAbsent(gameID, k -> new ArrayList<>()).add(session);
    }

    public void removeSessionFromGame(Session session, int gameID) {
        gameConnections.computeIfAbsent(gameID, k -> new ArrayList<>()).remove(session);
    }

    public void broadcastToGame(Session excludeSession, NotificationMessage notification, int gameID) throws IOException {
        String msg = gson.toJson(notification);
        for (Session connection : gameConnections.get(gameID)) {
            if (connection.isOpen()) {
                if (!connection.equals(excludeSession)) {
                    connection.getRemote().sendString(msg);
                }
            }
        }
    }

    public void sendGame(LoadGameMessage loadGameMessage, int gameID) throws IOException {
        String msg = gson.toJson(loadGameMessage);
        for (Session connection : gameConnections.get(gameID)) {
            if (connection.isOpen()) {
                connection.getRemote().sendString(msg);
            }
        }
    }
}