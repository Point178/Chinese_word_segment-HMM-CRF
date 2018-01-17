package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        ArrayList<String> sentences = new ArrayList<String>();
        ArrayList<String> marks = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("train_corpus.utf8"));
        String read;
        int sentence_num = 0;
        sentences.add("");
        marks.add("");

        //read train_corpus.utf8 and store it in ArrayList
        while ((read = br.readLine()) != null) {
            if (Objects.equals(read, "")) {
                sentence_num++;
                sentences.add("");
                marks.add("");
            } else {
                sentences.set(sentence_num, sentences.get(sentence_num) + read.split(" ")[0]);
                marks.set(sentence_num, marks.get(sentence_num) + read.split(" ")[1]);
            }
        }
        sentence_num++;
        br.close();

        //read train.utf8 and store it in ArrayList
        ArrayList<String> test_sentences = new ArrayList<String>();
        ArrayList<String> test_marks = new ArrayList<String>();
        br = new BufferedReader(new FileReader("ctb_test_corpus.utf8"));
        int test_num = 0;
        test_sentences.add("");
        test_marks.add("");
        while ((read = br.readLine()) != null) {
            if (Objects.equals(read, "")) {
                test_sentences.add("");
                test_marks.add("");
                test_num++;
            } else {
                test_sentences.set(test_num, test_sentences.get(test_num) + read.split(" ")[0]);
                test_marks.set(test_num, test_marks.get(test_num) + read.split(" ")[1]);
            }
        }
        test_num++;
        br.close();

        //create crf model
        int unigramNumber = 13;
        int bigramNumber = 13;
        int state = 4;
        HashMap<String, Integer>[][] unigramModel = new HashMap[unigramNumber][state + 1]; // s0
        for (int i = 0; i < unigramNumber; i++) {
            for (int j = 0; j < state + 1; j++) {
                unigramModel[i][j] = new HashMap<String, Integer>();
            }
        }
        HashMap<String, Integer>[][][] bigramModel = new HashMap[bigramNumber][state + 1][state + 1]; // s-1 s0
        for (int i = 0; i < unigramNumber; i++) {
            for (int j = 0; j < state + 1; j++) {
                for (int z = 0; z < state + 1; z++) {
                    bigramModel[i][j][z] = new HashMap<String, Integer>();
                }
            }
        }
        CRF crf = new CRF(unigramModel, bigramModel);

        //first round : add template function into crf
        for (int i = 0; i < sentence_num; i++) {
            crf.addTemplate(sentences.get(i), marks.get(i));
        }

        //start train
        int loop = 20;
        for (int step = 0; step < loop; step++) {
            for (int i = 0; i < sentence_num; i++) {
                Viterbi viterbi = new Viterbi(unigramModel, bigramModel);
                String train_mark = viterbi.getMarks(sentences.get(i));
                crf.train(train_mark, marks.get(i), sentences.get(i));
            }

            double accuracy = calculateAccuracy(unigramModel, bigramModel, test_num, test_sentences, test_marks);
            System.out.println("Step " + step + " : accuracy = " + accuracy);
        }
        saveToFile(unigramModel, bigramModel, test_num, test_sentences);
    }

    private static int getAccuracy(String a, String b) {
        int result = 0;
        for (int i = 0; i < b.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                result++;
            }
        }
        return result;
    }

    private static double calculateAccuracy(HashMap<String, Integer>[][] unigramModel, HashMap<String, Integer>[][][] bigramModel,
                                            int sentence_num, ArrayList<String> sentences, ArrayList<String> marks) {
        //Viterbi
        Viterbi viterbi = new Viterbi(unigramModel, bigramModel);
        ArrayList<String> trainMarks = new ArrayList<>();
        for (int i = 0; i < sentence_num; i++) {
            String trainMark = viterbi.getMarks(sentences.get(i));
            trainMarks.add(trainMark);
        }

        //calculate accuracy
        int total = 0;
        int accuracy = 0;
        for (int i = 0; i < sentence_num; i++) {
            total += marks.get(i).length();
            accuracy += getAccuracy(trainMarks.get(i), marks.get(i));
        }
        return ((double) accuracy / (double) total);
    }

    private static void saveToFile (HashMap<String, Integer>[][] unigramModel, HashMap<String, Integer>[][][] bigramModel,
                                    int sentence_num, ArrayList<String> sentences) throws Exception{
        File save  = new File("result.utf8");
        FileWriter fw = new FileWriter(save);
        //Viterbi
        Viterbi viterbi = new Viterbi(unigramModel, bigramModel);
        for (int i = 0; i < sentence_num; i++) {
            String trainMark = viterbi.getMarks(sentences.get(i));
            for(int j = 0; j < trainMark.length();j++){
                fw.write(String.valueOf(sentences.get(i).charAt(j))+" "+String.valueOf(trainMark.charAt(j))+"\r\n");
            }
            if(i != sentence_num-1) {
                fw.write("\r\n");
            }
        }
        fw.close();
    }
}
