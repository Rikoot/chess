package model.Requests;

import chess.ChessGame;

public record JoinRequest(String playerColor,
                          int gameID,
                          String authToken) {
    public JoinRequest addAuthToken(String newAuthToken) {
        return new JoinRequest(playerColor, gameID, newAuthToken);
    }
}
