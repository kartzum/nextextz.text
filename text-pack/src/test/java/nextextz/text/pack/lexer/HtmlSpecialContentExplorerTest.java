package nextextz.text.pack.lexer;

import com.google.common.collect.Lists;
import nextextz.text.pack.text.Factory;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class HtmlSpecialContentExplorerTest {
    @Test(expected = NullPointerException.class)
    public void test_null() {
        new HtmlSpecialContentExplorer(null, null);
    }

    @Test
    public void test_slash_comments_and_spaces() {
        final Collection<String> parts = Lists.newArrayList();
        final String text = "// 1\r\n2  // 4 /*  4 ***/  g";
        final HtmlSpecialContentExplorerHandler handler = new HtmlSpecialContentExplorerHandlerCollector(text, parts);
        final HtmlSpecialContentExplorer explorer = new HtmlSpecialContentExplorer(Factory.createSymbolProvider(text), handler);
        executeExplorer(explorer);
        assertParts(parts, Lists.newArrayList(" 1\r\n", "  4 ***/", " 4 /*  4 ***/  g"));
    }

    @Test
    public void test_xml_comments_and_spaces() {
        final Collection<String> parts = Lists.newArrayList();
        final String text = "/* 1 <!-- 2 --> 3 */ // 4\r\n <!-- 6 /* 7 */ -->";
        final HtmlSpecialContentExplorerHandler handler = new HtmlSpecialContentExplorerHandlerCollector(text, parts);
        final HtmlSpecialContentExplorer explorer = new HtmlSpecialContentExplorer(Factory.createSymbolProvider(text), handler);
        executeExplorer(explorer);
        assertParts(parts, Lists.newArrayList(" 2 --> ", " 1 <!-- 2 --> 3 */", "/ 4\r\n", " 7 */", " 6 /* 7 */ -->"));
    }

    private static void executeExplorer(HtmlSpecialContentExplorer explorer) {
        for (; ; ) {
            final boolean next = explorer.execute();
            if (!next) {
                break;
            }
        }
    }

    private static void assertParts(Collection<String> parts, Collection<String> test) {
        Iterator<String> testIterator = test.iterator();
        for (String part : parts) {
            final String testItem = testIterator.next();
            assertTrue(testItem.equals(part));
        }
    }

    private static class HtmlSpecialContentExplorerHandlerCollector implements HtmlSpecialContentExplorerHandler {
        private List<Long> startPositions = Lists.newArrayList();

        private final String text;
        private final Collection<String> parts;

        public HtmlSpecialContentExplorerHandlerCollector(String text, Collection<String> parts) {
            this.text = text;
            this.parts = parts;
        }

        @Override
        public void start(long position) {
            startPositions.add(position);
        }

        @Override
        public void finish(long position) {
            final long startPosition = startPositions.get(startPositions.size() - 1);
            final String content = text.substring((int) startPosition, (int) position);
            parts.add(content);
            startPositions.remove(startPositions.size() - 1);
        }
    }
}
