package nextextz.text.pack.lexer;

/**
 * Handles data from explorer.
 */
public interface HtmlSpecialContentExplorerHandler {
    /**
     * Handles start.
     *
     * @param position position.
     */
    void start(long position);

    /**
     * Handles finish.
     *
     * @param position position.
     */
    void finish(long position);
}
