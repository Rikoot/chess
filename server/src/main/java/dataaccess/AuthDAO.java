package dataaccess;

import model.AuthData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class AuthDAO {
    Collection<AuthData> authDataCollection;
    public AuthDAO() {
        authDataCollection = new HashSet<>();
    }
    public AuthData getAuth(String searchValue) {
        for (AuthData authData : authDataCollection) {
            if (Objects.equals(authData.username(), searchValue)
                    || Objects.equals(authData.authToken(), searchValue)) {
                return authData;
            }
        }
        return null;
    }

    public void deleteAuth(AuthData data) throws DataAccessException {
        if (authDataCollection.contains(data)) {
            authDataCollection.remove(data);
        } else {
            throw new DataAccessException("Token doesn't exist.");
        }

    }
    public void createAuth(AuthData data) throws DataAccessException {
        if (authDataCollection.contains(data)) {
            throw new DataAccessException("Token doesn't exist.");
        } else {
            authDataCollection.add(data);
        }
    }
    public void clearDb() {
        authDataCollection = new HashSet<>();
    }
}
