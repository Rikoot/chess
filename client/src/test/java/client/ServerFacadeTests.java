package client;

import org.junit.jupiter.api.*;
import server.Server;

public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade serverFacade;
    private static String testUser;
    private static String testPassword;
    private static String testEmail;
    private static String testAuthToken;
    private static String testGameName;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
        testUser = "rikoot";
        testPassword = "kwobanmelele";
        testEmail = "rikoot@rikoot.com";
        testAuthToken = "diklokainikimlaplokamron";
        testGameName = "kukkure";
        Assertions.assertTrue(serverFacade.clear());
    }

    @AfterAll
    static void stopServer() {
        Assertions.assertTrue(serverFacade.clear());
        server.stop();
    }

    @Test
    @DisplayName("Register - New user")
    public void registerTestSuccess() {
        String[] testArgs = {"register", testUser, testPassword, testEmail};
        Assertions.assertTrue(serverFacade.register(testArgs));
    }

    @Test
    @DisplayName("Register - User Already Taken")
    public void registerTestFailure() {
        String[] testArgs = {"register", testUser, testPassword, testEmail};
        Assertions.assertFalse(serverFacade.register(testArgs));
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
