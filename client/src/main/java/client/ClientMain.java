package client;

import chess.*;
import model.GameData;
import ui.PrintGame;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;

public class ClientMain {
    boolean loggedIn = false;

    public static void main(String[] args) {
        System.out.println("Welcome to rikoot's chess client.\nType help to get started.");
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }
        ServerFacade serverFacade = new ServerFacade(serverUrl);
        Scanner scanner = new Scanner(System.in);
        boolean loggedIn = false;
        boolean quitStatus = true;
        String username = null;
        while (quitStatus) {
            if (loggedIn) {
                System.out.print("[Logged in] >>>> ");
            } else {
                System.out.print("[Logged out] >>>> ");
            }
            String userInput = scanner.nextLine();
            String[] userArgs = userInput.split(" ");
            try {
                boolean[] returnValues = handleRequest(quitStatus, loggedIn, userArgs, serverFacade, username);
                quitStatus = returnValues[0];
                loggedIn = returnValues[1];
            } catch (ConnectException e) {
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
    private static boolean[] handleRequest(boolean quitStatus, boolean loggedIn, String[] userArgs, ServerFacade serverFacade, String username) throws ConnectException {
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
                    Collection<GameData> gameDataCollection = serverFacade.list();
                    if (gameDataCollection.isEmpty()) {
                        httpError();
                    } else if (gameDataCollection.size() == 1
                            && gameDataCollection.contains(
                            new GameData(0, null, null, null, null))) {
                        System.out.println("There are no games, feel free to create one!");
                    } else {
                        System.out.println("Below are the following games:");
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
                    if (serverFacade.join(userArgs)) {
                        System.out.println("Joined game!");
                    } else {
                        httpError();
                    }
                } else {
                    error();
                }
            }

            case "observe" -> {
                if (loggedIn && userArgs.length == 2) {
                    GameData gameData =  serverFacade.observe(userArgs);
                    if (gameData == null) {
                        httpError();
                    } else {
                        ChessGame.TeamColor teamColor = ChessGame.TeamColor.WHITE;
                        if (Objects.equals(gameData.blackUsername(), username)) {
                            teamColor = ChessGame.TeamColor.BLACK;
                        }
                        System.out.println(PrintGame.print(gameData.game(), teamColor));
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
                                join [COLOR] [GAMEID] - join a game
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
}
