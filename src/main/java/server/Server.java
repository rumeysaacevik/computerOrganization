/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Rümeysa
 */
public class Server extends Thread {

    ServerSocket serverSocket;
    List<SClient> clients = new ArrayList<>();
    Map<String, Integer> positions = new HashMap<>();
    int currentPlayerIndex = 0;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void Start() {
        System.out.println("Sunucu başlatıldı. Port: " + serverSocket.getLocalPort());
        this.start();
    }

    public synchronized void broadcast(String msg) throws IOException {
        for (SClient client : clients) {
            client.send(msg);
        }
    }

    public synchronized void nextTurn() throws IOException {
        String currentId = clients.get(currentPlayerIndex).clientId;
        clients.get(currentPlayerIndex).send("YOUR_TURN");
        broadcast("TURN:" + currentId);
    }

    public synchronized void processRoll(String clientId) throws IOException {
        if (!clientId.equals(clients.get(currentPlayerIndex).clientId)) {
            return;
        }

        int roll = new Random().nextInt(6) + 1;
        int pos = positions.getOrDefault(clientId, 0) + roll;
        pos = applySnakesAndLadders(pos);
        positions.put(clientId, pos);
        broadcast("MOVE:" + clientId + ":" + roll + ":" + pos);

        if (pos >= 100) {
            broadcast("WINNER:" + clientId);
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        nextTurn();
    }

    private int applySnakesAndLadders(int pos) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(3, 22);
        map.put(5, 8);
        map.put(11, 26);
        map.put(20, 29);
        map.put(27, 1);
        map.put(21, 9);
        map.put(17, 4);
        map.put(19, 7);
        map.put(34, 54);
        map.put(62, 18);
        map.put(87, 36);
        map.put(91, 73);

        return map.getOrDefault(pos, pos);
    }

    @Override
    public void run() {
        try {
            while (clients.size() < 2) {
                Socket s = serverSocket.accept();
                String clientId = "Player" + (clients.size() + 1);
                SClient client = new SClient(s, this, clientId);
                clients.add(client);
                positions.put(clientId, 0);
                client.start();
                broadcast("JOINED:" + clientId);
            }
            nextTurn();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
