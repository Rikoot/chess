package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserSQLDAO {

    public UserSQLDAO() {
        try  {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        this.clearDb();
    }

    public UserData getUser(String username) {
        UserData data = new UserData("Temp", "Temp", "Temp");

        return data;
    }
    public void createUser(String username,
                           String password,
                           String email) {

    }
    public void clearDb() {
        String statement = """
DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
    Username VARCHAR(255) NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Email VARCHAR(255) NOT NULL,
    PRIMARY KEY (Username)
);
""";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (DataAccessException e) {

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
