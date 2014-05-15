package nextextz.text.pack.lexer;

import com.google.common.collect.Lists;
import nextextz.text.pack.text.Factory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static junit.framework.Assert.assertTrue;

public class MathExpressionLexerTest {
    private MathExpressionLexerHandlerCollector handler;
    private Collection<MathExpressionLexer.Token> tokens;

    @Before
    public void setUp() {
        tokens = Lists.newLinkedList();
        handler = new MathExpressionLexerHandlerCollector(tokens);
    }

    @Test(expected = NullPointerException.class)
    public void test_null() {
        new MathExpressionLexer(null, null, null, null, null, null);
    }

    @Test
    public void test_empty() {
        executeLexer(createLexer("", handler), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_spaces() {
        executeLexer(createLexer("    ", handler), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_number() {
        executeLexer(createLexer("5", handler), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.createNumber("5"),
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_number_with_separator() {
        executeLexer(createLexer("5.777", handler), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.createNumber("5.777"),
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_number_with_spaces() {
        executeLexer(createLexer("   5         ", handler), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.createNumber("5"),
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_operation() {
        executeLexer(createLexer("+", handler), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.createOperation("+"),
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_bracket() {
        executeLexer(createLexer(")", handler), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.createBracket(")"),
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_number_operation_in_bracket() {
        executeLexer(createLexer("(+78.901)", handler), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.createBracket("("),
                MathExpressionLexer.Token.createOperation("+"),
                MathExpressionLexer.Token.createNumber("78.901"),
                MathExpressionLexer.Token.createBracket(")"),
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_with_free_operations() {
        executeLexer(
                createLexer("sin( -7.1 ) * tan( 9.7 )", handler, Arrays.asList("sin", "tan", "*", "-")), handler);
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.createOperation("sin"),
                MathExpressionLexer.Token.createBracket("("),
                MathExpressionLexer.Token.createOperation("-"),
                MathExpressionLexer.Token.createNumber("7.1"),
                MathExpressionLexer.Token.createBracket(")"),
                MathExpressionLexer.Token.createOperation("*"),
                MathExpressionLexer.Token.createOperation("tan"),
                MathExpressionLexer.Token.createBracket("("),
                MathExpressionLexer.Token.createNumber("9.7"),
                MathExpressionLexer.Token.createBracket(")"),
                MathExpressionLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    private static void executeLexer(MathExpressionLexer lexer, MathExpressionLexerHandlerCollector handler) {
        for (; ; ) {
            lexer.execute();
            if (!handler.next()) {
                break;
            }
        }
    }

    private static MathExpressionLexer createLexer(String text, MathExpressionLexerHandler handler) {
        return createLexer(text, handler, Constants.SIMPLE_MATH_OPERATIONS);
    }

    private static MathExpressionLexer createLexer(
            String text,
            MathExpressionLexerHandler handler,
            Collection<String> operations) {
        return new MathExpressionLexer(
                Factory.createText(text),
                handler,
                Constants.NUMBERS, operations, Constants.BRACKETS, Constants.NUMBER_SEPARATOR);
    }

    private static void assertToken(MathExpressionLexer.Token token, MathExpressionLexer.TokenType type, String value) {
        assertTrue(token.getType() == type);
        assertTrue(value.equals(token.getValue()));
    }

    private static void assertTokens(
            Collection<MathExpressionLexer.Token> tokens, Collection<MathExpressionLexer.Token> test) {
        Iterator<MathExpressionLexer.Token> testTokensIterator = test.iterator();
        for (MathExpressionLexer.Token t : tokens) {
            final MathExpressionLexer.Token next = testTokensIterator.next();
            assertToken(t, next.getType(), next.getValue());
        }
    }

    private static class MathExpressionLexerHandlerCollector implements MathExpressionLexerHandler {
        private final Collection<MathExpressionLexer.Token> tokens;

        private MathExpressionLexer.Token tail;

        public MathExpressionLexerHandlerCollector(Collection<MathExpressionLexer.Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public void handle(MathExpressionLexer.Token token) {
            tokens.add(token);
            tail = token;
        }

        public boolean next() {
            boolean result = true;
            if (tail != null) {
                if (tail.getType() == MathExpressionLexer.TokenType.EMPTY) {
                    result = false;
                }
            }
            return result;
        }
    }
}
