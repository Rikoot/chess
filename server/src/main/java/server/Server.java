package server;

import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import model.ErrorData;
import model.Requests.*;
import model.Results.CreateResult;
import model.Results.ListResult;
import model.Results.LoginResult;
import model.Results.RegisterResult;
import model.UserData;
import service.AuthService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;
    UserService userService = new UserService();
    AuthService authService = new AuthService();
    GameService gameService = new GameService();
    public Server() {
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
        javalin.post("/post", this::handleCreateGame);
        // PUT /game Join Game
        javalin.put("/game", this::handleJoinGame);
        // DELETE /db Clear DB
        javalin.delete("/db", this::handleClearDb);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void checkAuthToken(Context ctx) {
        if (!authService.validateSession(ctx.header("authorization"))) {
            ErrorData errorData = new ErrorData("Error: unauthorized");
            ctx.status(401).json(errorData);
        }
    }
    private void handleRegister(Context ctx) {
        try {

        } catch (HttpResponseException httpResponseException) {
            ErrorData errorData = new ErrorData("Error: bad request");
            ctx.status(400).json(errorData);
            return;
        }
        RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);
        RegisterResult registerResult;
        try {
           registerResult = userService.register(authService, registerRequest);
        } catch (DataAccessException dataAccessException) {
            ErrorData errorData = new ErrorData("Error Already Taken");
            ctx.status(403).json(errorData);
        }
    }
    private void handleLogin(Context ctx) {
        LoginRequest loginRequest = null;
        try {
            loginRequest = ctx.bodyAsClass(LoginRequest.class);
        } catch (HttpResponseException httpResponseException) {
            ErrorData errorData = new ErrorData("Error: bad request");
            ctx.status(400).json(errorData);
            return;
        }
        LoginResult loginResult = null;
        try {
            loginResult = userService.login(authService, loginRequest);
        } catch (DataAccessException dataAccessException) {
            ErrorData errorData = new ErrorData("Wrong Username or Password");
            ctx.status(401).json(errorData);
        }
    }
    private void handleLogout(Context ctx) {
        LogoutRequest logoutRequest = null;
        try {
            logoutRequest = ctx.bodyAsClass(LogoutRequest.class);
        } catch (HttpResponseException httpResponseException) {
            ErrorData errorData = new ErrorData("Error: bad request");
            ctx.status(400).json(errorData);
        }
        checkAuthToken(ctx);
        try {
            userService.logout(authService, logoutRequest);
        } catch (DataAccessException dataAccessException) {

        }

    }
    private void handleListGame(Context ctx) {
        ListRequest listRequest = null;
        try {
            listRequest = ctx.bodyAsClass(ListRequest.class);
        } catch (HttpResponseException httpResponseException) {
            ErrorData errorData = new ErrorData("Error: bad request");
            ctx.status(400).json(errorData);
        }
        checkAuthToken(ctx);
        ListResult listResult = gameService.listGames(listRequest);
    }
    private void handleCreateGame(Context ctx) {
        CreateRequest createRequest = null;
        try {
            createRequest = ctx.bodyAsClass(CreateRequest.class);
        } catch (HttpResponseException httpResponseException) {
            ErrorData errorData = new ErrorData("Error: bad request");
            ctx.status(400).json(errorData);
        }
        checkAuthToken(ctx);
        CreateResult createResult = gameService.createGame(createRequest);
    }
    private void handleJoinGame(Context ctx) {
        JoinRequest joinRequest = null;
        try {
            joinRequest = ctx.bodyAsClass(JoinRequest.class);
        } catch (HttpResponseException httpResponseException) {
            ErrorData errorData = new ErrorData("Error: bad request");
            ctx.status(400).json(errorData);
        }
        checkAuthToken(ctx);
        gameService.joinGame(joinRequest);
    }
    private void handleClearDb(Context ctx) {
        authService.clearDb();
        userService.clearDb();
    }
}
