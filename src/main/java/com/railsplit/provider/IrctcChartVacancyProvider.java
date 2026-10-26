
package com.railsplit.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Component
public class IrctcChartVacancyProvider implements ChartVacancyProvider {

    private static final String CHART_URL =
            "https://www.irctc.co.in/online-charts/api/coachComposition";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public IrctcChartVacancyProvider(ObjectMapper objectMapper) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();

        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode getCoachComposition(
            String trainNo,
            String boardingStation,
            String remoteStation,
            String trainSourceStation,
            String journeyDate,
            String travelClass,
            String coach
    ) {
        Map<String, String> requestBody = Map.of(
                "trainNo", trainNo,
                "boardingStation", boardingStation,
                "remoteStation", remoteStation,
                "trainSourceStation", trainSourceStation,
                "jDate", journeyDate,
                "cls", travelClass,
                "coach", coach
        );

        String responseBody = restClient.post()
                .uri(CHART_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (request, response) -> {
                            throw new IllegalStateException(
                                    "IRCTC request failed with HTTP status "
                                            + response.getStatusCode().value());
                        })
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException(
                    "IRCTC returned an empty response.");
        }

        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "IRCTC returned a response that is not valid JSON.", e);
        }
    }
}
