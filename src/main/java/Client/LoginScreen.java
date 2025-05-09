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
        setTitle("Snakes and Ladders - Giriş");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setUndecorated(true);

        JPanel panel = new GradientPanel();
        panel.setLayout(null);
        add(panel);

        // 🐍 Başlık
        JLabel title = new JLabel("🐍  SNAKES & LADDERS 🎲", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        title.setBounds(80, 40, 440, 50);
        panel.add(title);

        // ❓ Bilgi butonu
        JButton infoBtn = new JButton("❓");
        styleIconButton(infoBtn);
        infoBtn.setBounds(10, 10, 45, 30);
        infoBtn.addActionListener(e
                -> JOptionPane.showMessageDialog(this,
                        "🎯 Amaç: Zar atarak 100'e ulaşmak.\n🟩 Merdiven çıkartır, 🟥 Yılan düşürür.\n👥 2 kişi sırayla oynar, iyi şanslar!",
                        "Nasıl Oynanır?", JOptionPane.INFORMATION_MESSAGE)
        );
        panel.add(infoBtn);

        // ❌ Çıkış butonu
        JButton exitBtn = new JButton("❌");
        styleIconButton(exitBtn);
        exitBtn.setBounds(545, 10, 45, 30);
        exitBtn.addActionListener(e -> System.exit(0));
        panel.add(exitBtn);

        // 🔤 Etiket
        JLabel nameLabel = new JLabel("Adınızı giriniz:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(Color.DARK_GRAY);
        nameLabel.setBounds(240, 130, 200, 30);
        panel.add(nameLabel);

        // 🧾 TextField
        nameField = new JTextField();
        nameField.setBounds(180, 160, 240, 35);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        panel.add(nameField);

        // 🎮 BAŞLA butonu + ikon
        ImageIcon rawIcon = new ImageIcon(getClass().getResource("/images/play.png"));
        Image scaledImg = rawIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        JButton startBtn = new JButton("BAŞLA", scaledIcon);
        startBtn.setFont(new Font("Arial", Font.BOLD, 18));
        startBtn.setBounds(215, 220, 170, 45);
        startBtn.setBackground(new Color(255, 183, 77));
        startBtn.setForeground(Color.BLACK);
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        startBtn.setIconTextGap(10);

        startBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                dispose();
                new GUIClient(name);
            } else {
                JOptionPane.showMessageDialog(this, "Lütfen adınızı giriniz!");
            }
        });

        panel.add(startBtn);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
