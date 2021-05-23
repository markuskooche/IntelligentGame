package loganalyze.windows;

import controller.Game;
import loganalyze.controller.*;
import loganalyze.tablemodel.PlayerInformation;
import loganalyze.tablemodel.PlayerTableModel;
import loganalyze.additional.IncorrectGroupException;
import loganalyze.additional.OSValidator;
import loganalyze.colorize.BackgroundPoint;
import loganalyze.colorize.PlayerPoint;
import loganalyze.windows.panel.GameField;
import map.Board;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    private int groupNumber;
    private String groupString;

    private final String title;

    private int counter = 0;
    private String lastDirectory= ".";

    private final JMenuItem gameControl;

    private final JMenuItem exportItem;
    private final JMenuItem hideTransition;
    private final JMenuItem showTransition;
    private final JMenuItem visibleItem;
    private final JMenuItem mobilityItem;
    private final JMenuItem coinParityItem;
    private final JMenuItem mapValueItem;
    private final JMenuItem heuristicItem;
    private final JMenuItem visitedBoardItem;

    private final JLabel moveSize;
    private final JLabel ownPlayer;
    private final JLabel bombRadius;
    private final JLabel amountPlayer;
    private final JLabel fieldPercentage;

    private final JLabel currentMove;
    private final JLabel playerMove;

    private final JButton nextGame;
    private final JButton previousGame;

    private final JButton jumperButton;
    private final JTextField jumperInput;
    private final JRadioButton jumperRadio;

    private final List<PlayerInformation> playerList;
    private final PlayerTableModel playerTableModel;
    private final ListSelectionModel playerSelectionModel;

    private GameController gameController;
    private GamePanelManager gamePanelManager;
    private PlayerTableManager playerTableManager;
    private MenuBarPanelManager menuBarPanelManager;
    private StatisticWindowManager statisticWindowManager;
    private InformationPanelManager informationPanelManager;

    private final GameField.GamePanel gamePanel;

    public GameAnalyzer() {
        playerList = new ArrayList<>();

        String[] groups = {
                "Gruppe 01", "Gruppe 02", "Gruppe 03", "Gruppe 04", "Gruppe 05",
                "Gruppe 06", "Gruppe 07", "Gruppe 08", "Gruppe 09", "Gruppe 10"
        };
        Object group = JOptionPane.showInputDialog (
                GameAnalyzer.this,
                "Bitte wählen Sie Ihre Gruppe:",
                "Gruppe wählen",
                JOptionPane.QUESTION_MESSAGE,
                null,
                groups, groups[0]
        );

        try {
            groupString = String.valueOf(group).split(" ")[1];
            groupNumber = Integer.parseInt(groupString);
        }
        catch (Exception e) {
            System.exit(0);
        }

        title = "GameAnalyzer v0.7.0  [Gruppe " + groupNumber + "]";
        setTitle(title);
        if (OSValidator.isMac()) {
            setSize(1110, 890);
        } else {
            setSize(1110, 900);
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(null);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu;

        // ----- ----- ----- ----- GAME ANALYZER - MENU DATEI ----- ----- ----- -----

        menu = new JMenu("Datei");
        menuBar.add(menu);

        JMenuItem openItem = new JMenuItem("\u00d6ffnen");
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

        menu.add(new JSeparator());

        gameControl = new JMenuItem("Manuelle Spielf\u00fchrung");
        gameControl.addActionListener(e -> {
            LinkedList<BackgroundPoint> backgroundPoints = gamePanelManager.getBackgroundPoints(counter);
            LinkedList<PlayerPoint> playerPoints = gamePanelManager.getPlayerPoints(counter);
            Game currentGame = gamePanelManager.getGameState(counter);

            GameControlWindow window = new GameControlWindow(this, currentGame, backgroundPoints, playerPoints);
            gameControl.setEnabled(false);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    gameControl.setEnabled(true);
                }
            });
        });
        gameControl.setEnabled(false);
        menu.add(gameControl);

        // ----- ----- ----- ----- GAME ANALYZER - MENU FENSTER ----- ----- ----- -----

        menu = new JMenu("Fenster");
        menuBar.add(menu);

        JMenuItem colorItem = new JMenuItem("Farbbedeutung");
        colorItem.addActionListener(e -> {
            ColorFieldWindow colorFieldWindow = new ColorFieldWindow(this);
            colorItem.setEnabled(false);
            colorFieldWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    colorItem.setEnabled(true);
                }
            });
        });
        menu.add(colorItem);

        menu.add(new JSeparator());

        visibleItem = new JMenuItem("Erreichbare Felder");
        visibleItem.addActionListener(e -> {
            LinkedList<BackgroundPoint> reachableField = menuBarPanelManager.getReachableField();

            VisibleFieldWindow window = new VisibleFieldWindow(this, reachableField);
            visibleItem.setEnabled(false);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    visibleItem.setEnabled(true);
                }
            });
        });
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
            fd.setFilenameFilter((directory, name) -> name.endsWith(".csv"));
            fd.setDirectory(lastDirectory);
            fd.setVisible(true);

            try {
                String filename = fd.getDirectory() + fd.getFile();
                Path path = Paths.get(filename);
                List<String> file = Files.lines(path).collect(Collectors.toList());
                lastDirectory = fd.getDirectory();

                List<Integer> statisticList = new LinkedList<>();

                for (int i = 1; i < file.size(); i++) {
                    String value = file.get(i).split(";")[1];
                    statisticList.add(Integer.parseInt(value));
                }

                String tmpTitle = file.get(0).split(";")[1];
                String title = tmpTitle.substring(1, tmpTitle.length() - 1);
                new StatisticWindow(("Importierte " + title), statisticList, null, false);
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

        mobilityItem = new JMenuItem("Mobilit\u00e4t");
        mobilityItem.addActionListener(e -> {
            StatisticWindow window = new StatisticWindow("Mobilit\u00e4t", statisticWindowManager.getMobility(), GameAnalyzer.this, true);
            mobilityItem.setEnabled(false);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    mobilityItem.setEnabled(true);
                }
            });
        });
        mobilityItem.setEnabled(false);
        menu.add(mobilityItem);

        coinParityItem = new JMenuItem("Spielfeldbelegung");
        coinParityItem.addActionListener(e -> {
            StatisticWindow window = new StatisticWindow("Spielfeldbelegung", statisticWindowManager.getCoinParity(), GameAnalyzer.this, true);
            coinParityItem.setEnabled(false);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    coinParityItem.setEnabled(true);
                }
            });
        });
        coinParityItem.setEnabled(false);
        menu.add(coinParityItem);

        mapValueItem = new JMenuItem("Spielfeldgewichtung");
        mapValueItem.addActionListener(e -> {
            StatisticWindow window = new StatisticWindow("Spielfeldgewichtung", statisticWindowManager.getMapValue(), GameAnalyzer.this, true);
            mapValueItem.setEnabled(false);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    mapValueItem.setEnabled(true);
                }
            });
        });
        mapValueItem.setEnabled(false);
        menu.add(mapValueItem);

        heuristicItem = new JMenuItem("Gesamtheuristik");
        heuristicItem.addActionListener(e -> {
            StatisticWindow window = new StatisticWindow("Gesamtheuristik", statisticWindowManager.getHeuristic(), GameAnalyzer.this, true);
            heuristicItem.setEnabled(false);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    heuristicItem.setEnabled(true);
                }
            });
        });
        heuristicItem.setEnabled(false);
        menu.add(heuristicItem);

        menu.add(new JSeparator());

        visitedBoardItem = new JMenuItem("Besuchte Spielfelder");
        visitedBoardItem.addActionListener(e -> {
            StatisticWindow window = new StatisticWindow("Besuchte Spielfelder", statisticWindowManager.getVisitedBoards(), GameAnalyzer.this, true);
            visitedBoardItem.setEnabled(false);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    visitedBoardItem.setEnabled(true);
                }
            });
        });
        visitedBoardItem.setEnabled(false);
        menu.add(visitedBoardItem);

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
        fieldPercentage.setBounds(410, 10, 200, 20);
        add(fieldPercentage);

        moveSize = new JLabel("Anzahl Z\u00fcge: -");
        moveSize.setBounds(640, 10, 160, 20);
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
        if (OSValidator.isMac()) {
            titlePane.setBounds(790, 20, 300, 806);
        } else {
            titlePane.setBounds(790, 20, 300, 810);
        }
        add(titlePane);

        // ----- ----- ----- ----- GAME ANALYZER - GAME CONTROLLER ----- ----- ----- -----

        currentMove = new JLabel("Aktuell: -");
        currentMove.setBounds(30, 800, 120, 30);
        add(currentMove);

        playerMove = new JLabel("Spielerzug: -");
        playerMove.setBounds(160, 800, 120, 30);
        add(playerMove);

        previousGame = new JButton("Zur\u00fcck");
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
        jumperInput.addActionListener(e -> jumpMove());
        jumperInput.setBounds(640, 800, 50, 30);
        jumperInput.setEnabled(false);
        add(jumperInput);

        jumperRadio = new JRadioButton("Speichern", false);
        jumperRadio.setBounds(530, 800, 100, 30);
        jumperRadio.addActionListener(e -> jumperInput.setText(""));
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
            updateGameAnalyzer();

            if (counter == 0) {
                previousGame.setEnabled(false);
            }
        }
    }

    private void nextGame() {
        if (counter < gameController.getGameLength() - 1) {
            previousGame.setEnabled(true);
            counter++;
            updateGameAnalyzer();

            if (counter == gameController.getGameLength() - 1) {
                nextGame.setEnabled(false);

                disqualifyMessage();
            }
        }
    }

    private void loadGame() {
        FileDialog fd = new FileDialog(
                GameAnalyzer.this,
                "Analysedatei \u00f6ffnen",
                FileDialog.LOAD
        );
        fd.setFilenameFilter((directory, name) -> name.endsWith(".log"));
        fd.setDirectory(lastDirectory);
        fd.setVisible(true);

        try {
            String filename = fd.getDirectory() + fd.getFile();
            gameController = new GameController(groupString);
            gameController.load(filename);

            gameControl.setEnabled(true);

            gamePanelManager = gameController.getGamePanelManager();
            playerTableManager = gameController.getPlayerTableManager();
            menuBarPanelManager = gameController.getMenuBarPanelManager();
            statisticWindowManager = gameController.getStatisticWindowManager();
            informationPanelManager = gameController.getInformationPanelManager();

            mobilityItem.setEnabled(statisticWindowManager.hasMobility());
            mapValueItem.setEnabled(statisticWindowManager.hasMapValue());
            heuristicItem.setEnabled(statisticWindowManager.hasHeuristic());
            coinParityItem.setEnabled(statisticWindowManager.hasCoinParity());
            visitedBoardItem.setEnabled(statisticWindowManager.hasVisitedBoards());

            boolean reachableField = menuBarPanelManager.hasReachableField();
            visibleItem.setEnabled(reachableField);

            List<int[]> transitions = menuBarPanelManager.getTransitions();
            showTransition.setEnabled(!transitions.isEmpty());
            gamePanel.setTransitions(transitions);
            gamePanel.hideTransitions();

            jumperInput.setEnabled(true);
            jumperRadio.setEnabled(true);
            jumperButton.setEnabled(true);

            moveSize.setText("Anzahl Z\u00fcge: " + (gameController.getGameLength() - 1));

            ownPlayer.setText("Spielfigur: " + informationPanelManager.getOwnPlayer());
            bombRadius.setText("Bombenradius: " + informationPanelManager.getBombRadius());
            amountPlayer.setText("Anzahl Spieler: " + informationPanelManager.getPlayerAmount());

            nextGame.setEnabled(gameController.getGameLength() > 1);
            lastDirectory = fd.getDirectory();
            counter = 0;

            int[] disqualified = playerTableManager.getDisqualifiedPlayers();
            playerTableModel.setDisqualified(disqualified);

            gamePanel.disableHighlighting();
            exportItem.setEnabled(true);
            visibleItem.setEnabled(true);
            hideTransition.setEnabled(false);

            setTitle(title + " -> [" + fd.getFile() + "]");

            updateGameAnalyzer();
        }
        catch (IOException ignored) {
            // this happens when the user has not selected a file
        }
        catch (IncorrectGroupException ig) {
            ig.printStackTrace();
            JOptionPane.showMessageDialog(
                    GameAnalyzer.this,
                    "Sie haben eine Logdatei der Gruppe " + ig.getIncorrectGroup() + " ausgew\u00e4hlt!",
                    "Keine Gruppen\u00fcbereinstimmung",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        catch (Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    GameAnalyzer.this,
                    "Keine g\u00fcltige ReversiXT-Analysedatei ausgew\u00e4hlt!",
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
                String[] currentMap = gameController.getExportMap(counter);

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

            if (line >= 0 && line < (gameController.getGameLength())) {
                counter = line;
                updateGameAnalyzer();

                if (counter == gameController.getGameLength() - 1) {
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
                int range = (gameController.getGameLength() - 1);
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
        int[] disqualified = playerTableManager.getDisqualifiedPlayers();
        if (disqualified[informationPanelManager.getOwnPlayer() - 1] == counter) {
            String message = playerTableManager.getDisqualifyReason();

            JOptionPane.showMessageDialog(
                    GameAnalyzer.this,
                    message,
                    "Client wurde disqualifiziert",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateGameAnalyzer() {
        fieldPercentage.setText(gamePanelManager.getPercentageDistribution(counter));

        List<PlayerPoint> tmpPlayer = gamePanelManager.getPlayerPoints(counter);
        List<BackgroundPoint> tmpBackground = gamePanelManager.getBackgroundPoints(counter);

        if (tmpPlayer != null && tmpBackground != null) {
            gamePanel.updateFrame(tmpBackground, tmpPlayer);

            LinkedList<PlayerInformation> playerInformation = playerTableManager.getPlayerInformation(counter);

            if (playerInformation != null) {
                playerList.clear();
                playerList.addAll(playerTableManager.getPlayerInformation(counter));
                playerTableModel.fireTableDataChanged();
            }

            currentMove.setText("Aktuell: " + counter);
            playerMove.setText("Spielerzug: " + informationPanelManager.getPlayerTurn(counter));
        }
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
        counter = informationPanelManager.getOwnPlayerMove(playerMove);
        previousGame();
        nextGame();
    }
}
