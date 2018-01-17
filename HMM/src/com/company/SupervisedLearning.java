package com.company;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 昕点陈 on 2017/11/29.
 */
public class SupervisedLearning {
    private double[][] A = new double[4][4];
    private double[][] B;
    private double[] pi = new double[4];
    private ArrayList<String> sentences;
    private ArrayList<String> marks;
    private HashMap<Character, Integer> character_match;
    private int character_number;

    SupervisedLearning(ArrayList<String> sentences, ArrayList<String> marks, int character_number, HashMap<Character, Integer> character_match) {
        this.sentences = sentences;
        this.marks = marks;
        this.character_number = character_number;
        this.B = new double[4][character_number];
        this.character_match = character_match;
    }

    public void train() {
        int[] pi_number = new int[4];
        //train pi
        for (int i = 0; i < marks.size(); i++) {
            switch (marks.get(i).charAt(0)) {
                case 'S':
                    pi_number[0]++;
                    break;
                case 'B':
                    pi_number[1]++;
                    break;
                case 'I':
                    pi_number[2]++;
                    break;
                case 'E':
                    pi_number[3]++;
                    break;
                default:
                    System.out.println("error");
                    break;
            }
        }
        for (int i = 0; i < 4; i++) {
            pi[i] = (double)pi_number[i] / (double)marks.size();
        }

        //train A
        int a;
        int b;
        int[][] A_number = new int[4][4];
        for (int i = 0; i < marks.size(); i++) {
            for (int j = 0; j < marks.get(i).length() - 1; j++) {
                a = getStatus(marks.get(i).charAt(j));
                b = getStatus(marks.get(i).charAt(j + 1));
                A_number[a][b]++;
            }
        }

        for (int i = 0; i < 4; i++) {
            int totalTransfer = A_number[i][0] + A_number[i][1] + A_number[i][2] + A_number[i][3];
            for (int j = 0; j < 4; j++) {
                A[i][j] = (double)A_number[i][j] / (double)totalTransfer;
            }
        }

        //train B
        int[] B_total = new int[4]; //计算SBIE的总数量
        int[][] B_number = new int[4][character_number];
        for (int i = 0; i < sentences.size(); i++) {
            for (int j = 0; j < sentences.get(i).length(); j++) {
                int status = getStatus(marks.get(i).charAt(j));
                char character = sentences.get(i).charAt(j);
                // 先计算SBIE中每个汉字的出现数量
                B_total[status]++;
                B_number[status][character_match.get(character)]++;
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < character_number; j++) {
                B[i][j] = (double)B_number[i][j] / (double)B_total[i];
            }
        }
    }

    public double[][] getA() {
        return A;
    }

    public double[][] getB() {
        return B;
    }

    public double[] getPi() {
        return pi;
    }

    private int getStatus(char a) {
        switch (a) {
            case 'S':
                return 0;
            case 'B':
                return 1;
            case 'I':
                return 2;
        }
        return 3;
    }
}
