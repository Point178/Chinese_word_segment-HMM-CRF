package com.company;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 昕点陈 on 2017/11/29.
 */
public class EM {
    private double[][] A = new double[4][4];
    private double[][] B;
    private double[] pi = new double[4];
    private HashMap<Character, Integer> character_match;
    private int character_number;

    EM(double[][] A, double[][] B, double[] pi, HashMap<Character, Integer> character_match, int character_number) {
        this.A = A;
        this.B = B;
        this.pi = pi;
        this.character_match = character_match;
        this.character_number = character_number;
    }

    public void train(ArrayList<String> sentences) {
        double[] process_pi = new double[4];
        double[][][] process_A = new double[4][4][2]; //0代表分母，1代表分子
        double[][][] process_B = new double[4][character_number][2];

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            int length = sentence.length();
            double[][] forwardA = new double[length][4];
            double[][] backwardB = new double[length][4];
            double[][] y = new double[length - 1][4];
            ArrayList<Double> scale = new ArrayList<>();

            forwardA = forward(sentence, scale);
            backwardB = backward(sentence, scale);
            y = multiply(length, forwardA, backwardB);

            //calculate pi
            for (int status = 0; status < 4; status++) {
                process_pi[status] += y[0][status];
            }

            //calculate A
            double[][] Aup = updateAup(forwardA, backwardB, sentence);
            double[][] Adown = updateAdown(forwardA, backwardB, scale);
            for (int m = 0; m < 4; m++) {
                for (int n = 0; n < 4; n++) {
                    process_A[m][n][1] += Aup[m][n];
                    process_A[m][n][0] += Adown[m][n];
                }
            }

            //calculate B
            double[][] Bup = updateBup(forwardA, backwardB, sentence, scale);
            double[][] Bdown = updateBdown(forwardA, backwardB, sentence, scale);
            for (int m = 0; m < 4; m++) {
                for (int k = 0; k < character_number; k++) {
                    process_B[m][k][0] += Bdown[m][k];
                    process_B[m][k][1] += Bup[m][k];
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            pi[i] = process_pi[i] / (double) sentences.size();
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                A[i][j] = process_A[i][j][1] / process_A[i][j][0];
            }
            for (int k = 0; k < character_number; k++) {
                if(B[i][k] != 0) {
                    B[i][k] = process_B[i][k][1] / process_B[i][k][0];
                }
            }
        }
    }

    private double[][] updateAup(double[][] forwardA, double[][] backwardB, String sentence) {
        double[][] Aup = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double up = 0;
                for (int t = 0; t < forwardA.length - 1; t++) {
                    if (B[j][character_match.get(sentence.charAt(t + 1))] == 0) {
                        up += forwardA[t][i] * A[i][j] * (1.0 / character_number) * backwardB[t + 1][j];
                    } else {
                        up += forwardA[t][i] * A[i][j] * B[j][character_match.get(sentence.charAt(t + 1))] * backwardB[t + 1][j];
                    }
                }
                Aup[i][j] = up;
            }
        }
        return Aup;
    }

    private double[][] updateAdown(double[][] forwardA, double[][] backwardB, ArrayList<Double> scale) {
        double[][] Adown = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double down = 0;
                for (int t = 0; t < forwardA.length - 1; t++) {
                    down += forwardA[t][i] * backwardB[t][i] * scale.get(t);
                }
                Adown[i][j] = down;
            }
        }
        return Adown;
    }

    private double[][] updateBup(double[][] forwardA, double[][] backwardB, String sentence, ArrayList<Double> scale) {
        double[][] Bup = new double[4][character_number];
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < character_number; k++) {
                double up = 0;
                for (int t = 0; t < sentence.length(); t++) {
                    if (character_match.get(sentence.charAt(t)) == k) {
                        up += forwardA[t][i] * backwardB[t][i] * scale.get(t);
                    }
                }
                Bup[i][k] = up;
            }
        }
        return Bup;
    }

    private double[][] updateBdown(double[][] forwardA, double[][] backwardB, String sentence, ArrayList<Double> scale) {
        double[][] Bdown = new double[4][character_number];
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < character_number; k++) {
                double down = 0;
                for (int t = 0; t < sentence.length(); t++) {
                    down += forwardA[t][i] * backwardB[t][i] * scale.get(t);
                }
                Bdown[i][k] = down;
            }
        }
        return Bdown;
    }

    private double[][] forward(String sentence, ArrayList<Double> scale) {
        double[][] a = new double[sentence.length()][4];
        double total = 0;
        for (int i = 0; i < 4; i++) {
            if (B[i][character_match.get(sentence.charAt(0))] == 0) {
                a[0][i] = pi[i] * (1.0 / character_number);
            } else {
                a[0][i] = pi[i] * B[i][character_match.get(sentence.charAt(0))];
            }
            total += a[0][i];
        }
        scale.add(total);
        for (int i = 0; i < 4; i++) {
            a[0][i] = a[0][i] / total;
        }

        for (int t = 1; t < sentence.length(); t++) {
            total = 0;
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    a[t][j] = a[t][j] + a[t - 1][i] * A[i][j];
                }
                if (B[j][character_match.get(sentence.charAt(t))] == 0) {
                    a[t][j] = a[t][j] * (1.0 / character_number);
                } else {
                    a[t][j] = a[t][j] * B[j][character_match.get(sentence.charAt(t))];
                }
                total += a[t][j];
            }
            scale.add(total);
            for (int j = 0; j < 4; j++) {
                a[t][j] = a[t][j] / total;
            }
        }
        return a;
    }

    private double[][] backward(String sentence, ArrayList<Double> scale) {
        double[][] b = new double[sentence.length()][4];
        for (int i = 0; i < 4; i++) {
            b[sentence.length() - 1][i] = 1 * scale.get(sentence.length() - 1);
        }
        for (int t = sentence.length() - 2; t >= 0; t--) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (B[j][character_match.get(sentence.charAt(t + 1))] == 0) {
                        b[t][i] = b[t][i] + A[i][j] * (1.0 / character_number) * b[t + 1][j];
                    } else {
                        b[t][i] = b[t][i] + A[i][j] * B[j][character_match.get(sentence.charAt(t + 1))] * b[t + 1][j];
                    }
                }
                b[t][i] = b[t][i] / scale.get(t);
            }
        }
        return b;
    }

    private double[][] multiply(int length, double[][] forwardA, double[][] backwardB) {
        double y[][] = new double[length][4];
        for (int t = 0; t < length; t++) {
            double total = 0;
            for (int i = 0; i < 4; i++) {
                total += forwardA[t][i] * backwardB[t][i];
            }
            for (int i = 0; i < 4; i++) {
                y[t][i] = (forwardA[t][i] * backwardB[t][i]) / total;
            }
        }

        return y;
    }
}
