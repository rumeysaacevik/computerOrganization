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
    private JTextArea txtArea;
    private JButton btnRoll;
    private JButton btnSurrender; // Yeni buton
    private JPanel boardPanel;
    private String myId = "";
    private boolean myTurn = false;
    private String playerName;
    private Map<String, Integer> positions = new HashMap<>();
    private JLabel[] cells = new JLabel[100];

    private final Map<Integer, Integer> ladders = Map.of(
            3, 22, 5, 8, 11, 26, 20, 29
    );
    private final Map<Integer, Integer> snakes = Map.of(
            27, 1, 17, 4, 19, 7
    );
    private JButton btnRestart;

    public GUIClient(String name) {
        this.playerName = name;

        setTitle("Snakes and Ladders - Client");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        boardPanel = new JPanel(new GridLayout(10, 10));
        for (int i = 99; i >= 0; i--) {
            JLabel cell = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            cell.setOpaque(true);
            cell.setBackground(Color.WHITE);
            cells[i] = cell;
            boardPanel.add(cell);
        }
        add(boardPanel, BorderLayout.CENTER);

        txtArea = new JTextArea(5, 20);
        txtArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtArea);
        add(scrollPane, BorderLayout.NORTH);

        JPanel southPanel = new JPanel();
        btnRoll = new JButton("ZAR AT");
        btnRoll.setEnabled(false);
        btnRoll.addActionListener(e -> {
            out.println("ROLL");
            btnRoll.setEnabled(false);
        });
        southPanel.add(btnRoll);
        btnRestart = new JButton("YENİ OYUN");
        btnRestart.setEnabled(false);  // oyun bitmeden pasif
        btnRestart.addActionListener(e -> {
            out.println("RESTART");
            btnRestart.setEnabled(false);
            txtArea.append("Yeni oyun isteği gönderildi.\n");
        });
        southPanel.add(btnRestart);

        btnSurrender = new JButton("SURRENDER");
        btnSurrender.setEnabled(false);
        btnSurrender.addActionListener(e -> {
            out.println("SURRENDER");
            btnSurrender.setEnabled(false);
            btnRoll.setEnabled(false);
            txtArea.append("Oyundan pes ettiniz.\n");
        });
        southPanel.add(btnSurrender);

        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("NAME:" + playerName); // Oyuncu adı gönderilir

            new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        processMessage(line);
                    }
                } catch (IOException e) {
                    txtArea.append("Bağlantı koptu...\n");
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
                txtArea.setText("ID: " + myId + "\n");

            } else if (msg.startsWith("MATCHED:")) {
                txtArea.append("✅ " + msg.substring(8).trim() + "\n");
                btnSurrender.setEnabled(true);

            } else if (msg.startsWith("TURN:")) {
                String activeId = msg.substring(5);
                myTurn = activeId.equals(myId);

                btnRoll.setEnabled(myTurn);
                btnSurrender.setEnabled(true);

                if (myTurn) {
                    txtArea.append("\n🎯 Sıra sende! Zar atma zamanı! 🎲\n");
                } else {
                    txtArea.append("🟢 Sıra: " + activeId + "\n");
                }

            } else if (msg.startsWith("MOVE:")) {
                String[] parts = msg.split(":");
                if (parts.length == 4) {
                    String player = parts[1];
                    int roll = Integer.parseInt(parts[2]);
                    int pos = Integer.parseInt(parts[3]);

                    positions.put(player, pos);
                    updateBoard();

                    txtArea.append(player + " zar attı: " + roll + ", Yeni konum: " + pos + "\n");
                }

            } else if (msg.startsWith("WINNER:") || msg.startsWith("SURRENDERED:")) {
                String info = msg.startsWith("WINNER:")
                        ? "🏆 Kazanan: " + msg.substring(7)
                        : "⚠️ " + msg.substring(11);
                txtArea.append(info + "\n");
                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(false);
                btnRestart.setEnabled(true);

            } else if (msg.startsWith("SURRENDERED:")) {
                txtArea.append("⚠️ " + msg.substring(11).trim() + "\n");
                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(false);

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
                txtArea.append("✅ Her iki oyuncu kabul etti. Yeni oyun başlıyor!\n");

            } else if (msg.startsWith("RESTART_DENIED")) {
                txtArea.append("❌ Rakip yeni oyunu kabul etmedi.\n");

                try {
                    out.println("EXIT");
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                dispose();
                new LoginScreen();

            } else if (msg.startsWith("NEW_GAME:")) {
                txtArea.append("🎲 Yeni oyun başlatıldı!\n");
                positions.clear();
                for (int i = 0; i < 100; i++) {
                    cells[i].setText(String.valueOf(i + 1));
                    cells[i].setBackground(Color.WHITE);

                    if (ladders.containsKey(i + 1)) {
                        cells[i].setBackground(Color.GREEN);
                    } else if (snakes.containsKey(i + 1)) {
                        cells[i].setBackground(Color.RED);
                    }
                }
                btnRestart.setEnabled(false);
                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(true);

            } else {
                txtArea.append(msg + "\n");
            }
        });
    }

    private void updateBoard() {
        for (int i = 0; i < 100; i++) {
            cells[i].setText(String.valueOf(i + 1));
            cells[i].setBackground(Color.WHITE);

            if (ladders.containsKey(i + 1)) {
                cells[i].setBackground(Color.GREEN);
            } else if (snakes.containsKey(i + 1)) {
                cells[i].setBackground(Color.RED);
            }
        }

        for (Map.Entry<String, Integer> entry : positions.entrySet()) {
            int index = entry.getValue() - 1;
            if (index >= 0 && index < 100) {
                String currentText = cells[index].getText();
                cells[index].setText(currentText + " [" + entry.getKey() + "]");
                cells[index].setBackground(Color.YELLOW);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
