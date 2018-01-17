package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        // write your code here
        ArrayList<String> sentences = new ArrayList<String>();
        ArrayList<String> marks = new ArrayList<String>();
        HashMap<Character, Integer> character_match = new HashMap<>();

        //read train.utf8 and store it in ArrayList
        BufferedReader br = new BufferedReader(new FileReader("train_corpus.utf8"));
        String read;
        int sentence_num = 0;
        sentences.add("");
        marks.add("");

        int character_number = 0;
        while ((read = br.readLine()) != null) {
            if (Objects.equals(read, "")) {
                sentence_num++;
                sentences.add("");
                marks.add("");
            } else {
                if (!character_match.containsKey(read.split(" ")[0].toCharArray()[0])) {
                    character_match.put(read.split(" ")[0].toCharArray()[0], character_number);
                    character_number++;
                }
                sentences.set(sentence_num, sentences.get(sentence_num) + read.split(" ")[0]);
                marks.set(sentence_num, marks.get(sentence_num) + read.split(" ")[1]);
            }
        }
        br.close();

        // read train_utf8 and store it in the array list
        ArrayList<String> test_sentences = new ArrayList<String>();
        ArrayList<String> test_marks = new ArrayList<String>();
        br = new BufferedReader(new FileReader("ctb_test_corpus.utf8"));
        int test_num = 0;
        test_sentences.add("");
        test_marks.add("");
        while ((read = br.readLine()) != null) {
            if (Objects.equals(read, "")) {
                test_num++;
                test_sentences.add("");
                test_marks.add("");
            } else {
                test_sentences.set(test_num, test_sentences.get(test_num) + read.split(" ")[0]);
                test_marks.set(test_num, test_marks.get(test_num) + read.split(" ")[1]);
            }
        }
        test_num++;
        br.close();

        //train
        double[][] A = new double[4][4];
        double[][] B = new double[4][character_number];
        double[] pi = new double[4];

        //supervisedLearning
        SupervisedLearning supervise = new SupervisedLearning(sentences, marks, character_number, character_match);
        supervise.train();
        A = supervise.getA();
        B = supervise.getB();
        pi = supervise.getPi();

        double accuracy = calculateAccuracy(A, B, pi, character_match, test_num, test_sentences, test_marks);
        System.out.println("accuracy " + accuracy);

        /*//EM algorithm
        int loop = 30;
        EM em = new EM(A, B, pi, character_match, character_number);
        for (int circle = 0; circle < loop; circle++) {
            em.train(sentences);
            accuracy = calculateAccuracy(A, B, pi, character_match, test_num, test_sentences, test_marks);
            System.out.println("step " +circle + " :accuracy " + accuracy);
        }*/

        //save to file
        saveToFile(A, B, pi, character_match, test_num, test_sentences);
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

    private static double calculateAccuracy(double[][] A, double[][] B, double[] pi, HashMap<Character, Integer> character_match,
                                            int sentence_num, ArrayList<String> sentences, ArrayList<String> marks) {
        //Viterbi
        Viterbi viterbi = new Viterbi(A, B, pi, character_match);
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

    private static void saveToFile (double[][] A, double[][] B, double[] pi, HashMap<Character, Integer> character_match,
                                   int sentence_num, ArrayList<String> sentences) throws Exception{
        File save  = new File("result.utf8");
        FileWriter fw = new FileWriter(save);
        //Viterbi
        Viterbi viterbi = new Viterbi(A, B, pi, character_match);
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
