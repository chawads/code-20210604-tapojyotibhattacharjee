package com.btapo.interview.screening.algo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class ReverseStringTests {

    @Test
    @DisplayName("Reverse string without special character config")
    public void reverseIgnoringSpecialChar_1() {
        String result = ReverseAString.reverseIgnoringSpecialChar("a!!!b.c.d,e'f,ghi");
        assert result.equals("ed1c2bA34");
    }
}
