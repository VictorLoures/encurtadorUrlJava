package com.redirectUrlShortner;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, Object> handleRequest(final Map<String, Object> input, final Context context) {
        final String pathParameters = (String) input.get("rawPath");
        final String shortUrlCode = pathParameters.replace("/", "");

        if (shortUrlCode == null || shortUrlCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        final GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket("url-shortener-storage-victor-loures-10")
                        .key(shortUrlCode)
                        .build();

        final InputStream s3ObjectStream;
        try {
            s3ObjectStream = s3Client.getObject(getObjectRequest);
        } catch (final Exception e) {
            throw new RuntimeException("Error fetching URl data from S3: " + " " + shortUrlCode + ".json " + e.getMessage(), e);
        }

        final UrlData urlData;
        try {
            urlData = objectMapper.readValue(s3ObjectStream, UrlData.class);
        } catch (final Exception e) {
            throw new RuntimeException("Error deserializing URL data: " + e.getMessage(), e);
        }

        final Long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        final Map<String, Object> response = new HashMap<>();

        if (urlData.getExpirationTime() < currentTimeInSeconds) {
            response.put("statusCode", 410);
            response.put("body", "URL has expired");
            return response;
        }

        response.put("statusCode", 302);
        final Map<String, String> headers = new HashMap<>();
        headers.put("Location", urlData.getOriginalUrl());
        response.put("headers", headers);

        return response;
    }
}