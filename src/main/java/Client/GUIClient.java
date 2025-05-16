/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 *
 * @author Rümeysa
 */
public class GUIClient extends JFrame {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String myId = "";
    private boolean myTurn = false;
    private String playerName;

    private final Map<String, Integer> positions = new HashMap<>();
    private final Map<Integer, Integer> ladders = Map.of(3, 22, 5, 8, 11, 26, 20, 29);
    private final Map<Integer, Integer> snakes = Map.of(27, 1, 17, 4, 19, 7);

    private GameBoardPanel boardPanel;
    private ControlPanel controlPanel;

    public GUIClient(String name) {
        this.playerName = name;

        setTitle("Snakes and Ladders - Client");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        boardPanel = new GameBoardPanel(ladders, snakes);
        controlPanel = new ControlPanel();

        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("NAME:" + playerName);

            controlPanel.setRollAction(e -> {
                out.println("ROLL");
                controlPanel.setRollEnabled(false);
            });

            controlPanel.setSurrenderAction(e -> {
                out.println("SURRENDER");
                controlPanel.setRollEnabled(false);
                controlPanel.setSurrenderEnabled(false);
                controlPanel.appendText("Oyundan pes ettiniz.\n");
            });

            controlPanel.setRestartAction(e -> {
                out.println("RESTART");
                controlPanel.setRestartEnabled(false);
                controlPanel.appendText("Yeni oyun isteği gönderildi.\n");
            });

            new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        processMessage(line);
                    }
                } catch (IOException e) {
                    controlPanel.appendText("Bağlantı koptu...\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Sunucuya bağlanılamadı.");
        }
    }

    private void processMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.startsWith("WELCOME:")) {
                myId = msg.substring(8).trim();
                controlPanel.setPlayerId(myId);

            } else if (msg.startsWith("MATCHED:")) {
                controlPanel.appendText("✅ " + msg.substring(8).trim() + "\n");
                controlPanel.setSurrenderEnabled(true);

            } else if (msg.startsWith("TURN:")) {
                String activeId = msg.substring(5).trim();
                myTurn = activeId.equals(myId);

                controlPanel.setRollEnabled(myTurn);
                controlPanel.setSurrenderEnabled(true);

                if (myTurn) {
                    controlPanel.appendText("\n🎯 Sıra sende! Zar atma zamanı! 🎲\n");
                } else {
                    controlPanel.appendText("🟢 Sıra: " + activeId + "\n");
                }

            } else if (msg.startsWith("MOVE:")) {
                String[] parts = msg.split(":");
                if (parts.length == 4) {
                    String player = parts[1];
                    int roll = Integer.parseInt(parts[2]);
                    int pos = Integer.parseInt(parts[3]);

                    positions.put(player, pos);
                    boardPanel.updatePositions(positions);

                    controlPanel.appendText(player + " zar attı: " + roll + ", Yeni konum: " + pos + "\n");
                }

            } else if (msg.startsWith("WINNER:") || msg.startsWith("SURRENDERED:")) {
                String info = msg.startsWith("WINNER:")
                        ? "🏆 Kazanan: " + msg.substring(7)
                        : "⚠️ " + msg.substring(11);
                controlPanel.appendText(info + "\n");

                controlPanel.setRollEnabled(false);
                controlPanel.setSurrenderEnabled(false);
                controlPanel.setRestartEnabled(true);

            } else if (msg.startsWith("RESTART_REQUEST_FROM:")) {
                String fromPlayer = msg.substring("RESTART_REQUEST_FROM:".length()).trim();
                int response = JOptionPane.showConfirmDialog(
                        this,
                        fromPlayer + " yeni bir oyun başlatmak istiyor. Kabul ediyor musunuz?",
                        "Yeni Oyun Daveti",
                        JOptionPane.YES_NO_OPTION
                );
                if (response == JOptionPane.YES_OPTION) {
                    out.println("RESTART_RESPONSE:true");
                } else {
                    out.println("RESTART_RESPONSE:false");
                }

            } else if (msg.startsWith("RESTART_CONFIRMED")) {
                controlPanel.appendText("✅ Her iki oyuncu kabul etti. Yeni oyun başlıyor!\n");

            } else if (msg.startsWith("RESTART_DENIED")) {
                controlPanel.appendText("❌ Rakip yeni oyunu kabul etmedi.\n");
                try {
                    out.println("EXIT");
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                dispose();
                new LoginScreen();

            } else if (msg.startsWith("NEW_GAME:")) {
                controlPanel.appendText("🎲 Yeni oyun başlatıldı!\n");
                positions.clear();
                boardPanel.updatePositions(positions);
                controlPanel.setRestartEnabled(false);
                controlPanel.setRollEnabled(false);
                controlPanel.setSurrenderEnabled(true);

            } else {
                controlPanel.appendText(msg + "\n");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
