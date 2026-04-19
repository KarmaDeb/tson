# TSON

Welcome to TSON, a library made to parse, write, serialize and
deserialize JSON for java, made in java. The library uses streams
and virtual threads to parse any incoming json in any length, without
affecting too much in memory.

## Examples

### Parsing a JSON

```java
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.hydrator.Hydrator;
import dev.tagless.parser.tson.parser;
import dev.tagless.parser.tson.parser.lexer.JsonLexer;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public CompletableFuture<JsonObject> fetchVersion(final URI remote) {
    try (HttpClient client = HttpClient.newHttpClient();
         HttpRequest request = HttpRequest.newBuilder()
                 .uri(remote)
                 .header("Accepts", "application/json")
                 .method("GET", HttpRequest.BodyPublishers.noBody())
                 .build()) {
        try (HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
             InputStream stream = response.body()) {
            JsonLexer lexer = new JsonLexer(stream);

            return lexer.parse(false).thenCompose(token -> {
                try {
                    Hydrator hydrator = Hydrator.create(token);
                    return CompletableFuture.completedFuture(hydrator.process());
                } catch (Exception ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            });
        }
    }
}
```

### Writing a JSON

```java
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.serializer.JsonWriter;

public void saveData(final Path outputFile) {
    JsonObject object = new JsonObject();
    object.set("id", 1);
    object.set("list.items[0]", "A");
    object.set("list.items[1]", "B");
    object.set("list.items[3]", "C");

    try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
        JsonWriter jw = JsonWriter.writer(object);
        jw.write(writer);
    }
    
    // Writes {"id":1,"list":{"items": ["A","B","C"]}} to "outputFile"
}
```

### Serializing and deserializing a class

```java

import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.serializer.JsonWriter;
import dev.tagless.parser.tson.serializer.reflect.JsonInclude;
import dev.tagless.parser.tson.serializer.reflect.JsonSerializable;
import dev.tagless.parser.tson.serializer.reflect.constructor.JsonConstructor;
import dev.tagless.parser.tson.serializer.reflect.constructor.JsonField;
import dev.tagless.parser.tson.serializer.reflect.scan.ObjectScan;

import java.io.IOException;
import java.util.Objects;

@JsonSerializable(2L)
public final class MyClass {

    private long id;

    @JsonInclude("name")
    private String name;

    @JsonConstructor(2L)
    public MyClass(final String name) {
        this.name = name;
    }

    @JsonConstructor(1L)
    @Deprecated
    public MyClass(final long @JsonField("id") id, final String @JsonField("name") name) {
        this.id = id;
        this.name = name;
    }

    @JsonField("id")
    public void setId(final long id) {
        this.id = id;
    }

    @JsonInclude("id")
    public long getId() {
        if (this.id == 0) {
            this.id = Math.random() * Long.MAX_VALUE;
        }

        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public JsonElement asElement() {
        return ObjectScan.serialize(this);
    }

    public static MyClass resolve(final JsonElement element) {
        Object instance = ObjectScan.deserialize(element).orElse(null);
        if (!(instance instanceof MyClass c)) {
            return null;
        }
        
        return c;
    }
    
    @Override
    public String toString() {
        try {
            return JsonWriter.writer(
                    ObjectScan.represent(this)
            ).write();
            // returns: {"id":x,"name":"b"}
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
```