package service;

import dataaccess.DataAccessException;
import model.AuthData;
import model.requests.*;
import model.results.CreateResult;
import model.results.ListResult;
import model.results.LoginResult;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {
    private static AuthService authService;
    private static GameService gameService;
    private static UserService userService;
    private static String testUser;
    private static String testPassword;
    private static String testEmail;
    private static String testAuthToken;
    private static String testGameName;

    @BeforeAll
    public static void init() {
        authService = new AuthService();
        gameService = new GameService();
        userService = new UserService();
        testUser = "rikoot";
        testPassword = "kwobanmelele";
        testEmail = "rikoot@rikoot.com";
        testAuthToken = "diklokainikimlaplokamron";
        testGameName = "kukkure";
    }

    @BeforeEach
    public void setup() {
        authService.clearDb();
        gameService.clearDb();
        userService.clearDb();
    }

    // Auth Service Tests
    @Test
    @Order(1)
    @DisplayName("validateSession - Valid Session")
    public void validSessionSuccess() {
        AuthData createdAuthData;
        AuthData validatedAuthData;
        try {
            createdAuthData = authService.createSession(testUser);
        } catch (DataAccessException e) {
            return;
        }
        validatedAuthData = authService.validateSession(createdAuthData.authToken());
        Assertions.assertEquals(createdAuthData, validatedAuthData,
                "AuthData Objects don't Match");
    }

    @Test
    @Order(2)
    @DisplayName("validSession - Invalid Session")
    public void invalidSessionFailure(){
        for (int sessions = 0; sessions < 10; sessions++) {
            try {
                authService.createSession(testUser);
            } catch (DataAccessException e) {
                break;
            }
        }
        Assertions.assertNull(authService.validateSession(testAuthToken),
                "AuthData Object was returned");

    }

    @Test
    @Order(3)
    @DisplayName("createSession - New Session")
    public void createSessionSuccess() {
        AuthData createdAuthData;
        try {
            createdAuthData = authService.createSession(testUser);
        } catch (DataAccessException e) {
            return;
        }
        Assertions.assertNotNull(createdAuthData, "AuthData object was not returned");
        Assertions.assertEquals(testUser, createdAuthData.username(),
                "Usernames do not match");
    }

    @Test
    @Order(4)
    @DisplayName("deleteSession - Valid Session")
    public void deleteSessionSuccess() {
        AuthData createdAuthData;
        try {
            createdAuthData = authService.createSession(testUser);
        } catch (DataAccessException e) {
            return;
        }
        Assertions.assertDoesNotThrow(
                () -> {authService.deleteSession(createdAuthData.authToken());},
                "Delete Session threw an exception");
    }

    @Test
    @Order(5)
    @DisplayName("deleteSession - Invalid Session")
    public void deleteSessionFailure() {
        Assertions.assertThrows(DataAccessException.class,
                () -> {authService.deleteSession(testAuthToken);},
                "Delete Session threw an exception");
    }
    // Game Service Tests
    @Test
    @Order(6)
    @DisplayName("listGame - One Game")
    public void listGamesSuccess() {
        gameService.createGame(new CreateRequest(testGameName, testAuthToken));
        ListResult listResult = gameService.listGames(new ListRequest(testAuthToken));
        Assertions.assertNotNull(listResult,
                "No Games were returned");
    }

    @Test
    @Order(7)
    @DisplayName("createGame - New Game")
    public void createGameSuccess() {
        CreateResult createResult = gameService.createGame(new CreateRequest(testGameName, testAuthToken));
        Assertions.assertEquals(1, createResult.gameID(),
                "GameID is not 1");
    }

    @Test
    @Order(8)
    @DisplayName("joinGame - Join Valid Game")
    public void joinGameSuccess() {
        AuthData createdAuthData;
        try {
            createdAuthData = authService.createSession(testUser);
        } catch (DataAccessException e) {
            return;
        }
        CreateResult createResult = gameService.createGame(new CreateRequest(testGameName,
                createdAuthData.authToken()));
        Assertions.assertDoesNotThrow(
                () -> {gameService.joinGame(authService,
                        new JoinRequest("BLACK", createResult.gameID(), createdAuthData.authToken()));},
                "Error joining was thrown");
    }

    @Test
    @Order(9)
    @DisplayName("joinGame - Join Game with Color Taken")
    public void joinGameNameFailure() {
        AuthData createdAuthData;
        try {
            createdAuthData = authService.createSession(testUser);
        } catch (DataAccessException e) {
            return;
        }
        CreateResult createResult = gameService.createGame(new CreateRequest(testGameName,
                createdAuthData.authToken()));
        Assertions.assertDoesNotThrow(
                () -> {gameService.joinGame(authService,
                        new JoinRequest("BLACK", createResult.gameID(), createdAuthData.authToken()));},
                "Error joining thrown");
        Assertions.assertThrows(DataAccessException.class,
                () -> {gameService.joinGame(authService,
                        new JoinRequest("BLACK", createResult.gameID(), createdAuthData.authToken()));},
                "Error joining wasn't thrown");
    }

    @Test
    @Order(10)
    @DisplayName("joinGame - Join Invalid Game")
    public void joinGameIDFailure() {
        AuthData createdAuthData;
        try {
            createdAuthData = authService.createSession(testUser);
        } catch (DataAccessException e) {
            return;
        }
        Assertions.assertThrows(DataAccessException.class,
                () -> {gameService.joinGame(authService,
                        new JoinRequest("BLACK", 1, createdAuthData.authToken()));},
                "Error wasn't thrown");
    }

    // User Service Tests
    @Test
    @Order(11)
    @DisplayName("register - New User")
    public void registerSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            userService.register(authService,
                    new RegisterRequest(testUser, testPassword, testEmail));},
                "User wasn't created");
    }

    @Test
    @Order(12)
    @DisplayName("register - Existing User")
    public void registerFailure() {
        Assertions.assertDoesNotThrow(() -> {
            userService.register(authService,
                    new RegisterRequest(testUser, testPassword, testEmail));},
                "User wasn't created");
        Assertions.assertThrows(DataAccessException.class, () -> {
            userService.register(authService,
                    new RegisterRequest(testUser, testPassword, testEmail));},
                "User was incorrectly created");
    }

    @Test
    @Order(13)
    @DisplayName("login - Valid Login")
    public void loginSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            userService.register(authService,
                    new RegisterRequest(testUser, testPassword, testEmail));},
                "User wasn't created");
        Assertions.assertDoesNotThrow(() ->
                userService.login(authService, new LoginRequest(testUser, testPassword)),
                "User couldn't login");
    }

    @Test
    @Order(14)
    @DisplayName("login - Invalid Login")
    public void loginFailure() {
        Assertions.assertThrows(DataAccessException.class, () ->
                userService.login(authService, new LoginRequest(testUser, testPassword)),
                "Invalid User was able to login");
    }

    @Test
    @Order(13)
    @DisplayName("logout - Valid Session")
    public void logoutSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            userService.register(authService,
                    new RegisterRequest(testUser, testPassword, testEmail));},
                "User wasn't created");
        LoginResult loginResult;
        try {
            loginResult = userService.login(authService, new LoginRequest(testUser, testPassword));
        } catch (DataAccessException e) {
            return;
        }
        Assertions.assertDoesNotThrow(() ->
                userService.logout(authService, new LogoutRequest(loginResult.authToken())),
                "Session wasn't deleted");
    }

    @Test
    @Order(14)
    @DisplayName("logout - Invalid Session")
    public void logoutFailure() {
        Assertions.assertThrows(DataAccessException.class, () ->
                        userService.logout(authService, new LogoutRequest(testAuthToken)),
                "Session was deleted");
    }
}