package com.company;

import java.util.HashMap;

/**
 * Created by 昕点陈 on 2017/12/2.
 */
public class Viterbi {
    private double[][] A = new double[4][4];
    private double[][] B;
    private double[] pi = new double[4];
    private HashMap<Character, Integer> character_match;
    private int character_number;

    Viterbi(double[][] A, double[][] B, double[] pi, HashMap<Character, Integer> character_match) {
        this.A = A;
        this.B = B;
        this.pi = pi;
        this.character_match = character_match;
        this.character_number = character_match.size();
    }

    public String getMarks(String sentence) {
        int length = sentence.length();
        double[][][] p = new double[length][4][2]; //0 is possibility, 1 is the last max status
        p = forward(sentence);
        return backward(p);
    }

    private double[][][] forward(String sentence) {
        int length = sentence.length();
        double[][][] p = new double[length][4][2]; //1 is possibility, 2 is the max from
        // calculate t = 0
        for (int i = 0; i < 4; i++) {
            if (!character_match.containsKey(sentence.charAt(0))) {
                p[0][i][0] = pi[i] *  (1.0 / (double) character_number);
            } else {
                p[0][i][0] = pi[i] * B[i][character_match.get(sentence.charAt(0))];
            }
            p[0][i][1] = -1;
        }

        // calculate  1 <= t <= length-1
        for (int t = 1; t < length; t++) {
            for (int j = 0; j < 4; j++) {
                double[] possibility = new double[4];
                for (int i = 0; i < 4; i++) {
                    possibility[i] = p[t - 1][i][0] * A[i][j];
                }
                int max = getMax(possibility);
                p[t][j][1] = max;
                if (character_match.containsKey(sentence.charAt(t))) {
                    p[t][j][0] = possibility[max] * B[j][character_match.get(sentence.charAt(t))];
                } else {
                    p[t][j][0] = possibility[max] * 1.0 / (double) character_number;
                }
            }
        }
        return p;
    }

    private String backward(double[][][] p) {
        String marks;

        //get the max possibility of t
        int t = p.length - 1;
        double[] lastPossibility = new double[4];
        lastPossibility[0] = p[t][0][0];
        lastPossibility[3] = p[t][3][0];
        int max = getMax(lastPossibility);
        marks = getStatus(max);

        //backward to get possibility of 0 <= t <= length-2
        for (int i = p.length - 1; i > 0; i--) {
            max = (int) p[i][max][1];
            marks += getStatus(max);
        }
        return new StringBuffer(marks).reverse().toString();
    }

    private int getMax(double[] array) {
        int max = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[max] < array[i]) {
                max = i;
            }
        }
        return max;
    }

    private String getStatus(int number) {
        switch (number) {
            case 0:
                return "S";
            case 1:
                return "B";
            case 2:
                return "I";
            case 3:
                return "E";
            default:
                System.out.println("Error!!");
                return "W";
        }
    }
}
