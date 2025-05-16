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

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        System.out.println("Sunucu başladı.");
        new Thread(this::listenForClients).start();
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

public synchronized void removeClient(SClient client) {
    // 1. Bekleyen kuyruktan çıkar
    waitingQueue.remove(client);

    // 2. Aktif oyunlardan çıkar (oyun sona ermiş olabilir)
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

    System.out.println("✂️ " + client.clientId + " bağlantısı kesildi ve sistemden çıkarıldı.");
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
            client.send("WAITING: Oyun için başka bir oyuncu bekleniyor...");
        }
    }

    public synchronized void updateClientId(String oldId, String newId) {
        // Kuyruktaki oyuncuların ID'sini güncelle
        for (SClient client : waitingQueue) {
            if (client.clientId.equals(oldId)) {
                client.clientId = newId;
                client.playerName = newId;
                return;
            }
        }
        // Aktif oyunlardaki oyuncuların ID'sini güncelle
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
            broadcast("MATCHED: Eşleşme tamamlandı!");
            sendTurnToCurrentPlayer();
        }

        void restartGame() {
            // Pozisyonları sıfırla
            for (SClient p : players) {
                positions.put(p.clientId, 1);
            }
            currentPlayerIndex = 0;
            broadcast("NEW_GAME: Yeni oyun başlatıldı!");
            sendTurnToCurrentPlayer();
        }

        void handleRestartRequest(String fromId) {
            if (restartRequester == null) {
                restartRequester = fromId;
                restartVotes.clear();
                restartVotes.add(fromId);

                for (SClient p : players) {
                    if (!p.clientId.equals(fromId)) {
                        p.send("RESTART_REQUEST_FROM:" + fromId);
                    }
                }
            }
        }

        void handleRestartResponse(String fromId, boolean accepted) {
            if (!accepted) {
                broadcast("RESTART_DENIED");

                // restartRequester değerini sakla
                String requesterId = restartRequester;

                for (SClient player : players) {
                    if (player.clientId.equals(requesterId)) {
                        addClientToQueue(player); // Kuyruğa geri al
                    }
                }

                activeGames.remove(this); // Odayı kapat
                restartVotes.clear();
                restartRequester = null;
                return;
            }

            restartVotes.add(fromId);
            if (restartVotes.size() == 2) {
                restartGame();
                broadcast("RESTART_CONFIRMED");
                restartVotes.clear();
                restartRequester = null;
            }
        }

        void handleSurrender(String surrenderingClientId) {
            broadcast("SURRENDERED: " + surrenderingClientId + " oyundan pes etti.");
            // Rakip oyuncuyu kazanan ilan et
            for (SClient player : players) {
                if (!player.clientId.equals(surrenderingClientId)) {
                    broadcast("WINNER: " + player.clientId);
                    break;
                }
            }
            // Oyun sona erdiği için aktif oyunlar listesinden kaldırabilirsiniz
            activeGames.remove(this);
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
                    5, 8,
                    11, 26,
                    20, 29,
                    27, 1,
                    17, 4,
                    19, 7
            );
            return map.getOrDefault(pos, pos);
        }
    }
}
