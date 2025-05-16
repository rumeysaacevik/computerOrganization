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
import javax.imageio.ImageIO;

/**
 *
 * @author Rümeysa
 */
public class LoginScreen extends JFrame {

    private JTextField nameField;

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
        nameLabel.setBounds(240, 130, 200, 30);
        panel.add(nameLabel);

        // ✏️ Name input field
        nameField = new JTextField();
        nameField.setBounds(180, 160, 240, 35);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        panel.add(nameField);

        // ▶️ Start button
        JButton startBtn = new JButton("START");
        startBtn.setFont(new Font("Arial", Font.BOLD, 18));
        startBtn.setBounds(215, 220, 170, 45);
        startBtn.setBackground(new Color(255, 183, 77));
        startBtn.setForeground(Color.BLACK);
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                dispose();
                new GUIClient(name);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter your name!");
            }
        });
        panel.add(startBtn);

        // 🎲 Dice image from Desktop with opacity
        try {
            String path = "C:/Users/alpce/OneDrive/Masaüstü/dice.png";
            File imageFile = new File(path);
            if (!imageFile.exists()) {
                System.out.println("❌ File not found: " + path);
            } else {
                BufferedImage originalImage = ImageIO.read(imageFile);

                BufferedImage transparentImage = new BufferedImage(
                        originalImage.getWidth(),
                        originalImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );

                Graphics2D g2d = transparentImage.createGraphics();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.drawImage(originalImage, 0, 0, null);
                g2d.dispose();

                Image scaledImage = transparentImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                JLabel diceLabel = new JLabel(new ImageIcon(scaledImage));
                diceLabel.setBounds(500, 270, 100, 100);
                panel.add(diceLabel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 🐍 Snake image (left side)
        try {
            String snakePath = "C:/Users/alpce/OneDrive/Masaüstü/snakes.png";
            File snakeFile = new File(snakePath);
            if (!snakeFile.exists()) {
                System.out.println("❌ Snake image not found: " + snakePath);
            } else {
                BufferedImage snakeImage = ImageIO.read(snakeFile);
                BufferedImage transparentSnake = new BufferedImage(
                        snakeImage.getWidth(), snakeImage.getHeight(), BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g2dSnake = transparentSnake.createGraphics();
                g2dSnake.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2dSnake.drawImage(snakeImage, 0, 0, null);
                g2dSnake.dispose();

                Image scaledSnake = transparentSnake.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                JLabel snakeLabel = new JLabel(new ImageIcon(scaledSnake));
                snakeLabel.setBounds(10, 150, 160, 160);  // 👈 sol üst köşeye daha yakın
                panel.add(snakeLabel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

// 🪜 Ladder image (upper right corner - larger and shifted left)
        try {
            String ladderPath = "C:/Users/alpce/OneDrive/Masaüstü/ladders.png";
            File ladderFile = new File(ladderPath);
            if (!ladderFile.exists()) {
                System.out.println("❌ Ladder image not found: " + ladderPath);
            } else {
                BufferedImage ladderImage = ImageIO.read(ladderFile);
                BufferedImage transparentLadder = new BufferedImage(
                        ladderImage.getWidth(), ladderImage.getHeight(), BufferedImage.TYPE_INT_ARGB
                );

                Graphics2D g2dLadder = transparentLadder.createGraphics();
                g2dLadder.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // %50 opacity
                g2dLadder.drawImage(ladderImage, 0, 0, null);
                g2dLadder.dispose();

                // 👇 Boyut büyütüldü
                Image scaledLadder = transparentLadder.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                JLabel ladderLabel = new JLabel(new ImageIcon(scaledLadder));
                ladderLabel.setBounds(440, 120, 160, 160); // 👈 Daha büyük ve biraz sola çekildi
                panel.add(ladderLabel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 🔁 Scrolling text at bottom
        ScrollingText scroller = new ScrollingText();
        scroller.setBounds(0, 340, 600, 30);
        panel.add(scroller);

        setVisible(true);
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
