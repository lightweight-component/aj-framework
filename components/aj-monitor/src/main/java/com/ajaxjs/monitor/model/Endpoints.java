package com.ajaxjs.monitor.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@EqualsAndHashCode
@ToString
public class Endpoints implements Iterable<Endpoint>, Serializable {

    private static final long serialVersionUID = 5621206253990279887L;
    private final Map<String, Endpoint> endpoints;
    private static final Endpoints EMPTY = new Endpoints(Collections.emptyList());

    private Endpoints(Collection<Endpoint> endpoints) {
        if (endpoints.isEmpty())
            this.endpoints = Collections.emptyMap();
        else
            this.endpoints = endpoints.stream().collect(toMap(Endpoint::getId, Function.identity()));
    }

    public Endpoints(Map<String, Link> links) {
        if (links.isEmpty())
            endpoints = Collections.emptyMap();
        else {
            endpoints = new HashMap<>();
            links.forEach((k, v) -> endpoints.put(k, new Endpoint(k, v.getHref())));
        }
    }

    public Optional<Endpoint> get(String id) {
        return Optional.ofNullable(endpoints.get(id));
    }

    public boolean isPresent(String id) {
        return endpoints.containsKey(id);
    }

    @Override
    public Iterator<Endpoint> iterator() {
        return new UnmodifiableIterator<>(endpoints.values().iterator());
    }

    public static Endpoints empty() {
        return EMPTY;
    }

    public static Endpoints single(String id, String url) {
        return new Endpoints(Collections.singletonList(new Endpoint(id, url)));
    }

    public static Endpoints of(@Nullable Collection<Endpoint> endpoints) {
        if (endpoints == null || endpoints.isEmpty())
            return empty();

        return new Endpoints(endpoints);
    }

    public Endpoints withEndpoint(String id, String url) {
        Endpoint endpoint = new Endpoint(id, url);
        HashMap<String, Endpoint> newEndpoints = new HashMap<>(endpoints);
        newEndpoints.put(endpoint.getId(), endpoint);

        return new Endpoints(newEndpoints.values());
    }

    public Stream<Endpoint> stream() {
        return this.endpoints.values().stream();
    }

    private static class UnmodifiableIterator<T> implements Iterator<T> {
        private final Iterator<T> delegate;

        private UnmodifiableIterator(Iterator<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public T next() {
            return this.delegate.next();
        }
    }
}
