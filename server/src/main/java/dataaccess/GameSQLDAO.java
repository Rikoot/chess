package dataaccess;

import chess.ChessGameDeserializer;
import chess.ChessGame;
import com.google.gson.GsonBuilder;
import model.GameData;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class GameSQLDAO {
    private int gameIDCount;
    private final Gson serializer;

    public GameSQLDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGameDeserializer());
        serializer = gsonBuilder.create();
        gameIDCount = 1;
        this.createDb();
    }
    public Collection<GameData> getGames() {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        Collection<GameData> gameDataCollection = new HashSet<>();
        String statement = "SELECT * FROM Games;";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                gameDataCollection.add(new GameData(resultSet.getInt("gameID"),
                        resultSet.getString("whiteUsername"),
                        resultSet.getString("blackUsername"),
                        resultSet.getString("gameName"),
                        serializer.fromJson(resultSet.getString("game"), ChessGame.class)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return gameDataCollection;
    }

    public int createGame(String gameName) throws DataAccessException{
        int gameID = gameIDCount++;
        ChessGame newGame = new ChessGame();
        String statement = "INSERT INTO Games VALUES (?, ?, ?, ?, ?);";
        Connection conn = null;
        conn = DatabaseManager.getConnection();
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setInt(1, gameID);
            preparedStatement.setString(2, null);
            preparedStatement.setString(3, null);
            preparedStatement.setString(4, gameName);
            preparedStatement.setString(5, serializer.toJson(newGame));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: Couldn't create Game");
        }
        return gameID;
    }

    public void joinGame(String playerColor, int gameID, String username) throws DataAccessException {
        // get the game and check if it's taken, if not, update it
        String statement = "SELECT * FROM Games WHERE gameID = ?";
        GameData gameData = null;
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new RuntimeException();
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setInt(1, gameID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new DataAccessException("Error: Game ID Not Valid");
            }
            gameData = new GameData(resultSet.getInt("gameID"),
                    resultSet.getString("whiteUsername"),
                    resultSet.getString("blackUsername"),
                    resultSet.getString("gameName"),
                    serializer.fromJson(resultSet.getString("game"), ChessGame.class));
            if (Objects.equals(playerColor, "WHITE")) {
                if (gameData.whiteUsername() == null) {
                    gameData = gameData.setWhiteUsername(username);
                } else {
                    throw new DataAccessException("Error: Color Already Taken");
                }
            } else if (Objects.equals(playerColor, "BLACK")) {
                if (gameData.blackUsername() == null) {
                    gameData = gameData.setBlackUsername(username);
                } else {
                    throw new DataAccessException("Error: Color Already Taken");
                }
            }
            statement = "REPLACE INTO Games VALUES (?, ?, ?, ?, ?);";
            preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setInt(1, gameData.gameID());
            preparedStatement.setString(2, gameData.whiteUsername());
            preparedStatement.setString(3, gameData.blackUsername());
            preparedStatement.setString(4, gameData.gameName());
            preparedStatement.setString(5, serializer.toJson(gameData.game()));
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearDb() throws DataAccessException {
        gameIDCount = 1;
        String statement = "DROP TABLE IF EXISTS Games;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: Internal Error");
        }
        createDb();
    }
    private void createDb() throws DataAccessException {
        String statement = """
CREATE TABLE IF NOT EXISTS Games (
    gameID INT NOT NULL,
    whiteUsername VARCHAR(255),
    blackUsername VARCHAR(255),
    gameName VARCHAR(255) NOT NULL,
    game longtext NOT NULL,
    PRIMARY KEY (gameID)
);
""";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: Internal Error");
        }
    }
}
