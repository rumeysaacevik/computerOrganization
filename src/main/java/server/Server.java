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


 //sunucusu sınıfı.
 //bağlanan istemcileri ve aktif oyunları yönetir, eşleştirme ve oyun mantığını kontrol eder.
 
public class Server {

    private ServerSocket serverSocket;
    private final List<SClient> waitingQueue = new ArrayList<>();   // bekleyen oyuncular
    private final List<GameRoom> activeGames = new ArrayList<>();   // aktif oyun odaları

    //bir oyuncunun gönderdiği sohbet mesajını odaya iletir.
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

    //bir oyuncunun hangi aktif oyun odasında olduğunu bulur.
    private GameRoom findRoomOfClient(SClient client) {
        for (GameRoom game : activeGames) {
            if (game.players.contains(client)) {
                return game;
            }
        }
        return null;
    }

    //sunucu başlatılırken ilgili port dinlemeye alınır
    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    //Sunucu başlatıldığında, sürekli yeni istemci bağlantıları dinlenir
    public void start() {
        System.out.println("Server started.");
        new Thread(this::listenForClients).start();
    }

    //yeni istemci bağlantılarını dinler ve kabul eder.
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

    //oyuncuyu bekleme kuyruğuna ekler, 2 kişi olunca yeni oyun başlatır
    public synchronized void addClientToQueue(SClient client) {
        waitingQueue.add(client);
        if (waitingQueue.size() >= 2) {
            // 2 kişi tamamlandı, oyun başlatılır
            List<SClient> playersForGame = new ArrayList<>(waitingQueue.subList(0, 2));
            waitingQueue.subList(0, 2).clear();

            GameRoom newGame = new GameRoom(playersForGame);
            activeGames.add(newGame);
            newGame.start();
        } else {
            // Yeterli oyuncu yoksa bekletilir
            client.send("WAITING: Waiting for another player to join the game...");
        }
    }

    //bir oyuncu sistemden ayrıldığında ilgili oyun ve kuyruktan silinir
    public synchronized void removeClient(SClient client) {
        // Bekleme kuyruğundan çıkar
        waitingQueue.remove(client);

        // aktif oyunlardan çıkar ve odadaki diğer oyunculara bilgi verir
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

    //oyuncunun ismi güncellendiğinde tüm listelerde id'yi değiştirir
    public synchronized void updateClientId(String oldId, String newId) {
        // Bekleme kuyruğunda güncelle
        for (SClient client : waitingQueue) {
            if (client.clientId.equals(oldId)) {
                client.clientId = newId;
                client.playerName = newId;
                return;
            }
        }
        // aktif oyunlarda güncelle
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

    // oyun sırasında gelen taleplerin oyun odasına iletilmesi burada gerçekleşir

    // oyuncudan restart isteği gelince ilgili odadaki işlemi başlatır
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

    // oyuncunun restart teklifine verdiği cevabı ilgili odaya iletir
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

    //odaya restart komutu gelirse oyunu başlatır
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

    //zar atma isteği ilgili odadaki oyun mantığına yönlendirilir
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

    // teslim olma isteği ilgili oyun odasına bildirilir
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

    // oyun odası: her 2 oyunculu oyun için ayrı GameRoom nesnesi 
    private class GameRoom {

        List<SClient> players;           // odaya bağlı oyuncular
        private String restartRequester = null;     // kim restart istedi?
        private final Set<String> restartVotes = new HashSet<>(); // onaylayan oyuncular

        Map<String, Integer> positions = new HashMap<>();   // oyuncuların pozisyonu
        int currentPlayerIndex = 0;         // sıradaki oyuncunun indexi
        Random random = new Random();       // zar için random nesnesi

        
         //oda oluşturulurken 2 oyuncu ile başlar, herkes 1 konumunda olur
         
        GameRoom(List<SClient> players) {
            this.players = players;
            for (SClient p : players) {
                positions.put(p.clientId, 1);
            }
        }

        //oda başladığında oyunculara eşleşme bilgisi ve ilk sıra verilir
        void start() {
            broadcast("MATCHED: Match found!");
            sendTurnToCurrentPlayer();
        }

        // oda yeniden başlatılır, konumlar sıfırlanır, sıra başa döner
        void restartGame() {
            for (SClient p : players) {
                positions.put(p.clientId, 1);
            }
            currentPlayerIndex = 0;
            broadcast("NEW_GAME: New game started!");
            sendTurnToCurrentPlayer();
        }

        //oyuncudan gelen restart isteği işleniyor ve tüm oyuncular onay verirse oyun başlıyor
        void handleRestartRequest(String fromId) {
            System.out.println("📨 [SERVER] Restart request received: " + fromId);

            if (restartRequester == null) {
                // ilk restart isteği
                restartRequester = fromId;
                restartVotes.clear();
                restartVotes.add(fromId);

                // diğer oyuncuya teklif gönder
                for (SClient p : players) {
                    if (!p.clientId.equals(fromId)) {
                        System.out.println("➡️ [SERVER] Sending RESTART_REQUEST_FROM to: " + p.clientId);
                        p.send("RESTART_REQUEST_FROM:" + fromId);
                    }
                }
            } else if (!restartVotes.contains(fromId)) {
                // ikinci oyuncu da isterse otomatik başlatılır
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

        //oyuncunun restart teklifine cevabı işlenir. Kabul edilirse oyun başlar, red ise tüm oyunculara bildirilir ve oda kapatılır
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

        /** Bir oyuncu oyunu bırakırsa (surrender), diğer oyuncuya otomatik galibiyet verilir. */
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

        //tüm oyunculara mesaj göndermek için kullanılır
        void broadcast(String msg) {
            for (SClient player : players) {
                player.send(msg);
            }
        }

        // Sıra kimdeyse ona bildirim gönderir
        void sendTurnToCurrentPlayer() {
            SClient current = players.get(currentPlayerIndex);
            broadcast("TURN:" + current.clientId);
        }

        // oyuncu zar atarsa işlemleri yapar ve sonucu gönderir
        void processRoll(String clientId) {
            if (!clientId.equals(players.get(currentPlayerIndex).clientId)) {
                // sıra bu oyuncuda değilse işlem yapılmaz
                return;
            }

            int roll = random.nextInt(6) + 1;          // 1-6 arası zar
            int oldPos = positions.getOrDefault(clientId, 1);
            int newPos = Math.min(100, oldPos + roll); // 100'ü aşamaz

            newPos = applySnakesAndLadders(newPos);    // yılan veya merdiven kontrolü

            positions.put(clientId, newPos);
            broadcast("MOVE:" + clientId + ":" + roll + ":" + newPos);

            if (newPos == 100) {
                broadcast("WINNER:" + clientId);        // kazanan bildir
                return;
            }

            // sıradaki oyuncuya geç
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            sendTurnToCurrentPlayer();
        }

        //Yılan/merdiven var mı kontrolü ve yeni konumun belirlenmesi için
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