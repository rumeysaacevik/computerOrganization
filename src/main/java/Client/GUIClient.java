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
import java.util.LinkedHashMap;
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

    private JTextArea logArea;
    private JTextArea chatArea;
    private JButton btnRoll, btnSurrender, btnRestart;
    private BoardPanel boardPanel;
    private String myId = "";
    private boolean myTurn = false;
    private String playerName;
    private LinkedHashMap<String, Integer> positions = new LinkedHashMap<>();
    private final ImageIcon iconB, iconR;
    private final Image ladderImg, snakeImg;
    private String activePlayerId = null;
    private String serverIp;

    private final Map<Integer, Integer> ladders = Map.of(
            3, 22, 8, 30, 33, 65, 58, 77, 75, 86
    );

    private final Map<Integer, Integer> snakes = Map.of(
            97, 78, 89, 67, 62, 19, 36, 6, 25, 5
    );

    public GUIClient(String name, String serverIp) {
        this.playerName = name;
        this.serverIp = serverIp;

        setTitle("Snakes and Ladders - Client");
        setSize(950, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Images from resources
        iconB = new ImageIcon(getClass().getResource("/pieces/playerB.png"));
        iconR = new ImageIcon(getClass().getResource("/pieces/playerR.png"));
        ladderImg = new ImageIcon(getClass().getResource("/pieces/ladder.png")).getImage();
        snakeImg = new ImageIcon(getClass().getResource("/pieces/snake_transparent.png")).getImage();

        boardPanel = new BoardPanel(ladders, snakes, positions, iconB, iconR, ladderImg, snakeImg);
        add(boardPanel, BorderLayout.CENTER);

        // ==== RIGHT PANEL: Chat + Log ====
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        String[] presetMsgs = {"Good luck!", "Well played!", "Faster please!", "I have to go!", "Thank you!", "Sorry 😅"};
        JPanel presetPanel = new JPanel(new GridLayout(2, 3, 3, 3));
        for (String msg : presetMsgs) {
            JButton btn = new JButton(msg);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btn.addActionListener(e -> sendChat(msg));
            presetPanel.add(btn);
        }
        rightPanel.add(presetPanel, BorderLayout.NORTH);

        chatArea = new JTextArea(7, 25);
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createTitledBorder("Messages"));
        rightPanel.add(chatScroll, BorderLayout.CENTER);

        JPanel chatInputPanel = new JPanel(new BorderLayout(3, 0));
        JTextField chatField = new JTextField();
        chatField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> {
            String msg = chatField.getText().trim();
            if (!msg.isEmpty()) {
                sendChat(msg);
                chatField.setText("");
            }
        });
        chatInputPanel.add(chatField, BorderLayout.CENTER);
        chatInputPanel.add(sendBtn, BorderLayout.EAST);
        rightPanel.add(chatInputPanel, BorderLayout.SOUTH);

        logArea = new JTextArea(11, 25);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Game Log"));

        JPanel eastWrap = new JPanel(new BorderLayout(5, 5));
        eastWrap.add(rightPanel, BorderLayout.NORTH);
        eastWrap.add(logScroll, BorderLayout.CENTER);

        add(eastWrap, BorderLayout.EAST);

        // ==== BOTTOM PANEL ====
        JPanel southPanel = new JPanel();

        ImageIcon diceIcon = new ImageIcon(new ImageIcon(getClass().getResource("/pieces/dice.png")).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        btnRoll = new JButton(diceIcon);
        btnRoll.setEnabled(false);
        btnRoll.setContentAreaFilled(false);
        btnRoll.setBorderPainted(false);
        btnRoll.setFocusPainted(false);
        btnRoll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRoll.setToolTipText("Roll Dice");
        btnRoll.addActionListener(e -> {
            playDiceSound();
            out.println("ROLL");
            btnRoll.setEnabled(false);
        });
        southPanel.add(btnRoll);

        ImageIcon restartIcon = new ImageIcon(new ImageIcon(getClass().getResource("/pieces/restart.png")).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        btnRestart = new JButton(restartIcon);
        btnRestart.setEnabled(false);
        btnRestart.setToolTipText("Start New Game");
        btnRestart.setContentAreaFilled(false);
        btnRestart.setBorderPainted(false);
        btnRestart.setFocusPainted(false);
        btnRestart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRestart.addActionListener(e -> {
            out.println("RESTART");
            btnRestart.setEnabled(false);
            logArea.append("New game request sent.\n");
        });
        southPanel.add(btnRestart);

        ImageIcon flagIcon = new ImageIcon(getClass().getResource("/pieces/flag.png"));
        btnSurrender = new JButton(flagIcon);
        btnSurrender.setToolTipText("Surrender");
        btnSurrender.setEnabled(false);
        btnSurrender.setContentAreaFilled(false);
        btnSurrender.setBorderPainted(false);
        btnSurrender.setFocusPainted(false);
        btnSurrender.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSurrender.addActionListener(e -> {
            out.println("SURRENDER");
            btnSurrender.setEnabled(false);
            btnRoll.setEnabled(false);
            logArea.append("You surrendered.\n");
        });
        southPanel.add(btnSurrender);

        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
        connectToServer();
    }

    // ---- Play dice sound from resources ----
    private void playDiceSound() {
        try {
            InputStream audioSrc = getClass().getResourceAsStream("/sounds/MANYDICE.wav");
            if (audioSrc == null) {
                throw new FileNotFoundException("Sound file not found!");
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(audioSrc));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Dice sound could not be played!");
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverIp, 5000);
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
                    logArea.append("Connection lost...\n");
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server.");
        }
    }

    private void processMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.startsWith("WELCOME:")) {
                myId = msg.substring(8).trim();
                logArea.setText("ID: " + myId + "\n");
            } else if (msg.startsWith("MATCHED:")) {
                logArea.append("✅ " + msg.substring(8).trim() + "\n");
                btnSurrender.setEnabled(true);
            } else if (msg.startsWith("TURN:")) {
                String activeId = msg.substring(5).trim();
                myTurn = activeId.equals(myId);
                this.activePlayerId = activeId;
                boardPanel.setActivePlayer(activeId);
                btnRoll.setEnabled(myTurn);
                btnSurrender.setEnabled(true);
                if (myTurn) {
                    logArea.append("\n🎯 It's your turn! Roll the dice! 🎲\n");
                } else {
                    logArea.append("🟢 Turn: " + activeId + "\n");
                }
            } else if (msg.startsWith("CHAT:")) {
                String chatMsg = msg.substring(5);
                chatArea.append(chatMsg + "\n");
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            } else if (msg.startsWith("MOVE:")) {
                String[] parts = msg.split(":");
                if (parts.length == 4) {
                    String player = parts[1];
                    int roll = Integer.parseInt(parts[2]);
                    int pos = Integer.parseInt(parts[3]);
                    if (!positions.containsKey(player)) {
                        positions.put(player, pos);
                    } else {
                        positions.replace(player, pos);
                    }
                    updateBoard();
                    logArea.append(player + " rolled: " + roll + ", New position: " + pos + "\n");
                }
            } else if (msg.startsWith("WINNER:")) {
                String winner = msg.substring(7).trim();
                logArea.append("🏆 Winner: " + winner + "\n");
                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(false);
                btnRestart.setEnabled(true);
                if (winner.equals(myId)) {
                    // --- Winner Popup ---
                    JPanel panel = new JPanel();
                    panel.setBackground(new Color(255, 247, 230));
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(240, 180, 70), 4, true),
                            BorderFactory.createEmptyBorder(16, 32, 16, 32)
                    ));

                    JLabel fireworks = new JLabel("🎆  🎉  🥳  🎊  🎇", SwingConstants.CENTER);
                    fireworks.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
                    fireworks.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel congrats = new JLabel("<html><div style='text-align:center;'>"
                            + "<span style='font-size:28pt; font-weight:bold; color:#ca8000;'>Congratulations<br>"
                            + winner + "!</span></div></html>", SwingConstants.CENTER);
                    congrats.setFont(new Font("Segoe UI", Font.BOLD, 26));
                    congrats.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel sub = new JLabel("You won the game! 🎲", SwingConstants.CENTER);
                    sub.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    sub.setForeground(new Color(120, 120, 120));
                    sub.setAlignmentX(Component.CENTER_ALIGNMENT);

                    panel.add(Box.createVerticalStrut(8));
                    panel.add(fireworks);
                    panel.add(Box.createVerticalStrut(12));
                    panel.add(congrats);
                    panel.add(Box.createVerticalStrut(12));
                    panel.add(sub);
                    panel.add(Box.createVerticalStrut(8));

                    JOptionPane.showMessageDialog(
                            this,
                            panel,
                            "🏆 Game Over!",
                            JOptionPane.PLAIN_MESSAGE
                    );
                }
            } else if (msg.startsWith("SURRENDERED:")) {
                String surrenderedMsg = msg.substring(12).trim();
                logArea.append("⚠️ " + surrenderedMsg + "\n");
                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(false);
                btnRestart.setEnabled(true);
            } else if (msg.startsWith("RESTART_REQUEST_FROM:")) {
                String fromPlayer = msg.substring("RESTART_REQUEST_FROM:".length()).trim();
                int response = JOptionPane.showConfirmDialog(
                        this, fromPlayer + " wants to start a new game. Do you accept?",
                        "New Game Invitation", JOptionPane.YES_NO_OPTION);
                out.println("RESTART_RESPONSE:" + (response == JOptionPane.YES_OPTION ? "true" : "false"));
            } else if (msg.startsWith("RESTART_CONFIRMED")) {
                logArea.append("✅ Both players accepted. New game is starting!\n");
            } else if (msg.startsWith("RESTART_DENIED")) {
                logArea.append("❌ Opponent did not accept the new game.\n");
                btnRestart.setEnabled(true);
            } else if (msg.startsWith("NEW_GAME:")) {
                logArea.append("🎲 New game started!\n");
                positions.clear();
                updateBoard();
                btnRestart.setEnabled(false);
                btnRoll.setEnabled(false);
                btnSurrender.setEnabled(true);
            } else if (msg.equals("EXIT")) {
                this.dispose();
                new LoginScreen();
            }
        });
    }

    private void updateBoard() {
        boardPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }

    // ---- BoardPanel ----
    class BoardPanel extends JPanel {

        private final int rows = 10, cols = 10;
        private final Map<Integer, Integer> ladders, snakes;
        private final LinkedHashMap<String, Integer> playerPositions;
        private final ImageIcon iconB, iconR;
        private final Image ladderImg, snakeImg;
        private String activePlayer = null;

        public BoardPanel(Map<Integer, Integer> ladders, Map<Integer, Integer> snakes,
                LinkedHashMap<String, Integer> playerPositions, ImageIcon iconB, ImageIcon iconR,
                Image ladderImg, Image snakeImg) {
            this.ladders = ladders;
            this.snakes = snakes;
            this.playerPositions = playerPositions;
            this.iconB = iconB;
            this.iconR = iconR;
            this.ladderImg = ladderImg;
            this.snakeImg = snakeImg;
            setPreferredSize(new Dimension(800, 650));
        }

        public void setActivePlayer(String playerId) {
            this.activePlayer = playerId;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int panelW = getWidth(), panelH = getHeight();
            int cellW = (panelW - 40) / cols, cellH = (panelH - 60) / rows;
            int cellSize = Math.min(cellW, cellH);
            drawPlayerNamesBar(g, panelW, cellSize);

            for (int i = 0; i < 100; i++) {
                int[] xy = getCellXY(i + 1, cellSize, panelW, panelH);
                g.setColor(new Color(255, 230, 200));
                g.fillRect(xy[0], xy[1], cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(xy[0], xy[1], cellSize, cellSize);
                g.setColor(Color.BLACK);
                // Daha belirgin font, kalın ve büyük
                g.setFont(new Font("SansSerif", Font.BOLD, cellSize / 3 + 6));
                g.setColor(new Color(30, 30, 30)); // Çok koyu gri, istersen Color.BLACK de olur
                g.drawString(String.valueOf(i + 1), xy[0] + 8, xy[1] + 24);

            }
            Graphics2D g2 = (Graphics2D) g;
            if (ladderImg != null) {
                for (var entry : ladders.entrySet()) {
                    int[] start = getCellCenter(entry.getKey(), cellSize, panelW, panelH);
                    int[] end = getCellCenter(entry.getValue(), cellSize, panelW, panelH);
                    drawImageBetween(g2, ladderImg, start[0], start[1], end[0], end[1], cellSize);
                }
            }
            if (snakeImg != null) {
                for (var entry : snakes.entrySet()) {
                    int[] start = getCellCenter(entry.getKey(), cellSize, panelW, panelH);
                    int[] end = getCellCenter(entry.getValue(), cellSize, panelW, panelH);
                    drawImageBetween(g2, snakeImg, start[0], start[1], end[0], end[1], cellSize);
                }
            }
            int idx = 0;
            for (var entry : playerPositions.entrySet()) {
                int[] center = getCellCenter(entry.getValue(), cellSize, panelW, panelH);
                ImageIcon icon = idx == 0 ? iconB : iconR;
                if (icon != null) {
                    g.drawImage(icon.getImage(), center[0] - cellSize / 4, center[1] - cellSize / 4, cellSize / 2, cellSize / 2, null);
                } else {
                    g.setColor(idx == 0 ? Color.BLUE : Color.RED);
                    g.fillOval(center[0] - cellSize / 4, center[1] - cellSize / 4, cellSize / 2, cellSize / 2);
                }
                idx++;
            }
        }

        private void drawPlayerNamesBar(Graphics g, int panelW, int cellSize) {
            int barH = cellSize / 2 + 10, y = 5, x = (panelW - cellSize * cols) / 2;
            String[] names = playerPositions.keySet().toArray(new String[0]);
            if (names.length == 0) {
                return;
            }
            int w = (cellSize * cols) / 2;

            // Player 1
            String name1 = names[0];
            boolean turn1 = name1.equals(activePlayer);
            if (turn1) {
                g.setColor(new Color(255, 255, 170));
                g.fillRoundRect(x - 4, y - 4, w + 8, barH + 8, 22, 22);
                g.setColor(new Color(255, 50, 50));
                g.drawRoundRect(x - 4, y - 4, w + 8, barH + 8, 22, 22);
            }
            g.setColor(new Color(255, 180, 180));
            g.fillRoundRect(x, y, w, barH, 16, 16);
            g.setColor(Color.RED);
            g.drawRoundRect(x, y, w, barH, 16, 16);
            g.setColor(turn1 ? Color.RED.darker() : Color.DARK_GRAY);
            g.setFont(new Font("SansSerif", turn1 ? Font.BOLD : Font.PLAIN, cellSize / 3));
            drawCenteredString(g, "🔴 " + name1, x, y, w, barH);

            // Player 2
            if (names.length > 1) {
                String name2 = names[1];
                boolean turn2 = name2.equals(activePlayer);
                if (turn2) {
                    g.setColor(new Color(210, 250, 210));
                    g.fillRoundRect(x + w - 4, y - 4, w + 8, barH + 8, 22, 22);
                    g.setColor(new Color(0, 120, 255));
                    g.drawRoundRect(x + w - 4, y - 4, w + 8, barH + 8, 22, 22);
                }
                g.setColor(new Color(180, 200, 255));
                g.fillRoundRect(x + w, y, w, barH, 16, 16);
                g.setColor(Color.BLUE);
                g.drawRoundRect(x + w, y, w, barH, 16, 16);

                g.setColor(turn2 ? Color.BLUE.darker() : Color.DARK_GRAY);
                g.setFont(new Font("SansSerif", turn2 ? Font.BOLD : Font.PLAIN, cellSize / 3));
                drawCenteredString(g, "🔵 " + name2, x + w, y, w, barH);
            }
        }

        private void drawCenteredString(Graphics g, String text, int x, int y, int w, int h) {
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int tx = x + (w - metrics.stringWidth(text)) / 2;
            int ty = y + ((h - metrics.getHeight()) / 2) + metrics.getAscent();
            g.drawString(text, tx, ty);
        }

        private int[] getCellXY(int pos, int cellSize, int panelW, int panelH) {
            int row = 9 - (pos - 1) / 10;
            int col = (row % 2 == 0) ? (pos - 1) % 10 : 9 - (pos - 1) % 10;
            int startX = (panelW - cellSize * cols) / 2;
            int startY = 40 + (panelH - cellSize * rows - 40) / 2;
            return new int[]{startX + col * cellSize, startY + row * cellSize};
        }

        private int[] getCellCenter(int pos, int cellSize, int panelW, int panelH) {
            int[] xy = getCellXY(pos, cellSize, panelW, panelH);
            return new int[]{xy[0] + cellSize / 2, xy[1] + cellSize / 2};
        }

        private void drawImageBetween(Graphics2D g2, Image img, int x1, int y1, int x2, int y2, int cellSize) {
            // Uç noktaları kutunun ortasına sabitle
            double dx = x2 - x1, dy = y2 - y1, distance = Math.hypot(dx, dy);
            double angle = Math.atan2(dy, dx);

            int imgW = (int) (cellSize * 0.6);  // Daha ince merdiven için gerekirse ayarla
            int imgH = (int) distance;

            g2 = (Graphics2D) g2.create();
            g2.translate(x1, y1);
            g2.rotate(angle - Math.PI / 2);

            // Merdivenin tam ucu kutunun merkezinden başlasın/çıksın
            g2.drawImage(img, -imgW / 2, 0, imgW, imgH, null);
            g2.dispose();
        }

    }

    private void sendChat(String message) {
        out.println("CHAT:" + message);
    }
}
