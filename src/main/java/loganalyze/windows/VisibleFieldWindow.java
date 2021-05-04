package loganalyze.windows;

import loganalyze.controller.GameField;
import loganalyze.additional.OSValidator;
import loganalyze.colorize.BackgroundPoint;

import javax.swing.*;
import java.util.LinkedList;

public class VisibleFieldWindow extends JDialog {

    public VisibleFieldWindow(JFrame parent, LinkedList<BackgroundPoint> reachable, boolean finished) {
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

        if (!finished) {
            JOptionPane.showMessageDialog(
                    VisibleFieldWindow.this,
                    "Es ist ein Fehler bei der Überprüfung aufgetreten!\n" +
                            "Die Anzeige könnte von der Realität abweichen.",
                    "Fehler beim Erzeugen",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        GameField.GamePanel gamePanel = new GameField.GamePanel();
        gamePanel.setBounds(0, 0, 770, 770);
        add(gamePanel);

        gamePanel.updateFrame(reachable, new LinkedList<>());
        setVisible(true);
    }
}
