/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;

/**
 *
 * @author Rümeysa
 */
public class ServerMain {

    public static void main(String[] args) {
        try {
            Server server = new Server(5000);  // Port 5000
            server.Start(); // Dinlemeye başla
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


