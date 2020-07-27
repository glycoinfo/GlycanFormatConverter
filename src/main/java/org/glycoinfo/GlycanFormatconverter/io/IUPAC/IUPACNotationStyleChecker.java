package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

public class IUPACNotationStyleChecker {

    public static boolean isUpperCaseCharacter (char _currentChar) {
        return (String.valueOf(_currentChar).matches("[A-Z]"));
    }

    public static boolean isLowerCaseCharacter (char _currentChar) {
        return (String.valueOf(_currentChar).matches("[a-z]"));
    }

    public static boolean isAlphabet (char _currentChar) {
        return (String.valueOf(_currentChar).matches("[A-Za-z]"));
    }

    public static boolean isInteger (char _currentChar) {
        return (String.valueOf(_currentChar).matches("\\d"));
    }

    public static boolean isUndefinedCharacter (char _currentChar) {
        return (String.valueOf(_currentChar).matches("\\?"));
    }

    public static boolean isHyphen (char _currentChar) {
        return (String.valueOf(_currentChar).matches("-"));
    }

    public static boolean isAnomericState (char _currentChar) {
        return (String.valueOf(_currentChar).matches("[ab?\\d]"));
    }

    public static boolean isRightSideBracket (char _currentChar) {
        return (String.valueOf(_currentChar).matches("\\)"));
    }

    public static boolean isLeftSideBracket (char _currentChar) {
        return (String.valueOf(_currentChar).matches("\\("));
    }

    public static boolean isRightBlockBracket (char _currentChar) {
        return (String.valueOf(_currentChar).matches("]"));
    }

    public static boolean isLeftBlockBracket (char _currentChar) {
        return (String.valueOf(_currentChar).matches("\\["));
    }
}
