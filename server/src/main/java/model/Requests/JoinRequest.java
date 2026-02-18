package model.Requests;

import chess.ChessGame;

public record JoinRequest(String playerColor,
                          String gameID,
                          String authToken) {
}
