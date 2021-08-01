package com.btapo.interview.screening.algo;

import java.util.Stack;

public class ParenthesisChecker {

    public static void main(String[] args) {
        String expression = "[()]{}{[()()]()}";
//        String expression = "{([])}";
//        String expression = "()";
//        String expression = "([]";
        boolean correctPairs = getIfCorrectPairsOfParenthesis(expression);
        System.out.println(correctPairs);
    }

    private static boolean getIfCorrectPairsOfParenthesis(String expression) {
        Stack<String> stack = new Stack<>();

        for (String s : expression.split("")) {
            if (stack.isEmpty()) {
                stack.push(s);
                continue;
            }
            if (")".equals(s)) {
                String lastElement = stack.pop();
                if (!lastElement.equals("(")) {
                    return false;
                }
                continue;
            } else if ("}".equals(s)) {
                String lastElement = stack.pop();
                if (!lastElement.equals("{")) {
                    return false;
                }
                continue;
            } else if ("]".equals(s)) {
                String lastElement = stack.pop();
                if (!lastElement.equals("[")) {
                    return false;
                }
                continue;
            }
            stack.push(s);
        }
        return stack.isEmpty();
    }
}
