package chess;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Set;

public class ChessGameDeserializer implements JsonDeserializer<ChessGame> {
    @Override
    public ChessGame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ChessGame chessGame = new ChessGame();
        ChessBoard chessBoard = chessGame.getBoard();
        // iterate through the piece positions and create the copies into the chess board
        JsonObject jsonBoard = jsonObject.get("board").getAsJsonObject().get("board").getAsJsonObject();
        for (String key : jsonBoard.keySet()) {
            addChessPosition(chessBoard, key, jsonBoard.get(key));
        }
        chessGame.setBoard(chessBoard);
        switch (jsonObject.get("currentTeam").getAsString()) {
            case ("WHITE") -> chessGame.setTeamTurn(ChessGame.TeamColor.WHITE);
            case ("BLACK") -> chessGame.setTeamTurn(ChessGame.TeamColor.BLACK);
        }
        return chessGame;
    }
    private void addChessPosition(ChessBoard chessBoard, String keyPosition, JsonElement position) {
        JsonObject jsonPiece = position.getAsJsonObject();
        ChessGame.TeamColor pieceColor = null;
        ChessPiece.PieceType pieceType = null;
        switch (jsonPiece.get("pieceColor").getAsString()) {
            case ("WHITE") -> pieceColor = ChessGame.TeamColor.WHITE;
            case ("BLACK") -> pieceColor = ChessGame.TeamColor.BLACK;
        }
        switch (jsonPiece.get("type").getAsString()) {
            case ("PAWN") -> pieceType = ChessPiece.PieceType.PAWN;
            case ("BISHOP") -> pieceType = ChessPiece.PieceType.BISHOP;
            case ("KNIGHT") -> pieceType = ChessPiece.PieceType.KNIGHT;
            case ("ROOK") -> pieceType = ChessPiece.PieceType.ROOK;
            case ("QUEEN") -> pieceType = ChessPiece.PieceType.QUEEN;
            case ("KING") -> pieceType = ChessPiece.PieceType.KING;
        }
        ChessPiece chessPiece = new ChessPiece(pieceColor, pieceType);

        chessBoard.addPiece(new ChessPosition(keyPosition.charAt(1),keyPosition.charAt(3)),
                chessPiece);
    }
}
