package dev.tagless.parser.tson.element.navigator.path;

import dev.tagless.parser.tson.element.navigator.JsonPath;
import dev.tagless.parser.tson.element.navigator.path.exception.EmptyPathException;
import dev.tagless.parser.tson.element.navigator.path.exception.PathException;
import dev.tagless.parser.tson.element.navigator.path.processor.IndexProcessor;
import dev.tagless.parser.tson.element.navigator.path.processor.WordProcessor;
import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;
import java.util.LinkedList;

/**
 * The path processor parses a string path
 * into tokens which can be used to build
 * a {@link JsonPath}
 */
public abstract class PathProcessor {

    protected final CharBuffer buffer;
    protected final LinkedList<PathToken<?>> tokens;

    /**
     * Create a new path processor
     * @param buffer the path buffer
     * @param tokens the path tokens
     */
    protected PathProcessor(final @NotNull CharBuffer buffer, final LinkedList<PathToken<?>> tokens) {
        this.buffer = buffer;
        this.tokens = tokens;
    }

    /**
     * Process the path
     * @throws PathException if there's a problem while processing
     * the path
     */
    public abstract void process() throws PathException;

    /**
     * Process the path
     * @param path the path to process
     * @return the processed path
     * @throws PathException if there's a problem while processing
     * the path
     */
    @NotNull
    public static LinkedList<PathToken<?>> tokenize(final String path) throws PathException {
        String route = path;
        if (route.startsWith("$")) {
            route = route.substring(1);
        }
        while (route.startsWith(".")) {
            route = route.substring(1);
        }

        if (route.isEmpty()) {
            throw new EmptyPathException();
        }

        char[] chars = route.toCharArray();
        CharBuffer b = CharBuffer.wrap(chars);

        LinkedList<PathToken<?>> tokens = new LinkedList<>();
        PathProcessor processor = chars[0] == '[' ? new IndexProcessor(
                b, tokens
        ) : new WordProcessor(
                b, tokens
        );
        processor.process();

        return tokens;
    }
}
