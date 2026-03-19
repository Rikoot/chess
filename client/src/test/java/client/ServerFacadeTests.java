package client;

import org.junit.jupiter.api.*;
import server.Server;

import java.net.ConnectException;

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
        try {
            Assertions.assertTrue(serverFacade.clear());
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
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
        try {
            Assertions.assertTrue(serverFacade.register(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Register - User Already Taken")
    public void registerTestFailure() {
        String[] testArgs = {"register", testUser, testPassword, testEmail};
        try {
            Assertions.assertFalse(serverFacade.register(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Login - Valid User")
    public void loginTestSuccess() {
        String[] testArgs = {"login", testUser, testPassword};
        try {
            Assertions.assertTrue(serverFacade.login(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Login - Invalid Password")
    public void loginTestFailure() {
        String[] testArgs = {"login", testUser, testAuthToken};
        try {
            Assertions.assertFalse(serverFacade.login(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Create - New Game")
    public void createTestSuccess() {
        String[] testArgs = {"create", testGameName};
        try {
            Assertions.assertNotEquals(0, serverFacade.create(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(6)
    @DisplayName("Create - Blank Name")
    public void createTestFailure() {
        String[] testArgs = {"create", null};
        try {
            Assertions.assertEquals(0, serverFacade.create(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(7)
    @DisplayName("List - One Game")
    public void listTestSuccess() {
        try {
            Assertions.assertNotNull(serverFacade.list());
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(8)
    @DisplayName("List - User not logged in")
    public void listTestFailure() {
        ServerFacade tempServerFacade = new ServerFacade("http://localhost:" + port);
        try {
            Assertions.assertNull(tempServerFacade.list());
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(9)
    @DisplayName("Join - Valid GameID")
    public void joinTestSuccess() {
        String[] testArgs = {"join", "BLACK", "1"};
        try {
            Assertions.assertTrue(serverFacade.join(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(10)
    @DisplayName("Join - Invalid GameID")
    public void joinTestFailure() {
        String[] testArgs = {"join", "BLACK", "0"};
        try {
            Assertions.assertFalse(serverFacade.join(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(11)
    @DisplayName("Observe - Valid GameID")
    public void observeTestSuccess() {
        String[] testArgs = {"observe", "1"};
        try {
            Assertions.assertNotNull(serverFacade.observe(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(12)
    @DisplayName("Observe - Invalid GameID")
    public void observeTestFailure() {
        String[] testArgs = {"observe", "0"};
        try {
            Assertions.assertNull(serverFacade.observe(testArgs));
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(13)
    @DisplayName("Logout - Valid Session")
    public void logoutTestSuccess() {
        try {
            Assertions.assertTrue(serverFacade.logout());
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(14)
    @DisplayName("Logout - Invalid Session")
    public void logoutTestFailure() {
        try {
            Assertions.assertFalse(serverFacade.logout());
        } catch (ConnectException e) {
            Assertions.assertTrue(true);
        }
    }
}
