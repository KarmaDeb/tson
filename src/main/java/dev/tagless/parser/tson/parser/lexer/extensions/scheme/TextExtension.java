package dev.tagless.parser.tson.parser.lexer.extensions.scheme;

import dev.tagless.parser.tson.parser.lexer.extensions.ExtensionStream;
import dev.tagless.parser.tson.parser.lexer.extensions.JsonLexerExtension;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * raw, text and JSON scheme extension
 */
public final class TextExtension extends JsonLexerExtension {

    public static final JsonLexerExtension INSTANCE = new TextExtension();

    /**
     * Create a new JSON lexer extension
     */
    TextExtension() {
        super(new String[]{
                "raw", "text", "json"
        });
    }

    /**
     * Process a URI
     *
     * @param uri the uri
     * @return the processed URI
     * @throws Throwable if there's any problem while processing
     *                   the URI
     */
    @Override
    public @NotNull ExtensionStream process(final @NotNull URI uri) throws Throwable {
        return new ExtensionStream(
                new ByteArrayInputStream(uri.getSchemeSpecificPart().getBytes(StandardCharsets.UTF_8))
        );
    }
}
