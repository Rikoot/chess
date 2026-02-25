package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import model.ErrorData;
import model.requests.*;
import model.results.CreateResult;
import model.results.ListResult;
import model.results.LoginResult;
import model.results.RegisterResult;
import service.AuthService;
import service.GameService;
import service.UserService;

import java.util.Objects;

public class Server {

    private final Javalin javalin;
    UserService userService;
    AuthService authService;
    GameService gameService;
    Gson serializer = new Gson();
    public Server() {
        try {
            userService = new UserService();
            authService = new AuthService();
            gameService = new GameService();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        // POST /user Registration
        javalin.post("/user", this::handleRegister);
        // POST /session Login
        javalin.post("/session", this::handleLogin);
        // DELETE /session Logout
        javalin.delete("/session", this::handleLogout);
        // GET /game List Games
        javalin.get("/game", this::handleListGame);
        // POST /game Create Game
        javalin.post("/game", this::handleCreateGame);
        // PUT /game Join Game
        javalin.put("/game", this::handleJoinGame);
        // DELETE /db Clear DB
        javalin.delete("/db", this::handleClearDb);
        // Error handler
        javalin.exception(RuntimeException.class, (e,ctx) -> {
            ErrorData errorData = new ErrorData("Error: Internal Error");
            ctx.status(500).json(serializer.toJson(errorData));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private boolean checkAuthToken(Context ctx) {
        if (authService.validateSession(ctx.header("authorization")) == null) {
            ErrorData errorData = new ErrorData("Error: Unauthorized");
            ctx.status(401).json(serializer.toJson(errorData));
            return true;
        }
        return false;
    }
    private void handleRegister(Context ctx) {
        RegisterRequest registerRequest = serializer.fromJson(ctx.body(), RegisterRequest.class);
        if (registerRequest.username() == null
                || registerRequest.password() == null
                || registerRequest.email() == null) {
            ErrorData errorData = new ErrorData("Error: Bad Request");
            ctx.status(400).json(serializer.toJson(errorData));
            return;
        }
        RegisterResult registerResult = null;
        try {
           registerResult = userService.register(authService, registerRequest);
           ctx.status(200).json(serializer.toJson(registerResult));
        } catch (DataAccessException dataAccessException) {
            ErrorData errorData = new ErrorData("Error: Already Taken");
            ctx.status(403).json(serializer.toJson(errorData));
        }
    }
    private void handleLogin(Context ctx) {
        LoginRequest loginRequest = serializer.fromJson(ctx.body(), LoginRequest.class);
        if (loginRequest.username() == null
                || loginRequest.password() == null) {
            ErrorData errorData = new ErrorData("Error: Bad Request");
            ctx.status(400).json(serializer.toJson(errorData));
            return;
        }
        LoginResult loginResult = null;
        try {
            loginResult = userService.login(authService, loginRequest);
            ctx.status(200).json(serializer.toJson(loginResult));
        } catch (DataAccessException dataAccessException) {
            ErrorData errorData = new ErrorData("Error: Wrong Username or Password");
            ctx.status(401).json(serializer.toJson(errorData));
        }
    }
    private void handleLogout(Context ctx) {
        LogoutRequest logoutRequest = new LogoutRequest(ctx.header("authorization"));
        if (logoutRequest.authToken() == null) {
            ErrorData errorData = new ErrorData("Error: Bad Request");
            ctx.status(400).json(serializer.toJson(errorData));
            return;
        }
        if (checkAuthToken(ctx)) {
            return;
        }
        try {
            userService.logout(authService, logoutRequest);
            ctx.status(200);
        } catch (DataAccessException dataAccessException) {
            ErrorData errorData = new ErrorData("Error: Unknown");
            ctx.status(401).json(serializer.toJson(errorData));
        }

    }
    private void handleListGame(Context ctx) {
        ListRequest listRequest = new ListRequest(ctx.header("authorization"));
        if (listRequest.authToken() == null) {
            ErrorData errorData = new ErrorData("Error: Bad Request");
            ctx.status(400).json(serializer.toJson(errorData));
            return;
        }
        if (checkAuthToken(ctx)) {
            return;
        }
        ListResult listResult = null;
        try {
            listResult = gameService.listGames(listRequest);
        } catch (DataAccessException e) {
            ErrorData errorData = new ErrorData("Error: Internal Error");
            ctx.status(500).json(serializer.toJson(errorData));
        }

        ctx.status(200).json(serializer.toJson(listResult));

    }
    private void handleCreateGame(Context ctx) {
        CreateRequest createRequest = serializer.fromJson(ctx.body(), CreateRequest.class);
        if (createRequest.gameName() == null) {
            ErrorData errorData = new ErrorData("Error: Bad Request");
            ctx.status(400).json(serializer.toJson(errorData));
            return;
        }
        if (checkAuthToken(ctx)) {
            return;
        }
        CreateResult createResult = null;
        try {
            createResult = gameService.createGame(createRequest);
        } catch (DataAccessException e) {
            ErrorData errorData = new ErrorData("Error: Internal Error");
            ctx.status(500).json(serializer.toJson(errorData));
        }
        ctx.status(200).json(serializer.toJson(createResult));
    }
    private void handleJoinGame(Context ctx) {
        if (checkAuthToken(ctx)) {
            return;
        }
        JoinRequest joinRequest = serializer.fromJson(ctx.body(), JoinRequest.class);
        if ((joinRequest.gameID() == 0) || (joinRequest.playerColor() == null) || ((!Objects.equals(joinRequest.playerColor(), "WHITE"))
                && !Objects.equals(joinRequest.playerColor(), "BLACK"))) {
            ErrorData errorData = new ErrorData("Error: Bad Request");
            ctx.status(400).json(serializer.toJson(errorData));
            return;
        }
        joinRequest = joinRequest.addAuthToken(ctx.header("authorization"));
        try {
            gameService.joinGame(authService, joinRequest);
            ctx.status(200);
        } catch (DataAccessException dataAccessException) {
            ErrorData errorData = new ErrorData(dataAccessException.getMessage());
            ctx.status(403).json(serializer.toJson(errorData));
        }

    }
    private void handleClearDb(Context ctx) {
        try {
            authService.clearDb();
            userService.clearDb();
            gameService.clearDb();
        } catch (DataAccessException dataAccessException) {
            ErrorData errorData = new ErrorData("Error: Internal Error");
            ctx.status(500).json(serializer.toJson(errorData));
        }


    }
}
