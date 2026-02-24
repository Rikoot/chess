package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSQLDAO {

    public UserSQLDAO() {
        try  {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        this.createDb();
    }

    public UserData getUser(String username) {
        UserData data = null;
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        String statement = "SELECT * FROM Users WHERE Username = ? LIMIT 1;";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return data;
            }
            data = new UserData(resultSet.getString("Username"),
                    resultSet.getString("Password"),
                    resultSet.getString("Email"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
    public void createUser(String username,
                           String password,
                           String email) throws DataAccessException {
        String statement = "INSERT INTO Users VALUES (?, ?, ?);";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, email);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Taken");
        }

    }
    public void clearDb() {
        String statement = "DROP TABLE IF EXISTS Users;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (DataAccessException e) {

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createDb();
    }

    private void createDb() {
        String statement = """
CREATE TABLE IF NOT EXISTS Users (
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
