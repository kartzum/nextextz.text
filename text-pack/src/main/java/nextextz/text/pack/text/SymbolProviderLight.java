package nextextz.text.pack.text;

import static com.google.common.base.Preconditions.checkNotNull;

class SymbolProviderLight implements SymbolProvider {
    private final String value;
    private long position;

    public SymbolProviderLight(String value) {
        checkNotNull(value);
        this.value = value;
    }

    @Override
    public Character getSymbol() {
        Character result = null;
        if (position < value.length()) {
            result = value.charAt((int) position);
        }
        return result;
    }

    @Override
    public void move() {
        position++;
    }

    @Override
    public long getPosition() {
        return position;
    }
}
