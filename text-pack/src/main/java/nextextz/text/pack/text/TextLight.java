package nextextz.text.pack.text;

import static com.google.common.base.Preconditions.checkNotNull;

class TextLight implements Text {
    private final String value;

    public TextLight(String value) {
        checkNotNull(value);
        this.value = value;
    }

    @Override
    public Character getSymbol(long index) {
        Character result = null;
        if (index < value.length()) {
            result = value.charAt((int) index);
        }
        return result;
    }
}
