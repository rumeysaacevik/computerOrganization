/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Rümeysa
 */
// bağlantı için test classı
 
 
public class TestClient4 {
    public static void main(String[] args) {
        try {
            // sunucuya bağlantı kurulur (localhost, port 5000)
            Socket socket = new Socket("localhost", 5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);

            // sunucudan gelen mesajları sürekli dinleyen ayrı bir thread başlatılır
            new Thread(() -> {
                String serverMsg;
                try {
                    while ((serverMsg = in.readLine()) != null) {
                        // gelen mesajı konsola yazdırıyor
                        System.out.println("Server: " + serverMsg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // kullanıcıdan komut alır ve sunucuya iletir
            while (true) {
                String input = scanner.nextLine();
                // kullanıcı "ROLL" yazarsa sunucuya ROLL komutu gönderilir
                if (input.equalsIgnoreCase("ROLL")) {
                    out.println("ROLL");
                }
                // kullanıcı "exit" yazarsa bağlantı kapanır ve programdan çıkılır
                else if (input.equalsIgnoreCase("exit")) {
                    socket.close();
                    break;
                }
                // buraya istersek baska komutları da ekleyebiliriz 
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }