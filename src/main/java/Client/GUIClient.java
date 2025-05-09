/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
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
    private String myId = "";
    private boolean myTurn = false;

    public GUIClient() {
        setTitle("Snakes and Ladders - Client");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        txtArea = new JTextArea();
        txtArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtArea);
        add(scrollPane, BorderLayout.CENTER);

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
                txtArea.append("HAMLE → " + msg.substring(5) + "\n");
            } else if (msg.startsWith("WINNER:")) {
                txtArea.append("🏆 Kazanan: " + msg.substring(7) + " 🏁\n");
                btnRoll.setEnabled(false);
            } else {
                txtArea.append(msg + "\n");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIClient());
    }
}
