package nextextz.text.pack.lexer;

/**
 * Handles tags from 'Html Lexer'.
 */
public interface HtmlLexerHandler {

    /**
     * Handles token.
     *
     * @param token token.
     */
    void handle(HtmlLexer.Token token);
}
