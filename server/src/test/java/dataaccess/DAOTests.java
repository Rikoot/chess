package dataaccess;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Collection;

public class DAOTests {
    private static AuthSQLDAO authSQLDAO;
    private static GameSQLDAO gameSQLDAO;
    private static UserSQLDAO userSQLDAO;
    private static String testUser;
    private static String testPassword;
    private static String testEmail;
    private static String testAuthToken;
    private static String testGameName;

    @BeforeAll
    public static void start() {
        try {
            authSQLDAO = new AuthSQLDAO();
            gameSQLDAO = new GameSQLDAO();
            userSQLDAO = new UserSQLDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        testUser = "rikoot";
        testPassword = "kwobanmelele";
        testEmail = "rikoot@rikoot.com";
        testAuthToken = "diklokainikimlaplokamron";
        testGameName = "kukkure";
    }

    @BeforeEach
    public void setup() {
        try {
            authSQLDAO.clearDb();
            gameSQLDAO.clearDb();
            userSQLDAO.clearDb();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // Auth DAO Tests
    @Test
    @Order(1)
    @DisplayName("getAuth - Valid Session")
    public void getAuthSuccess() {
        AuthData createdAuthData = new AuthData(testAuthToken, testUser);
        AuthData validatedAuthData;
        try {
            authSQLDAO.createAuth(createdAuthData);
        } catch (DataAccessException e) {
            return;
        }
        validatedAuthData = authSQLDAO.getAuth(createdAuthData.authToken());
        Assertions.assertEquals(createdAuthData, validatedAuthData,
                "AuthData Objects don't Match");
    }

    @Test
    @Order(2)
    @DisplayName("getAuth - Invalid Session")
    public void getAuthFailure(){
        Assertions.assertNull(authSQLDAO.getAuth(testAuthToken),
                "AuthData Object was returned");
    }

    @Test
    @Order(3)
    @DisplayName("createAuth - New Session")
    public void createAuthSuccess() {
        AuthData createdAuthData = new AuthData(testAuthToken, testUser);
        Assertions.assertDoesNotThrow(() ->
            {authSQLDAO.createAuth(createdAuthData);});
    }

    @Test
    @Order(4)
    @DisplayName("createAuth - Invalid Session")
    public void createAuthFailure() {
        AuthData createdAuthData = new AuthData(null, null);
        Assertions.assertThrows(DataAccessException.class, () -> {
           authSQLDAO.createAuth(createdAuthData);
        });
    }

    @Test
    @Order(5)
    @DisplayName("deleteAuth - Valid Session")
    public void deleteAuthSuccess() {
        AuthData createdAuthData = new AuthData(testAuthToken, testUser);
        try {
            authSQLDAO.createAuth(createdAuthData);
        } catch (DataAccessException e) {
            return;
        }
        Assertions.assertDoesNotThrow(
                () -> {authSQLDAO.deleteAuth(createdAuthData);},
                "Delete Session threw an exception");
    }

    @Test
    @Order(6)
    @DisplayName("deleteAuth - Invalid Session")
    public void deleteAuthFailure() {
        Assertions.assertThrows(DataAccessException.class,
                () -> {authSQLDAO.deleteAuth(authSQLDAO.getAuth(testAuthToken));},
                "Delete Session did not throw an exception");
    }

    // Game DAO Tests
    @Test
    @Order(7)
    @DisplayName("getGames - One Game")
    public void getGamesSuccess() {
        try {
            gameSQLDAO.createGame(testGameName);
        } catch (DataAccessException e) {
            Assertions.assertNull(e, "Create Game threw an Error");
        }
        Collection<GameData> gameData = null;
        try {
            gameData = gameSQLDAO.getGames();
        } catch (DataAccessException e) {
            Assertions.assertNotNull(e, "Get Games threw an error");
        }
        Assertions.assertNotNull(gameData);
    }

    @Test
    @Order(8)
    @DisplayName("createGame - New Game")
    public void createGameSuccess() {
        int gameID = 0;
        try {
            gameID = gameSQLDAO.createGame(testGameName);
        } catch (DataAccessException e) {
            Assertions.assertNull(e, "Create Game threw an Error");
        }
        Assertions.assertEquals(1, gameID,
                "GameID is not 1");
    }

    @Test
    @Order(9)
    @DisplayName("joinGame - Join Valid Game")
    public void joinGameSuccess() {
        AuthData createdAuthData = new AuthData(testAuthToken, testUser);
        Assertions.assertDoesNotThrow(() ->
            {authSQLDAO.createAuth(createdAuthData);});
        int gameID = 0;
        try {
            gameID = gameSQLDAO.createGame(testGameName);
        } catch (DataAccessException e) {
            Assertions.assertNull(e, "Create Game threw an Error");
        }
        int finalGameID = gameID;
        Assertions.assertDoesNotThrow(() ->
            {gameSQLDAO.joinGame("BLACK", finalGameID, testUser);});
    }

    @Test
    @Order(10)
    @DisplayName("joinGame - Join Game with Color Taken")
    public void joinGameNameFailure() {
        AuthData createdAuthData = new AuthData(testAuthToken, testUser);
        Assertions.assertDoesNotThrow(() ->
            {authSQLDAO.createAuth(createdAuthData);});
        int gameID = 0;
        try {
            gameID = gameSQLDAO.createGame(testGameName);
        } catch (DataAccessException e) {
            Assertions.assertNull(e, "Create Game threw an Error");
        }
        int finalGameID = gameID;
        Assertions.assertDoesNotThrow(() ->
            {gameSQLDAO.joinGame("BLACK", finalGameID, testUser);});
        Assertions.assertThrows(DataAccessException.class, () ->
            {gameSQLDAO.joinGame("BLACK", finalGameID, testUser);},
                "Error wasn't thrown.");
    }

    @Test
    @Order(11)
    @DisplayName("joinGame - Join Invalid Game")
    public void joinGameIDFailure() {
        Assertions.assertThrows(DataAccessException.class, () ->
            {gameSQLDAO.joinGame("BLACK", 1, testUser);},
                "Error wasn't thrown");
    }

    // User Service Tests
    @Test
    @Order(12)
    @DisplayName("createUser - New User")
    public void createUserSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            userSQLDAO.createUser(testUser, testPassword, testEmail);
        }, "User wasn't created");
    }

