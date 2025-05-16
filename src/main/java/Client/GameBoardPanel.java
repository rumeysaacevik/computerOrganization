/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author Rümeysa
 */
public class GameBoardPanel extends JPanel {

    private final JLabel[] cells = new JLabel[100];
    private final Map<Integer, Integer> ladders;
    private final Map<Integer, Integer> snakes;

    public GameBoardPanel(Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        this.ladders = ladders;
        this.snakes = snakes;

        setLayout(new GridLayout(10, 10));
        setPreferredSize(new Dimension(600, 600));

        buildBoard();
    }

    private void buildBoard() {
        // Zigzag numaralandırma (1-100)
        for (int row = 9; row >= 0; row--) {
            boolean leftToRight = row % 2 == 0;

            for (int col = 0; col < 10; col++) {
                int index = leftToRight ? (row * 10 + col) : (row * 10 + (9 - col));
                int number = index + 1;

                JLabel cell = new JLabel(String.valueOf(number), SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                cell.setBackground(Color.WHITE);

                if (ladders.containsKey(number)) {
                    cell.setBackground(Color.GREEN);
                } else if (snakes.containsKey(number)) {
                    cell.setBackground(Color.RED);
                }

                cells[index] = cell;
                add(cell);
            }
        }
    }

    public void updatePositions(Map<String, Integer> playerPositions) {
        // Temizle
        for (int i = 0; i < 100; i++) {
            cells[i].setText(String.valueOf(i + 1));

            if (ladders.containsKey(i + 1)) {
                cells[i].setBackground(Color.GREEN);
            } else if (snakes.containsKey(i + 1)) {
                cells[i].setBackground(Color.RED);
            } else {
                cells[i].setBackground(Color.WHITE);
            }
        }

        // Oyuncuları göster
        for (Map.Entry<String, Integer> entry : playerPositions.entrySet()) {
            int pos = entry.getValue() - 1;
            if (pos >= 0 && pos < 100) {
                String existing = cells[pos].getText();
                cells[pos].setText(existing + " [" + entry.getKey() + "]");
                cells[pos].setBackground(Color.YELLOW);
            }
        }
    }
}
