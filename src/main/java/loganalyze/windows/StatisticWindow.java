package loganalyze.windows;

import loganalyze.controller.GameAnalyzer;
import loganalyze.additional.IncorrectStatisticException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticWindow extends JDialog {

    private static class StatisticPanel extends JPanel {

        // This code was not developed independently but copied
        // from stackoverflow and adapted to its own needs.
        // LINK: https://stackoverflow.com/a/8693635

        private final List<Integer> scores;
        private List<Integer> compare;

        private Graphics2D g2;

        private final int padding = 20;
        private final int labelPadding = 30;
        private final int pointDistance = 4;
        private final int totalPadding = padding + labelPadding;

        private static final Stroke GRAPH_STROKE = new BasicStroke(2f);

        private final Color gridColor = new Color(200, 200, 200, 200);

        private StatisticPanel(List<Integer> scores) {
            this.scores = scores;
            this.compare = new LinkedList<>();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // draw white background
            g2.setColor(Color.WHITE);
            g2.fillRect(totalPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
            g2.setColor(Color.BLACK);

            // create hatch marks and grid lines for y axis
            int yAxisLabeling = 10;
            for (int i = 0; i < yAxisLabeling + 1; i++) {
                int x0 = padding + labelPadding;
                int x1 = pointDistance + padding + labelPadding;
                int y = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / yAxisLabeling + totalPadding);
                if (scores.size() > 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(totalPadding + 1 + pointDistance, y, getWidth() - padding, y);
                    g2.setColor(Color.BLACK);

                    int yLabelNumber = (int) (getMinScore() + (getDistance() * ((i * 1.0) / yAxisLabeling)));
                    String yLabel;
                    if (yLabelNumber >= 1000000) {
                        if ((yLabelNumber / 1000000) < 10) {
                            double labelNumber = ((double) yLabelNumber) / 1000000;
                            yLabel = "" + String.format("%.1f", labelNumber) + "M";
                        } else {
                            yLabel = "" + (yLabelNumber / 1000000) + "M";
                        }
                    } else if (yLabelNumber >= 1000) {
                        if ((yLabelNumber / 1000) < 10) {
                            double labelNumber = ((double) yLabelNumber) / 1000;
                            yLabel = "" + String.format("%.1f", labelNumber) + "K";
                        } else {
                            yLabel = "" + (yLabelNumber / 1000) + "K";
                        }
                    } else {
                        yLabel = "" + yLabelNumber;
                    }

                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth(yLabel);
                    g2.drawString(yLabel, x0 - labelWidth - 5, y + (metrics.getHeight() / 2) - 3);
                }
                g2.drawLine(x0, y, x1, y);
            }

            // create hatch marks and grid lines for x axis
            for (int i = 0; i < statisticWidth(); i++) {
                if (statisticWidth() > 1) {
                    int x = i * (getWidth() - padding * 2 - labelPadding) / (scores.size() - 1) + totalPadding;
                    int y0 = getHeight() - padding - labelPadding;
                    int y1 = y0 - pointDistance;
                    if ((i % ((int) ((scores.size() / 20.0)) + 1)) == 0) {
                        g2.setColor(gridColor);
                        g2.drawLine(x, getHeight() - totalPadding - 1 - pointDistance, x, padding);
                        g2.setColor(Color.BLACK);
                        String xLabel = i + "";
                        FontMetrics metrics = g2.getFontMetrics();
                        int labelWidth = metrics.stringWidth(xLabel);
                        g2.drawString(xLabel, x - labelWidth / 2, y0 + metrics.getHeight() + 3);
                        g2.drawLine(x, y0, x, y1);
                    }
                }
            }

            // create a border around the diagram
            g2.drawLine(totalPadding, getHeight() - totalPadding, totalPadding, padding);
            g2.drawLine(totalPadding, getHeight() - totalPadding, getWidth() - padding, getHeight() - totalPadding);
            g2.drawLine(totalPadding, padding, getWidth() - padding, padding);
            g2.drawLine(getWidth() - padding, padding, getWidth() - padding, getHeight() - totalPadding);

            // draws a statistic and if available also the comparison statistic
            double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores.size() - 1);
            double yScale = ((double) getHeight() - 2 * padding - labelPadding) / getDistance();
            Stroke stroke = g2.getStroke();

            drawStatistic(stroke, xScale, yScale, scores, false);

            if (!compare.isEmpty()) {
                drawStatistic(stroke, xScale, yScale, compare, true);
            }
        }

        private void drawStatistic(Stroke stroke, double xScale, double yScale, List<Integer> list, boolean second) {
            Color pointColor = new Color(100, 100, 100, 180);
            Color lineColor;

            if (second) {
                lineColor = new Color(230, 102, 44, 180);
            } else {
                lineColor = new Color(44, 102, 230, 180);
            }

            List<Point> listPoints = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                int x1 = (int) (i * xScale + totalPadding);
                int y1 = (int) ((getMaxScore() - list.get(i)) * yScale + padding);
                listPoints.add(new Point(x1, y1));
            }

            g2.setColor(lineColor);
            g2.setStroke(GRAPH_STROKE);
            for (int i = 0; i < listPoints.size() - 1; i++) {
                int x1 = listPoints.get(i).x;
                int y1 = listPoints.get(i).y;
                int x2 = listPoints.get(i + 1).x;
                int y2 = listPoints.get(i + 1).y;
                g2.drawLine(x1, y1, x2, y2);
            }


            g2.setStroke(stroke);
            g2.setColor(pointColor);
            for (Point point : listPoints) {
                int x = point.x - pointDistance / 2;
                int y = point.y - pointDistance / 2;
                g2.fillOval(x, y, pointDistance, pointDistance);
            }
        }

        private int getDistance() {
            return (getMaxScore() - getMinScore());
        }

        private int getMinScore() {
            int minScore = Integer.MAX_VALUE;

            for (Integer score : scores) {
                minScore = Math.min(minScore, score);
            }

            if (!compare.isEmpty()) {
                for (Integer score : compare) {
                    minScore = Math.min(minScore, score);
                }
            }

            return minScore;
        }

        private int getMaxScore() {
            int maxScore = Integer.MIN_VALUE;

            for (Integer score : scores) {
                maxScore = Math.max(maxScore, score);
            }

            if (!compare.isEmpty()) {
                for (Integer score : compare) {
                    maxScore = Math.max(maxScore, score);
                }
            }

            return maxScore;
        }

        private int statisticWidth() {
            return Math.max(scores.size(), compare.size());
        }

        public void compareStatistic(List<Integer> compare) {
            this.compare = compare;
            repaint();
        }
    }

    private final String title;
    private final StatisticPanel statisticPanel;

    private final JMenuItem compareItem;
    private final JMenuItem compareStatistics;

    public StatisticWindow(String title, List<Integer> scores, GameAnalyzer parent, boolean export) {
        this.title = title;

        setTitle(title);
        setMinimumSize(new Dimension(1400, 500));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1,0));

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu;

        menu = new JMenu("Datei");
        menuBar.add(menu);

        JMenuItem exportItem = new JMenuItem("Exportieren");
        exportItem.addActionListener(e -> exportStatistic());
        exportItem.setEnabled(export);
        menu.add(exportItem);

        compareItem = new JMenuItem("Vergleichen");
        compareItem.addActionListener(e -> compareStatistic());
        menu.add(compareItem);

        menu.add(new JSeparator());

        JMenuItem closeApp = new JMenuItem("Beenden");
        menu.add(closeApp);
        closeApp.addActionListener(e -> dispose());

        statisticPanel = new StatisticPanel(scores);
        statisticPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int graphWidth = (getWidth() - (2 * statisticPanel.padding + statisticPanel.labelPadding));
                int graphHeight = (getHeight() - (2 * statisticPanel.padding + statisticPanel.labelPadding));

                double pixelDistanceX = ((double) statisticPanel.scores.size()) / graphWidth;
                double tmpX = (pixelDistanceX * (e.getX() - 40));
                // this calculation is used to smooth the inaccuracy of double
                int x = (int) (tmpX - (tmpX / graphWidth));

                int distance = statisticPanel.getDistance();
                double pixelDistanceY = ((double) distance) / graphHeight;
                int y = statisticPanel.getMaxScore() - ((int) (pixelDistanceY * (e.getY() - 20)));

                System.out.println("[" + x + "]: " + statisticPanel.scores.get(x) + "  " + y);

                if (export) {
                    parent.updateCounter(x);
                }
            }
        });
        add(statisticPanel);

        menu = new JMenu("Fenster");
        menuBar.add(menu);

        compareStatistics = new JMenuItem("Statistik anzeigen");
        compareStatistics.addActionListener(e -> new CompareWindow(statisticPanel.scores, statisticPanel.compare));
        compareStatistics.setEnabled(false);
        menu.add(compareStatistics);

        setVisible(true);
    }

    private void exportStatistic() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Aktuelle Statistik exportieren");

        int userSelection = fileChooser.showSaveDialog(StatisticWindow.this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                String fileName = fileChooser.getSelectedFile().toString();
                int lineCounter = 1;

                if (!fileName.endsWith(".csv")) {
                    fileName += ".csv";
                }

                Path path = Paths.get(fileName);
                List<String> list = new ArrayList<>();
                list.add("\"Wert\";\"" + title + "\"");

                for (Integer score : statisticPanel.scores) {
                    list.add(lineCounter + ";" + score);
                    lineCounter++;
                }

                Files.write(path, list);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void compareStatistic() {
        FileDialog fd = new FileDialog(
                StatisticWindow.this,
                "Statistik vergleichen",
                FileDialog.LOAD
        );

        fd.setFilenameFilter((directory, name) -> name.endsWith(".csv"));
        fd.setVisible(true);

        try {
            String filename = fd.getDirectory() + fd.getFile();

            Path path = Paths.get(filename);
            List<String> file = Files.lines(path).collect(Collectors.toList());

            String tmpTitle = file.get(0).split(";")[1];
            String importedTitle = tmpTitle.substring(1, tmpTitle.length() - 1);

            String windowTitle;
            if (title.startsWith("Importierte")) {
                windowTitle = title.substring(12);
            } else {
                windowTitle = title;
            }

            System.out.println("Title: " + windowTitle);

            if (!windowTitle.equals(importedTitle)) {
                throw new IncorrectStatisticException(importedTitle);
            }

            List<Integer> compareList = new LinkedList<>();
            for (int i = 1; i < file.size(); i++) {
                String value = file.get(i).split(";")[1];
                compareList.add(Integer.parseInt(value));
            }

            statisticPanel.compareStatistic(compareList);
            compareItem.setEnabled(false);

            if (title.contains("Besuchte Spielfelder")) {
                compareStatistics.setEnabled(true);
            }
        }
        catch (IncorrectStatisticException ise) {
            String message = "Sie können '" + ise.getStatisticName() + "' nicht mit '" + title + "' vergleichen.";
            JOptionPane.showMessageDialog(
                    StatisticWindow.this,
                    message,
                    "Unterschiedliche Statistiken",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        catch (IOException ignored) {
            // this happens when the user has not selected a file
        }
        catch (Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    StatisticWindow.this,
                    "Keine gültige ReversiXT-Statistik ausgewählt!",
                    "Fehlerhafte Datei",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
