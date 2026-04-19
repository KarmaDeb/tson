package dev.tagless.parser.tson.element.navigator;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.navigator.path.PathProcessor;
import dev.tagless.parser.tson.element.navigator.path.PathToken;
import dev.tagless.parser.tson.element.navigator.path.exception.BadTokenPath;
import dev.tagless.parser.tson.element.navigator.path.exception.PathException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collector;

/**
 * The JSON path represents a part of a
 * path which can be navigated in JSON
 */
@SuppressWarnings("unused")
public final class JsonPath {

    private final PathPart part;

    private JsonPath previous;
    private JsonPath next;

    private JsonPath first;
    private JsonPath last;

    /**
     * Create a new JSON path
     * @param part the path part
     * @param previous the previous path
     * @param next the next path
     */
    private JsonPath(final @NotNull PathPart part,
                     final @Nullable JsonPath previous,
                     final @Nullable JsonPath next) {
        this.part = part;
        this.previous = previous;
        this.next = next;
    }

    /**
     * Split the path stating from the
     * head of the path until the current
     * path part
     * @return the split path
     */
    @NotNull
    public JsonPath split() {
        JsonPath h = this.first;
        if (h.equals(this)) {
            return h;
        }

        LinkedList<JsonPath> newPaths = new LinkedList<>();

        while (h.next != null) {
            newPaths.add(new JsonPath(h.part, null, null));
            if (h.equals(this)) {
                break;
            }

            h = h.next;
        }

        link(newPaths);
        return newPaths.getLast();
    }

    /**
     * Split the path stating from the
     * tail of the path until the current
     * path part
     * @return the split path
     */
    @NotNull
    public JsonPath rsplit() {
        JsonPath h = this;
        if (h.equals(this.last)) {
            return h;
        }

        LinkedList<JsonPath> newPaths = new LinkedList<>();

        while (h.next != null) {
            newPaths.add(new JsonPath(h.part, null, null));
            h = h.next;
        }
        newPaths.add(new JsonPath(h.part, null, null));
        // Adds the tail, because tail.next == null, causing it to early leaving the
        // mapper loop

        link(newPaths);
        return newPaths.getLast();
    }

    /**
     * Get the path head
     * @return the head of the path
     */
    @NotNull
    public JsonPath getHead() {
        return this.first;
    }

    /**
     * Get the path tail
     * @return the tail of the path
     */
    @NotNull
    public JsonPath getTail() {
        return this.last;
    }

    /**
     * Get the next path
     * @return the next path
     */
    @NotNull
    public Optional<JsonPath> getNext() {
        return Optional.ofNullable(this.next);
    }

    /**
     * Get the previous path
     * @return the previous path
     */
    @NotNull
    public Optional<JsonPath> getPrevious() {
        return Optional.ofNullable(this.previous);
    }

    /**
     * Resolves the path against a root element
     * @param root the starting element
     * @return an optional containing the found element
     */
    public Optional<JsonElement> resolve(final @NotNull JsonElement root) {
        JsonElement current = root;
        JsonPath pathNode = this;

        while (pathNode != null) {
            if (current instanceof JsonObject obj) {
                Optional<JsonElement> nextElement = obj.getDirect(pathNode.part.get());
                if (nextElement.isEmpty()) return Optional.empty();
                current = nextElement.get();
            } else {
                return Optional.empty();
            }

            for (int index : pathNode.part.getIndexes()) {
                if (current instanceof JsonArray arr) {
                    Optional<JsonElement> nextIndex = arr.getDirect(index);
                    if (nextIndex.isEmpty()) return Optional.empty();
                    current = nextIndex.get();
                } else {
                    return Optional.empty();
                }
            }
            pathNode = pathNode.next;
        }
        return Optional.of(current);
    }

    /**
     * Sets a value in the path
     * @param createParents if missing nodes should be created
     * @param element the element to set
     * @return true if successful
     */
    public boolean set(final @NotNull JsonObject root, boolean createParents, final JsonElement element) {
        JsonElement current = root;
        JsonPath pathNode = this;

        while (pathNode != null) {
            String key = pathNode.part.get();
            boolean isLastPath = (pathNode.next == null);
            int[] indexes = pathNode.part.getIndexes();

            if (!(current instanceof JsonObject obj)) return false;

            if (isLastPath && indexes.length == 0) {
                obj.setDirect(key, element);
                return true;
            }

            Optional<JsonElement> nextOpt = obj.getDirect(key);
            JsonElement nextInChain;

            if (nextOpt.isEmpty()) {
                if (!createParents) return false;
                nextInChain = (indexes.length > 0) ? new JsonArray() : new JsonObject();
                obj.setDirect(key, nextInChain);
            } else {
                nextInChain = nextOpt.get();
            }

            for (int i = 0; i < indexes.length; i++) {
                if (!(nextInChain instanceof JsonArray arr)) return false;
                int idx = indexes[i];
                boolean isLastIndex = (i == indexes.length - 1);

                if (isLastPath && isLastIndex) {
                    arr.setDirect(idx, element);
                    return true;
                }

                Optional<JsonElement> childOpt = arr.getDirect(idx);
                if (childOpt.isEmpty() || (childOpt.get().isPrimitive() && !isLastIndex)) {
                    if (!createParents) return false;

                    JsonElement placeholder = (isLastIndex) ? new JsonObject() : new JsonArray();
                    arr.setDirect(idx, placeholder);
                    nextInChain = placeholder;
                } else {
                    nextInChain = childOpt.get();
                }
            }

            current = nextInChain;
            pathNode = pathNode.next;
        }
        return false;
    }

