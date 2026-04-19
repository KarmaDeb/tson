package dev.tagless.parser.tson.parser;

import dev.tagless.parser.tson.parser.exception.JsonException;
import dev.tagless.parser.tson.parser.token.Token;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * The lexer allows to read an entire JSON document
 * and transform it into tokens.
 */
@SuppressWarnings("unused")
public final class JsonLexer {

    private final InputStream stream;

    public JsonLexer(final InputStream stream) {
        this.stream = stream;
    }

    /**
     * Parse a JSON document
     * @param lenient enable lenient mode
     * @return the parse task
     */
    public CompletableFuture<Token> parse(final boolean lenient) {
        CompletableFuture<Token> future = new CompletableFuture<>();

        Thread.ofVirtual().start(() -> {
            try (InputStreamReader reader = new InputStreamReader(this.stream, StandardCharsets.UTF_8)) {
                JsonProcessor processor = lenient ? JsonProcessor.createLenient() : JsonProcessor.create();

                char[] buffer = new char[8192];
                int charsRead;

                while ((charsRead = reader.read(buffer)) != -1) {
                    CharBuffer b = CharBuffer.wrap(buffer, 0, charsRead);
                    if (processor.process(b)) {
                        break;
                    }
                }

                Token token = processor.getToken();
                token.clean();

                future.complete(token.first());
            } catch (IOException | JsonException ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }
}
