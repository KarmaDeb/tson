package dev.tagless.parser.tson.element.navigator.path.processor;

import dev.tagless.parser.tson.element.navigator.path.PathProcessor;
import dev.tagless.parser.tson.element.navigator.path.PathToken;
import dev.tagless.parser.tson.element.navigator.path.exception.BadTokenPosition;
import dev.tagless.parser.tson.element.navigator.path.exception.PathException;
import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;
import java.util.LinkedList;

import static dev.tagless.parser.tson.element.navigator.path.PathToken.*;

/**
 * Index property processor
 */
public final class IndexProcessor extends PathProcessor {

    private PathToken<?> expected = INDEX_START;

    /**
     * Create a new path processor
     *
     * @param buffer the path buffer
     * @param tokens the path tokens
     */
    public IndexProcessor(final @NotNull CharBuffer buffer, final LinkedList<PathToken<?>> tokens) {
        super(buffer, tokens);
    }

    /**
     * Process the path
     * @throws PathException if there's a problem while processing
     * the path
     */
    @Override
    public void process() throws PathException {
        StringBuilder currentDigitBuilder = new StringBuilder();

        LinkedList<PathToken<?>> proxy = new LinkedList<>();
        while (buffer.hasRemaining()) {
            char character = buffer.get();
            if (!expected.test(character)) {
                if (expected.equals(NUMBER) && character == ']' && !currentDigitBuilder.isEmpty()) {
                    int digit = Integer.parseInt(currentDigitBuilder.toString());
                    proxy.add(NUMBER.bind(digit));
                    proxy.add(INDEX_END);

                    break;
                }

                proxy.clear();
                throw new BadTokenPosition(buffer.position(), character, expected.getType());
            }

            if (expected.equals(INDEX_START)) {
                proxy.add(INDEX_START);
                expected = NUMBER;
                continue;
            }

            if (!NUMBER.test(character)) {
                proxy.clear();
                throw new BadTokenPosition(buffer.position(), character, expected.getType());
            }

            currentDigitBuilder.append(character);
        }

        this.tokens.addAll(proxy);
    }
}
