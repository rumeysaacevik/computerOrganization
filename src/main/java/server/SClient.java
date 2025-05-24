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


 //bir istemciyi (oyuncuyu) sunucu tarafında temsil eden thread sınıfıdır.
 //her client bağlantısı için bir SClient oluşturulur.
 
public class SClient extends Thread {

    Socket socket;        // istemciye ait socket
    Server server;        // bağlı olduğu ana sunucu objesi
    String clientId;      // başta: Player1/Player2, sonra: oyuncu ismi
    String playerName;    // oyuncunun görünen ismi
    BufferedReader in;    // istemciden gelen mesajları okumak için
    PrintWriter out;      // istemciye mesaj göndermek için

    //kuruc(socket ve server referansı ile başlatılır)
    public SClient(Socket socket, Server server, String clientId) throws IOException {
        this.socket = socket;
        this.server = server;
        this.clientId = clientId;
        this.playerName = clientId;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

   //istemciye mesaj göndermek için kullanılır.
    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    
     //Bu thread başlatıldığında (start() çağrıldığında) çalışır.
     //Sürekli olarak istemciden gelen mesajları okur ve sunucuya bildirir.
     
    @Override
    public void run() {
        try {
            String msg;
            // istemciden mesaj geldiği sürece döngü devam eder
            while ((msg = in.readLine()) != null) {
                // ilk mesaj olarak oyuncu ismini alır
                if (msg.startsWith("NAME:")) {
                    String oldId = this.clientId;
                    String newName = msg.substring(5).trim();
                    this.clientId = newName;
                    this.playerName = newName;
                    server.updateClientId(oldId, this.clientId);  // sunucuda ID güncellenir
                    send("WELCOME:" + this.clientId);            // hoşgeldin mesajı gönder
                    server.addClientToQueue(this);               // oyun sırasına eklenir
                }
                // zar atma isteği
                else if (msg.equals("ROLL")) {
                    server.processRoll(clientId);
                }
                // yeni oyun başlatma isteği
                else if (msg.equals("RESTART")) {
                    server.processRestartRequest(clientId);
                }
                // yeni oyun teklifine verilen cevap
                else if (msg.startsWith("RESTART_RESPONSE:")) {
                    boolean accepted = msg.substring(17).equalsIgnoreCase("true");
                    server.processRestartResponse(clientId, accepted);
                }
                // teslim olma (oyunu bırakma)
                else if (msg.equals("SURRENDER")) {
                    server.processSurrender(clientId);
                }
                // sohbet mesajı
                else if (msg.startsWith("CHAT:")) {
                    String chatMsg = "[" + playerName + "]: " + msg.substring(5).trim();
                    server.sendChatToRoom(this, chatMsg); // Mesajı tüm oda oyuncularına ilet
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // bağlantı sonlandığında: socket kapatılır ve sunucudan oyuncu silinir
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            server.removeClient(this);
        }
    }
}
