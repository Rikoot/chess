package client;

import chess.*;

import java.util.Objects;
import java.util.Scanner;

public class ClientMain {
    boolean loggedIn = false;

    public static void main(String[] args) {
        System.out.println("Welcome to rikoot's chess client.\nType help to get started.");
        ServerFacade serverFacade = new ServerFacade();
        Scanner scanner = new Scanner(System.in);
        boolean loggedIn = false;
        boolean quitStatus = true;
        while (quitStatus) {
            if (loggedIn) {
                System.out.print("[Logged in] >>>> ");
            } else {
                System.out.print("[Logged out] >>>> ");
            }
            String userInput = scanner.nextLine();
            String[] userArgs = userInput.split("\\a+");
            switch (userArgs[0].toLowerCase()) {
                // logged out commands
                case "register" -> {
                    if (!loggedIn && userArgs.length == 4) {
                        serverFacade.register();
                    } else {
                        error();
                    }
                }

                case "login" -> {
                    if (!loggedIn && userArgs.length == 3) {
                        serverFacade.login();
                        loggedIn = true;
                    } else {
                        error();
                    }
                }
                // logged in commands
                case "create" -> {
                    if (loggedIn && userArgs.length == 2) {
                        serverFacade.create();
                    } else {
                        error();
                    }
                }

                case "list" -> {
                    if (loggedIn && userArgs.length == 1) {
                        serverFacade.list();
                    } else {
                        error();
                    }
                }

                case "join" -> {
                    if (loggedIn && userArgs.length == 2) {
                        serverFacade.join();
                    } else {
                        error();
                    }
                }

                case "observe" -> {
                    if (loggedIn && userArgs.length == 2) {
                        serverFacade.observe();
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
                    if (loggedIn && userArgs.length == 1) {
                        serverFacade.create();
                    } else {
                        error();
                    }
                }

                case "help" -> {
                    if (loggedIn && userArgs.length == 1) {
                        System.out.println("create [NAME] - create a new game\nlist - list all games\njoin");
                    } else if (!loggedIn && userArgs.length == 1) {
                        System.out.println("register [USERNAME] [PASSWORD] [EMAIL] - create a login\nlogin [USERNAME] [PASSWORD] - login to the server\nquit - exit the client\nhelp - print available commands");
                    } else {
                        error();
                    }
                }
                default -> {
                    error();
                }
            }
        }
        System.exit(0);
    }

    private static void error() {
        System.out.println("Invalid command or command options. Enter help for command formats.");
    }
}
