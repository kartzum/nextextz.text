package nextextz.text.pack.lexer;

import com.google.common.collect.Lists;
import nextextz.text.pack.text.Factory;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static junit.framework.Assert.assertTrue;

public class MathExpressionLexerTest {

    @Test(expected = NullPointerException.class)
    public void test_null() {
        new MathExpressionLexer(null, null, null, null, null);
    }

    @Test
    public void test_empty() {
        final MathExpressionLexer lexer = createLexer("");
        assertTrue(lexer.getNext() == MathExpressionLexer.Token.getEmpty());
    }

    @Test
    public void test_spaces() {
        final MathExpressionLexer lexer = createLexer("    ");
        assertTrue(lexer.getNext() == MathExpressionLexer.Token.getEmpty());
    }

    @Test
    public void test_number() {
        final MathExpressionLexer lexer = createLexer("5");
        assertToken(lexer.getNext(), MathExpressionLexer.TokenType.NUMBER, "5");
    }

    @Test
    public void test_number_with_separator() {
        final MathExpressionLexer lexer = createLexer("5.777");
        assertToken(lexer.getNext(), MathExpressionLexer.TokenType.NUMBER, "5.777");
    }

    @Test
    public void test_number_with_spaces() {
        final MathExpressionLexer lexer = createLexer("   5         ");
        assertToken(lexer.getNext(), MathExpressionLexer.TokenType.NUMBER, "5");
    }

    @Test
    public void test_operation() {
        final MathExpressionLexer lexer = createLexer("+");
        assertToken(lexer.getNext(), MathExpressionLexer.TokenType.OPERATION, "+");
    }

    @Test
    public void test_bracket() {
        final MathExpressionLexer lexer = createLexer(")");
        assertToken(lexer.getNext(), MathExpressionLexer.TokenType.BRACKET, ")");
    }

    @Test
    public void test_number_operation_in_bracket() {
        final Collection<MathExpressionLexer.Token> tokens = executeLexer(createLexer("(+78.901)"));
        final Collection<MathExpressionLexer.Token> test = Lists.newArrayList(
                MathExpressionLexer.Token.createBracket("("),
                MathExpressionLexer.Token.createOperation("+"),
                MathExpressionLexer.Token.createNumber("78.901"),
                MathExpressionLexer.Token.createBracket(")")
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_with_free_operations() {
        final Collection<MathExpressionLexer.Token> tokens =
                executeLexer(createLexer("sin( -7.1 ) * tan( 9.7 )", Arrays.asList("sin", "tan", "*", "-")));
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
                MathExpressionLexer.Token.createBracket(")")
        );
        assertTokens(tokens, test);
    }

    private static Collection<MathExpressionLexer.Token> executeLexer(MathExpressionLexer lexer) {
        final Collection<MathExpressionLexer.Token> result = Lists.newArrayList();
        MathExpressionLexer.Token token;
        while ((token = lexer.getNext()) != MathExpressionLexer.Token.getEmpty()) {
            result.add(token);
        }
        return result;
    }

    private static MathExpressionLexer createLexer(String text) {
        return createLexer(text, Constants.SIMPLE_MATH_OPERATIONS);
    }

    private static MathExpressionLexer createLexer(String text, Collection<String> operations) {
        return new MathExpressionLexer(
                Factory.createText(text),
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
}
