package com.company;

import java.util.HashMap;

/**
 * Created by 昕点陈 on 2017/12/8.
 */
public class CRF {
    private HashMap<String, Integer>[][] unigramModel;
    private HashMap<String, Integer>[][][] bigramModel;
    private int modelNumber;

    CRF(HashMap<String, Integer>[][] unigramModel, HashMap<String, Integer>[][][] bigramModel) {
        this.unigramModel = unigramModel;
        this.bigramModel = bigramModel;
        this.modelNumber = 13;
    }

    public void addTemplate(String sentence, String mark) {
        String sentence_long = "  " + sentence + "  ";
        for (int t = 0; t < sentence.length(); t++) {
            int s0 = getState(mark.charAt(t));
            int s1;
            if (t > 0) {
                s1 = getState(mark.charAt(t - 1));
            } else {
                s1 = 4;
            }
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

            //00-12
            for (int k = 0; k < modelNumber; k++) {
                putWeight(unigramModel[k][s0], letter[k]);
                putWeight(bigramModel[k][s1][s0], letter[k]);
            }
        }
    }

    public void train(String train_mark, String standard_mark, String sentence) {
        String sentence_long = "  " + sentence + "  ";
        for (int t = 0; t < train_mark.length(); t++) {
            if (train_mark.charAt(t) != standard_mark.charAt(t)) {
                int s0 = getState(standard_mark.charAt(t));
                int t0 = getState(train_mark.charAt(t));
                int s1;
                int t1;
                if (t > 0) {
                    s1 = getState(standard_mark.charAt(t - 1));
                    t1 = getState(train_mark.charAt(t - 1));
                } else {
                    s1 = 4;
                    t1 = 4;
                }
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

                for (int i = 0; i < modelNumber; i++) {
                    unigramModel[i][s0].replace(letter[i], unigramModel[i][s0].get(letter[i]) + 1);
                    bigramModel[i][s1][s0].replace(letter[i], bigramModel[i][s1][s0].get(letter[i]) + 1);
                    if (!unigramModel[i][t0].containsKey(letter[i])) {
                        unigramModel[i][t0].put(letter[i], -1);
                    } else {
                        unigramModel[i][t0].replace(letter[i], (unigramModel[i][t0].get(letter[i]) - 1));
                    }
                    if (!bigramModel[i][t1][t0].containsKey(letter[i])) {
                        bigramModel[i][t1][t0].put(letter[i], -1);
                    } else {
                        bigramModel[i][t1][t0].replace(letter[i], (bigramModel[i][t1][t0].get(letter[i]) - 1));
                    }
                }
            }
        }
    }

    public int getState(char state) {
        switch (state) {
            case 'B':
                return 0;
            case 'I':
                return 1;
            case 'E':
                return 2;
            case 'S':
                return 3;
            default:
                return 4;
        }
    }

    public void putWeight(HashMap<String, Integer> model, String key) {
        if (!model.containsKey(key)) {
            model.put(key, 0);
        }
    }
}
