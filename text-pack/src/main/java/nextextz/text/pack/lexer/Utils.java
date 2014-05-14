package nextextz.text.pack.lexer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains some functions for 'Lexer'.
 */
public final class Utils {
    private Utils() {
    }

    /**
     * Distribute values by level and chars.
     *
     * @param values values.
     * @return distributed chars.
     */
    public static Map<Integer, Collection<Character>> distribute(Collection<String> values) {
        checkNotNull(values);
        final Map<Integer, Collection<Character>> result = Maps.newHashMap();
        for (String value : values) {
            if (!Strings.isNullOrEmpty(value)) {
                for (int i = 0; i < value.length(); i++) {
                    final Character c = value.charAt(i);
                    Collection<Character> characters = result.get(i);
                    if (characters == null) {
                        characters = Lists.newLinkedList();
                        result.put(i, characters);
                    }
                    characters.add(c);
                }
            }
        }
        return result;
    }
}
