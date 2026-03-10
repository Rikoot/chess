package client;

import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;
import server.Server;
import service.AuthService;
import service.GameService;
import service.UserService;


public class ServerFacadeTests {
    private static Server server;
    private static String testUser;
    private static String testPassword;
    private static String testEmail;
    private static String testAuthToken;
    private static String testGameName;

    @BeforeAll
    public static void init() {
        testUser = "rikoot";
        testPassword = "kwobanmelele";
        testEmail = "rikoot@rikoot.com";
        testAuthToken = "diklokainikimlaplokamron";
        testGameName = "kukkure";
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("Register - New user")
    public void registerTestSuccess() {

    }

    @Test
    @DisplayName("Register - User Already Taken")
    public void registerTestFailure() {

    }

    @Test
    @DisplayName("Login - Valid User")
    public void loginTestSuccess() {

    }

    @Test
    @DisplayName("Login - Invalid User")
    public void loginTestFailure() {

    }

    @Test
    @DisplayName("Create - New Game")
    public void createTestSuccess() {

    }

    @Test
    @DisplayName("Create - Blank Name")
    public void createTestFailure() {

    }

    @Test
    @DisplayName("List - One Game")
    public void listTestSuccess() {

    }

    @Test
    @DisplayName("List - Invalid Auth Token")
    public void listTestFailure() {

    }

    @Test
    @DisplayName("Join - Valid GameID")
    public void joinTestSuccess() {

    }

    @Test
    @DisplayName("Join - Invalid GameID")
    public void joinTestFailure() {

    }

    @Test
    @DisplayName("Observe - Valid GameID")
    public void observeTestSuccess() {

    }

    @Test
    @DisplayName("Observe - Invalid GameID")
    public void observeTestFailure() {

    }

    @Test
    @DisplayName("Logout - Valid GameID")
    public void logoutTestSuccess() {

    }

    @Test
    @DisplayName("Logout - Invalid GameID")
    public void logoutTestFailure() {

    }
}
