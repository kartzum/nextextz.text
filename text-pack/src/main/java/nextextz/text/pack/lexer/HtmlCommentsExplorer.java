package nextextz.text.pack.lexer;

import com.google.common.collect.Lists;
import nextextz.text.pack.text.SymbolProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Finds comments and skips.
 */
public class HtmlCommentsExplorer {
    private static final Character SLASH = '/';
    private static final Character SPACE = ' ';
    private static final Character ASTERISK = '*';
    private static final Character NEW_LINE = '\n';

    private final SymbolProvider symbolProvider;
    private final HtmlCommentsExplorerHandler handler;
    private final Collection<CommentsDetector> detectors = Lists.newArrayList();

    private final List<CommentsDetector> history = Lists.newArrayList();

    /**
     * Creates new explorer.
     *
     * @param symbolProvider provider.
     * @param handler        handler.
     */
    public HtmlCommentsExplorer(SymbolProvider symbolProvider, HtmlCommentsExplorerHandler handler) {
        checkNotNull(symbolProvider);
        checkNotNull(handler);

        this.symbolProvider = symbolProvider;
        this.handler = handler;

        detectors.add(new LineCommentsDetector());
        detectors.add(new ComplexCommentsDetector());
        detectors.add(new XmlCommentsDetector());
    }

    /**
     * Executes.
     *
     * @return true if can move to next step.
     */
    public boolean execute() {
        boolean result = true;

        final Character symbol = getSymbol();

        if (symbol == null) {
            if (history.size() > 0) {
                executeFinish();
            }
            result = false;
        } else {
            final CommentsDetector activeDetector = findActiveCommentsDetector(symbol);
            if (activeDetector != null) {
                move();
                executeStart(activeDetector);
            } else {
                if (history.size() > 0) {
                    final CommentsDetector detector = history.get(history.size() - 1);
                    if (detector.isFinish(symbol)) {
                        move();
                        executeFinish();
                    } else {
                        move();
                    }
                } else {
                    move();
                }
            }
        }

        return result;
    }

    private Character getSymbol() {
        return symbolProvider.getSymbol();
    }

    private void move() {
        symbolProvider.move();
    }

    private long getPosition() {
        return symbolProvider.getPosition();
    }

    private void executeFinish() {
        history.remove(history.size() - 1);
        handler.finish(getPosition());
    }

    private void executeStart(CommentsDetector activeDetector) {
        history.add(activeDetector);
        handler.start(getPosition());
    }

    private CommentsDetector findActiveCommentsDetector(Character symbol) {
        CommentsDetector result = null;
        for (CommentsDetector detector : detectors) {
            final boolean isStart = detector.isStart(symbol);
            if (isStart && result == null) {
                result = detector;
            }
        }
        return result;
    }

    private static abstract class CommentsDetector {
        public abstract boolean isStart(Character symbol);

        public abstract boolean isFinish(Character symbol);
    }

    private static class LineCommentsDetector extends CommentsDetector {
        private static final List<Character> START_ARRAY = Arrays.asList('/', '/');

        private final FixSizeBuffer<Character> buffer = new FixSizeBuffer<>(2);

        @Override
        public boolean isStart(Character symbol) {
            boolean result = false;
            if (SPACE != symbol) {
                buffer.add(symbol);
            }
            if (buffer.match(START_ARRAY)) {
                result = true;
                buffer.reset();
            }
            return result;
        }

        @Override
        public boolean isFinish(Character symbol) {
            return NEW_LINE == symbol;
        }
    }

    private static class ComplexCommentsDetector extends CommentsDetector {
        private static final List<Character> START_ARRAY = Arrays.asList('/', '*');

        private final FixSizeBuffer<Character> buffer = new FixSizeBuffer<>(2);

        private Character previousSymbol;

        @Override
        public boolean isStart(Character symbol) {
            boolean result = false;
            if (SPACE != symbol) {
                buffer.add(symbol);
            }
            if (buffer.match(START_ARRAY)) {
                result = true;
                buffer.reset();
            }
            return result;
        }

        @Override
        public boolean isFinish(Character symbol) {
            boolean result = false;
            if (ASTERISK == previousSymbol && SLASH == symbol) {
                result = true;
                previousSymbol = null;
            } else {
                previousSymbol = symbol;
            }
            return result;
        }
    }

    private static class XmlCommentsDetector extends CommentsDetector {
        private static final List<Character> START_ARRAY = Arrays.asList('<', '!', '-', '-');
        private static final List<Character> FINISH_ARRAY = Arrays.asList('-', '-', '>');

        private final FixSizeBuffer<Character> startBuffer = new FixSizeBuffer<>(4);
        private final FixSizeBuffer<Character> finishBuffer = new FixSizeBuffer<>(3);

        @Override
        public boolean isStart(Character symbol) {
            boolean result = false;
            if (SPACE != symbol) {
                startBuffer.add(symbol);
            }
            if (startBuffer.match(START_ARRAY)) {
                result = true;
                startBuffer.reset();
            }
            return result;
        }

        @Override
        public boolean isFinish(Character symbol) {
            boolean result = false;
            if (finishBuffer.match(FINISH_ARRAY)) {
                result = true;
                finishBuffer.reset();
            } else {
                if (SPACE != symbol) {
                    finishBuffer.add(symbol);
                }
            }
            return result;
        }
    }
}
