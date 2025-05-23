/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 *
 * @author Rümeysa
 */

public class Server {

    private ServerSocket serverSocket;
    private final List<SClient> waitingQueue = new ArrayList<>();
    private final List<GameRoom> activeGames = new ArrayList<>();

    // Sends chat messages to all players in the same room
    public void sendChatToRoom(SClient sender, String chatMsg) {
        GameRoom room = findRoomOfClient(sender);
        if (room != null) {
            for (SClient p : room.players) {
                p.send("CHAT:" + chatMsg);
            }
        } else {
            System.out.println("[WARN] Could not find a room for chat!");
        }
    }

    // Finds which room the client belongs to
    private GameRoom findRoomOfClient(SClient client) {
        for (GameRoom game : activeGames) {
            if (game.players.contains(client)) {
                return game;
            }
        }
        return null;
    }

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        System.out.println("Server started.");
        new Thread(this::listenForClients).start();
    }

    private void listenForClients() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                String clientId = "Player" + (waitingQueue.size() + activeGames.size() * 2 + 1);
                SClient client = new SClient(socket, this, clientId);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addClientToQueue(SClient client) {
        waitingQueue.add(client);
        if (waitingQueue.size() >= 2) {
            List<SClient> playersForGame = new ArrayList<>(waitingQueue.subList(0, 2));
            waitingQueue.subList(0, 2).clear();

            GameRoom newGame = new GameRoom(playersForGame);
            activeGames.add(newGame);
            newGame.start();
        } else {
            client.send("WAITING: Waiting for another player to join the game...");
        }
    }

    public synchronized void removeClient(SClient client) {
        // Remove from waiting queue
        waitingQueue.remove(client);

        // Remove from active games
        GameRoom toRemove = null;
        for (GameRoom game : activeGames) {
            if (game.players.contains(client)) {
                game.players.remove(client);
                game.broadcast("DISCONNECTED:" + client.clientId);
                toRemove = game;
                break;
            }
        }
        if (toRemove != null) {
            activeGames.remove(toRemove);
        }

        System.out.println("✂️ " + client.clientId + " disconnected and removed from the system.");
    }

    public synchronized void updateClientId(String oldId, String newId) {
        // Update ID in waiting queue
        for (SClient client : waitingQueue) {
            if (client.clientId.equals(oldId)) {
                client.clientId = newId;
                client.playerName = newId;
                return;
            }
        }
        // Update ID in active games
        for (GameRoom game : activeGames) {
            for (SClient player : game.players) {
                if (player.clientId.equals(oldId)) {
                    player.clientId = newId;
                    player.playerName = newId;
                    return;
                }
            }
        }
    }

    public synchronized void processRestartRequest(String clientId) {
        for (GameRoom game : activeGames) {
            for (SClient player : game.players) {
                if (player.clientId.equals(clientId)) {
                    game.handleRestartRequest(clientId);
                    return;
                }
            }
        }
    }

    public synchronized void processRestartResponse(String clientId, boolean accepted) {
        for (GameRoom game : activeGames) {
            for (SClient player : game.players) {
                if (player.clientId.equals(clientId)) {
                    game.handleRestartResponse(clientId, accepted);
                    return;
                }
            }
        }
    }

    public synchronized void processRestart(String clientId) {
        for (GameRoom game : activeGames) {
            for (SClient player : game.players) {
                if (player.clientId.equals(clientId)) {
                    game.restartGame();
                    return;
                }
            }
        }
    }

    public synchronized void processRoll(String clientId) {
        for (GameRoom game : activeGames) {
            for (SClient player : game.players) {
                if (player.clientId.equals(clientId)) {
                    game.processRoll(clientId);
                    return;
                }
            }
        }
    }

    public synchronized void processSurrender(String clientId) {
        for (GameRoom game : activeGames) {
            for (SClient player : game.players) {
                if (player.clientId.equals(clientId)) {
                    game.handleSurrender(clientId);
                    return;
                }
            }
        }
    }

    // ---- Inner Class: GameRoom ----
    private class GameRoom {

        List<SClient> players;
        private String restartRequester = null;
        private final Set<String> restartVotes = new HashSet<>();

        Map<String, Integer> positions = new HashMap<>();
        int currentPlayerIndex = 0;
        Random random = new Random();

        GameRoom(List<SClient> players) {
            this.players = players;
            for (SClient p : players) {
                positions.put(p.clientId, 1);
            }
        }

        void start() {
            broadcast("MATCHED: Match found!");
            sendTurnToCurrentPlayer();
        }

        void restartGame() {
            for (SClient p : players) {
                positions.put(p.clientId, 1);
            }
            currentPlayerIndex = 0;
            broadcast("NEW_GAME: New game started!");
            sendTurnToCurrentPlayer();
        }

        void handleRestartRequest(String fromId) {
            System.out.println("📨 [SERVER] Restart request received: " + fromId);

            if (restartRequester == null) {
                restartRequester = fromId;
                restartVotes.clear();
                restartVotes.add(fromId);

                for (SClient p : players) {
                    if (!p.clientId.equals(fromId)) {
                        System.out.println("➡️ [SERVER] Sending RESTART_REQUEST_FROM to: " + p.clientId);
                        p.send("RESTART_REQUEST_FROM:" + fromId);
                    }
                }
            } else if (!restartVotes.contains(fromId)) {
                System.out.println("🔄 Both players requested restart, automatically accepted.");
                restartVotes.add(fromId);
                if (restartVotes.size() == players.size()) {
                    restartGame();
                    broadcast("RESTART_CONFIRMED");
                    restartVotes.clear();
                    restartRequester = null;
                }
            } else {
                System.out.println("⚠️ Restart already requested: " + fromId);
            }
        }

        void handleRestartResponse(String fromId, boolean accepted) {
            if (!accepted) {
                broadcast("RESTART_DENIED");
                for (SClient p : players) {
                    p.send("EXIT");
                }
                activeGames.remove(this);
                restartVotes.clear();
                restartRequester = null;
                return;
            }
            restartVotes.add(fromId);
            if (restartVotes.size() == players.size()) {
                restartGame();
                broadcast("RESTART_CONFIRMED");
                restartVotes.clear();
                restartRequester = null;
            }
        }

        void handleSurrender(String surrenderingClientId) {
            for (SClient player : players) {
                if (player.clientId.equals(surrenderingClientId)) {
                    player.send("SURRENDERED: You surrendered the game.");
                } else {
                    player.send("SURRENDERED: " + surrenderingClientId + " has surrendered the game.");
                    player.send("WINNER: " + player.clientId);
                }
            }
        }

        void broadcast(String msg) {
            for (SClient player : players) {
                player.send(msg);
            }
        }

        void sendTurnToCurrentPlayer() {
            SClient current = players.get(currentPlayerIndex);
            broadcast("TURN:" + current.clientId);
        }

        void processRoll(String clientId) {
            if (!clientId.equals(players.get(currentPlayerIndex).clientId)) {
                return;
            }

            int roll = random.nextInt(6) + 1;
            int oldPos = positions.getOrDefault(clientId, 1);
            int newPos = Math.min(100, oldPos + roll);

            newPos = applySnakesAndLadders(newPos);

            positions.put(clientId, newPos);
            broadcast("MOVE:" + clientId + ":" + roll + ":" + newPos);

            if (newPos == 100) {
                broadcast("WINNER:" + clientId);
                return;
            }

            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            sendTurnToCurrentPlayer();
        }

        int applySnakesAndLadders(int pos) {
            Map<Integer, Integer> map = Map.of(
                    3, 22,
                    8, 30,
                    33, 65,
                    58, 77,
                    75, 86,
                    97, 78,
                    89, 67,
                    62, 19,
                    36, 6,
                    25, 5
            );
            return map.getOrDefault(pos, pos);
        }
    }
}