package com.btapo.interview.screening.algo;

public class ReverseAString {

    public static String reverseIgnoringSpecialChar(String input) {
        if (input == null || input.isEmpty()) {
            throw new RuntimeException("Input is null or Empty");
        }
        char[] arr = input.toCharArray();

        int l = 0, r = arr.length - 1;
        while (l < r) {
            char cl = arr[l];
            char cr = arr[r];
            if (Character.isAlphabetic(cl) && Character.isAlphabetic(cr)) {
                char tmp = arr[l];
                arr[l] = arr[r];
                arr[r] = tmp;
                l++;
                r--;
            } else if (!Character.isAlphabetic(cl)) {
                l++;
            } else if (!Character.isAlphabetic(cr)) {
                r--;
            }
        }
        return new String(arr);
    }
}
