import javax.swing.*;
import java.awt.*;

public class GameAnalyzer extends JFrame {

    private static final long serialVersionUID = 1L;

    private int counter;
    private String lastDirectory = ".";

    private final JButton next;
    private final JButton previous;
    private final JLabel moveSize;
    private final JLabel bombRadius;
    private final JLabel currentMove;
    private final JLabel ownPlayer;
    private final JLabel amountPlayer;

    private JRadioButton jumperRadio;
    private final JTextField lineJumper;
    private final JButton jumperButton;

    private GameFileManager gameFileManager;

    private final DefaultListModel<String> defaultListModelBoard;
    private final DefaultListModel<String> defaultListModelPlayer;

    public GameAnalyzer() {

        setTitle("GameAnalyzer v0.2.1");
        setSize(1000, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(null);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu = new JMenu("Datei");
        menuBar.add(menu);

        JMenuItem openItem = new JMenuItem("Öffnen");
        menu.add(openItem);
        openItem.addActionListener(e -> loadGame());

        menu.add(new JSeparator());

        JMenuItem closeApp = new JMenuItem("Beenden");
        menu.add(closeApp);
        closeApp.addActionListener(e -> dispose());

        // ----- ----- ----- ----- GAME ANALYZER - WINDOW ----- ----- ----- -----

        amountPlayer = new JLabel("Anzahl Spieler: -");
        amountPlayer.setBounds(10, 10, 120, 20);
        add(amountPlayer);

        ownPlayer = new JLabel("Spielfigur: -");
        ownPlayer.setBounds(140, 10, 100, 20);
        add(ownPlayer);

        bombRadius = new JLabel("Bombenradius: -");
        bombRadius.setBounds(250, 10, 120, 20);
        add(bombRadius);

        moveSize = new JLabel("Anzahl Züge: -");
        moveSize.setBounds(550, 10, 150, 20);
        add(moveSize);

        defaultListModelBoard = new DefaultListModel<>();
        JList<String> listBoard = new JList<>(defaultListModelBoard);
        listBoard.setFont(new Font("Courier", Font.PLAIN,14));
        JScrollPane scrollPaneBoard = new JScrollPane(listBoard);
        scrollPaneBoard.setBounds(10, 40, 730, 560);
        add(scrollPaneBoard);

        defaultListModelPlayer = new DefaultListModel<>();
        JList<String> listPlayer = new JList<>(defaultListModelPlayer);
        listPlayer.setFont(new Font("Courier", Font.PLAIN,14));
        JScrollPane scrollPanePlayer = new JScrollPane(listPlayer);
        scrollPanePlayer.setBounds(750, 10, 240, 630);
        add(scrollPanePlayer);

        currentMove = new JLabel("Aktuell: -");
        currentMove.setBounds(30, 615, 120, 20);
        add(currentMove);

        previous = new JButton("Zurück");
        previous.setBounds(260, 610, 80, 30);
        previous.addActionListener(e -> previousGame());
        previous.setEnabled(false);
        add(previous);

        next = new JButton("Weiter");
        next.setBounds(350, 610, 80, 30);
        next.addActionListener(e -> nextGame());
        getRootPane().setDefaultButton(next);
        next.setEnabled(false);
        add(next);

        lineJumper = new JTextField();
        lineJumper.setBounds(610, 610, 50, 30);
        lineJumper.setEnabled(false);
        add(lineJumper);

        jumperRadio = new JRadioButton("Speichern", false);
        jumperRadio.setBounds(500, 610, 100, 30);
        jumperRadio.addActionListener(e -> {
            lineJumper.setText("");
        });
        jumperRadio.setEnabled(false);
        add(jumperRadio);

        jumperButton = new JButton("Enter");
        jumperButton.setBounds(660, 610, 80, 30);
        jumperButton.addActionListener(e -> jumpMove());
        jumperButton.setEnabled(false);
        add(jumperButton);

        setVisible(true);
    }

    private void loadGame() {
        FileDialog fd = new FileDialog(GameAnalyzer.this, "Analysedatei öffnen", FileDialog.LOAD);
        fd.setFilenameFilter((directory, name) -> name.endsWith(".log"));
        fd.setDirectory(lastDirectory);
        fd.setVisible(true);

        lastDirectory = fd.getDirectory();

        try {
            String filename = fd.getDirectory() + fd.getFile();
            gameFileManager = new GameFileManager(filename);
            gameFileManager.load();
            counter = 0;

            amountPlayer.setText("Anzahl Spieler: " + gameFileManager.getPlayerAmount());
            ownPlayer.setText("Spielfigur: " + gameFileManager.getPlayer());
            bombRadius.setText("Bombenradius: " + gameFileManager.getBombRadius());
            moveSize.setText("Anzahl Züge: " + gameFileManager.getMoveSize());
            jumperButton.setEnabled(true);
            jumperRadio.setEnabled(true);
            lineJumper.setEnabled(true);

            next.setEnabled(gameFileManager.getGameSize() != 1);
            updateFrame();
        }
        catch (Exception ignored) {
        }
    }

    private void previousGame() {
        if (counter >= 1) {
            next.setEnabled(true);
            counter--;
            updateFrame();

            if (counter == 0) {
                previous.setEnabled(false);
            }
        }
    }

    private void nextGame() {
        if (counter < gameFileManager.getGameSize() - 1) {
            previous.setEnabled(true);
            counter++;
            updateFrame();

            if (counter == gameFileManager.getGameSize() - 1) {
                next.setEnabled(false);
            }
        }
    }

    private void jumpMove() {
        String input = lineJumper.getText();
        int previousCounter = counter;
        int line;

        try {
            line = Integer.parseInt(input);
            if (line >= 0 && line < gameFileManager.getGameSize()) {
                counter = line;
                updateFrame();

                if (counter == gameFileManager.getGameSize() - 1) {
                    previous.setEnabled(true);
                    next.setEnabled(false);
                } else if (counter == 0) {
                    previous.setEnabled(false);
                    next.setEnabled(true);
                } else {
                    previous.setEnabled(true);
                    next.setEnabled(true);
                }
            }
        }
        catch (Exception ignored) {}

        if (jumperRadio.isSelected()) {
            lineJumper.setText("" + previousCounter);
        } else {
            lineJumper.setText("");
        }
    }

    private void updateFrame() {
        String[] board = gameFileManager.getBoard(counter);
        defaultListModelBoard.clear();
        for(String line : board) {
            defaultListModelBoard.addElement(line);
        }

        String[] player = gameFileManager.getPlayer(counter);
        defaultListModelPlayer.clear();
        for(String line : player) {
            defaultListModelPlayer.addElement(line);
        }

        currentMove.setText("Aktuell: " + counter);
    }

    public static void main(String[] args) {
        new GameAnalyzer();
    }
}
