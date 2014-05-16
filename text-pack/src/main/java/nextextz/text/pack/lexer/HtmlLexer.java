package nextextz.text.pack.lexer;

import nextextz.text.pack.text.Text;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts tokens from html.
 */
public class HtmlLexer {
    private static final Character START_SYMBOL = '<';
    private static final Character FINISH_SYMBOL = '>';

    private final Text text;
    private final HtmlLexerHandler handler;

    private long position;

    /**
     * Creates new lexer.
     *
     * @param text    text (can not be null).
     * @param handler handler (can not be null).
     */
    public HtmlLexer(
            Text text,
            HtmlLexerHandler handler) {
        checkNotNull(text);
        checkNotNull(handler);

        this.text = text;
        this.handler = handler;
    }

    /**
     * Executes getting next token.
     */
    public void execute() {
        Token result = Token.getEmpty();
        final Character symbol = getSymbol();
        if (symbol != null) {
            if (isTag(symbol)) {
                result = getTag();
            } else {
                result = getContent();
            }
        }
        handler.handle(result);
    }

    private Token getTag() {
        final StringBuilder buffer = new StringBuilder();

        long firstStartSymbol = -1;
        for (; ; ) {
            final Character symbol = getSymbol();
            if (symbol == null) {
                break;
            }
            if (START_SYMBOL == symbol && firstStartSymbol == -1) {
                firstStartSymbol = getPosition();
            }
            buffer.append(symbol);
            if (FINISH_SYMBOL == symbol || (firstStartSymbol != getPosition() && START_SYMBOL == symbol)) {
                makeStep();
                break;
            }
            makeStep();
        }
        return Token.createTag(buffer.toString());
    }

    private Token getContent() {
        final StringBuilder buffer = new StringBuilder();
        for (; ; ) {
            final Character symbol = getSymbol();
            if (symbol == null) {
                break;
            }
            if (isTag(symbol)) {
                break;
            }
            buffer.append(symbol);
            makeStep();
        }
        return Token.createContent(buffer.toString());
    }

    private boolean isTag(Character symbol) {
        return START_SYMBOL == symbol || FINISH_SYMBOL == symbol;
    }

    private Character getSymbol() {
        return text.getSymbol(position);
    }

    private void makeStep() {
        position++;
    }

    private long getPosition() {
        return position;
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
         * Tag.
         */
        TAG,

        /**
         * Content.
         */
        CONTENT
    }

    /**
     * Token.
     */
    public static class Token {
        private static final String EMPTY_TOKEN_VALUE = "";

        private static final Token EMPTY = new Token(TokenType.EMPTY, EMPTY_TOKEN_VALUE);

        private final TokenType type;
        private final String value;

        /**
         * Creates new token.
         *
         * @param type  type (can not be null).
         * @param value value (can not be null).
         */
        Token(TokenType type, String value) {
            checkNotNull(type);
            checkNotNull(value);

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

        @Override
        public String toString() {
            return "type:\"" + type.toString() + "\", value:\"" + value + "\"";
        }

        /**
         * Returns empty.
         *
         * @return empty.
         */
        public static Token getEmpty() {
            return EMPTY;
        }

        /**
         * Returns new tag.
         *
         * @param value value.
         * @return tag.
         */
        public static Token createTag(String value) {
            return new Token(TokenType.TAG, value);
        }

        /**
         * Returns new content.
         *
         * @param value value.
         * @return contetn.
         */
        public static Token createContent(String value) {
            return new Token(TokenType.CONTENT, value);
        }
    }
}