    @Test
    @Order(13)
    @DisplayName("createUser - Existing User")
    public void createUserFailure() {
        Assertions.assertDoesNotThrow(() -> {
            userSQLDAO.createUser(testUser, testPassword, testEmail);
        }, "User wasn't created");
        Assertions.assertThrows(DataAccessException.class, () -> {
            userSQLDAO.createUser(testUser, testPassword, testEmail);
        }, "User was incorrectly created");
    }

    @Test
    @Order(14)
    @DisplayName("getUser - Valid User Returned")
    public void getUserSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            userSQLDAO.createUser(testUser, testPassword, testEmail);
        }, "User wasn't created");
        Assertions.assertDoesNotThrow(() -> {
            userSQLDAO.getUser(testUser);
        });
    }

    @Test
    @Order(15)
    @DisplayName("getUser - Valid User not returned")
    public void getUserFailure() {
        Assertions.assertNull(userSQLDAO.getUser(testUser));
    }

    // Clear DB tests
    @Test
    @Order(16)
    @DisplayName("Auth ClearDB Test")
    public void clearAuthDbSuccess() {
        Assertions.assertDoesNotThrow(() -> {authSQLDAO.clearDb();});
    }

    @Test
    @Order(17)
    @DisplayName("Game ClearDB Test")
    public void clearGameDbSuccess() {
        Assertions.assertDoesNotThrow(() -> {gameSQLDAO.clearDb();});
    }

    @Test
    @Order(18)
    @DisplayName("User ClearDB Test")
    public void clearUserDbSuccess() {
        Assertions.assertDoesNotThrow(() -> {userSQLDAO.clearDb();});
    }
}

