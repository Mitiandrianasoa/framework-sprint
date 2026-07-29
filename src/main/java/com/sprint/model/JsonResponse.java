package com.sprint.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SPRINT 9 - Enveloppe standard d'une réponse JSON.
 *
 * Produit un JSON de la forme :
 * {
 *   "status": "success",
 *   "code": 200,
 *   "data": { ... }
 * }
 */
public class JsonResponse {

    private String status;
    private int code;
    private Object data;
    private Map<String, Object> metadata;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonResponse() {
        this.metadata = new HashMap<>();
    }

    public JsonResponse(String status, int code) {
        this.status = status;
        this.code = code;
        this.metadata = new HashMap<>();
    }

    public JsonResponse(String status, int code, Object data) {
        this.status = status;
        this.code = code;
        this.data = data;
        this.metadata = new HashMap<>();
    }

    public String getStatus() { return status; }
    public JsonResponse setStatus(String status) { this.status = status; return this; }

    public int getCode() { return code; }
    public JsonResponse setCode(int code) { this.code = code; return this; }

    public Object getData() { return data; }
    public JsonResponse setData(Object data) { this.data = data; return this; }

    public Map<String, Object> getMetadata() { return metadata; }
    public JsonResponse addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    // Fabriques utilitaires
    public static JsonResponse success(Object data) { return new JsonResponse("success", 200, data); }
    public static JsonResponse success() { return new JsonResponse("success", 200); }

    public static JsonResponse error(String message, int code) {
        JsonResponse response = new JsonResponse("error", code);
        response.addMetadata("message", message);
        return response;
    }

    public static JsonResponse notFound(String message) { return error(message, 404); }
    public static JsonResponse badRequest(String message) { return error(message, 400); }
    public static JsonResponse serverError(String message) { return error(message, 500); }

    /** Sérialise cette réponse en chaîne JSON. */
    public String toJson() throws IOException {
        return objectMapper.writeValueAsString(this);
    }

    @Override
    public String toString() {
        try {
            return toJson();
        } catch (IOException e) {
            return "{\"status\":\"error\",\"code\":500}";
        }
    }
}
