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
    private JPanel boardPanel;

    private String myId = "";
    private boolean myTurn = false;

    private Map<String, Integer> positions = new HashMap<>();
    private JLabel[] cells = new JLabel[100];

    // Merdiven ve yılan pozisyonları
    private final Map<Integer, Integer> ladders = Map.of(
        3, 22,
        5, 8,
        11, 26,
        20, 29
    );

    private final Map<Integer, Integer> snakes = Map.of(
        27, 1,
        17, 4,
        19, 7
    );

    public GUIClient() {
        setTitle("Snakes and Ladders - Client");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // TAHTA
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

        // MESAJ ALANI
        txtArea = new JTextArea(5, 20);
        txtArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtArea);
        add(scrollPane, BorderLayout.NORTH);

        // ZAR AT BUTONU
        btnRoll = new JButton("ZAR AT");
        btnRoll.setEnabled(false);
        btnRoll.addActionListener(e -> {
            out.println("ROLL");
            btnRoll.setEnabled(false);
        });
        add(btnRoll, BorderLayout.SOUTH);

        setVisible(true);
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

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
                myId = msg.substring(8);
                txtArea.append("ID: " + myId + "\n");

            } else if (msg.startsWith("TURN:")) {
                String activeId = msg.substring(5);
                myTurn = activeId.equals(myId);
                txtArea.append("Sıra: " + activeId + "\n");
                btnRoll.setEnabled(myTurn);

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

            } else if (msg.startsWith("WINNER:")) {
                String winner = msg.substring(7);
                txtArea.append("🏆 Kazanan: " + winner + "\n");
                btnRoll.setEnabled(false);

            } else if (msg.startsWith("JOINED:")) {
                String joinedPlayer = msg.substring(7);
                positions.putIfAbsent(joinedPlayer, 0);
                updateBoard();

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
                cells[i].setBackground(Color.GREEN); // Merdiven
            } else if (snakes.containsKey(i + 1)) {
                cells[i].setBackground(Color.RED); // Yılan
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
        SwingUtilities.invokeLater(() -> new GUIClient());
    }
}