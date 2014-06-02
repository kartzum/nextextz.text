package nextextz.text.pack.lexer;

import com.google.common.collect.Lists;
import nextextz.text.pack.text.Factory;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static junit.framework.Assert.assertTrue;

public class HtmlLexerTest {
    private HtmlLexerHandlerCollector handler;
    private Collection<HtmlLexer.Token> tokens;

    @Before
    public void setUp() {
        tokens = Lists.newLinkedList();
        handler = new HtmlLexerHandlerCollector(tokens);
    }

    @Test(expected = NullPointerException.class)
    public void test_null() {
        createLexer(null, null);
    }

    @Test
    public void test_empty() {
        executeLexer(createLexer("", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_start_tag() {
        executeLexer(createLexer("  <  tag   ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("<  tag   "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_start_tag_with_finish() {
        executeLexer(createLexer("  <  tag   >  ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("<  tag   >"),
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_start_empty_tag() {
        executeLexer(createLexer("  <  tag   /  >  ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("<  tag   /  >"),
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_finish_tag() {
        executeLexer(createLexer("  <  / tag   ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("<  / tag   "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_finish_tag_and_spaces() {
        executeLexer(createLexer("  <  / tag   >    ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("<  / tag   >"),
                HtmlLexer.Token.createContent("    "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_start_and_finish_tags() {
        executeLexer(createLexer("   < tag  >   <  /  tag>  ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("   "),
                HtmlLexer.Token.createTag("< tag  >"),
                HtmlLexer.Token.createContent("   "),
                HtmlLexer.Token.createTag("<  /  tag>"),
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_start_and_finish_and_empty_tags() {
        executeLexer(createLexer("   < tag  >   <  /  tag>  </>  < another/>     ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("   "),
                HtmlLexer.Token.createTag("< tag  >"),
                HtmlLexer.Token.createContent("   "),
                HtmlLexer.Token.createTag("<  /  tag>"),
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("</>"),
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("< another/>"),
                HtmlLexer.Token.createContent("     "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_not_closed_tags() {
        executeLexer(createLexer("  <tag> <another  <  </  ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("<tag>"),
                HtmlLexer.Token.createContent(" "),
                HtmlLexer.Token.createTag("<another  <"),
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag("</  "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_only_opened_tags() {
        executeLexer(createLexer("  >> tag> >another> ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createContent(" tag"),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createContent(" "),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createContent("another"),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createContent(" "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_opened_and_not_closed_tags() {
        executeLexer(createLexer("  >> tag1> <another> < tag2 <tag3/> </tag4>", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createContent(" tag1"),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createContent(" "),
                HtmlLexer.Token.createTag("<another>"),
                HtmlLexer.Token.createContent(" "),
                HtmlLexer.Token.createTag("< tag2 <"),
                HtmlLexer.Token.createContent("tag3/"),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createContent(" "),
                HtmlLexer.Token.createTag("</tag4>"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_content() {
        executeLexer(createLexer(" < tag> value   <  /  tag>  ", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createContent(" "),
                HtmlLexer.Token.createTag("< tag>"),
                HtmlLexer.Token.createContent(" value   "),
                HtmlLexer.Token.createTag("<  /  tag>"),
                HtmlLexer.Token.createContent("  "),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_header_xml() {
        executeLexer(createLexer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_header_html() {
        executeLexer(createLexer("<!DOCTYPE html>", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createTag("<!DOCTYPE html>"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_header_html_and_meta() {
        executeLexer(createLexer("<!DOCTYPE html>" +
                "<html lang=\"en-US\">" +
                "<head>" +
                "<title>XML Tutorial</title>" +
                "<meta charset=\"utf-8\">" +
                "</head>", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createTag("<!DOCTYPE html>"),
                HtmlLexer.Token.createTag("<html lang=\"en-US\">"),
                HtmlLexer.Token.createTag("<head>"),
                HtmlLexer.Token.createTag("<title>"),
                HtmlLexer.Token.createContent("XML Tutorial"),
                HtmlLexer.Token.createTag("</title>"),
                HtmlLexer.Token.createTag("<meta charset=\"utf-8\">"),
                HtmlLexer.Token.createTag("</head>"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_comments() {
        executeLexer(createLexer("<!-- text <p> sample </p> -->", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createTag("<!--"),
                HtmlLexer.Token.createComments(" text <p> sample </p> "),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_comments_with_spaces() {
        executeLexer(createLexer("<!-- text --   >", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createTag("<!--"),
                HtmlLexer.Token.createComments(" text "),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_several_comments() {
        executeLexer(createLexer("<!-- text --><p>title</p><!-- text2 -->", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createTag("<!--"),
                HtmlLexer.Token.createComments(" text "),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.createTag("<p>"),
                HtmlLexer.Token.createContent("title"),
                HtmlLexer.Token.createTag("</p>"),
                HtmlLexer.Token.createTag("<!--"),
                HtmlLexer.Token.createComments(" text2 "),
                HtmlLexer.Token.createTag(">"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_script() {
        executeLexer(createLexer("<script> alert('!'); </script>", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createTag("<script>"),
                HtmlLexer.Token.createScript(" alert('!'); "),
                HtmlLexer.Token.createTag("</script>"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    @Test
    public void test_script_comments() {
        executeLexer(createLexer("<script> alert('!'); <!-- 2 --> /* 3 */ </script>", handler), handler);
        final Collection<HtmlLexer.Token> test = Lists.newArrayList(
                HtmlLexer.Token.createTag("<script>"),
                HtmlLexer.Token.createScript(" alert('!'); <!-- 2 --> /* 3 */ "),
                HtmlLexer.Token.createTag("</script>"),
                HtmlLexer.Token.getEmpty()
        );
        assertTokens(tokens, test);
    }

    private static void executeLexer(HtmlLexer lexer, HtmlLexerHandlerCollector handler) {
        for (; ; ) {
            lexer.execute();
            if (!handler.next()) {
                break;
            }
        }
    }

    private static HtmlLexer createLexer(String text, HtmlLexerHandler handler) {
        return new HtmlLexer(Factory.createText(text), handler);
    }

    private static void assertTokens(Collection<HtmlLexer.Token> tokens, Collection<HtmlLexer.Token> test) {
        Iterator<HtmlLexer.Token> testTokensIterator = test.iterator();
        for (HtmlLexer.Token t : tokens) {
            final HtmlLexer.Token next = testTokensIterator.next();
            assertToken(t, next.getType(), next.getValue());
        }
    }

    private static void assertToken(HtmlLexer.Token token, HtmlLexer.TokenType type, String value) {
        assertTrue(token.getType() == type);
        assertTrue(value.equals(token.getValue()));
    }

    private static class HtmlLexerHandlerCollector implements HtmlLexerHandler {
        private final Collection<HtmlLexer.Token> tokens;

        private HtmlLexer.Token tail;

        public HtmlLexerHandlerCollector(Collection<HtmlLexer.Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public void handle(HtmlLexer.Token token) {
            tokens.add(token);
            tail = token;
        }

        public boolean next() {
            boolean result = true;
            if (tail != null) {
                if (tail.getType() == HtmlLexer.TokenType.EMPTY) {
                    result = false;
                }
            }
            return result;
        }
    }
}
