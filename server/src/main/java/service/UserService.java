package service;

import model.Requests.LoginRequest;
import model.Requests.LogoutRequest;
import model.Requests.RegisterRequest;
import model.Results.LoginResult;
import model.Results.RegisterResult;

public class UserService {
    public RegisterResult register(RegisterRequest registerRequest) {
        return new RegisterResult("Temp", "Temp");
    }
    public LoginResult login(LoginRequest loginRequest) {
        return new LoginResult("Temp", "Temp");
    }
    public void logout(LogoutRequest logoutRequest) {}
}
