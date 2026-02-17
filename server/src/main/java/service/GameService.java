package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.Requests.CreateRequest;
import model.Requests.JoinRequest;
import model.Requests.ListRequest;
import model.Results.CreateResult;
import model.Results.ListResult;

public class GameService {
    GameDAO gameDAO;

    public GameService() {
        gameDAO = new GameDAO();
    }
    public ListResult listGames(AuthService service, ListRequest listRequest) {

        return new ListResult();
    }
    public CreateResult createGame(AuthService service, CreateRequest createRequest) {
        return new CreateResult();
    }
    public void joinGame(AuthService service, JoinRequest joinRequest) {

        try {
            gameDAO.joinGame();
        } catch (DataAccessException dataAccessException) {

        }
    }
}
