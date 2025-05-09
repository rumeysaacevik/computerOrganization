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
    private List<SClient> clients = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private Random random = new Random();
    private Map<String, Integer> positions = new HashMap<>();

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void Start() {
        System.out.println("Sunucu başlatıldı.");
        new Thread(this::listenForClients).start();
    }

    private void listenForClients() {
        try {
            while (clients.size() < 2) {
                Socket socket = serverSocket.accept();
                String clientId = "Player" + (clients.size() + 1);
                SClient client = new SClient(socket, this, clientId);
                clients.add(client);
                positions.put(clientId, 1);
                broadcast("JOINED:" + clientId);
                client.send("WELCOME:" + clientId);
                client.start();
            }

            // Oyuna başla
            sendTurnToCurrentPlayer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processRoll(String clientId) {
        if (!clientId.equals(clients.get(currentPlayerIndex).clientId)) {
            return; // Sırası değilse yoksay
        }

        int roll = random.nextInt(6) + 1;
        int oldPos = positions.get(clientId);
        int newPos = Math.min(100, oldPos + roll);
        newPos = applySnakesAndLadders(newPos);

        positions.put(clientId, newPos);
        broadcast("MOVE:" + clientId + ":" + roll + ":" + newPos);

        if (newPos == 100) {
            broadcast("WINNER:" + clientId);
            return;
        }

        // Sırayı değiştir
        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        sendTurnToCurrentPlayer();
    }

    private void sendTurnToCurrentPlayer() {
        SClient current = clients.get(currentPlayerIndex);
        current.send("YOUR_TURN");
        broadcast("TURN:" + current.clientId);
    }

    private int applySnakesAndLadders(int pos) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(3, 22);
        map.put(5, 8);
        map.put(11, 26);
        map.put(20, 29);
        map.put(34, 54);
        map.put(91, 73);
        map.put(17, 4);
        map.put(19, 7);
        map.put(21, 9);
        map.put(27, 1);
        map.put(62, 18);
        map.put(87, 36);
        return map.getOrDefault(pos, pos);
    }

    public void broadcast(String msg) {
        for (SClient client : clients) {
            client.send(msg);
        }
    }
}
