package service;

import model.Requests.CreateRequest;
import model.Requests.JoinRequest;
import model.Requests.ListRequest;
import model.Results.CreateResult;
import model.Results.ListResult;

public class GameService {
    public ListResult listGames(ListRequest listRequest) {

        return new ListResult();
    }
    public CreateResult createGame(CreateRequest createRequest) {
        return new CreateResult();
    }
    public void joinGame(JoinRequest joinRequest) {

    }
}
