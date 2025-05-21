/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.*;
import java.net.Socket;

/**
 *
 * @author Rümeysa
 */
// --- SClient.java ---
public class SClient extends Thread {

    Socket socket;
    Server server;
    String clientId;   // Örneğin Player1, Player2 gibi
    String playerName; // Gerçek kullanıcı adı, login ekranından gelir
    BufferedReader in;
    PrintWriter out;

    public SClient(Socket socket, Server server, String clientId) throws IOException {
        this.socket = socket;
        this.server = server;
        this.clientId = clientId;
        this.playerName = clientId; // İlk başta clientId ile aynı
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        } else {
            System.err.println("Client output stream is null.");
        }
    }

   @Override
public void run() {
    try {
        String msg;
        while ((msg = in.readLine()) != null) {
            if (msg.startsWith("NAME:")) {
                String oldId = this.clientId;
                String newName = msg.substring(5).trim();
                this.clientId = newName;
                this.playerName = newName;
                server.updateClientId(oldId, this.clientId);
                send("WELCOME:" + this.clientId);
                server.addClientToQueue(this);
            } else if (msg.equals("ROLL")) {
                server.processRoll(clientId);
            } else if (msg.equals("RESTART")) {
                server.processRestartRequest(clientId);
            } else if (msg.startsWith("RESTART_RESPONSE:")) {
                boolean accepted = msg.substring(17).equalsIgnoreCase("true");
                server.processRestartResponse(clientId, accepted);
            } else if (msg.equals("SURRENDER")) {
                server.processSurrender(clientId);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            socket.close(); // Bağlantıyı kapat
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        server.removeClient(this); // Sunucudan bu istemciyi sil
    }
}
}
