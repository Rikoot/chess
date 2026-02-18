package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.Requests.CreateRequest;
import model.Requests.JoinRequest;
import model.Requests.ListRequest;
import model.Results.CreateResult;
import model.Results.ListResult;

public class GameService {
    GameDAO gameDao;

    public GameService() {
        gameDao = new GameDAO();
    }
    public ListResult listGames(AuthService service, ListRequest listRequest) {
        AuthData authData = service.validateSession(listRequest.authToken());
        return new ListResult(gameDao.getGames(authData.username()));
    }
    public CreateResult createGame(CreateRequest createRequest) {
        return new CreateResult(gameDao.createGame(createRequest.gameName()));
    }
    public void joinGame(AuthService service, JoinRequest joinRequest) throws DataAccessException {
        AuthData authData = service.validateSession(joinRequest.authToken());
        try {
            gameDao.joinGame(joinRequest.playerColor(), joinRequest.gameID(), authData.username());
        } catch (DataAccessException dataAccessException) {
            throw new DataAccessException(dataAccessException.getMessage());
        }
    }
    public void clearDb() {
        gameDao.clearDb();
    }
}
