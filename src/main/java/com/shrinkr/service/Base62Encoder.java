package com.shrinkr.service;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    private static final int CODE_LENGTH = 6;

    public String encode(long id) {
        long salted = id * 2537 + 91823;
        
        if (salted == 0) return padLeft("0");

        StringBuilder sb = new StringBuilder();
        long num = salted;

        while (num > 0) {
            int remainder = (int) (num % BASE);
            sb.append(CHARSET.charAt(remainder));
            num /= BASE;
        }   

        return padLeft(sb.reverse().toString());
    }

    public long decode(String code) {
        long result = 0;
        for (char c : code.toCharArray()) {
            result = result * BASE + CHARSET.indexOf(c);
        }
        return result;
    }

    private String padLeft(String s) {
        if (s.length() >= CODE_LENGTH) return s.substring(s.length() - CODE_LENGTH);
        return "0".repeat(CODE_LENGTH - s.length()) + s;
    }
}
