package model;

import chess.ChessGame;

public record GameData(String gameID,
                       String whiteUsername,
                       String blackUsername,
                       String gameName,
                       ChessGame game) {
    public GameData setWhiteUsername(String username) {
        return new GameData(gameID, username, blackUsername, gameName, game);
    }
    public GameData setBlackUsername(String username) {
        return new GameData(gameID, whiteUsername, username, gameName, game);
    }
    public GameData updateGame(ChessGame updatedGame) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, updatedGame);
    }
}
