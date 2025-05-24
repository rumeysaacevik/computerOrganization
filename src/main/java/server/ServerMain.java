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

//programın giriş noktasıdır sunucuyu başlatır
public class ServerMain {

    public static void main(String[] args) {
        try {
            Server server = new Server(5000);  // Port 5000 üzerinden Server nesnesi oluşturulur
            server.start(); // sunucu dinlemeye başlar (istemci bağlantısı bekler)
        } catch (IOException e) {
            //sunucu başlatılırken hata oluşursa ekrana yazdırılır
            e.printStackTrace();
        }
    }
}


