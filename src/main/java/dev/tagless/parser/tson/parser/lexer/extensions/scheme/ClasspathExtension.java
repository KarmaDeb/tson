package dev.tagless.parser.tson.parser.lexer.extensions.scheme;

import dev.tagless.parser.tson.parser.lexer.JsonLexer;
import dev.tagless.parser.tson.parser.lexer.extensions.ExtensionStream;
import dev.tagless.parser.tson.parser.lexer.extensions.JsonLexerExtension;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * classpath scheme extension
 */
public final class ClasspathExtension extends JsonLexerExtension {

    public static final JsonLexerExtension INSTANCE = new ClasspathExtension();

    /**
     * Create a new JSON lexer extension
     */
    ClasspathExtension() {
        super(new String[]{"classpath"});
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
        String path = uri.getPath();
        if (!path.startsWith("/")) {
            path = String.format("/%s", path);
        }

        InputStream stream = JsonLexer.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException(String.format("Could not find %s in internal classpath from uri: %s",
                    path, uri));
        }

        return new ExtensionStream(stream);
    }
}
