package client;

import chess.*;
import model.GameData;

import java.net.ConnectException;
import java.util.Collection;
import java.util.Scanner;

public class ClientMain {

    private static Collection<GameData> gameDataCollection;
    private static boolean loggedIn;
    private static boolean quitStatus;
    private static String username;
    private static ServerFacade serverFacade;

    public static void main(String[] args) {
        System.out.println("Welcome to rikoot's chess client.\nType help to get started.");
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }
        serverFacade = new ServerFacade(serverUrl);
        Scanner scanner = new Scanner(System.in);
        loggedIn = false;
        quitStatus = true;
        username = null;
        gameDataCollection = null;

        while (quitStatus) {
            if (loggedIn) {
                System.out.print("[Logged in] >>>> ");
            } else {
                System.out.print("[Logged out] >>>> ");
            }
            String userInput = scanner.nextLine();
            String[] userArgs = userInput.split(" ");
            try {
                handleRequest(userArgs);
            } catch (Exception e) {
                RequestHandlers.httpError();
            }
        }
        System.exit(0);
    }


    private static void handleRequest(String[] userArgs) throws ConnectException {
        switch (userArgs[0].toLowerCase()) {
            // logged out commands
            case "register" -> {
                RequestHandlers.handleRegister(serverFacade, loggedIn, userArgs, username);
            }

            case "login" -> {
                RequestHandlers.handleLogin(serverFacade, loggedIn, userArgs, username);
            }
            // logged in commands
            case "create" -> {
                RequestHandlers.handleCreate(serverFacade, loggedIn, userArgs);
            }

            case "list" -> {
                RequestHandlers.handleList(serverFacade, loggedIn, userArgs, gameDataCollection);
            }

            case "join" -> {
                RequestHandlers.handleJoin(serverFacade, loggedIn, userArgs, username, gameDataCollection);
            }

            case "observe" -> {
                RequestHandlers.handleObserve(serverFacade, loggedIn, userArgs, gameDataCollection);
            }

            case "logout" -> {
                RequestHandlers.handleLogout(serverFacade, loggedIn, userArgs);
            }
            // generic commands
            case "quit" -> {
                quitStatus = false;
                if (userArgs.length != 1) {
                    RequestHandlers.error();
                }
            }

            case "help" -> {
                RequestHandlers.printHelp(loggedIn);
            }
            default -> {
                RequestHandlers.error();
            }
        }
    }


}
