package nextextz.text.pack.lexer;

/**
 * Handles tokens from 'Math Lexer'.
 */
public interface MathExpressionLexerHandler {

    /**
     * Handles token.
     *
     * @param token token.
     */
    void handle(MathExpressionLexer.Token token);
}
