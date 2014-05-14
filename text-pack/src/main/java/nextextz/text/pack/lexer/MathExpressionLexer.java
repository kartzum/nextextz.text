package nextextz.text.pack.lexer;

import nextextz.text.pack.text.Text;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts tokens from math expressions. '(9+1)' -> {'(', '9', '+', '1', ')'}.
 */
public class MathExpressionLexer {
    private final Collection<Character> numbers;
    private final Collection<String> operations;
    private final Collection<Character> brackets;
    private final Character numberSeparator;

    private final Map<Integer, Collection<Character>> distributedOperations;

    private final Text text;

    private long position;

    /**
     * Constructs new object.
     *
     * @param text            text (can not be null).
     * @param numbers         numbers (can not be null).
     * @param operations      operations (can not be null)
     * @param brackets        brackets (can not be null)
     * @param numberSeparator numberSeparator (can not be null)
     */
    public MathExpressionLexer(
            Text text,
            Collection<Character> numbers,
            Collection<String> operations,
            Collection<Character> brackets,
            Character numberSeparator) {

        checkNotNull(text);
        checkNotNull(numbers);
        checkNotNull(operations);
        checkNotNull(brackets);
        checkNotNull(numberSeparator);

        this.text = text;
        this.numbers = numbers;
        this.operations = operations;
        this.brackets = brackets;
        this.numberSeparator = numberSeparator;

        this.distributedOperations = Utils.distribute(this.operations);
    }

    /**
     * Returns next token.
     *
     * @return token.
     */
    public Token getNext() {
        Token result = Token.getEmpty();
        for (; ; ) {
            final Character symbol = getSymbol();
            if (symbol == null) {
                break;
            }
            if (isNumberStart(symbol)) {
                result = getNumber();
                break;
            } else if (isOperationStart(symbol)) {
                result = getOperation();
                break;
            } else if (isBracketStart(symbol)) {
                result = getBracket();
                break;
            }
            makeStep();
        }
        return result;
    }

    private Token getNumber() {
        final StringBuilder buffer = new StringBuilder();
        for (; ; ) {
            final Character symbol = getSymbol();
            if (symbol == null) {
                break;
            }
            if (numbers.contains(symbol) || numberSeparator == symbol) {
                buffer.append(symbol);
            } else {
                break;
            }
            makeStep();
        }
        return Token.createNumber(buffer.toString());
    }

    private Token getOperation() {
        final StringBuilder buffer = new StringBuilder();
        int i = 0;
        for (; ; ) {
            final Character symbol = getSymbol();
            if (symbol == null) {
                break;
            }
            final Collection<Character> characters = distributedOperations.get(i);
            if (characters != null && characters.contains(symbol)) {
                buffer.append(symbol);
            } else {
                break;
            }
            i++;
            makeStep();
        }
        return Token.createOperation(buffer.toString());
    }

    private Token getBracket() {
        final Character symbol = getSymbol();
        makeStep();
        return Token.createBracket(String.valueOf(symbol));
    }

    private boolean isNumberStart(Character symbol) {
        return numbers.contains(symbol);
    }

    private boolean isOperationStart(Character symbol) {
        Boolean result = false;
        for (String operation : operations) {
            if (operation.startsWith(String.valueOf(symbol))) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean isBracketStart(Character symbol) {
        return brackets.contains(symbol);
    }

    private Character getSymbol() {
        return text.getSymbol(position);
    }

    private void makeStep() {
        position++;
    }

    /**
     * Type of token.
     */
    public enum TokenType {
        /**
         * Empty.
         */
        EMPTY,

        /**
         * Number.
         */
        NUMBER,

        /**
         * Operation.
         */
        OPERATION,

        /**
         * Bracket.
         */
        BRACKET
    }

    /**
     * Represents token.
     */
    public static class Token {
        private static final String EMPTY_TOKEN_VALUE = "";

        private static final Token EMPTY = new Token(TokenType.EMPTY, EMPTY_TOKEN_VALUE);

        private final TokenType type;
        private final String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        /**
         * Returns type.
         *
         * @return type.
         */
        public TokenType getType() {
            return type;
        }

        /**
         * Returns value.
         *
         * @return value.
         */
        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Token token = (Token) o;
            return type == token.type && value.equals(token.value);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }

        /**
         * Returns empty token.
         *
         * @return empty token.
         */
        public static Token getEmpty() {
            return EMPTY;
        }

        /**
         * Returns new number token.
         *
         * @param value value.
         * @return token.
         */
        public static Token createNumber(String value) {
            return new Token(TokenType.NUMBER, value);
        }

        /**
         * Returns new operation token.
         *
         * @param value value.
         * @return token.
         */
        public static Token createOperation(String value) {
            return new Token(TokenType.OPERATION, value);
        }

        /**
         * Returns new bracket token.
         *
         * @param value value.
         * @return token.
         */
        public static Token createBracket(String value) {
            return new Token(TokenType.BRACKET, value);
        }
    }
}
