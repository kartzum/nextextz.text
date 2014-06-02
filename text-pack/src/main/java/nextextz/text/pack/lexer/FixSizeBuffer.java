package nextextz.text.pack.lexer;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * Contains buffer with fix size.
 *
 * @param <T> type.
 */
public class FixSizeBuffer<T> {
    private final List<T> buffer = Lists.newArrayList();
    private final int size;

    /**
     * Creates new buffer.
     *
     * @param size size.
     */
    public FixSizeBuffer(int size) {
        this.size = size;
    }

    /**
     * Adds item.
     *
     * @param item item.
     */
    public void add(T item) {
        if (buffer.size() + 1 > size) {
            buffer.remove(0);
        }
        buffer.add(item);
    }

    /**
     * Clears.
     */
    public void reset() {
        buffer.clear();
    }

    /**
     * Compares buffer and items.
     *
     * @param items items for comparing.
     * @return true, if equal.
     */
    public boolean match(List<T> items) {
        boolean result = true;
        if (items == null) {
            result = false;
        } else if (items.size() != buffer.size()) {
            result = false;
        } else {
            for (int i = 0; i < buffer.size(); i++) {
                if (!Objects.equals(buffer.get(i), items.get(i))) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}