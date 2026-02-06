package model.Requests;

import chess.ChessGame;

public record JoinRequest(ChessGame.TeamColor playerColor,
                          String gameID,
                          String authToken) {
}
