package dev.tagless.parser.tson.parser.lexer.extensions.scheme;

import dev.tagless.parser.tson.parser.lexer.extensions.ExtensionStream;
import dev.tagless.parser.tson.parser.lexer.extensions.JsonLexerExtension;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * http and https scheme extension
 */
public final class WebExtension extends JsonLexerExtension {

    public static final JsonLexerExtension INSTANCE = new WebExtension();

    /**
     * Create a new JSON lexer extension
     */
    WebExtension() {
        super(new String[]{"http", "https"});
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
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody());

        String userInfo = uri.getUserInfo();
        if (userInfo != null && userInfo.contains(":")) {
            request.header("Authorization", String.format("Bearer %s",
                    Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8))));
        }

        HttpResponse<InputStream> response = client.send(request.build(),
                HttpResponse.BodyHandlers.ofInputStream());

        return new ExtensionStream(response.body(), client);
    }
}
