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

    private BufferedImage boardImage;
    private Image player1Icon, player2Icon;
    private Map<String, Integer> positions;
    private final Map<Integer, Point> cellPositions = new java.util.HashMap<>();
    private Map<Integer, Integer> ladders;
    private Map<Integer, Integer> snakes;

    // Parametresiz constructor
    public GameBoardPanel() {
        loadImages();
        calculateCellCoordinates();
    }

    // Yılan ve merdiven verileri alan constructor
    public GameBoardPanel(Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        this(); // ortak başlatıcıyı çağır
        this.ladders = ladders;
        this.snakes = snakes;
    }

    private void loadImages() {
        try {
            boardImage = ImageIO.read(new File("C:\\Users\\alpce\\OneDrive\\Masaüstü\\boardF.png"));
            player1Icon = ImageIO.read(new File("C:\\Users\\alpce\\OneDrive\\Masaüstü\\playerB.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            player2Icon = ImageIO.read(new File("C:\\Users\\alpce\\OneDrive\\Masaüstü\\playerR.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateCellCoordinates() {
        int size = 60; // hücre boyutu
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int number;
                if (row % 2 == 0) {
                    number = 100 - (row * 10 + col);
                } else {
                    number = 100 - (row * 10 + (9 - col));
                }
                int x = col * size + 20;
                int y = row * size + 20;
                cellPositions.put(number, new Point(x, y));
            }
        }
    }

    public void updatePositions(Map<String, Integer> pos) {
        this.positions = pos;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Arka plan resmi çiz
        if (boardImage != null) {
            g.drawImage(boardImage, 0, 0, getWidth(), getHeight(), this);
        }

        // Yılanlar ve merdivenleri çiz (isteğe bağlı görsellik)
        if (ladders != null) {
            g.setColor(Color.GREEN.darker());
            for (Map.Entry<Integer, Integer> entry : ladders.entrySet()) {
                drawLineBetweenCells(g, entry.getKey(), entry.getValue());
            }
        }

        if (snakes != null) {
            g.setColor(Color.RED);
            for (Map.Entry<Integer, Integer> entry : snakes.entrySet()) {
                drawLineBetweenCells(g, entry.getKey(), entry.getValue());
            }
        }

        // Oyuncu ikonlarını pozisyonlara göre çiz
        if (positions != null) {
            int index = 0;
            for (Map.Entry<String, Integer> entry : positions.entrySet()) {
                int cell = entry.getValue();
                Point p = cellPositions.get(cell);
                if (p != null) {
                    Image icon = (index == 0) ? player1Icon : player2Icon;
                    g.drawImage(icon, p.x + (index * 20), p.y, this);
                    index++;
                }
            }
        }
    }

    private void drawLineBetweenCells(Graphics g, int fromCell, int toCell) {
        Point from = cellPositions.get(fromCell);
        Point to = cellPositions.get(toCell);
        if (from != null && to != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(from.x + 15, from.y + 15, to.x + 15, to.y + 15);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }
}
