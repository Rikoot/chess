package client;

import client.websocket.WebSocketFacade;
import model.GameData;

import java.net.ConnectException;
import java.net.URI;
import java.util.Collection;
import java.util.Scanner;

public class ClientMain {

    private static Collection<GameData> gameDataCollection;
    private static boolean loggedIn;
    private static boolean quitStatus;
    private static String username;
    private static ServerFacade serverFacade;
    private static WebSocketFacade webSocketFacade;

    public static void main(String[] args) {
        System.out.println("Welcome to rikoot's chess client.\nType help to get started.");
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }
        serverFacade = new ServerFacade(serverUrl);
        try {
            String url = serverUrl.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            webSocketFacade = new WebSocketFacade(socketURI, gameDataCollection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                loggedIn = RequestHandlers.handleRegister(serverFacade, loggedIn, userArgs);
                if (loggedIn) {
                    username = userArgs[1];
                }
            }

            case "login" -> {
                loggedIn = RequestHandlers.handleLogin(serverFacade, loggedIn, userArgs);
                if (loggedIn) {
                    username = userArgs[1];
                }
            }
            // logged in commands
            case "create" -> {
                RequestHandlers.handleCreate(serverFacade, loggedIn, userArgs);
            }

            case "list" -> {
                gameDataCollection = RequestHandlers.handleList(serverFacade, loggedIn, userArgs);
            }

            case "join" -> {
                RequestHandlers.handleJoin(serverFacade, loggedIn, userArgs, username, gameDataCollection, webSocketFacade);
            }

            case "observe" -> {
                RequestHandlers.handleObserve(serverFacade, loggedIn, userArgs, gameDataCollection, webSocketFacade);
            }

            case "logout" -> {
                loggedIn = RequestHandlers.handleLogout(serverFacade, loggedIn, userArgs);
            }
            // generic commands
            case "quit" -> {
                if (userArgs.length != 1) {
                    RequestHandlers.argsError();
                }
                quitStatus = false;
            }

            case "help" -> {
                RequestHandlers.printHelp(loggedIn);
            }
            default -> {
                RequestHandlers.commandError();
            }
        }
    }


}
