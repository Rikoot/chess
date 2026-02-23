package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class GameSQLDAO {
    private int gameIDCount;

    public GameSQLDAO() {
        try  {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        this.clearDb();
    }
    public Collection<GameData> getGames() {
        Collection<GameData> gameDataCollection = new HashSet<>();

        return gameDataCollection;
    }

    public int createGame(String gameName) {
        int gameID = gameIDCount++;
        GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());

        return gameID;
    }

    public void joinGame(String playerColor, int gameID, String username) throws DataAccessException {

    }

    public void clearDb() {
        gameIDCount = 1;
        String statement = """
DROP TABLE IF EXISTS Games;
CREATE TABLE Games (
    gameID INT NOT NULL,
    whiteUsername VARCHAR(255),
    blackUsername VARCHAR(255),
    gameName VARCHAR(255) NOT NULL,
    game JSON NOT NULL,
    PRIMARY KEY (gameID)
);
""";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (DataAccessException e) {

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
