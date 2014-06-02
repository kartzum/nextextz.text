package nextextz.text.pack.lexer;

import nextextz.text.pack.text.SymbolProvider;
import nextextz.text.pack.text.Text;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts tokens from html.
 */
public class HtmlLexer {
    private static final Character START_SYMBOL = '<';
    private static final Character FINISH_SYMBOL = '>';

    private static final Character SPACE = ' ';
    private static final Character MINUS = '-';
    private static final Character SLASH = '/';

    private static final String COMMENTS_TAG = "!--";
    private static final String SCRIPT_TAG = "script";

    private final Text text;
    private final HtmlLexerHandler handler;

    private long position;

    private boolean isCommentsProcessing;
    private boolean isScriptProcessing;

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
            if (isScriptProcessing()) {
                result = getScript();
                finishScriptProcessing();
            } else if (isCommentsProcessing()) {
                result = getComments();
                finishCommentsProcessing();
            } else if (isTag(symbol)) {
                result = getTag();
            } else {
                result = getContent();
            }
        }
        handler.handle(result);
    }

    private Token getTag() {
        final StringBuilder buffer = new StringBuilder();
        final StringBuilder tagNameBuffer = new StringBuilder();

        long firstStartSymbol = -1;
        for (; ; ) {
            final Character symbol = getSymbol();
            if (symbol == null) {
                break;
            }
            if (START_SYMBOL == symbol && firstStartSymbol == -1) {
                firstStartSymbol = getPosition();
            }
            if (extractTagName(symbol, tagNameBuffer)) {
                final String tagName = tagNameBuffer.toString();
                if (COMMENTS_TAG.equals(tagName)) {
                    startCommentsProcessing();
                    break;
                } else if (SCRIPT_TAG.equalsIgnoreCase(tagName)) {
                    startScriptProcessing();
                    if (FINISH_SYMBOL == symbol) {
                        buffer.append(symbol);
                        makeStep();
                    }
                    break;
                }
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

    private Token getComments() {
        final StringBuilder buffer = new StringBuilder();
        for (; ; ) {
            final Character symbol = getSymbol();
            if (symbol == null) {
                break;
            }

            if (buffer.length() >= 2) {
                final char symbolBeforeLast = buffer.charAt(buffer.length() - 2);
                final char symbolLast = buffer.charAt(buffer.length() - 1);
                if (MINUS == symbolBeforeLast && MINUS == symbolLast) {
                    buffer.deleteCharAt(buffer.length() - 1);
                    buffer.deleteCharAt(buffer.length() - 1);
                    skipSpaces();
                    break;
                }
            }

            buffer.append(symbol);
            makeStep();
        }
        return Token.createComments(buffer.toString());
    }

    private Token getScript() {
        final long startPosition = getPosition();
        final StringBuilder buffer = new StringBuilder();
        final SymbolProviderText symbolProvider = new SymbolProviderText(buffer);
        final HtmlCommentsExplorer explorer = new HtmlCommentsExplorer(symbolProvider, symbolProvider);
        for (; ; ) {
            final boolean next = explorer.execute();
            if (!next) {
                break;
            }
        }
        final long finishPosition = symbolProvider.getFinishPosition();
        final int size = (int) (finishPosition - startPosition);
        for (int i = 0; i < buffer.length() - size; i++) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        shiftPosition(finishPosition);
        return Token.createScript(buffer.toString());
    }

    private boolean isTag(Character symbol) {
        return START_SYMBOL == symbol || FINISH_SYMBOL == symbol;
    }

    private boolean extractTagName(Character symbol, StringBuilder buffer) {
        boolean result = false;
        if (SPACE != symbol) {
            if (FINISH_SYMBOL == symbol) {
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(0);
                    result = true;
                }
            } else {
                buffer.append(symbol);
            }
        } else {
            if (buffer.length() > 0) {
                buffer.deleteCharAt(0);
                result = true;
            }
        }
        return result;
    }

    private Character getSymbol() {
        return getSymbol(position);
    }

    private Character getSymbol(long index) {
        return text.getSymbol(index);
    }

    private void makeStep() {
        position++;
    }

    private long getPosition() {
        return position;
    }

    private boolean isCommentsProcessing() {
        return isCommentsProcessing;
    }

    private void finishCommentsProcessing() {
        isCommentsProcessing = false;
    }

    private void startCommentsProcessing() {
        isCommentsProcessing = true;
    }

    private boolean isScriptProcessing() {
        return isScriptProcessing;
    }

    private void finishScriptProcessing() {
        isScriptProcessing = false;
    }

    private void startScriptProcessing() {
        isScriptProcessing = true;
    }

    private void skipSpaces() {
        for (; ; ) {
            final Character symbol = getSymbol();
            if (symbol == null) {
                break;
            }
            if (SPACE != symbol) {
                break;
            }
            makeStep();
        }
    }

    private void shiftPosition(long position) {
        this.position = position;
    }

    private class SymbolProviderText implements SymbolProvider, HtmlCommentsExplorerHandler {
        private final StringBuilder content;

        private final List<Character> FINISH_ARRAY = Arrays.asList(START_SYMBOL, SLASH);

        private final FixSizeBuffer<Character> buffer = new FixSizeBuffer<>(2);

        private int counter;

        private long finishPosition = -1;

        public SymbolProviderText(StringBuilder content) {
            this.content = content;
        }

        @Override
        public Character getSymbol() {
            Character result = HtmlLexer.this.getSymbol();

            if (counter == 0) {
                if (SPACE != result) {
                    buffer.add(result);

                    if (START_SYMBOL == result) {
                        finishPosition = getPosition();
                    }
                }
            }

            if (buffer.match(FINISH_ARRAY)) {
                result = null;
            }

            if (result != null) {
                content.append(result);
            }

            return result;
        }

        @Override
        public void move() {
            HtmlLexer.this.makeStep();
        }

        @Override
        public long getPosition() {
            return HtmlLexer.this.getPosition();
        }

        @Override
        public void start(long position) {
            counter++;
            reset();
        }

        @Override
        public void finish(long position) {
            counter--;
        }

        private void reset() {
            buffer.reset();
            finishPosition = -1;
        }

        public long getFinishPosition() {
            return finishPosition;
        }
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
        CONTENT,

        /**
         * Comments.
         */
        COMMENTS,

        /**
         * Script.
         */
        SCRIPT
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
         * @return content.
         */
        public static Token createContent(String value) {
            return new Token(TokenType.CONTENT, value);
        }

        /**
         * Returns new comments.
         *
         * @param value value.
         * @return comments.
         */
        public static Token createComments(String value) {
            return new Token(TokenType.COMMENTS, value);
        }

        /**
         * Returns new script.
         *
         * @param value value.
         * @return script.
         */
        public static Token createScript(String value) {
            return new Token(TokenType.SCRIPT, value);
        }
    }
}
