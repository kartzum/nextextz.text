package nextextz.text.pack.lexer;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class FixSizeBufferTest {
    @Test
    public void test_with_nulls() {
        final List<Character> ARRAY = Arrays.asList(' ', null);
        final FixSizeBuffer<Character> buffer = new FixSizeBuffer<>(2);
        buffer.add(' ');
        buffer.add(null);
        assertTrue(buffer.match(ARRAY));
    }

    @Test
    public void test_nulls() {
        final FixSizeBuffer<Character> buffer = new FixSizeBuffer<>(2);
        buffer.add(' ');
        assertFalse(buffer.match(null));
    }

    @Test
    public void test_different_sizes() {
        final List<Character> ARRAY = Arrays.asList(' ', '3');
        final FixSizeBuffer<Character> buffer = new FixSizeBuffer<>(2);
        buffer.add(' ');
        assertFalse(buffer.match(ARRAY));
    }

    @Test
    public void test_different_reset() {
        final List<Character> ARRAY = Arrays.asList(' ');
        final FixSizeBuffer<Character> buffer = new FixSizeBuffer<>(2);
        buffer.add(' ');
        buffer.reset();
        assertFalse(buffer.match(ARRAY));
    }
}
