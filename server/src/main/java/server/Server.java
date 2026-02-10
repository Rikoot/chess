package server;

import io.javalin.*;
import io.javalin.http.Context;
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
        // POST /session Registration
        javalin.post("/session", )
        // POST /session Login

        // DELETE /session Logout

        // GET /game List Games

        // POST /game Create Game

        // PUT /game Join Game

        // DELETE /db Clear DB
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void handleRegister(Context ctx) {
        RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);
        RegisterResult registerResult = userService.register(authService, registerRequest);

    }
    private void handleLogin(Context ctx) {
        LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
        LoginResult loginResult = userService.login(authService, loginRequest);
    }
    private void handleLogout(Context ctx) {
        LogoutRequest logoutRequest = ctx.bodyAsClass(LogoutRequest.class);
        userService.logout(authService, logoutRequest);
    }
    private void handleListGame(Context ctx) {
        ListRequest listRequest = ctx.bodyAsClass(ListRequest.class);
        ListResult listResult = gameService.listGames(listRequest);
    }
    private void handleCreateGame(Context ctx) {
        CreateRequest createRequest = ctx.bodyAsClass(CreateRequest.class);
        CreateResult createResult = gameService.createGame(createRequest);
    }
    private void handleJoinGame(Context ctx) {
        JoinRequest joinRequest = ctx.bodyAsClass(JoinRequest.class);
        gameService.joinGame(joinRequest);
    }
    private void handleClearDb(Context ctx) {
        authService.clearDb();
        userService.clearDb();
    }
}
