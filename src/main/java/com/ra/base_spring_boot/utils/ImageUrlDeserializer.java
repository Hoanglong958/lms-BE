package com.ra.base_spring_boot.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class ImageUrlDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.isObject()) {
            if (node.has("url")) {
                return node.get("url").asText();
            } else if (node.has("response") && node.get("response").has("url")) {
                return node.get("response").get("url").asText();
            } else if (node.has("file") && node.get("file").has("response") && node.get("file").get("response").has("url")) {
                return node.get("file").get("response").get("url").asText();
            }
            // fallback for generic object
            return node.toString();
        } else if (node.isArray() && !node.isEmpty()) {
            JsonNode firstNode = node.get(0);
            if (firstNode.isObject() && firstNode.has("response") && firstNode.get("response").has("url")) {
                return firstNode.get("response").get("url").asText();
            } else if (firstNode.isObject() && firstNode.has("url")) {
                return firstNode.get("url").asText();
            }
            return firstNode.asText();
        }

        return node.asText();
    }
}
