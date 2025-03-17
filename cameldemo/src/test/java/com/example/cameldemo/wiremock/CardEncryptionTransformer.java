package com.example.cameldemo.wiremock;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CardEncryptionTransformer extends ResponseTransformer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        try {
            JsonNode json = mapper.readTree(request.getBodyAsString());
            String cardNumber = json.get("cardNumber").asText();
            String encrypted = "mockEncrypted_" + cardNumber;

            String responseBody = String.format("{\"cardNumber\": \"%s\"}", encrypted);

            return Response.response()
                    .status(200)
                    .body(responseBody)
                    .headers(new HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json")))
                    .build();
        } catch (Exception e) {
            return Response.response()
                    .status(500)
                    .body("{\"error\": \"Transformation error\"}")
                    .build();
        }
    }

    @Override
    public String getName() {
        return "card-encryption-transformer";
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}