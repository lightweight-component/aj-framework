package com.ajaxjs.util.json.jackson3;

import com.ajaxjs.util.json.JsonEngine;
import com.ajaxjs.util.json.JsonEngineProvider;

public class Jackson3Provider implements JsonEngineProvider {
    /**
     * Returns the provider priority. Higher values take precedence.
     *
     * @return the priority value
     */
    @Override
    public int priority() {
        return 20;
    }

    /**
     * Creates a new Jackson 2 JSON engine instance.
     *
     * @return the JSON engine
     */
    @Override
    public JsonEngine create() {
        return new Jackson3Engine();
    }
}