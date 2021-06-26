package com.btapo.interview.screening.algo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

// https://www.hackerearth.com/practice/algorithms/dynamic-programming/introduction-to-dynamic-programming-1/practice-problems/algorithm/roy-and-sweets/
public class RoyAndSweets1 {

    private static LinkedHashMap<Integer, Integer> sweetnessLevelQuantityMap = new LinkedHashMap<>();
    private static ArrayList<Integer> familiesAndMembers = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        Scanner s = new Scanner(new FileInputStream(new File("/home/tapo/IdeaProjects/interview/code-20210604-tapojyotibhattacharjee/data/in/roy-and-sweets-input-1.txt")));
//        Scanner s = new Scanner(System.in);
        int numberOfSweets = Integer.parseInt(s.nextLine());
        for (int i = 0; i < numberOfSweets; i++) {
            String[] sweetnessLevelAndQuantityMap = s.nextLine().split(" ");
            sweetnessLevelQuantityMap.put(Integer.parseInt(sweetnessLevelAndQuantityMap[0]),
                    Integer.valueOf(sweetnessLevelAndQuantityMap[1]));
        }
        int numberOfGuestFamilies = Integer.parseInt(s.nextLine());
        for (int i = 0; i < numberOfGuestFamilies; i++) {
            familiesAndMembers.add(Integer.parseInt(s.nextLine()));
        }

        int moneyCollected = 0;
        for (Integer familyAndMembers : familiesAndMembers) {
            List<EvaluationIfSweetCanBeServed> evaluations = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : sweetnessLevelQuantityMap.entrySet()) {
                EvaluationIfSweetCanBeServed evaluationIfSweetCanBeServed = evaluate(familyAndMembers, entry);
                if (evaluationIfSweetCanBeServed.satisfiesConstraint) {
                    evaluations.add(evaluationIfSweetCanBeServed);
                }
            }
            Map<Integer, List<EvaluationIfSweetCanBeServed>> similarSweetnessLevel = getAllSimilarSweetnessLevel(evaluations);
            Map<Integer, Integer> amountPerSweetNessLevel = getAmountForSweetnessLevel(similarSweetnessLevel);
            if (amountPerSweetNessLevel.size() > 0) {
                int amount = Collections.max(amountPerSweetNessLevel.values());
                moneyCollected += amount;
            }
        }
        System.out.println(moneyCollected);
    }

    private static Map<Integer, Integer> getAmountForSweetnessLevel(Map<Integer, List<EvaluationIfSweetCanBeServed>> successfulEvaluations) {
        Map<Integer, Integer> amount = new HashMap<>();
        for (Map.Entry<Integer, List<EvaluationIfSweetCanBeServed>> entry : successfulEvaluations.entrySet()) {
            amount.put(entry.getKey(), calculateAmount(entry.getValue()));
        }
        return amount;
    }

    private static Integer calculateAmount(List<EvaluationIfSweetCanBeServed> value) {
        int amount = 0;
        for (EvaluationIfSweetCanBeServed evaluationIfSweetCanBeServed : value) {
            amount += 100 * evaluationIfSweetCanBeServed.eachMemberHadSweets;
        }
        return amount;
    }

    private static Map<Integer, List<EvaluationIfSweetCanBeServed>> getAllSimilarSweetnessLevel(List<EvaluationIfSweetCanBeServed> evaluations) {
        Map<Integer, List<EvaluationIfSweetCanBeServed>> similarSweetnessLevels = new HashMap<>();
        for (EvaluationIfSweetCanBeServed evaluationIfSweetCanBeServed : evaluations) {
            similarSweetnessLevels.computeIfAbsent(evaluationIfSweetCanBeServed.sweetnessLevel, k -> new ArrayList<>())
                    .add(evaluationIfSweetCanBeServed);
        }
        return similarSweetnessLevels;
    }

    private static EvaluationIfSweetCanBeServed evaluate(Integer familyAndMembers, Map.Entry<Integer, Integer> entry) {
        EvaluationIfSweetCanBeServed evaluationIfSweetCanBeServed = new EvaluationIfSweetCanBeServed();
        int sweetnessLevelOfTheSweet = entry.getKey();
        int quantityOfTheSweet = entry.getValue();
        if (quantityOfTheSweet < familyAndMembers
                || sweetnessLevelOfTheSweet < familyAndMembers
        ) {
            evaluationIfSweetCanBeServed.satisfiesConstraint = false;
        } else {
            evaluationIfSweetCanBeServed.eachMemberHadSweets = quantityOfTheSweet / familyAndMembers;
            evaluationIfSweetCanBeServed.satisfiesConstraint = true;
            evaluationIfSweetCanBeServed.sweetnessLevel = sweetnessLevelOfTheSweet;
        }
        return evaluationIfSweetCanBeServed;
    }

    private static class EvaluationIfSweetCanBeServed {
        private boolean satisfiesConstraint = false;
        private int eachMemberHadSweets = 0;
        private int sweetnessLevel = 0;
    }
}
