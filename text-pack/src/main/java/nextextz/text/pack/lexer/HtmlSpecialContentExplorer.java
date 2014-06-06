package nextextz.text.pack.lexer;

import com.google.common.collect.Lists;
import nextextz.text.pack.text.SymbolProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Finds special content and skips.
 */
public class HtmlSpecialContentExplorer {
    private static final Character SLASH = '/';
    private static final Character BACK_SLASH = '\\';
    private static final Character SPACE = ' ';
    private static final Character ASTERISK = '*';
    private static final Character NEW_LINE = '\n';
    private static final Character QUOTE = '\'';
    private static final Character DOUBLE_QUOTE = '"';
    private static final Character START = '<';
    private static final Character FINISH = '>';
    private static final Character EXCLAMATION_MARK = '!';
    private static final Character MINUS = '-';

    private final SymbolProvider symbolProvider;
    private final HtmlSpecialContentExplorerHandler handler;
    private final Collection<Detector> detectors = Lists.newArrayList();

    private final List<Detector> history = Lists.newArrayList();

    /**
     * Creates new explorer.
     *
     * @param symbolProvider provider.
     * @param handler        handler.
     */
    public HtmlSpecialContentExplorer(SymbolProvider symbolProvider, HtmlSpecialContentExplorerHandler handler) {
        checkNotNull(symbolProvider);
        checkNotNull(handler);

        this.symbolProvider = symbolProvider;
        this.handler = handler;

        final FlagContainer symbolFlagContainer = new FlagContainer();

        detectors.add(new LineCommentsDetector());
        detectors.add(new ComplexCommentsDetector());
        detectors.add(new XmlCommentsDetector());
        detectors.add(new SymbolDetector(QUOTE, symbolFlagContainer));
        detectors.add(new SymbolDetector(DOUBLE_QUOTE, symbolFlagContainer));
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
            if (hasDetectors()) {
                executeFinish();
            }
            result = false;
        } else {
            final Detector activeDetector = findActiveDetector(symbol);
            if (activeDetector != null) {
                move();
                executeStart(activeDetector);
            } else {
                if (hasDetectors()) {
                    final Detector detector = getTailDetector();
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

    private void executeStart(Detector activeDetector) {
        history.add(activeDetector);
        handler.start(getPosition());
    }

    private boolean hasDetectors() {
        return history.size() > 0;
    }

    private Detector getTailDetector() {
        Detector result = null;
        if (history.size() > 0) {
            result = history.get(history.size() - 1);
        }
        return result;
    }

    private boolean isTailDetectorHierarchical() {
        boolean result = false;
        final Detector detector = getTailDetector();
        if (detector != null && !(isDetectorLinear(detector))) {
            result = true;
        }
        return result;
    }

    private boolean isTailDetectorLinear() {
        boolean result = false;
        final Detector detector = getTailDetector();
        if (detector != null && isDetectorLinear(detector)) {
            result = true;
        }
        return result;
    }

    private boolean isDetectorLinear(Detector detector) {
        return detector instanceof SymbolDetector;
    }

    private Detector findActiveDetector(Character symbol) {
        Detector result = null;
        for (Detector detector : detectors) {
            if ((isTailDetectorHierarchical() && isDetectorLinear(detector)) || isTailDetectorLinear()) {
                continue;
            }
            final boolean isStart = detector.isStart(symbol);
            if (isStart && result == null) {
                result = detector;
            }
        }
        return result;
    }

    private static abstract class Detector {
        public abstract boolean isStart(Character symbol);

        public abstract boolean isFinish(Character symbol);
    }

    private static class LineCommentsDetector extends Detector {
        private static final List<Character> START_ARRAY = Arrays.asList(SLASH, SLASH);

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

    private static class ComplexCommentsDetector extends Detector {
        private static final List<Character> START_ARRAY = Arrays.asList(SLASH, ASTERISK);

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

    private static class XmlCommentsDetector extends Detector {
        private static final List<Character> START_ARRAY = Arrays.asList(START, EXCLAMATION_MARK, MINUS, MINUS);
        private static final List<Character> FINISH_ARRAY = Arrays.asList(MINUS, MINUS, FINISH);

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

    private static class FlagContainer {
        private boolean flag;

        public boolean isFlag() {
            return flag;
        }

        public void resentFlag() {
            flag = false;
        }

        public void turnOn() {
            flag = true;
        }
    }

    private static class SymbolDetector extends Detector {
        private final Character symbol;
        private final FlagContainer container;
        private boolean isNextSpecial;

        public SymbolDetector(Character symbol, FlagContainer container) {
            this.symbol = symbol;
            this.container = container;
        }

        @Override
        public boolean isStart(Character symbol) {
            boolean result = false;
            if (BACK_SLASH == symbol) {
                isNextSpecial = true;
            } else {
                if (isNextSpecial) {
                    isNextSpecial = false;
                } else if (!container.isFlag() && symbol == this.symbol) {
                    container.turnOn();
                    result = true;
                }
            }
            return result;
        }

        @Override
        public boolean isFinish(Character symbol) {
            boolean result = false;
            if (container.isFlag() && symbol == this.symbol) {
                container.resentFlag();
                result = true;
            }
            return result;
        }
    }
}
