package service;

import dataaccess.DataAccessException;
import dataaccess.UserSQLDAO;
import model.AuthData;
import model.requests.LoginRequest;
import model.requests.LogoutRequest;
import model.requests.RegisterRequest;
import model.results.LoginResult;
import model.results.RegisterResult;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    UserSQLDAO userDao;

    public UserService() throws DataAccessException{
        userDao = new UserSQLDAO();
    }
    public RegisterResult register(AuthService service, RegisterRequest registerRequest) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        userDao.createUser(registerRequest.username(), hashedPassword, registerRequest.email());
        AuthData authData = service.createSession(registerRequest.username());
        return new RegisterResult(authData.username(), authData.authToken());
    }
    public LoginResult login(AuthService service, LoginRequest loginRequest) throws DataAccessException {
        UserData userData = userDao.getUser(loginRequest.username());
        if (userData == null) {
            throw new DataAccessException("Invalid User");
        }
        if (!BCrypt.checkpw(loginRequest.password(), userData.password())) {
            throw new DataAccessException("Invalid Password");
        }
        AuthData authData = service.createSession(userData.username());
        return new LoginResult(authData.username(), authData.authToken());
    }
    public void logout(AuthService service, LogoutRequest logoutRequest) throws DataAccessException {
        service.deleteSession(logoutRequest.authToken());
    }
    public void clearDb() throws DataAccessException {
        userDao.clearDb();
    }
}
