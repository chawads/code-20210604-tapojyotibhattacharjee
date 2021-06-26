package com.btapo.interview.screening.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

//https://www.hackerearth.com/practice/basic-programming/bit-manipulation/basics-of-bit-manipulation/practice-problems/algorithm/sum-of-numbers-9/
public class SumOfNumbers {

    private static final List<TestCase> testCases = new ArrayList<>();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Scanner s = new Scanner(System.in);
        int numberOfTestCases = Integer.parseInt(s.nextLine());
        for (int i = 0; i < numberOfTestCases; i++) {
            TestCase testCase = new TestCase();
            testCase.numberCount = Integer.parseInt(s.nextLine());
            testCase.numbers = convertToIntArray(s.nextLine().split(" "));
            testCase.sum = Integer.parseInt(s.nextLine());
            testCases.add(testCase);
        }
        for (TestCase testCase : testCases) {
            System.out.println(evaluate(testCase) ? "YES" : "NO");
        }

        System.out.println((System.currentTimeMillis() - start) / 1000);
    }

    private static boolean evaluate(TestCase testCase) {
        Arrays.sort(testCase.numbers);
        int currSum = 0;
        for (int i = 0; i < testCase.numbers.length - 1; i++) {
            currSum += testCase.numbers[i];
            if (currSum == testCase.sum) {
                return true;
            } else if (currSum > testCase.sum) {
                return false;
            }
        }
        return false;
    }

    private static int[] convertToIntArray(String[] s) {
        int[] arr = new int[s.length];
        int i = 0;
        for (String si : s) {
            arr[i++] = Integer.parseInt(si);
        }
        return arr;
    }

    static class TestCase {
        public int numberCount;
        private int sum;
        private int[] numbers;
    }
}
