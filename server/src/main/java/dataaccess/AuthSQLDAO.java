package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthSQLDAO {

    public AuthSQLDAO() {
        try  {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        this.createDb();
    }

    public AuthData getAuth(String searchValue)  {
        String statement = "SELECT * FROM Auth WHERE (Username = ? OR Authtoken = ?) LIMIT 1;";
        AuthData authData = null;
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, searchValue);
            preparedStatement.setString(2, searchValue);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return authData;
            }
            authData = new AuthData(resultSet.getString("Authtoken"),
                    resultSet.getString("Username"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return authData;
    }

    public void deleteAuth(AuthData data) throws DataAccessException {
        if (data == null) {
            throw new DataAccessException("Invalid Session");
        }
        String statement = "DELETE FROM Auth WHERE Username = ?;";
        Connection conn = DatabaseManager.getConnection();
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, data.username());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createAuth(AuthData data) throws DataAccessException {
        String statement = "INSERT INTO Auth VALUES (?, ?);";
        Connection conn = DatabaseManager.getConnection();
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, data.username());
            preparedStatement.setString(2, data.authToken());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void clearDb() {
        String statement = "DROP TABLE IF EXISTS Auth;";
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
CREATE TABLE IF NOT EXISTS Auth (
    Username VARCHAR(255) NOT NULL,
    Authtoken VARCHAR(255) NOT NULL,
    PRIMARY KEY (Authtoken)
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
