package service;

import dataaccess.DataAccessException;
import dataaccess.GameSQLDAO;
import model.AuthData;
import model.requests.CreateRequest;
import model.requests.JoinRequest;
import model.requests.ListRequest;
import model.results.CreateResult;
import model.results.ListResult;

public class GameService {
    GameSQLDAO gameDao;

    public GameService() throws DataAccessException {
        gameDao = new GameSQLDAO();
    }
    public ListResult listGames(ListRequest listRequest) throws DataAccessException {
        return new ListResult(gameDao.getGames());
    }
    public CreateResult createGame(CreateRequest createRequest) throws DataAccessException{
        return new CreateResult(gameDao.createGame(createRequest.gameName()));
    }
    public void joinGame(AuthService service, JoinRequest joinRequest) throws DataAccessException {
        AuthData authData = service.validateSession(joinRequest.authToken());
         gameDao.joinGame(joinRequest.playerColor(), joinRequest.gameID(), authData.username());
    }
    public void clearDb() throws DataAccessException{
        gameDao.clearDb();
    }
}
