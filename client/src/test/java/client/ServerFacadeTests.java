package client;

import org.junit.jupiter.api.*;
import server.Server;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade serverFacade;
    private static String testUser;
    private static String testPassword;
    private static String testEmail;
    private static String testAuthToken;
    private static String testGameName;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
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
        //Assertions.assertTrue(serverFacade.clear());
        server.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Register - New user")
    public void registerTestSuccess() {
        String[] testArgs = {"register", testUser, testPassword, testEmail};
        Assertions.assertTrue(serverFacade.register(testArgs));
    }

    @Test
    @Order(2)
    @DisplayName("Register - User Already Taken")
    public void registerTestFailure() {
        String[] testArgs = {"register", testUser, testPassword, testEmail};
        Assertions.assertFalse(serverFacade.register(testArgs));
    }

    @Test
    @Order(3)
    @DisplayName("Login - Valid User")
    public void loginTestSuccess() {
        String[] testArgs = {"login", testUser, testPassword};
        Assertions.assertTrue(serverFacade.login(testArgs));
    }

    @Test
    @Order(4)
    @DisplayName("Login - Invalid Password")
    public void loginTestFailure() {
        String[] testArgs = {"login", testUser, testAuthToken};
        Assertions.assertFalse(serverFacade.login(testArgs));
    }

    @Test
    @Order(5)
    @DisplayName("Create - New Game")
    public void createTestSuccess() {
        String[] testArgs = {"create", testGameName};
        Assertions.assertNotEquals(0, serverFacade.create(testArgs));
    }

    @Test
    @Order(6)
    @DisplayName("Create - Blank Name")
    public void createTestFailure() {
        String[] testArgs = {"create", null};
        Assertions.assertEquals(0, serverFacade.create(testArgs));
    }

    @Test
    @Order(7)
    @DisplayName("List - One Game")
    public void listTestSuccess() {
        Assertions.assertNotNull(serverFacade.list());
    }

    @Test
    @Order(8)
    @DisplayName("List - User not logged in")
    public void listTestFailure() {
        ServerFacade tempServerFacade = new ServerFacade("http://localhost:" + port);
        Assertions.assertNull(tempServerFacade.list());
    }

    @Test
    @Order(9)
    @DisplayName("Join - Valid GameID")
    public void joinTestSuccess() {
        String[] testArgs = {"join", "BLACK", "1"};
        Assertions.assertTrue(serverFacade.join(testArgs));
    }

    @Test
    @Order(10)
    @DisplayName("Join - Invalid GameID")
    public void joinTestFailure() {
        String[] testArgs = {"join", "BLACK", "0"};
        Assertions.assertFalse(serverFacade.join(testArgs));
    }

    @Test
    @Order(11)
    @DisplayName("Observe - Valid GameID")
    public void observeTestSuccess() {
        String[] testArgs = {"observe", "1"};
        Assertions.assertNotNull(serverFacade.observe(testArgs));
    }

    @Test
    @Order(12)
    @DisplayName("Observe - Invalid GameID")
    public void observeTestFailure() {
        String[] testArgs = {"observe", "0"};
        Assertions.assertNull(serverFacade.observe(testArgs));
    }

    @Test
    @Order(13)
    @DisplayName("Logout - Valid Session")
    public void logoutTestSuccess() {
        Assertions.assertTrue(serverFacade.logout());
    }

    @Test
    @Order(14)
    @DisplayName("Logout - Invalid Session")
    public void logoutTestFailure() {
        Assertions.assertFalse(serverFacade.logout());
    }
}
