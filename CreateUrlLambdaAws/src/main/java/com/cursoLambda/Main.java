package com.cursoLambda;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(final Map<String, Object> input, final Context context) {
        final String body = (String) input.get("body");

        Map<String, String> bodyMap;
        try {
            bodyMap = objectMapper.readValue(body, Map.class);
        } catch (final Exception e) {
            throw new RuntimeException("Error parsing JSON body: " + e.getMessage(), e);
        }

        final String originalUrl = bodyMap.get("originalUrl");
        final String expirationTime = bodyMap.get("expirationTime");
        final Long expirationTimeInSeconds = Long.parseLong(expirationTime);

        final String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);

        final UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);

        try {
            final String urlDataJson = objectMapper.writeValueAsString(urlData);
            final PutObjectRequest request =
                    PutObjectRequest.builder().bucket("url-shortener-storage-victor-loures-10").key(shortUrlCode).build();
            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        } catch (final Exception e) {
            throw new RuntimeException("Error saving url on S3: " + e.getMessage(), e);
        }

        final Map<String, String> response = new HashMap<>();
        response.put("code", shortUrlCode);

        return response;
    }
}