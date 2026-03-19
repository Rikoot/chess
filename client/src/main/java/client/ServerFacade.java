package client;

import chess.ChessGame;
import chess.ChessGameDeserializer;
import com.google.gson.*;
import model.GameData;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.HashSet;

public class ServerFacade {
    private final Gson serializer;
    private final String serverUrl;
    private String authToken;
    private final HttpClient httpClient;

    public ServerFacade(String url) {
        serverUrl = url;
        httpClient = HttpClient.newHttpClient();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGameDeserializer());
        serializer = gsonBuilder.create();
        authToken = "temp-authToken";
    }

    // logged out commands
    public boolean register(String[] args) throws ConnectException {
        JsonObject json = new JsonObject();
        json.addProperty("username", args[1]);
        json.addProperty("password", args[2]);
        json.addProperty("email", args[3]);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        return extractAuthToken(request);
    }

    public boolean login(String[] args) throws ConnectException {
        JsonObject json = new JsonObject();
        json.addProperty("username", args[1]);
        json.addProperty("password", args[2]);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        return extractAuthToken(request);
    }

    private boolean extractAuthToken(HttpRequest request) throws ConnectException {
        HttpResponse<String> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        if (response.statusCode() != 200) {
            return false;
        } else {
            authToken = JsonParser.parseString(response.body())
                    .getAsJsonObject().get("authToken").getAsString();
            return true;
        }
    }

    // logged in commands
    public int create(String[] args) throws ConnectException {
        JsonObject json = new JsonObject();
        json.addProperty("gameName", args[1]);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .header("authorization", authToken)
                .build();
        HttpResponse<String> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        if (response.statusCode() == 200) {
            JsonObject responseJson = JsonParser.parseString(response.body())
                    .getAsJsonObject();
            return responseJson.get("gameID").getAsInt();
        } else {
            return 0;
        }

    }

    public Collection<GameData> list() throws ConnectException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .GET()
                .header("authorization", authToken)
                .build();
        HttpResponse<String> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .join();
        if (response.statusCode() != 200) {
            return null;
        }
        Collection<GameData> gameDataCollection = new HashSet<>();
        JsonArray responseJson = JsonParser.parseString(response.body())
                .getAsJsonObject()
                .get("games").getAsJsonArray();
        if (responseJson.isEmpty()) {
            gameDataCollection.add(new GameData(0,
                    null, null, null, null));
            return  gameDataCollection;
        }
        for (int i = 0; i < responseJson.size(); i++) {
            JsonObject gameData = responseJson.get(i).getAsJsonObject();
            int gameID = gameData.get("gameID").getAsInt();
            String whiteUsername =  (gameData.get("whiteUsername") == null) ? null : gameData.get("whiteUsername").getAsString();
            String blackUsername = (gameData.get("blackUsername") == null) ? null : gameData.get("blackUsername").getAsString();
            String gameName = gameData.get("gameName").getAsString();
            ChessGame chessGame = serializer.fromJson(gameData.get("game"), ChessGame.class);
            gameDataCollection.add(new GameData(
                    gameID,
                    whiteUsername,
                    blackUsername,
                    gameName,
                    chessGame));
        }
        return gameDataCollection;
    }

    public boolean join(String[] args) throws ConnectException {
        JsonObject json = new JsonObject();
        json.addProperty("playerColor", args[1]);
        json.addProperty("gameID", Integer.parseInt(args[2]));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(serverUrl + "/game"))
                .PUT(HttpRequest.BodyPublishers.ofString(json.toString()))
                .header("authorization", authToken)
                .build();
        HttpResponse<String> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        return response.statusCode() == 200;
    }

    public GameData observe(String[] args) throws ConnectException {
        Collection<GameData> chessGameCollection = list();
        for (GameData gameData : chessGameCollection) {
            if (gameData.gameID() == Integer.parseInt(args[1])) {
                return gameData;
            }
        }
        return null;
    }

    public boolean logout() throws ConnectException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .DELETE()
                .header("authorization", authToken)
                .build();
        HttpResponse<String> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        return response.statusCode() == 200;
    }
    public boolean clear() throws ConnectException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/db"))
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        return response.statusCode() == 200;
    }

}
