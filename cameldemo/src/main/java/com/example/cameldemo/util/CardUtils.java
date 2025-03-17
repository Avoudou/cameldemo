package com.example.cameldemo.util;

public class CardUtils {

    public static boolean isValidCard(String cardNumber) {
        if (cardNumber == null){
            return false;
        }
        String sanitized = sanitizeCardNumber(cardNumber);

        if (!isAllDigits(sanitized)){
            return false;
        }

        if (sanitized.length() != 16){
            return false;
        }

        return isValidLuhn(sanitized);
    }

    public static String sanitizeCardNumber(String cardNumber) {
        return cardNumber == null ? null : cardNumber.replace(" ", "");
    }


    private static boolean isAllDigits(String input) {
        if (input == null || input.isEmpty()) return false;
        for (char c : input.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}
