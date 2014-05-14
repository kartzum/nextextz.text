package nextextz.text.pack.lexer;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

public class UtilsTest {
    @Test(expected = NullPointerException.class)
    public void test_null() {
        Utils.distribute(null);
    }

    @Test
    public void test_distribute() {
        final Map<Integer, Collection<Character>> data = Utils.distribute(Arrays.asList("+", "add", "-"));
        final Map<Integer, Collection<Character>> test = Maps.newHashMap();
        test.put(0, Arrays.asList('+', 'a', '-'));
        test.put(1, Arrays.asList('d'));
        test.put(2, Arrays.asList('d'));
        for (Map.Entry<Integer, Collection<Character>> e : data.entrySet()) {
            assertTrue(test.containsKey(e.getKey()));
            final Collection<Character> characters = test.get(e.getKey());
            for (Character character : characters) {
                assertTrue(e.getValue().contains(character));
            }
        }
    }
}
