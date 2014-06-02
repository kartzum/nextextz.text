package nextextz.text.pack.text;

/**
 * Calculates symbols.
 */
public interface SymbolProvider {
    /**
     * Returns current symbol.
     *
     * @return current symbol.
     */
    Character getSymbol();

    /**
     * Moves.
     */
    void move();

    /**
     * Returns current position.
     *
     * @return position.
     */
    long getPosition();
}
