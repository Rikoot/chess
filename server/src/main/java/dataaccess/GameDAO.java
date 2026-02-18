package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class GameDAO {
    Collection<GameData> gameDataCollection;
    public GameDAO() {
        gameDataCollection = new HashSet<>();
    }
    public Collection<GameData> getGames(String username) {
        Collection<GameData> gameData = new HashSet<>();
        for (GameData game : gameDataCollection) {
            if (game.blackUsername().equals(username)
                    || game.whiteUsername().equals(username)) {
                gameData.add(game);
            }
        }
        return gameData;
    }

    public String createGame(String gameName) {
        String gameID = UUID.randomUUID().toString();
        GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        gameDataCollection.add(newGame);
        return gameID;
    }

    public void joinGame(String playerColor, String gameID, String username) throws DataAccessException {
        GameData gameData = null;
        for (GameData game : gameDataCollection) {
            if (Objects.equals(game.gameID(), gameID)) {
                gameData = game;
            }
        }
        if (gameData == null) {
            throw new DataAccessException("Game ID not valid");
        }
        gameDataCollection.remove(gameData);
        if (Objects.equals(playerColor, "WHITE")) {
            if (gameData.whiteUsername() == null) {
                gameData = gameData.setWhiteUsername(username);
            } else {
                throw new DataAccessException("Color already Taken");
            }
        } else if (Objects.equals(playerColor, "BLACK")) {
            if (gameData.blackUsername() == null) {
                gameData = gameData.setBlackUsername(username);
            } else {
                throw new DataAccessException("Color already Taken");
            }

        } else {
            throw  new DataAccessException("playerColor not valid");
        }
    }
    public void clearDb() {
        gameDataCollection = new HashSet<>();
    }
}
