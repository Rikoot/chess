package client;

import chess.ChessGame;
import model.GameData;
import ui.PrintGame;

import java.net.ConnectException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class RequestHandlers {
    public static void commandError() {
        System.out.println("Invalid command. Enter help for available commands.");
    }
    public static void argsError() {
        System.out.println("Invalid command options. Enter help for command formats.");
    }
    public static void httpError() {
        System.out.println("Some error occurred between the client and server.");
    }
    public static void printHelp(boolean loggedIn) {
        if (loggedIn) {
            System.out.println("""
                                create [NAME] - create a new game
                                list - list all games
                                join [COLOR(BLACK|WHITE)] [GAMEID] - join a game
                                observe [GAMEID] - watch a game
                                logout - end your session
                                quit - exit the client
                                help - print available commands
                                """);
        } else {
            System.out.println("""
                                register [USERNAME] [PASSWORD] [EMAIL] - create a login
                                login [USERNAME] [PASSWORD] - login to the server
                                quit - exit the client
                                help - print available commands
                                """);
        }
    }

    public static boolean handleRegister(ServerFacade serverFacade, boolean loggedIn, String[] userArgs) throws ConnectException {
        if (!loggedIn && userArgs.length == 4) {
            if (serverFacade.register(userArgs)) {
                return true;
            } else {
                httpError();
            }
        } else {
            argsError();
        }
        return false;
    }

    public static boolean handleLogin(ServerFacade serverFacade, boolean loggedIn, String[] userArgs) throws ConnectException {
        if (!loggedIn && userArgs.length == 3) {
            if (serverFacade.login(userArgs)) {
                return true;
            } else {
                httpError();
            }

        } else {
            argsError();
        }
        return false;
    }

    public static void handleCreate(ServerFacade serverFacade, boolean loggedIn, String[] userArgs) throws ConnectException {
        if (loggedIn && userArgs.length == 2) {
            int gameID = serverFacade.create(userArgs);
            if (gameID == 0) {
                httpError();
            } else {
                System.out.println("Created new game!");
            }
        } else {
            argsError();
        }
    }

    public static Collection<GameData> handleList(ServerFacade serverFacade, boolean loggedIn,
                                  String[] userArgs) throws ConnectException {
        if (loggedIn && userArgs.length == 1) {
            Collection<GameData> gameDataCollection = serverFacade.list();
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
            return gameDataCollection;
        } else {
            argsError();
            return null;
        }
    }

    public static void handleJoin(ServerFacade serverFacade, boolean loggedIn,
                                  String[] userArgs, String username, Collection<GameData> gameDataCollection) throws ConnectException {
        if (loggedIn && userArgs.length == 3) {
            if (gameDataCollection == null) {
                System.out.println("List games first!");
                return;
            }
            userArgs[2] = convertGameNumber(userArgs[2], gameDataCollection);
            if (serverFacade.join(userArgs)) {
                GameData gameData = null;
                for (GameData game : gameDataCollection) {
                    if (game.gameID() == Integer.parseInt(userArgs[2])) {
                        gameData = game;
                        break;
                    }
                }
                if (gameData == null) {
                    httpError();
                    return;
                }
                System.out.println("Joined game!");
                ChessGame.TeamColor teamColor = (Objects.equals(gameData.blackUsername(), username))
                        ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                System.out.println(PrintGame.print(gameData.game(), teamColor));
            } else {
                httpError();
            }
        } else {
            argsError();
        }
    }

    public static void handleObserve(ServerFacade serverFacade, boolean loggedIn,
                                     String[] userArgs,Collection<GameData> gameDataCollection) throws ConnectException {
        if (loggedIn && userArgs.length == 2) {
            if (gameDataCollection == null) {
                System.out.println("List games first!");
                return;
            }
            userArgs[1] = convertGameNumber(userArgs[1], gameDataCollection);
            GameData gameData =  serverFacade.observe(userArgs);
            if (gameData == null) {
                httpError();
            } else {
                System.out.println(PrintGame.print(gameData.game(), ChessGame.TeamColor.WHITE));
            }
        } else {
            argsError();
        }
    }

    public static boolean handleLogout(ServerFacade serverFacade, boolean loggedIn, String[] userArgs) throws ConnectException {
        if (loggedIn && userArgs.length == 1) {
            return !serverFacade.logout();
        } else {
            argsError();
        }
        return true;
    }

    private static String convertGameNumber(String number, Collection<GameData> gameDataCollection) {
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
