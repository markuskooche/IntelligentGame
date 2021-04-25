package loganalyze;

import javax.swing.*;

public class VisibleFieldWindow extends JDialog {

    public VisibleFieldWindow(JFrame parent) {
        super(parent);

        setModal(false);
        setLayout(null);
        setResizable(false);
        setSize(770, 800);
        setTitle("Erreichbare Felder");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        GameField.GamePanel gamePanel = new GameField.GamePanel();
        gamePanel.setBounds(0, 0, 770, 770);
        add(gamePanel);

        setVisible(true);
    }
}
