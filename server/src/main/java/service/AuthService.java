package service;

import dataaccess.AuthSQLDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {
    AuthSQLDAO authDao;
    public AuthService() throws DataAccessException {
        authDao = new AuthSQLDAO();
    }

    public AuthData validateSession(String authToken) {
        return authDao.getAuth(authToken);
    }

    public AuthData createSession(String username) throws DataAccessException {
        AuthData authData = new AuthData(AuthData.generateToken(), username);
        authDao.createAuth(authData);
        return authData;
    }

    public void deleteSession(String authToken) throws DataAccessException {
        AuthData authData = authDao.getAuth(authToken);
        authDao.deleteAuth(authData);
    }

    public void clearDb() throws DataAccessException {
        authDao.clearDb();
    }
}
