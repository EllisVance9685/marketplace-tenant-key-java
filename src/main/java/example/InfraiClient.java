package example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class InfraiClient {
    private final HttpClient http;
    private final String baseUrl;
    private final String apiKey;

    public InfraiClient(String baseUrl, String apiKey, HttpClient http) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.http = http;
    }

    public String request(String method, String path, String body) throws IOException, InterruptedException {
        // Infrai authentication uses Authorization: Bearer with the environment key.
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json");
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);
        HttpResponse<String> response = http.send(builder.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        if (envelope.contains("\"ok\":false")) {
            throw new IOException("Infrai business error: " + envelope);
        }
        if (response.statusCode() >= 500) {
            throw new IOException("Infrai transport error: HTTP " + response.statusCode());
        }
        return envelope;
    }
}
