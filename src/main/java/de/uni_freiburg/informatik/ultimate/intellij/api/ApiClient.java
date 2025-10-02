package de.uni_freiburg.informatik.ultimate.intellij.api;

import de.uni_freiburg.informatik.ultimate.intellij.UltimatePlugin;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private final HttpClient client;
    private String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("User-Agent", "Ultimate-IntelliJ-Plugin/" + UltimatePlugin.getPluginVersion())
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> postForm(String endpoint, Map<String, String> formData) {
        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (!encoded.isEmpty()) {
                encoded.append("&");
            }
            encoded.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            encoded.append("=");
            encoded.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("User-Agent", "Ultimate-IntelliJ-Plugin/" + UltimatePlugin.getPluginVersion())
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(encoded.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) return "";
        String trimmed = url.trim();
        if (trimmed.endsWith("/")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed;
    }
}
