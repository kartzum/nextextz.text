package nextextz.text.pack.text;

/**
 * Creates entities from 'Text'.
 */
public final class Factory {
    private Factory() {
    }

    /**
     * Create new text bases on string.
     *
     * @param value value.
     * @return text.
     */
    public static Text createText(String value) {
        return new TextLight(value);
    }

    /**
     * Creates new symbol provider.
     *
     * @param value value.
     * @return provider.
     */
    public static SymbolProvider createSymbolProvider(String value) {
        return new SymbolProviderLight(value);
    }
}
