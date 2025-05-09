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
    String clientId;
    BufferedReader in;
    PrintWriter out;

    public SClient(Socket socket, Server server, String clientId) throws IOException {
        this.socket = socket;
        this.server = server;
        this.clientId = clientId;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                if (msg.startsWith("NAME:")) {
                    String oldId = this.clientId;
                    this.clientId = msg.substring(5).trim();
                    server.updateClientId(oldId, this.clientId);
                    send("WELCOME:" + this.clientId);
                    server.broadcast("JOINED:" + this.clientId);
                } else if (msg.equals("ROLL")) {
                    server.processRoll(clientId);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
