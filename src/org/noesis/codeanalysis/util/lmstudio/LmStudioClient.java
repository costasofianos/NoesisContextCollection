package org.noesis.codeanalysis.util.lmstudio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LmStudioClient {
    private final String baseUrl;
    private final HttpClient client;
    private final String model = "qwen/qwen2.5-coder-14b";

    public LmStudioClient() {
        this("http://127.0.0.1:1234/v1");
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public LmStudioClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(25))
                .build();
    }

    public String getCompletion(String prompt) throws Exception {
        //System.out.println(prompt);

//        HttpRequest testRequest = HttpRequest.newBuilder()
//                .version(HttpClient.Version.HTTP_1_1)
//                .uri(URI.create("http://127.0.0.1:1234/v1/models"))
//                .timeout(Duration.ofSeconds(30))
//                .header("Accept", "application/json")
//                .GET()
//                .build();
//
//        System.out.println("Sending request...");
//        HttpResponse<String> testResponse = client.send(testRequest, HttpResponse.BodyHandlers.ofString());
//        System.out.println("Response received!");
//        System.out.println("Status: " + testResponse.statusCode());
//        System.out.println("Body: " + testResponse.body());

//        System.out.println("Status: " + testResponse.statusCode());
//        System.out.println("Body: " + testResponse.body());


        JSONArray messages = new JSONArray()
                .put(new JSONObject()
                        .put("role", "user")
                        .put("content", prompt));

        JSONObject requestBody = new JSONObject()
                .put("model", model)
                .put("messages", messages)
                .put("temperature", 0.7)
                .put("max_tokens", 2000)
                .put("stream", false)
                .put("keep_tokens", 0);

        HttpRequest request = HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .uri(URI.create(baseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        //System.out.println("Sending request to: " + baseUrl + "/chat/completions");
        //System.out.println("Request JSON:\n" + requestBody.toString(2));

        System.out.println("Sending request to "+model);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


//        System.out.println("Status: " + response.statusCode());
//        System.out.println("Response: " + response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error from LM Studio API: " + response.body());
        }

        JSONObject jsonResponse = new JSONObject(response.body());
        return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}
