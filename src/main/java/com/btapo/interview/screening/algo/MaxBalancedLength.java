package com.btapo.interview.screening.algo;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

public class MaxBalancedLength {

    public static int maxBalancedLength(String s) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < s.length(); i++) {
            int l = getIfBalanced(s.substring(i));
            System.out.println(s.substring(i) + "::" + l);
            max = Math.max(l, max);
        }
        return max;
    }

    private static int getIfBalanced(String expression) {
        int balanceCount = 0;
        Set<String> chars = new HashSet<>();
        Set<String> expectedChars = new HashSet<>();
        Stack<String> stack = new Stack<>();
        for (String s : expression.split("")) {
            String counterCaseOfs = Character.isLowerCase(s.charAt(0)) ?
                    s.toUpperCase(Locale.ROOT) : s.toLowerCase(Locale.ROOT);
            if (expectedChars.contains(s)) {
                if (!stack.pop().equals(counterCaseOfs)) {
                    return balanceCount;
                }
            }
            stack.push(s);
            chars.add(s);
            expectedChars.add(counterCaseOfs);
            ++balanceCount;
        }
        return balanceCount;
    }

    public static void main(String[] args) {
        System.out.println(maxBalancedLength("CATattac"));
    }
}
