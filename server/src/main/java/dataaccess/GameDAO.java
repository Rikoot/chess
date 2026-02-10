package dataaccess;

import chess.ChessGame;

import java.util.Collection;
import java.util.HashSet;

public class GameDAO {
    public static Collection<ChessGame> getGames(String username) {
        return new HashSet<>();
    }

    public static String createGame(String gameName) {
        return "GAME_ID_TEMP";
    }

    public static void joinGame(ChessGame.TeamColor playerColor, String gameID) {

    }
}
