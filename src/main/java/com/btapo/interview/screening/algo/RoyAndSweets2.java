package com.btapo.interview.screening.algo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

// https://www.hackerearth.com/practice/algorithms/dynamic-programming/introduction-to-dynamic-programming-1/practice-problems/algorithm/roy-and-sweets/
public class RoyAndSweets2 {

    private static final TreeMap<Integer, Integer> sweetnessLevelQuantityMap = new TreeMap<>(Collections.reverseOrder());
    private static final ArrayList<Integer> familiesAndMembers = new ArrayList<>();
    private static final Map<Integer, Integer> maxSweetsForFamilyMember = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
//        Scanner s = new Scanner(new FileInputStream("/home/tapo/IdeaProjects/interview/code-20210604-tapojyotibhattacharjee/data/in/roy-and-sweets-input-1.txt"));
        Scanner s = new Scanner(System.in);
        int numberOfSweets = Integer.parseInt(s.nextLine());
        for (int i = 0; i < numberOfSweets; i++) {
            String[] sweetnessLevelAndQuantityMap = s.nextLine().split(" ");
            sweetnessLevelQuantityMap.put(Integer.parseInt(sweetnessLevelAndQuantityMap[0]),
                    Integer.valueOf(sweetnessLevelAndQuantityMap[1]));
        }
        int numberOfGuestFamilies = Integer.parseInt(s.nextLine());
        for (int i = 0; i < numberOfGuestFamilies; i++) {
            int familyMemberCount = Integer.parseInt(s.nextLine());
            familiesAndMembers.add(familyMemberCount);
        }

        int maxQuantity = 0;
        for (Map.Entry<Integer, Integer> entry : sweetnessLevelQuantityMap.entrySet()) {
            if (maxQuantity < entry.getValue()) {
                maxQuantity = entry.getValue();
            }
            maxSweetsForFamilyMember.put(entry.getKey(), maxQuantity);
        }

        long amount = 0;
        for (Integer f : familiesAndMembers) {
            amount = amount + 100L * (maxSweetsForFamilyMember.getOrDefault(f, 0) / f);
        }

        System.out.println(amount);
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }
}
