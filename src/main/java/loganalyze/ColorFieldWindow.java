package loganalyze;

import loganalyze.additionals.OSValidator;

import javax.swing.*;
import java.awt.*;

public class ColorFieldWindow extends JDialog {

    public ColorFieldWindow(JFrame parent) {
        super(parent);

        setModal(false);
        setLayout(null);
        setResizable(false);
        if (OSValidator.isMac()) {
            setSize(200, 250);
        } else if (OSValidator.isWindows()) {
            setSize(200, 260);
        } else {
            setSize(200, 255);
        }
        setTitle("Farbbedeutung");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel colorPanel = new JPanel() {
            public void paintComponent( Graphics g ) {
                super.paintComponent(g);

                g.setColor(BackgroundPoint.EXPANSION);
                g.fillRect(0, 0, 30, 30);

                g.setColor(BackgroundPoint.INVERSION);
                g.fillRect(0, 50, 30, 30);

                g.setColor(BackgroundPoint.CHOICE);
                g.fillRect(0, 100, 30, 30);

                g.setColor(BackgroundPoint.BONUS);
                g.fillRect(0, 150, 30, 30);
            }
        };
        colorPanel.setBounds(20, 20, 30, 200);
        add(colorPanel);

        JLabel expansionLabel = new JLabel("Expansionsstein");
        expansionLabel.setBounds(70, 20, 150, 30);
        add(expansionLabel);

        JLabel inversionLabel = new JLabel("Inversionsfeld");
        inversionLabel.setBounds(70, 70, 150, 30);
        add(inversionLabel);

        JLabel choiceLabel = new JLabel("Auswahlfeld");
        choiceLabel.setBounds(70, 120, 150, 30);
        add(choiceLabel);

        JLabel bonusLabel = new JLabel("Bonusfeld");
        bonusLabel.setBounds(70, 170, 150, 30);
        add(bonusLabel);

        setVisible(true);
    }
}
