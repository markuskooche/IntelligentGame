package loganalyze.windows;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.List;

public class CompareWindow extends JDialog {

    public CompareWindow(List<Integer> scores, List<Integer> compare) {
        setTitle("Statistik vergleichen");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(270, 220);
        setLayout(null);

        JLabel originallyMin = new JLabel("1. Minimum:  " + getMinimum(scores));
        originallyMin.setBounds(20, 10, 250, 25);
        add(originallyMin);

        JLabel compareMin = new JLabel("2. Minimum:  " + getMinimum(compare));
        compareMin.setBounds(20, 35, 250, 25);
        add(compareMin);

        JLabel originallyMax = new JLabel("1. Maximum:  " + getMaximum(scores));
        originallyMax.setBounds(20, 70, 250, 25);
        add(originallyMax);

        JLabel compareMax = new JLabel("2. Maximum:  " + getMaximum(compare));
        compareMax.setBounds(20, 95, 250, 25);
        add(compareMax);

        JLabel percentNormal = new JLabel("Unterschied im Mittel: " + normalDifference(scores, compare));
        percentNormal.setBounds(20, 130, 250, 25);
        add(percentNormal);

        JLabel descriptionOne = new JLabel("Hierbei werden die Summen der");
        descriptionOne.setBounds(20, 150, 250, 15);
        descriptionOne.setForeground(Color.DARK_GRAY);
        add(descriptionOne);

        JLabel descriptionTwo = new JLabel("Skalen durcheinander geteilt.");
        descriptionTwo.setBounds(20, 165, 250, 15);
        descriptionTwo.setForeground(Color.DARK_GRAY);
        add(descriptionTwo);

        setVisible(true);
    }


    private int getMinimum(List<Integer> list) {
        int minValue = Integer.MAX_VALUE;

        for (int value : list) {
            minValue = Math.min(minValue, value);
        }

        return minValue;
    }

    private int getMaximum(List<Integer> list) {
        int maxValue = Integer.MIN_VALUE;

        for (int value : list) {
            maxValue = Math.max(maxValue, value);
        }

        return maxValue;
    }

    private String normalDifference(List<Integer> a, List<Integer> b) {
        int maxComparison = Math.min(a.size(), b.size());
        BigInteger sumA = new BigInteger(String.valueOf(0));
        BigInteger sumB = new BigInteger(String.valueOf(0));

        for (int i = 0; i < maxComparison; i++) {
            sumA = sumA.add(BigInteger.valueOf(a.get(i)));
            sumB = sumB.add(BigInteger.valueOf(b.get(i)));
        }

        if (getMaximum(a) > getMaximum(b)) {
            sumA = sumA.multiply(BigInteger.valueOf(100));
            BigInteger percentage = sumA.divide(sumB);
            return percentage + "%";
        }

        sumB = sumB.multiply(BigInteger.valueOf(100));
        BigInteger percentage = sumB.divide(sumA);
        return percentage + "%";
    }
}
