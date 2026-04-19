package dev.tagless.parser.tson.parser.lexer.extensions;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Arrays;

/**
 * The JSON lexer extension allows to add support
 * for different kinds of URI schemes
 */
public abstract class JsonLexerExtension {

    protected final String[] schemes;

    /**
     * Create a new JSON lexer extension
     * @param schemes the supported schemes
     */
    protected JsonLexerExtension(final String @NotNull [] schemes) {
        this.schemes = Arrays.copyOf(schemes, schemes.length);
    }

    /**
     * Get the supported schemes
     * @return the supported schemes
     */
    public final String @NotNull[] getSchemes() {
        return Arrays.copyOf(this.schemes, this.schemes.length);
    }

    /**
     * Process a URI
     * @param uri the uri
     * @return the processed URI
     * @throws Throwable if there's any problem while processing
     * the URI
     */
    @NotNull
    public abstract ExtensionStream process(final @NotNull URI uri) throws Throwable;
}
