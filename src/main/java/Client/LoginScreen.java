/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

        // 🐍 Title
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
                        "How to Play", JOptionPane.INFORMATION_MESSAGE));
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

        // 🧾 Name field
        nameField = new JTextField();
        nameField.setBounds(180, 160, 240, 35);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        panel.add(nameField);

        // 🟧 START button
        JButton startBtn = new JButton("START");
        startBtn.setFont(new Font("Arial", Font.BOLD, 18));
        startBtn.setBounds(215, 220, 170, 45);
        startBtn.setBackground(new Color(255, 183, 77));
        startBtn.setForeground(Color.BLACK);
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
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

        // 🎲 Dice icon (bottom-right)
        try {
            ImageIcon diceIcon = new ImageIcon(getClass().getResource("/Client/images/zar.png"));
            Image scaledDice = diceIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel diceLabel = new JLabel(new ImageIcon(scaledDice));
            diceLabel.setBounds(530, 320, 50, 50); // Adjusted for bottom-right
            panel.add(diceLabel);
        } catch (Exception ex) {
            System.out.println("⚠️ Dice image not found: " + ex.getMessage());
        }

        setVisible(true);
    }

    // Button style
    private void styleIconButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(255, 255, 255, 180));
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    // Gradient background panel
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
