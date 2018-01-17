package com.company;

import java.util.HashMap;

/**
 * Created by 昕点陈 on 2017/12/8.
 */
public class Viterbi {
    private HashMap<String, Integer>[][] unigramModel;
    private HashMap<String, Integer>[][][] bigramModel;
    private int modelNumber;

    Viterbi(HashMap<String, Integer>[][] unigramModel, HashMap<String, Integer>[][][] bigramModel) {
        this.unigramModel = unigramModel;
        this.bigramModel = bigramModel;
        this.modelNumber = 13;
    }

    public String getMarks(String sentence) {
        int length = sentence.length();
        int[][][] score = new int[length][4][2]; //0 is score, 1 is the last max status
        score = forward(sentence);
        return backward(score);
    }

    private int[][][] forward(String sentence) {
        int length = sentence.length();
        int[][][] score = new int[length][4][2]; //0 is score, 1 is the max from
        String sentence_long = "  " + sentence + "  ";
        for (int t = 0; t < sentence.length(); t++) {
            String letter[] = new String[modelNumber];
            for (int i = t; i < t + 5; i++) {
                letter[i - t] = String.valueOf(sentence_long.charAt(i));
            }
            letter[5] = letter[0] + letter[1];
            letter[6] = letter[1] + letter[2];
            letter[7] = letter[2] + letter[3];
            letter[8] = letter[1] + letter[3];
            letter[9] = letter[3] + letter[4];
            letter[10] = letter[1] + letter[2] + letter[3];
            letter[11] = letter[0] + letter[1] + letter[2];
            letter[12] = letter[2] + letter[3] + letter[4];

            if (t == 0) {
                for (int i = 0; i < 4; i++) {
                    score[0][i][1] = -1;
                    for (int k = 0; k < modelNumber; k++) {
                        if(unigramModel[k][i].containsKey(letter[k])) {
                            score[0][i][0] += unigramModel[k][i].get(letter[k]);
                        }
                        if(bigramModel[k][4][i].containsKey(letter[k])) {
                            score[0][i][0] += bigramModel[k][4][i].get(letter[k]);
                        }
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    int possible[] = new int[4];
                    //come from 4 states before
                    for (int j = 0; j < 4; j++) {
                        possible[j] = score[t - 1][j][0];
                        for (int k = 0; k < modelNumber; k++) {
                            if(unigramModel[k][i].containsKey(letter[k])) {
                                possible[j] += unigramModel[k][i].get(letter[k]);
                            }
                            if(bigramModel[k][j][i].containsKey(letter[k])) {
                                possible[j] += bigramModel[k][j][i].get(letter[k]);
                            }
                        }
                    }

                    score[t][i][1] = getMax(possible);
                    score[t][i][0] = possible[score[t][i][1]];
                }
            }
        }
        return score;
    }

    private String backward(int[][][] score) {
        String marks;

        //get the max possibility of t
        int t = score.length - 1;
        int[] lastPossibility = new int[4];
        lastPossibility[2] = score[t][2][0];
        lastPossibility[3] = score[t][3][0];
        int max = getMax(lastPossibility);
        marks = getStatus(max);

        //backward to get possibility of 0 <= t <= length-2
        for (int i = score.length - 1; i > 0; i--) {
            max = score[i][max][1];
            marks += getStatus(max);
        }
        return new StringBuffer(marks).reverse().toString();
    }

    private int getMax(int[] array) {
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
                return "B";
            case 1:
                return "I";
            case 2:
                return "E";
            case 3:
                return "S";
            default:
                System.out.println("Error!!");
                return "W";
        }
    }
}
