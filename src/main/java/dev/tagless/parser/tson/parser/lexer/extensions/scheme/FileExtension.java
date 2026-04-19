package dev.tagless.parser.tson.parser.lexer.extensions.scheme;

import dev.tagless.parser.tson.parser.lexer.extensions.ExtensionStream;
import dev.tagless.parser.tson.parser.lexer.extensions.JsonLexerExtension;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * file scheme extension
 */
public final class FileExtension extends JsonLexerExtension {

    public static final JsonLexerExtension INSTANCE = new FileExtension();

    /**
     * Create a new JSON lexer extension
     */
    FileExtension() {
        super(new String[]{"file"});
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
        Path file = Path.of(uri);
        return new ExtensionStream(Files.newInputStream(file));
    }
}
