package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, List<Session>> gameConnections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void addSessionToGame(Session session, int gameID) {
        gameConnections.getOrDefault(gameID, new ArrayList<>()).add(session);
    }

    public void removeSessionFromGame(Session session, int gameID) {
        gameConnections.getOrDefault(gameID, new ArrayList<>()).remove(session);
    }

    public void broadcastToGame(Session excludeSession, NotificationMessage notification, int gameID) throws IOException {
        String msg = gson.toJson(notification);
        List<Session> players = gameConnections.getOrDefault(gameID, new ArrayList<>());

        for (Session connection : players) {
            if (connection.isOpen()) {
                if (!connection.equals(excludeSession)) {
                    connection.getRemote().sendString(msg);
                }
            }
        }
    }
}