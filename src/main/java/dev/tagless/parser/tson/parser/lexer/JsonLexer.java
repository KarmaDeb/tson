package dev.tagless.parser.tson.parser.lexer;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.lexer.extensions.JsonLexerExtension;
import dev.tagless.parser.tson.parser.lexer.extensions.scheme.*;
import dev.tagless.parser.tson.parser.token.Token;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The lexer allows to read an entire JSON document
 * and transform it into tokens.
 */
@SuppressWarnings("unused")
public final class JsonLexer {

    private static final Map<String, JsonLexerExtension> SCHEME_EXTENSIONS = new ConcurrentHashMap<>();

    private URI uri;
    private InputStream stream;

    static {
        registerExtension(ClasspathExtension.INSTANCE);
        registerExtension(DataExtension.INSTANCE);
        registerExtension(FileExtension.INSTANCE);
        registerExtension(JarExtension.INSTANCE);
        registerExtension(TextExtension.INSTANCE);
        registerExtension(WebExtension.INSTANCE);
    }

    /**
     * Register an extension for a scheme URI. For instance, the
     * official FTPSon extension which allows for ftp:// ftps:// and sftp://
     * uri schemes
     * @param extension the extension
     */
    public static void registerExtension(final @NotNull JsonLexerExtension extension) {
        for (String scheme : extension.getSchemes()) {
            SCHEME_EXTENSIONS.put(scheme.toLowerCase(), extension);
        }
    }

    /**
     * Creates a new JSON lexer
     * @param stream the stream to lex from
     */
    public JsonLexer(final InputStream stream) {
        this.stream = stream;
    }

    /**
     * Creates a new JSON lexer
     * @param raw the raw data to lex from
     */
    public JsonLexer(final byte[] raw) {
        this(URI.create("data:text/plain;base64," + Base64.getEncoder().encodeToString(raw)));
    }

    /**
     * Creates a new json lexer
     * @param raw the raw data to lex from
     */
    public JsonLexer(final String raw) {
        this(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a new json lexer
     * @param remoteStream the remote stream to lex from
     */
    public JsonLexer(final URI remoteStream) {
        this.uri = remoteStream;
    }

    private InputStream connectStream() throws Throwable {
        if (this.stream != null) {
            return this.stream;
        }

        String scheme = this.uri.getScheme();
        if (scheme == null) {
            throw new IOException("Bad URI scheme");
        }

        JsonLexerExtension extension = SCHEME_EXTENSIONS.get(scheme.toLowerCase());
        if (extension == null) {
            throw new IOException("Unsupported URI scheme: " + scheme);
        }

        return extension.process(this.uri);
    }

    /**
     * Parse a JSON document
     * @param lenient enable lenient mode
     * @return the parse task
     */
    public CompletableFuture<Token> parse(final boolean lenient) {
        CompletableFuture<Token> future = new CompletableFuture<>();

        Thread.ofVirtual().start(() -> {
            try (InputStream stream = this.connectStream();
                 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
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
            } catch (Throwable ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }
}
