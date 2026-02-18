package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class GameDAO {
    Collection<GameData> gameDataCollection;
    int gameIDCount;
    public GameDAO() {
        gameDataCollection = new HashSet<>();
        gameIDCount = 1;
    }
    public Collection<GameData> getGames() {
        return gameDataCollection;
    }

    public int createGame(String gameName) {
        int gameID = gameIDCount++;
        GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        gameDataCollection.add(newGame);
        return gameID;
    }

    public void joinGame(String playerColor, int gameID, String username) throws DataAccessException {
        GameData gameData = null;
        for (GameData game : gameDataCollection) {
            if (Objects.equals(game.gameID(), gameID)) {
                gameData = game;
                break;
            }
        }
        if (gameData == null) {
            throw new DataAccessException("Error: Game ID Not Valid");
        }
        gameDataCollection.remove(gameData);
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
        gameDataCollection.add(gameData);
    }
    public void clearDb() {
        gameDataCollection = new HashSet<>();
        gameIDCount = 1;
    }
}
