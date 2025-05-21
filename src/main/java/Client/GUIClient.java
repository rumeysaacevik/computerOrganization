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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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
    private JButton btnSurrender;
    private JPanel boardPanel;
    private String myId = "";
    private boolean myTurn = false;
    private String playerName;
    private Map<String, Integer> positions = new HashMap<>();
    private JLabel[] cells = new JLabel[100];
    private final ImageIcon iconB;
    private final ImageIcon iconR;

    private final Map<Integer, Integer> ladders = Map.of(
            3, 22,
            8, 30,
            28, 84,
            58, 77,
            75, 86
    );

    private final Map<Integer, Integer> snakes = Map.of(
            97, 78,
            89, 67,
            62, 19,
            36, 6,
            25, 5
    );

    private JButton btnRestart;

    public GUIClient(String name) {
        this.playerName = name;

        setTitle("Snakes and Ladders - Client");
        setSize(800, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        iconB = new ImageIcon("C:/Users/alpce/OneDrive/Masaüstü/playerB.png");
        iconR = new ImageIcon("C:/Users/alpce/OneDrive/Masaüstü/playerR.png");

        boardPanel = new JPanel(new GridLayout(10, 10));
        for (int i = 99; i >= 0; i--) {
            JLabel cell = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            cell.setOpaque(true);
            cell.setBackground(new Color(255, 230, 200));
            cells[i] = cell;
            boardPanel.add(cell);
        }
        add(boardPanel, BorderLayout.CENTER);

        txtArea = new JTextArea(5, 20);
        txtArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtArea);
        add(scrollPane, BorderLayout.EAST);

        JPanel southPanel = new JPanel();

        ImageIcon originalIcon = new ImageIcon("C:/Users/alpce/OneDrive/Masaüstü/dice.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon diceIcon = new ImageIcon(scaledImage);

        btnRoll = new JButton(diceIcon);
        btnRoll.setEnabled(false);
        btnRoll.setContentAreaFilled(false);
        btnRoll.setBorderPainted(false);
        btnRoll.setFocusPainted(false);
        btnRoll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRoll.setToolTipText("Zar At");
        btnRoll.addActionListener(e -> {
            playDiceSound();
            out.println("ROLL");
            btnRoll.setEnabled(false);
        });
        southPanel.add(btnRoll);

        ImageIcon restartIcon = new ImageIcon("C:/Users/alpce/OneDrive/Masaüstü/restart.png");
        Image restartImg = restartIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        btnRestart = new JButton(new ImageIcon(restartImg));
        btnRestart.setEnabled(false);
        btnRestart.setToolTipText("Yeni Oyuna Başla");
        btnRestart.setContentAreaFilled(false);
        btnRestart.setBorderPainted(false);
        btnRestart.setFocusPainted(false);
        btnRestart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRestart.addActionListener(e -> {
            out.println("RESTART");
            btnRestart.setEnabled(false);
            txtArea.append("Yeni oyun isteği gönderildi.\n");
        });
        southPanel.add(btnRestart);

        ImageIcon flagIcon = new ImageIcon("C:/Users/alpce/OneDrive/Masaüstü/flag (1).png");
        btnSurrender = new JButton(flagIcon);
        btnSurrender.setToolTipText("Pes Et");
        btnSurrender.setEnabled(false);
        btnSurrender.setContentAreaFilled(false);
        btnSurrender.setBorderPainted(false);
        btnSurrender.setFocusPainted(false);
        btnSurrender.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

    private void playDiceSound() {
        try {
            File soundFile = new File("C:/Users/alpce/OneDrive/Masaüstü/MANYDICE.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Zar sesi çalınamadı!");
            e.printStackTrace();
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("NAME:" + playerName);

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

    // Sınıfın başına şunu ekle:
    private String lastMessage = "";
    private String lastSurrenderedMsg = "";

    private void processMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.startsWith("WELCOME:")) {
                myId = msg.substring(8).trim();
                txtArea.setText("ID: " + myId + "\n");
                lastMessage = "";

            } else if (msg.startsWith("MATCHED:")) {
                txtArea.append("✅ " + msg.substring(8).trim() + "\n");
                lastMessage = "";
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
                lastMessage = "";

            } else if (msg.startsWith("MOVE:")) {
                String[] parts = msg.split(":");
                if (parts.length == 4) {
                    String player = parts[1];
                    int roll = Integer.parseInt(parts[2]);
                    int pos = Integer.parseInt(parts[3]);

                    positions.put(player, pos);
                    updateBoard();

                    txtArea.append(player + " zar attı: " + roll + ", Yeni konum: " + pos + "\n");
                    lastMessage = "";
                }

            } else if (msg.startsWith("WINNER:")) {
                String winner = msg.substring(7).trim();
                txtArea.append("🏆 Kazanan: " + winner + "\n");

                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(false);
                btnRestart.setEnabled(true);

                if (winner.equals(myId)) {
                    // Sadece kazanan için kutlama ekranı
                    JPanel panel = new JPanel();
                    panel.setBackground(new Color(255, 245, 220));
                    panel.setLayout(new BorderLayout());

                    JLabel fireworks = new JLabel("🎆🎉🎇", SwingConstants.CENTER);
                    fireworks.setFont(new Font("Serif", Font.PLAIN, 50));

                    JLabel congrats = new JLabel("Tebrikler " + winner + "!", SwingConstants.CENTER);
                    congrats.setFont(new Font("SansSerif", Font.BOLD, 24));
                    congrats.setForeground(new Color(50, 50, 50));

                    panel.add(fireworks, BorderLayout.NORTH);
                    panel.add(congrats, BorderLayout.CENTER);

                    JOptionPane.showMessageDialog(
                            this,
                            panel,
                            "🏆 Oyun Bitti!",
                            JOptionPane.PLAIN_MESSAGE
                    );
                }
                lastMessage = "";

            } else if (msg.startsWith("SURRENDERED:")) {
                // Çiftli mesajı temizle!
                String surrenderedMsg = msg.substring(12).trim();
                // Sunucu bazen iki kere yollayabiliyor; buradan yakalayıp filtrele
                surrenderedMsg = surrenderedMsg.replaceAll("(oyundan pes ettiniz\\.)+", "oyundan pes ettiniz.");
                surrenderedMsg = surrenderedMsg.replaceAll("(oyundan pes etti\\.)+", "oyundan pes etti.");
                String display = "⚠️ " + surrenderedMsg + "\n";

                // Son yazılan SURRENDERED mesajı ile aynıysa tekrar yazma!
                if (!display.equals(lastSurrenderedMsg)) {
                    txtArea.append(display);
                    lastSurrenderedMsg = display;
                }
                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(false);
                btnRestart.setEnabled(true);

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
                lastMessage = "";

            } else if (msg.startsWith("RESTART_CONFIRMED")) {
                txtArea.append("✅ Her iki oyuncu kabul etti. Yeni oyun başlıyor!\n");
                lastMessage = "";

            } else if (msg.startsWith("RESTART_DENIED")) {
                txtArea.append("❌ Rakip yeni oyunu kabul etmedi.\n");
                btnRestart.setEnabled(true);
                lastMessage = "";

            } else if (msg.startsWith("NEW_GAME:")) {
                txtArea.append("🎲 Yeni oyun başlatıldı!\n");
                positions.clear();
                updateBoard();
                btnRestart.setEnabled(false);
                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(true);
                lastMessage = "";

            } else if (msg.equals("EXIT")) {
                this.dispose();
                new LoginScreen();

            } else {
                // Tekrarlı mesajı bir daha yazma
                String toPrint = msg + "\n";
                if (!lastMessage.equals(toPrint)) {
                    txtArea.append(toPrint);
                    lastMessage = toPrint;
                }
            }
        });
    }

    private void updateBoard() {
        for (int i = 0; i < 100; i++) {
            cells[i].setText(String.valueOf(i + 1));
            cells[i].setBackground(new Color(255, 230, 200));
            cells[i].setIcon(null);

            if (ladders.containsKey(i + 1)) {
                cells[i].setBackground(Color.GREEN);
            } else if (snakes.containsKey(i + 1)) {
                cells[i].setBackground(Color.RED);
            }
        }

        int count = 0;
        for (Map.Entry<String, Integer> entry : positions.entrySet()) {
            int index = entry.getValue() - 1;
            if (index >= 0 && index < 100) {
                ImageIcon icon = (count == 0) ? iconB : iconR;
                cells[index].setIcon(icon);
                count++;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
