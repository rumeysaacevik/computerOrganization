/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Rümeysa
 */
public class ControlPanel extends JPanel {

    private JTextArea txtArea;
    private JButton btnRoll, btnRestart, btnSurrender;

    public ControlPanel() {
        setLayout(new BorderLayout());

        txtArea = new JTextArea(5, 30);
        txtArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        btnRoll = new JButton("ZAR AT");
        btnRestart = new JButton("YENİ OYUN");
        btnSurrender = new JButton("PES ET");

        btnRoll.setEnabled(false);
        btnRestart.setEnabled(false);
        btnSurrender.setEnabled(false);

        buttonPanel.add(btnRoll);
        buttonPanel.add(btnRestart);
        buttonPanel.add(btnSurrender);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setRollEnabled(boolean enabled) {
        btnRoll.setEnabled(enabled);
    }

    public void setRestartEnabled(boolean enabled) {
        btnRestart.setEnabled(enabled);
    }

    public void setSurrenderEnabled(boolean enabled) {
        btnSurrender.setEnabled(enabled);
    }

    public void setPlayerId(String id) {
        txtArea.setText("ID: " + id + "\n");
    }

    public void appendText(String msg) {
        txtArea.append(msg);
    }

    public void setRollAction(ActionListener listener) {
        btnRoll.addActionListener(listener);
    }

    public void setRestartAction(ActionListener listener) {
        btnRestart.addActionListener(listener);
    }

    public void setSurrenderAction(ActionListener listener) {
        btnSurrender.addActionListener(listener);
    }
}
