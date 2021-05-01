package loganalyze;

import loganalyze.additionals.OSValidator;

import javax.swing.*;

public class VisibleFieldWindow extends JDialog {

    public VisibleFieldWindow(JFrame parent) {
        super(parent);

        setModal(false);
        setLayout(null);
        setResizable(false);
        if (OSValidator.isMac()) {
            setSize(770, 800);
        } else {
            setSize(785, 810);
        }
        setTitle("Erreichbare Felder");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        GameField.GamePanel gamePanel = new GameField.GamePanel();
        gamePanel.setBounds(0, 0, 770, 770);
        add(gamePanel);

        setVisible(true);
    }
}
