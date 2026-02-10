package dataaccess;

import model.UserData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class UserDAO {
    Collection<UserData> userDataCollection;
    public UserDAO() {
        userDataCollection = new HashSet<>();
    }
    public UserData getUser(String username) {
        for (UserData userData : userDataCollection) {
            if (Objects.equals(userData.username(), username)) {
                return userData;
            }
        }
        return null;
    }
    public void crateUser(String username,
                                 String password,
                                 String email) throws DataAccessException {
        if (getUser(username) == null) {
            UserData userData = new UserData(username, password, email);
            userDataCollection.add(userData);
        } else {
            throw new DataAccessException("Username already taken");
        }

    }
    public void clearDb() {
        userDataCollection = new HashSet<>();
    }
}
