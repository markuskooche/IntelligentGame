package loganalyze;

import javax.swing.*;

public class VisibleFieldWindow extends JDialog {

    public VisibleFieldWindow(JFrame parent) {
        super(parent);

        setModal(true);
        setLayout(null);
        setResizable(false);
        setSize(770, 800);
        setTitle("Erreichbare Felder");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        GameField.GamePanel gamePanel = new GameField.GamePanel();
        gamePanel.setBounds(10, 10, 751, 751);
        add(gamePanel);

        setVisible(true);
    }
}
