/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Rümeysa
 */

public class LoginScreen extends JFrame {

    private JTextField nameField;
    private JTextField ipField; // Sunucu IP girişi

    public LoginScreen() {
        setTitle("Snakes and Ladders - Login");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setUndecorated(true);

        JPanel panel = new GradientPanel();
        panel.setLayout(null);
        add(panel);

        // 🎲 Title
        JLabel title = new JLabel("🐍  SNAKES & LADDERS 🎲", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setBounds(80, 40, 440, 50);
        panel.add(title);

        // ❓ Info button
        JButton infoBtn = new JButton("❓");
        styleIconButton(infoBtn);
        infoBtn.setBounds(10, 10, 45, 30);
        infoBtn.addActionListener(e
                -> JOptionPane.showMessageDialog(this,
                        "🎯 Goal: Reach 100 by rolling the dice.\n🟩 Ladders lift you up, 🟥 snakes bring you down.\n👥 Two players take turns. Good luck!",
                        "How to Play", JOptionPane.INFORMATION_MESSAGE)
        );
        panel.add(infoBtn);

        // ❌ Exit button
        JButton exitBtn = new JButton("❌");
        styleIconButton(exitBtn);
        exitBtn.setBounds(545, 10, 45, 30);
        exitBtn.addActionListener(e -> System.exit(0));
        panel.add(exitBtn);

        // 🔤 Name label
        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(Color.DARK_GRAY);
        nameLabel.setBounds(240, 110, 200, 30);
        panel.add(nameLabel);

        // ✏️ Name input field
        nameField = new JTextField();
        nameField.setBounds(180, 140, 240, 35);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        panel.add(nameField);

        // 🌐 IP label
        JLabel ipLabel = new JLabel("Server IP:");
        ipLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        ipLabel.setForeground(Color.DARK_GRAY);
        ipLabel.setBounds(240, 190, 200, 30);
        panel.add(ipLabel);

        // 🌐 IP input field
        ipField = new JTextField("127.0.0.1"); // Varsayılan localhost
        ipField.setBounds(180, 220, 240, 35);
        ipField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        panel.add(ipField);

        // ▶️ Start button
        JButton startBtn = new JButton("START");
        startBtn.setFont(new Font("Arial", Font.BOLD, 18));
        startBtn.setBounds(215, 280, 170, 45);
        startBtn.setBackground(new Color(255, 183, 77));
        startBtn.setForeground(Color.BLACK);
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String ip = ipField.getText().trim();
            if (!name.isEmpty() && !ip.isEmpty()) {
                dispose();
                new GUIClient(name, ip); // IP ile client başlatılıyor
            } else {
                JOptionPane.showMessageDialog(this, "Please enter your name and server IP!");
            }
        });
        panel.add(startBtn);

        // 🎲 Dice image (resources/pieces/dice.png) -- %50 opacity
        addImageWithOpacity(panel, "/pieces/dice.png", 500, 270, 100, 100, 0.5f);

        // 🐍 Snake image (resources/pieces/snake_transparent.png)
        addImageWithOpacity(panel, "/pieces/snakes.png", 10, 150, 160, 160, 0.5f);

        // 🪜 Ladder image (resources/pieces/ladder.png)
        addImageWithOpacity(panel, "/pieces/ladders.png", 440, 120, 160, 160, 0.5f);

        // 🔁 Scrolling text at bottom
        ScrollingText scroller = new ScrollingText();
        scroller.setBounds(0, 340, 600, 30);
        panel.add(scroller);

        setVisible(true);
    }

    // REUSABLE: Add image with opacity from resource
    private void addImageWithOpacity(JPanel panel, String resourcePath, int x, int y, int w, int h, float opacity) {
        try {
            java.net.URL imgUrl = getClass().getResource(resourcePath);
            if (imgUrl == null) {
                System.out.println("❌ Resource not found: " + resourcePath);
                return;
            }
            BufferedImage originalImage = ImageIO.read(imgUrl);
            BufferedImage transparentImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = transparentImage.createGraphics();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

            Image scaledImage = transparentImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            JLabel label = new JLabel(new ImageIcon(scaledImage));
            label.setBounds(x, y, w, h);
            panel.add(label);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void styleIconButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(255, 255, 255, 180));
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(255, 204, 153),
                    0, getHeight(), new Color(255, 140, 100));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class ScrollingText extends JPanel {

        private final String message = "🎯 Reach 100 first to win! Ladders lift you, snakes drop you. 🎲 Good luck!";
        private int x = 600;
        private final Timer timer;

        public ScrollingText() {
            setPreferredSize(new Dimension(600, 30));
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 14));

            timer = new Timer(20, e -> {
                x -= 2;
                if (x < -getFontMetrics(getFont()).stringWidth(message)) {
                    x = getWidth();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(getForeground());
            g.setFont(getFont());
            g.drawString(message, x, 25);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
