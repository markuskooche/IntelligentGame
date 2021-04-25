package loganalyze;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GameAnalyzer extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color HEADER_COLOR = new Color(190, 190, 190);
    private static final Color TABLE_COLOR = new Color(245, 245, 245);

    private int counter = 0;
    //private String lastDirectory= ".";
    private String lastDirectory= "/Users/markuskooche/Desktop/server/all_logfiles/logfiles_04-23_08/logfiles";

    private final JMenuItem exportItem;
    private final JMenuItem hideTransition;
    private final JMenuItem showTransition;
    private final JMenuItem visibleItem;
    private final JMenuItem mobilityItem;
    private final JMenuItem coinParityItem;
    private final JMenuItem mapValueItem;
    private final JMenuItem heuristicItem;

    private final JLabel moveSize;
    private final JLabel ownPlayer;
    private final JLabel bombRadius;
    private final JLabel amountPlayer;
    private final JLabel fieldPercentage;

    private final JLabel currentMove;

    private final JButton nextGame;
    private final JButton previousGame;

    private final JButton jumperButton;
    private final JTextField jumperInput;
    private final JRadioButton jumperRadio;

    private final List<PlayerInformation> playerList;
    private final PlayerTableModel playerTableModel;
    private final ListSelectionModel playerSelectionModel;

    private GamePanelManager gamePanelManager;
    private final GameField.GamePanel gamePanel;

    public GameAnalyzer() {
        playerList = new ArrayList<>();

        setTitle("GameAnalyzer v0.4.0");
        setSize(1110, 890);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(null);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu;

        // ----- ----- ----- ----- GAME ANALYZER - MENU DATEI ----- ----- ----- -----

        menu = new JMenu("Datei");
        menuBar.add(menu);

        JMenuItem openItem = new JMenuItem("Öffnen");
        menu.add(openItem);
        openItem.addActionListener(e -> loadGame());

        exportItem = new JMenuItem("Exportieren");
        exportItem.setEnabled(false);
        menu.add(exportItem);
        exportItem.addActionListener(e -> exportGame());

        menu.add(new JSeparator());

        JMenuItem closeApp = new JMenuItem("Beenden");
        menu.add(closeApp);
        closeApp.addActionListener(e -> dispose());

        // ----- ----- ----- ----- GAME ANALYZER - MENU BEARBEITEN ----- ----- ----- -----

        menu = new JMenu("Bearbeiten");
        menuBar.add(menu);

        showTransition = new JMenuItem("Transition ein");
        showTransition.addActionListener(e -> showTransition());
        showTransition.setEnabled(false);
        menu.add(showTransition);

        hideTransition = new JMenuItem("Transition aus");
        hideTransition.addActionListener(e -> hideTransition());
        hideTransition.setEnabled(false);
        menu.add(hideTransition);

        // ----- ----- ----- ----- GAME ANALYZER - MENU FENSTER ----- ----- ----- -----

        menu = new JMenu("Fenster");
        menuBar.add(menu);

        JMenuItem colorItem = new JMenuItem("Farbbedeutung");
        colorItem.addActionListener(e -> new ColorFieldWindow(this));
        menu.add(colorItem);

        menu.add(new JSeparator());

        visibleItem = new JMenuItem("Unerreichbar");
        visibleItem.addActionListener(e -> openVisibleWindow());
        visibleItem.setEnabled(false);
        menu.add(visibleItem);

        menu.add(new JSeparator());
        menu.add(new JSeparator());

        JMenuItem loadStatistik = new JMenuItem("Statistik laden");
        loadStatistik.addActionListener(e -> {
            FileDialog fd = new FileDialog(
                    GameAnalyzer.this,
                    "Statistik laden",
                    FileDialog.LOAD
            );
            fd.setFilenameFilter((directory, name) -> name.endsWith(".txt"));
            fd.setDirectory(lastDirectory);
            fd.setVisible(true);

            try {
                String filename = fd.getDirectory() + fd.getFile();
                Path path = Paths.get(filename);
                List<String> file = Files.lines(path).collect(Collectors.toList());

                List<Integer> statisticList = new LinkedList<>();

                for (int i = 1; i < file.size(); i++) {
                    statisticList.add(Integer.parseInt(file.get(i)));
                }

                new StatisticWindow(("Importierte " + file.get(0)), statisticList, null, false);
            }
            catch (IOException ignored) {
                // this happens when the user has not selected a file
            }
            catch (Exception ex){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        GameAnalyzer.this,
                        "Keine gültige ReversiXT-Statistik ausgewählt!",
                        "Fehlerhafte Datei",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
        menu.add(loadStatistik);

        menu.add(new JSeparator());

        mobilityItem = new JMenuItem("Mobilität");
        mobilityItem.addActionListener(e -> {
            new StatisticWindow("Mobilität", gamePanelManager.getMobility(), GameAnalyzer.this, true);
        });
        mobilityItem.setEnabled(false);
        menu.add(mobilityItem);

        coinParityItem = new JMenuItem("Spielfeldbelegung");
        coinParityItem.addActionListener(e -> {
            new StatisticWindow("Spielfeldbelegung", gamePanelManager.getCoinParity(), GameAnalyzer.this, true);
        });
        coinParityItem.setEnabled(false);
        menu.add(coinParityItem);

        mapValueItem = new JMenuItem("Spielfeldgewichtung");
        mapValueItem.addActionListener(e -> {
            new StatisticWindow("Spielfeldgewichtung", gamePanelManager.getMapValue(), GameAnalyzer.this, true);
        });
        mapValueItem.setEnabled(false);
        menu.add(mapValueItem);

        heuristicItem = new JMenuItem("Gesamtheuristik");
        heuristicItem.addActionListener(e -> {
            new StatisticWindow("Gesamtheuristik", gamePanelManager.getHeuristic(), GameAnalyzer.this, true);
        });
        heuristicItem.setEnabled(false);
        menu.add(heuristicItem);

        // ----- ----- ----- ----- GAME ANALYZER - WINDOW ----- ----- ----- -----

        amountPlayer = new JLabel("Anzahl Spieler: -");
        amountPlayer.setBounds(20, 10, 120, 20);
        add(amountPlayer);

        ownPlayer = new JLabel("Spielfigur: -");
        ownPlayer.setBounds(150, 10, 100, 20);
        add(ownPlayer);

        bombRadius = new JLabel("Bombenradius: -");
        bombRadius.setBounds(250, 10, 120, 20);
        add(bombRadius);

        fieldPercentage = new JLabel("Verteilung: --% ---/----");
        fieldPercentage.setBounds(410, 10, 180, 20);
        add(fieldPercentage);

        moveSize = new JLabel("Anzahl Züge: -");
        moveSize.setBounds(640, 10, 150, 20);
        add(moveSize);

        gamePanel = new GameField.GamePanel();
        gamePanel.setBounds(10, 30, 770, 770);

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = (e.getX() / 15) - 1;
                int y = (e.getY() / 15) - 1;
                gamePanel.highlightPlayer(x, y);
            }
        });

        add(gamePanel);

        // ----- ----- ----- ----- GAME ANALYZER - PLAYER TABLE ----- ----- ----- -----

        playerTableModel = new PlayerTableModel(playerList);

        JTable table = new JTable(playerTableModel) {
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int columnIndex) {
                Component component = super.prepareRenderer(renderer, rowIndex, columnIndex);

                if (!component.getBackground().equals(getSelectionBackground())) {
                    Color c = (rowIndex % 2 == 1 ? TABLE_COLOR : Color.WHITE);
                    component.setBackground(c);
                }
                return component;
            }
        };

        playerSelectionModel = table.getSelectionModel();
        playerSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    int row = playerSelectionModel.getMinSelectionIndex();
                    int selectedPlayer = (row / PlayerTableModel.ITEMS) + 1;

                    if (row % PlayerTableModel.ITEMS != 4) {
                        gamePanel.highlightPlayer(selectedPlayer);
                    }
                }
            }
        });

        table.getTableHeader().setBackground(HEADER_COLOR);
        table.getTableHeader().setFont(new Font("Lucida", Font.PLAIN, 14));
        table.setFont(new Font("Lucida", Font.PLAIN, 13));

        table.getColumnModel().getColumn(0);
        table.getColumnModel().getColumn(1);

        JScrollPane scrollPane = new JScrollPane(table);
        JPanel titlePane = new JPanel();
        titlePane.setLayout(new BorderLayout());
        titlePane.add(scrollPane);
        titlePane.setBounds(790, 20, 300, 800);
        add(titlePane);

        // ----- ----- ----- ----- GAME ANALYZER - GAME CONTROLLER ----- ----- ----- -----

        currentMove = new JLabel("Aktuell: -");
        currentMove.setBounds(30, 805, 120, 20);
        add(currentMove);

        previousGame = new JButton("Zurück");
        previousGame.setBounds(295, 800, 100, 30);
        previousGame.addActionListener(e -> previousGame());
        previousGame.setEnabled(false);
        add(previousGame);

        nextGame = new JButton("Weiter");
        nextGame.setBounds(395, 800, 100, 30);
        nextGame.addActionListener(e -> nextGame());
        nextGame.setEnabled(false);
        add(nextGame);

        jumperInput = new JTextField();
        jumperInput.setBounds(640, 800, 50, 30);
        jumperInput.setEnabled(false);
        add(jumperInput);

        jumperRadio = new JRadioButton("Speichern", false);
        jumperRadio.setBounds(530, 800, 100, 30);
        jumperRadio.addActionListener(e -> {
            jumperInput.setText("");
        });
        jumperRadio.setEnabled(false);
        add(jumperRadio);

        jumperButton = new JButton("Enter");
        jumperButton.setBounds(690, 800, 80, 30);
        jumperButton.addActionListener(e -> jumpMove());
        jumperButton.setEnabled(false);
        add(jumperButton);

        setVisible(true);
    }

    private void previousGame() {
        if (counter >= 1) {
            nextGame.setEnabled(true);
            counter--;
            updateGamePanel();

            if (counter == 0) {
                previousGame.setEnabled(false);
            }
        }
    }

    private void nextGame() {
        if (counter < gamePanelManager.getGameSize() - 1) {
            previousGame.setEnabled(true);
            counter++;
            updateGamePanel();

            if (counter == gamePanelManager.getGameSize() - 1) {
                nextGame.setEnabled(false);

                disqualifyMessage();
            }
        }
    }

    private void openVisibleWindow() {
        new VisibleFieldWindow(this);
    }

    private void loadGame() {
        FileDialog fd = new FileDialog(
                GameAnalyzer.this,
                "Analysedatei öffnen",
                FileDialog.LOAD
        );
        fd.setFilenameFilter((directory, name) -> name.endsWith(".log"));
        fd.setDirectory(lastDirectory);
        fd.setVisible(true);

        try {
            String filename = fd.getDirectory() + fd.getFile();
            gamePanelManager = new GamePanelManager(filename);
            gamePanelManager.load();

            List<int[]> transitions = gamePanelManager.getTransitions();
            gamePanel.setTransitions(transitions);
            gamePanel.hideTransitions();
            if (!transitions.isEmpty()) {
                showTransition.setEnabled(true);
            }

            jumperInput.setEnabled(true);
            jumperRadio.setEnabled(true);
            jumperButton.setEnabled(true);

            moveSize.setText("Anzahl Züge: " + (gamePanelManager.getGameSize() - 1));

            ownPlayer.setText("Spielfigur: " + gamePanelManager.getOwnPlayer());
            bombRadius.setText("Bombenradius: " + gamePanelManager.getBombRadius());
            amountPlayer.setText("Anzahl Spieler: " + gamePanelManager.getPlayerAmount());

            nextGame.setEnabled(gamePanelManager.getGameSize() != 1);
            lastDirectory = fd.getDirectory();
            counter = 0;

            int[] disqualified = gamePanelManager.getDisqualifiedPlayer();
            playerTableModel.setDisqualified(disqualified);

            gamePanel.disableHighlighting();
            exportItem.setEnabled(true);
            visibleItem.setEnabled(true);
            hideTransition.setEnabled(false);

            mobilityItem.setEnabled(true);
            coinParityItem.setEnabled(true);
            mapValueItem.setEnabled(true);
            heuristicItem.setEnabled(true);

            updateGamePanel();
        }
        catch (IOException ignored) {
            // this happens when the user has not selected a file
        }
        catch (Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    GameAnalyzer.this,
                    "Keine gültige ReversiXT-Analysedatei ausgewählt!",
                    "Fehlerhafte Datei",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void exportGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Aktuelles Spielfeld exportieren");
        fileChooser.setCurrentDirectory(new File(lastDirectory));

        int userSelection = fileChooser.showSaveDialog(GameAnalyzer.this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                String fileName = fileChooser.getSelectedFile().toString();

                if (!fileName.endsWith(".map")) {
                    fileName += ".map";
                }

                Path path = Paths.get(fileName);
                List<String> list = new ArrayList<>();
                String[] currentMap = gamePanelManager.getCurrentMap(counter);

                Collections.addAll(list, currentMap);

                Files.write(path, list);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void jumpMove() {
        String input = jumperInput.getText();
        int previousCounter = counter;
        int line;

        try {
            line = Integer.parseInt(input);

            if (line >= 0 && line < gamePanelManager.getGameSize()) {
                counter = line;
                updateGamePanel();

                if (counter == gamePanelManager.getGameSize() - 1) {
                    previousGame.setEnabled(true);
                    nextGame.setEnabled(false);

                    disqualifyMessage();
                } else if (counter == 0) {
                    previousGame.setEnabled(false);
                    nextGame.setEnabled(true);
                } else {
                    previousGame.setEnabled(true);
                    nextGame.setEnabled(true);
                }
            } else {
                int range = gamePanelManager.getGameSize() - 1;
                JOptionPane.showMessageDialog(
                        GameAnalyzer.this,
                        "Sie müssen eine Zahl zwischen 0 und " + range + " eingeben!",
                        "Fehlerhafte Eingabe",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        catch (Exception e) {
            jumperInput.setText("");
        }

        if (jumperRadio.isSelected()) {
            jumperInput.setText("" + previousCounter);
        } else {
            jumperInput.setText("");
        }
    }

    private void disqualifyMessage() {
        int[] disqualified = gamePanelManager.getDisqualifiedPlayer();
        if (disqualified[gamePanelManager.getOwnPlayer() - 1] == counter) {
            String message = gamePanelManager.getDisqualifyReason();

            JOptionPane.showMessageDialog(
                    GameAnalyzer.this,
                    message,
                    "Client wurde disqualifiziert",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateGamePanel() {
        fieldPercentage.setText(gamePanelManager.getPercentageDistribution(counter));

        List<PlayerPoint> tmpPlayer = gamePanelManager.getPlayer(counter);
        List<BackgroundPoint> tmpBackground = gamePanelManager.getBackground(counter);
        gamePanel.updateFrame(tmpBackground, tmpPlayer);

        playerList.clear();
        playerList.addAll(gamePanelManager.getPlayerInformation(counter));
        playerTableModel.fireTableDataChanged();

        currentMove.setText("Aktuell: " + counter);
    }

    private void showTransition() {
        showTransition.setEnabled(false);
        hideTransition.setEnabled(true);
        gamePanel.showTransitions();
    }

    private void hideTransition() {
        hideTransition.setEnabled(false);
        showTransition.setEnabled(true);
        gamePanel.hideTransitions();
    }

    public void updateCounter(int playerMove) {
        counter = gamePanelManager.getRealPlayerMove(playerMove);
        previousGame();
        nextGame();
    }
}
