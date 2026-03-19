package client;

import chess.*;
import model.GameData;
import ui.PrintGame;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
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
                boolean[] returnValues = handleRequest(userArgs);
                quitStatus = returnValues[0];
                loggedIn = returnValues[1];
            } catch (Exception e) {
                httpError();
            }

        }
        System.exit(0);
    }

    private static void error() {
        System.out.println("Invalid command or command options. Enter help for command formats.");
    }
    private static void httpError() {
        System.out.println("Some error occurred on the server.");
    }
    private static boolean[] handleRequest(String[] userArgs) throws ConnectException {
        switch (userArgs[0].toLowerCase()) {
            // logged out commands
            case "register" -> {
                if (!loggedIn && userArgs.length == 4) {
                    if (serverFacade.register(userArgs)) {
                        loggedIn = true;
                        username = userArgs[1];
                    } else {
                        httpError();
                    }
                } else {
                    error();
                }
            }

            case "login" -> {
                if (!loggedIn && userArgs.length == 3) {
                    if (serverFacade.login(userArgs)) {
                        loggedIn = true;
                        username = userArgs[1];
                    } else {
                        httpError();
                    }

                } else {
                    error();
                }
            }
            // logged in commands
            case "create" -> {
                if (loggedIn && userArgs.length == 2) {
                    int gameID = serverFacade.create(userArgs);
                    if (gameID == 0) {
                        httpError();
                    } else {
                        System.out.println("Created new game: " + gameID);
                    }
                } else {
                    error();
                }
            }

            case "list" -> {
                if (loggedIn && userArgs.length == 1) {
                    gameDataCollection = serverFacade.list();
                    if (gameDataCollection.isEmpty()) {
                        httpError();
                    } else if (gameDataCollection.size() == 1
                            && gameDataCollection.contains(
                            new GameData(0, null, null, null, null))) {
                        System.out.println("There are no games, feel free to create one!");
                    } else {
                        System.out.println("Below are the available games:\n----------");
                        int gameCounter = 1;
                        for (GameData gameData : gameDataCollection) {
                            System.out.println("Game #: " + gameCounter++);
                            System.out.println("Name:" + gameData.gameName());
                            System.out.println("White User: " + gameData.whiteUsername());
                            System.out.println("Black User: " + gameData.blackUsername());
                            System.out.println("----------");
                        }
                    }
                } else {
                    error();
                }
            }

            case "join" -> {
                if (loggedIn && userArgs.length == 3) {
                    if (gameDataCollection == null) {
                        System.out.println("List games first!");
                        break;
                    }
                    userArgs[2] = convertGameNumber(userArgs[2]);
                    if (serverFacade.join(userArgs)) {
                        GameData gameData = null;
                        for (GameData game : gameDataCollection) {
                            if (game.gameID() == Integer.parseInt(userArgs[2])) {
                                gameData = game;
                                break;
                            }
                        }
                        System.out.println("Joined game!");
                        ChessGame.TeamColor teamColor = (Objects.equals(gameData.blackUsername(), username))
                                ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                        System.out.println(PrintGame.print(gameData.game(), teamColor));
                    } else {
                        httpError();
                    }
                } else {
                    error();
                }
            }

            case "observe" -> {
                if (loggedIn && userArgs.length == 2) {
                    if (gameDataCollection == null) {
                        System.out.println("List games first!");
                        break;
                    }
                    userArgs[1] = convertGameNumber(userArgs[1]);
                    GameData gameData =  serverFacade.observe(userArgs);
                    if (gameData == null) {
                        httpError();
                    } else {
                        System.out.println(PrintGame.print(gameData.game(), ChessGame.TeamColor.WHITE));
                    }
                } else {
                    error();
                }
            }

            case "logout" -> {
                if (loggedIn && userArgs.length == 1) {
                    serverFacade.logout();
                } else {
                    error();
                }
            }
            // generic commands
            case "quit" -> {
                quitStatus = false;
                if (userArgs.length != 1) {
                    error();
                }
            }

            case "help" -> {
                if (loggedIn && userArgs.length == 1) {
                    System.out.println("""
                                create [NAME] - create a new game
                                list - list all games
                                join [COLOR(BLACK|WHITE)] [GAMEID] - join a game
                                observe [GAMEID] - watch a game
                                logout - end your session
                                quit - exit the client
                                help - print available commands
                                """);
                } else if (!loggedIn && userArgs.length == 1) {
                    System.out.println("""
                                register [USERNAME] [PASSWORD] [EMAIL] - create a login
                                login [USERNAME] [PASSWORD] - login to the server
                                quit - exit the client
                                help - print available commands
                                """);
                } else {
                    error();
                }
            }
            default -> {
                error();
            }
        }
        return new boolean[]{quitStatus, loggedIn};
    }
    public static String convertGameNumber(String number) {
        int gameNumber = Integer.parseInt(number);
        GameData gameData = null;
        Iterator<GameData> iterator = gameDataCollection.iterator();
        for (int i = 0; i < gameNumber; i++) {
            if (iterator.hasNext()) {
                gameData = iterator.next();
            } else {
                break;
            }
        }
        return String.valueOf((gameData != null) ? gameData.gameID() : 0);
    }
}