    /**
     * Deletes the element at this path
     * @param root the root element
     * @return true if deleted
     */
    public boolean delete(final @NotNull JsonObject root) {
        Optional<JsonElement> targetOpt = this.resolve(root);

        if (targetOpt.isEmpty()) {
            return false;
        }

        JsonElement target = targetOpt.get();
        Optional<JsonElement> parentOpt = target.getParent();

        if (parentOpt.isEmpty()) {
            return false;
        }

        JsonElement parent = parentOpt.get();
        String key = target.getKey();

        if (parent instanceof JsonObject obj) {
            return obj.removeDirect(key);
        }

        if (parent instanceof JsonArray arr) {
            try {
                int index = Integer.parseInt(key.replaceAll("[\\[\\]]", ""));
                return arr.removeDirect(index);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * Create a new JSON path
     * @param path the path to use
     * @return the processed path
     * @throws PathException if there's a problem while processing
     * the path
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static JsonPath path(final @NotNull String path) throws PathException {
        LinkedList<PathToken<?>> tokens = PathProcessor.tokenize(path);
        LinkedList<PathPart> created = new LinkedList<>();

        PathToken<?> token;
        PathToken.Type expected = PathToken.Type.WORD;

        String currentPath = null;
        LinkedList<Integer> indexes = new LinkedList<>();

        while ((token = tokens.poll()) != null) {
            if (token.getType().equals(PathToken.Type.INDEX_START)) {
                expected = PathToken.Type.NUMBER;
                continue;
            }

            if (expected != null && !expected.equals(token.getType())) {
                throw new BadTokenPath(token.getType(), expected);
            } else if (expected == null) {
                if (!token.getType().equals(PathToken.Type.SPACE) && !token.getType().equals(PathToken.Type.INDEX_END)) {
                    throw new BadTokenPath(token.getType(), PathToken.Type.SPACE, PathToken.Type.INDEX_START);
                }
            }

            switch (token.getType()) {
                case WORD -> {
                    PathToken<String> str = (PathToken<String>) token;
                    currentPath = str.get().orElseThrow();
                    expected = null;
                }
                case SPACE -> {
                    PathPart part = new PathPart(currentPath, indexes.stream().mapToInt(i -> i).toArray());
                    created.add(part);

                    indexes.clear();
                    currentPath = null;
                    expected = PathToken.Type.WORD;
                }
                case NUMBER -> {
                    PathToken<Integer> num = (PathToken<Integer>) token;
                    indexes.add(num.get().orElseThrow());

                    expected = PathToken.Type.INDEX_END;
                }
                case INDEX_END -> expected = null;
            }
        }
        if (currentPath != null) {
            PathPart part = new PathPart(currentPath, indexes.stream().mapToInt(i -> i).toArray());
            created.add(part);
        }

        LinkedList<JsonPath> paths = created.stream().map(p -> new JsonPath(p, null, null)).collect(
                Collector.of(LinkedList::new, LinkedList::add, (o1, o2) -> {
                    o1.addAll(o2);
                    return o1;
                })
        );

        link(paths);
        return paths.getLast();
    }

    private static void link(final LinkedList<JsonPath> paths) {
        JsonPath f = paths.getFirst();
        JsonPath l = paths.getLast();

        JsonPath pre = null;
        for (JsonPath p : paths) {
            if (pre != null) {
                pre.next = p;
            }

            p.previous = pre;
            p.first = f;
            p.last = l;

            pre = p;
        }
    }

    /**
     * {@return a hash code value for this object} This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@link
     *     #equals(Object) equals} method, then calling the {@code
     *     hashCode} method on each of the two objects must produce the
     *     same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link #equals(Object) equals} method, then
     *     calling the {@code hashCode} method on each of the two objects
     *     must produce distinct integer results.  However, the programmer
     *     should be aware that producing distinct integer results for
     *     unequal objects may improve the performance of hash tables.
     * </ul>
     *
     * @implSpec As far as is reasonably practical, the {@code hashCode} method defined
     * by class {@code Object} returns distinct integers for distinct objects.
     * @apiNote The {@link Objects#hash(Object...) hash} and {@link
     * Objects#hashCode(Object) hashCode} methods of {@link
     * Objects} can be used to help construct simple hash codes.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public int hashCode() {
        return this.part.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an <dfn>{@index "equivalence relation"}</dfn>
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     *
     * <p>
     * An equivalence relation partitions the elements it operates on
     * into <i>equivalence classes</i>; all the members of an
     * equivalence class are equal to each other. Members of an
     * equivalence class are substitutable for each other, at least
     * for some purposes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @implSpec The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * In other words, under the reference equality equivalence
     * relation, each equivalence class only has a single element.
     * @apiNote It is generally necessary to override the {@link #hashCode() hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     * <p>The two-argument {@link Objects#equals(Object,
     * Object) Objects.equals} method implements an equivalence relation
     * on two possibly-null object references.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof JsonPath path)) return false;

        return Objects.equals(this.part, path.part);
    }

    /**
     * {@return a string representation of the object}
     * <p>
     * Satisfying this method's contract implies a non-{@code null}
     * result must be returned.
     *
     * @apiNote In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * The string output is not necessarily stable over time or across
     * JVM invocations.
     * @implSpec The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * {@snippet lang = java:
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     *}
     * The {@link Objects#toIdentityString(Object)
     * Objects.toIdentityString} method returns the string for an
     * object equal to the string that would be returned if neither
     * the {@code toString} nor {@code hashCode} methods were
     * overridden by the object's class.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.first == null) {
            return this.part.toString();
        }

        JsonPath f = this.first;
        while (f != null) {
            builder.append(f.part);
            if (f == this) {
                break;
            }

            if (f.next != null) {
                builder.append('.');
            }
            f = f.next;
        }

        return builder.toString();
    }
}

class PathPart {

    private final String part;
    private final int[] indexes;

    public PathPart(final String part, final int[] indexes) {
        this.part = part;
        this.indexes = indexes;
    }

    /**
     * Get the path part
     * @return the part of the path
     */
    public String get() {
        return this.part;
    }

    /**
     * Get the part navigable
     * indexes. This allows for a JSON object
     * to navigate through its {@link #get()}
     * value indexes if its value is an array
     * @return the part indexes
     */
    public int[] getIndexes() {
        return this.indexes;
    }

    /**
     * {@return a string representation of the object}
     * <p>
     * Satisfying this method's contract implies a non-{@code null}
     * result must be returned.
     *
     * @apiNote In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * The string output is not necessarily stable over time or across
     * JVM invocations.
     * @implSpec The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * {@snippet lang = java:
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     *}
     * The {@link Objects#toIdentityString(Object)
     * Objects.toIdentityString} method returns the string for an
     * object equal to the string that would be returned if neither
     * the {@code toString} nor {@code hashCode} methods were
     * overridden by the object's class.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.part);
        for (int index : this.indexes) {
            builder.append('[').append(index).append(']');
        }

        return builder.toString();
    }

    /**
     * {@return a hash code value for this object} This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@link
     *     #equals(Object) equals} method, then calling the {@code
     *     hashCode} method on each of the two objects must produce the
     *     same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link #equals(Object) equals} method, then
     *     calling the {@code hashCode} method on each of the two objects
     *     must produce distinct integer results.  However, the programmer
     *     should be aware that producing distinct integer results for
     *     unequal objects may improve the performance of hash tables.
     * </ul>
     *
     * @implSpec As far as is reasonably practical, the {@code hashCode} method defined
     * by class {@code Object} returns distinct integers for distinct objects.
     * @apiNote The {@link Objects#hash(Object...) hash} and {@link
     * Objects#hashCode(Object) hashCode} methods of {@link
     * Objects} can be used to help construct simple hash codes.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.part, Arrays.hashCode(this.indexes));
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an <dfn>{@index "equivalence relation"}</dfn>
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     *
     * <p>
     * An equivalence relation partitions the elements it operates on
     * into <i>equivalence classes</i>; all the members of an
     * equivalence class are equal to each other. Members of an
     * equivalence class are substitutable for each other, at least
     * for some purposes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @implSpec The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * In other words, under the reference equality equivalence
     * relation, each equivalence class only has a single element.
     * @apiNote It is generally necessary to override the {@link #hashCode() hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     * <p>The two-argument {@link Objects#equals(Object,
     * Object) Objects.equals} method implements an equivalence relation
     * on two possibly-null object references.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PathPart p)) return false;

        return Objects.equals(this.part, p.part) &&
                Objects.deepEquals(this.indexes, p.indexes);
    }
}