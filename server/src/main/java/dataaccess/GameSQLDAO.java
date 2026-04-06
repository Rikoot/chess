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

    public Collection<GameData> getGames() throws DataAccessException{
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
            throw new DataAccessException("Error: Internal Error");
        }
        return gameDataCollection;
    }

    public GameData getGame(int gameID) throws DataAccessException{
        String statement = "SELECT * FROM Games WHERE gameID = ?";
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
            return new GameData(resultSet.getInt("gameID"),
                    resultSet.getString("whiteUsername"),
                    resultSet.getString("blackUsername"),
                    resultSet.getString("gameName"),
                    serializer.fromJson(resultSet.getString("game"), ChessGame.class));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int createGame(String gameName) throws DataAccessException{
        ChessGame newGame = new ChessGame();
        int gameID = 0;
        String statement = "INSERT INTO Games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?);";
        Connection conn = null;
        conn = DatabaseManager.getConnection();
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, null);
            preparedStatement.setString(2, null);
            preparedStatement.setString(3, gameName);
            preparedStatement.setString(4, serializer.toJson(newGame));
            preparedStatement.executeUpdate();
            ResultSet resultSet = conn.prepareStatement("SELECT LAST_INSERT_ID() AS gameID;").executeQuery();
            if (!resultSet.next()) {
                throw new DataAccessException("Error: Game ID Not Valid");
            }
            gameID = resultSet.getInt("gameID");
            
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
            if (!updateGame(gameData)) {
                throw new SQLException("Error occurred");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateGame(GameData game) {
        Connection conn = null;
        String statement = "REPLACE INTO Games VALUES (?, ?, ?, ?, ?);";
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            return false;
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setInt(1, game.gameID());
            preparedStatement.setString(2, game.whiteUsername());
            preparedStatement.setString(3, game.blackUsername());
            preparedStatement.setString(4, game.gameName());
            preparedStatement.setString(5, serializer.toJson(game.game()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        return true;
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
    gameID INT AUTO_INCREMENT NOT NULL,
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
