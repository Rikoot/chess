package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.requests.CreateRequest;
import model.requests.JoinRequest;
import model.requests.ListRequest;
import model.results.CreateResult;
import model.results.ListResult;

public class GameService {
    GameDAO gameDao;

    public GameService() {
        gameDao = new GameDAO();
    }
    public ListResult listGames(ListRequest listRequest) {
        return new ListResult(gameDao.getGames());
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
