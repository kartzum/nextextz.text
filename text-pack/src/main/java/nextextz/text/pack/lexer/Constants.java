package nextextz.text.pack.lexer;

import java.util.Arrays;
import java.util.Collection;

/**
 * Contains constants for 'Lexer'.
 */
public final class Constants {
    private Constants() {
    }

    /**
     * Separator in numbers.
     */
    public static final Character NUMBER_SEPARATOR = '.';

    /**
     * Decimal numbers.
     */
    public static final Collection<Character>
            NUMBERS = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

    /**
     * Brackets - '(' and ')'.
     */
    public static final Collection<Character> BRACKETS = Arrays.asList('(', ')');

    /**
     * Ordinary math operations: '+', '-', '*', '/'.
     */
    public static final Collection<String> SIMPLE_MATH_OPERATIONS = Arrays.asList("+", "-", "*", "/");
}
